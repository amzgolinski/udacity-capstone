package com.amzgolinski.yara.activity;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.SubredditAdapter;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.util.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class SubredditActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private final String LOG_TAG = SubredditActivityFragment.class.getSimpleName();

  private SubredditAdapter mAdapter;

  private static final int SUBREDDIT_LOADER = 0;

  private static final String[] SUBREDDIT_COLUMNS = {
      RedditContract.SubredditEntry.COLUMN_ID,
      RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID,
      RedditContract.SubredditEntry.COLUMN_NAME,
      RedditContract.SubredditEntry.COLUMN_TITLE,
      RedditContract.SubredditEntry.COLUMN_RELATIVE_LOCATION,
      RedditContract.SubredditEntry.COLUMN_SELECTED,
  };

  public static final int COL_ID = 0;
  public static final int COL_SUBREDDIT_ID = 1;
  public static final int COL_NAME = 2;
  public static final int COL_TITLE = 3;
  public static final int COL_RELATIVE_LOCATION = 4;
  public static final int COL_SELECTED = 5;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBREDDIT_LOADER, null, this);

  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(
        getActivity(),
        RedditContract.SubredditEntry.CONTENT_URI,
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
    subredditList.setLayoutManager(new LinearLayoutManager(getContext()));
    mAdapter = new SubredditAdapter(getActivity(), null);
    subredditList.setAdapter(mAdapter);

    return root;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(LOG_TAG, "onLoadFinished");
    // Swap the new cursor in.  (The framework will take care of closing the
    // old cursor once we return.)
    Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));

    /*
    if (Utils.isCursorEmpty(data)) {
      new FetchSubredditsTask(this.getContext()).execute();
    }
    */

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
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    Log.d(LOG_TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
  }
}
