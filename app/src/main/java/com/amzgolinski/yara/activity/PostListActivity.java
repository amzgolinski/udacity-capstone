package com.amzgolinski.yara.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.callbacks.AccountRetrievedCallback;
import com.amzgolinski.yara.tasks.FetchLoggedInAccountTask;
import com.amzgolinski.yara.tasks.RefreshAccessTokenTask;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.LoggedInAccount;

import butterknife.OnClick;

public class PostListActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, AccountRetrievedCallback {

  // Static variables
  private static final String LOG_TAG = PostListActivity.class.getName();

  private DrawerLayout mDrawerLayout;
  private LoggedInAccount mRedditAccount;
  private TextView mUsername;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreate");
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
        // Code here will be triggered once the drawer open as we don't want anything to happen so
        // we leave this blank
        super.onDrawerOpened(drawerView);
      }
    };

    //Setting the actionbarToggle to drawer layout
    mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

    //calling sync state is necessary or else your hamburger icon wont show up
    actionBarDrawerToggle.syncState();
  }

  @OnClick(R.id.login_button)
  public void login(View view) {startActivity(new Intent(this, LoginActivity.class));}

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
        //Toast.makeText(getApplicationContext(), "Accounts", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AccountsActivity.class));
        return true;

      case R.id.drawer_subscriptions:
        //Toast.makeText(getApplicationContext(), "Subscriptions", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SubredditActivity.class));
        return true;
    }
    return false;
  }

  @Override
  public void onBackPressed() {

    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    AuthenticationState state = AuthenticationManager.get().checkAuthState();
    Log.d(LOG_TAG, "AuthenticationState for onResume(): " + state);

    switch (state) {
      case READY:
        configUser();
        break;
      case NEED_REFRESH:
        new RefreshAccessTokenTask(this.getApplicationContext(), this).execute();
        break;
      case NONE:
        Toast.makeText(PostListActivity.this, "Log in first", Toast.LENGTH_SHORT).show();
        break;
    }
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


  public void onAccountRetrieved(LoggedInAccount account) {
    Log.d(LOG_TAG, "onAccountRetrieved");
    mRedditAccount = account;
    configUser();
  }

  private void configUser() {
    Log.d(LOG_TAG, "configUser");
    if (mRedditAccount == null) {
      new FetchLoggedInAccountTask(this.getApplicationContext(), this).execute();
      return;
    }
    NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(this);
    View header = navigationView.getHeaderView(0);
    mUsername = (TextView) header.findViewById(R.id.reddit_username);
    mUsername.setText(mRedditAccount.getFullName());

  }


}
