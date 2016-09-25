package com.amzgolinski.yara.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;
import com.amzgolinski.yara.R;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "JRAW_EXAMPLE";
  private String[] mNavigationDrawerOptions;
  private ActionBarDrawerToggle mDrawerToggle;
  private CharSequence mDrawerTitle;
  private CharSequence mTitle;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);

    mNavigationDrawerOptions =
        getResources().getStringArray(R.array.navigation_drawer_options_array);
    mTitle = mDrawerTitle = getTitle();
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        mToolbar, R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        getSupportActionBar().setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        getSupportActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };


    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);
    mDrawerList = (ListView) findViewById(R.id.navigation_view);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    mDrawerToggle.syncState();

    // Set the adapter for the list view
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_item, mNavigationDrawerOptions));
    // Set the list's click listener
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      selectItem(position);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /* Called whenever we call invalidateOptionsMenu() */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
    return super.onPrepareOptionsMenu(menu);
  }

  /** Swaps fragments in the main content view */
  private void selectItem(int position) {
    /*
    // Create a new fragment and specify the planet to show based on position
    Fragment fragment = new PlanetFragment();
    Bundle args = new Bundle();
    args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
    fragment.setArguments(args);

    // Insert the fragment by replacing any existing fragment
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment)
        .commit();

    */
    // Highlight the selected item, update the title, and close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(mNavigationDrawerOptions[position]);
    mDrawerLayout.closeDrawer(mDrawerList);
    Toast.makeText(MainActivity.this, mNavigationDrawerOptions[position], Toast.LENGTH_SHORT).show();
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title.toString();
    getActionBar().setTitle(mTitle);
  }


  public void login(View view) { startActivity(new Intent(this, LoginActivity.class)); }
  public void userInfo(View view) { startActivity(new Intent(this, UserInfoActivity.class)); }

  @Override
  protected void onResume() {
    super.onResume();
    AuthenticationState state = AuthenticationManager.get().checkAuthState();
    Log.d(TAG, "AuthenticationState for onResume(): " + state);

    switch (state) {
      case READY:
        break;
      case NONE:
        Toast.makeText(MainActivity.this, "Log in first", Toast.LENGTH_SHORT).show();
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
          Log.e(TAG, "Could not refresh access token", e);
        }
        return null;
      }

      @Override
      protected void onPostExecute(Void v) {
        Log.d(TAG, "Reauthenticated");
      }
    }.execute();
  }
}
