package com.amzgolinski.yara.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amzgolinski.yara.R;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;

import butterknife.OnClick;

public class PostListActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  // Static variables
  private static final String LOG_TAG = PostListActivity.class.getName();

  private DrawerLayout mDrawerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_list);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    //Initializing NavigationView
    NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

    //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
    navigationView.setNavigationItemSelectedListener(this);

    // Initializing Drawer Layout and ActionBarToggle
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
    ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
        this,
        mDrawerLayout,
        toolbar,
        R.string.drawer_open, R.string.drawer_close) {

      @Override
      public void onDrawerClosed(View drawerView) {
        // Code here will be triggered once the drawer closes as we dont want anything to happen so
        // we leave this blank
        super.onDrawerClosed(drawerView);
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        // Code here will be triggered once the drawer open as we dont want anything to happen so
        // we leave this blank
        super.onDrawerOpened(drawerView);
      }
    };

    //Setting the actionbarToggle to drawer layout
    mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

    //calling sync state is necessary or else your hamburger icon wont show up
    actionBarDrawerToggle.syncState();

    /*
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show();
      }
    });
    */
  }

  //@OnClick(R.id.login_button)
  public void login(View view) {
    Toast.makeText(getApplicationContext(), "Login", Toast.LENGTH_SHORT).show();
    startActivity(new Intent(this, LoginActivity.class));
  }

  @OnClick(R.id.user_info_button)
  public void userInfo(View view) {
    startActivity(new Intent(this, UserInfoActivity.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_post_list, menu);
    return true;
  }

  @Override
  public boolean onNavigationItemSelected(final MenuItem menuItem) {
    Log.d(LOG_TAG, "CLICKED");
    menuItem.setChecked(true);
    //Checking if the item is in checked state or not, if not make it in checked state
    if(menuItem.isChecked()) menuItem.setChecked(false);
    else menuItem.setChecked(true);

    //Closing drawer on item click
    mDrawerLayout.closeDrawers();

    //Check to see which item was being clicked and perform appropriate action
    switch (menuItem.getItemId()) {

      //Replacing the main content with ContentFragment Which is our Inbox View;
      case R.id.drawer_accounts:
        Toast.makeText(getApplicationContext(), "Accounts", Toast.LENGTH_SHORT).show();
        return true;

      case R.id.drawer_subscriptions:
        Toast.makeText(getApplicationContext(), "Subscriptions", Toast.LENGTH_SHORT).show();
        return true;
    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();
    AuthenticationState state = AuthenticationManager.get().checkAuthState();
    Log.d(LOG_TAG, "AuthenticationState for onResume(): " + state);

    switch (state) {
      case READY:
        break;
      case NONE:
        Toast.makeText(PostListActivity.this, "Log in first", Toast.LENGTH_SHORT).show();
        break;
      case NEED_REFRESH:
        refreshAccessTokenAsync();
        break;
    }
  }

  private void refreshAccessTokenAsync() {
    new AsyncTask<Credentials, Void, Void>() {
      @Override
      protected Void doInBackground(Credentials... params) {
        try {
          AuthenticationManager.get().refreshAccessToken(LoginActivity.CREDENTIALS);
        } catch (NoSuchTokenException | OAuthException e) {
          Log.e(LOG_TAG, "Could not refresh access token", e);
        }
        return null;
      }

      @Override
      protected void onPostExecute(Void v) {
        Log.d(LOG_TAG, "Reauthenticated");
      }
    }.execute();
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
