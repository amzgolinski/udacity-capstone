package com.amzgolinski.yara.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.SubredditAdapter;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.service.YaraUtilityService;

import java.util.ArrayList;


public class SubredditFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private final String LOG_TAG = SubredditFragment.class.getName();

  private static final int SUBREDDIT_LOADER = 0;

  private static final String[] SUBREDDIT_COLUMNS = {
      RedditContract.SubredditsEntry.COLUMN_ID,
      RedditContract.SubredditsEntry.COLUMN_SUBREDDIT_ID,
      RedditContract.SubredditsEntry.COLUMN_NAME,
      RedditContract.SubredditsEntry.COLUMN_TITLE,
      RedditContract.SubredditsEntry.COLUMN_RELATIVE_LOCATION,
      RedditContract.SubredditsEntry.COLUMN_SELECTED,
  };

  public static final int COL_ID = 0;
  public static final int COL_SUBREDDIT_ID = 1;
  public static final int COL_NAME = 2;
  public static final int COL_TITLE = 3;
  public static final int COL_RELATIVE_LOCATION = 4;
  public static final int COL_SELECTED = 5;

  private SubredditAdapter mAdapter;
  private BroadcastReceiver mReceiver;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBREDDIT_LOADER, null, this);
    mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, intent.getAction());
        restartLoader();
      }
    };
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreate");
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(
        getActivity(),
        RedditContract.SubredditsEntry.CONTENT_URI,
        SUBREDDIT_COLUMNS,
        null,
        null,
        null
    );
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_subreddit, container, false);
    RecyclerView subredditList = (RecyclerView) root.findViewById(R.id.subreddit_list);

    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL_LIST);
    subredditList.addItemDecoration(itemDecoration);
    subredditList.setLayoutManager(new LinearLayoutManager(getContext()));
    mAdapter = new SubredditAdapter(getActivity(), null);
    subredditList.setAdapter(mAdapter);

    return root;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(LOG_TAG, "onLoadFinished");
    mAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(LOG_TAG, "onLoaderReset");
    mAdapter.swapCursor(null);
  }

  @Override
  public void onPause() {
    super.onPause();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver((mReceiver));
  }

  @Override
  public void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(
        mReceiver, new IntentFilter(YaraUtilityService.ACTION_SUBREDDIT_UNSUBSCRIBE));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    Log.d(LOG_TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(LOG_TAG, "onOptionsItemSelected");
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_refresh:
        Toast.makeText(getContext(), "Refresh", Toast.LENGTH_SHORT).show();
        return true;
      case R.id.action_save:
        ArrayList<String> toRemove = mAdapter.getToRemove();
        Toast.makeText(getContext(), "Remove " + toRemove.size(), Toast.LENGTH_SHORT).show();
        if (toRemove.size() > 0) {
          YaraUtilityService.subredditUnsubscribe(getContext(), toRemove);
        }
        getActivity().onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    Log.d(LOG_TAG, "onCreateOptionsMenu");
    inflater.inflate(R.menu.menu_subreddit, menu);
  }

  private void restartLoader() {
    if (isAdded()) {
      getLoaderManager().restartLoader(SUBREDDIT_LOADER, null, this);
    }
  }
}
