package com.amzgolinski.yara.ui;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.SubmissionsAdapter;
import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.tasks.FetchSubredditsTask;
import com.amzgolinski.yara.util.Utils;


public class SubmissionListFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private final String LOG_TAG = SubmissionListFragment.class.getName();

  private static final int SUBMISSIONS_LOADER = 0;

  private static final String[] SUBMISSION_COLUMNS = {
      RedditContract.SubmissionsEntry.COLUMN_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBMISSION_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_NAME,
      RedditContract.SubmissionsEntry.COLUMN_TITLE,
      RedditContract.SubmissionsEntry.COLUMN_THUMBNAIL,
      RedditContract.SubmissionsEntry.COLUMN_COMMENT_COUNT,
      RedditContract.SubmissionsEntry.COLUMN_SCORE,
  };

  public static final int COL_ID = 0;
  public static final int COL_SUBMISSION_ID = 1;
  public static final int COL_SUBREDDIT_ID = 2;
  public static final int COL_SUBREDDIT_NAME = 3;
  public static final int COL_TITLE = 4;
  public static final int COL_THUMBNAIL = 5;
  public static final int COL_COMMENT_COUNT = 6;
  public static final int COL_SCORE = 7;

  private SubmissionsAdapter mAdapter;
  private ViewGroup mProgress;
  private SwipeRefreshLayout mSwipeRefreshLayout;

  public SubmissionListFragment() {
    // empty
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onActivityCreated");
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBMISSIONS_LOADER, null, this);

  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Log.d(LOG_TAG, "onCreateLoader");
    return new CursorLoader(
        getActivity(),
        RedditContract.SubmissionsEntry.CONTENT_URI,
        SUBMISSION_COLUMNS,
        null,
        null,
        RedditContract.SubmissionsEntry.COLUMN_SCORE + " DESC"
    );
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    Log.d(LOG_TAG, "onCreateView");
    View root = inflater.inflate(R.layout.fragment_submission_list, container, false);
    RecyclerView submissionsList = (RecyclerView) root.findViewById(R.id.submission_list);
    mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

    mSwipeRefreshLayout.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            Log.d(LOG_TAG, "onRefresh");
            fetchSubreddits();
          }
        });


    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL_LIST);
    submissionsList.addItemDecoration(itemDecoration);
    submissionsList.setLayoutManager(new LinearLayoutManager(getContext()));
    mAdapter = new SubmissionsAdapter(getActivity(), null);
    submissionsList.setAdapter(mAdapter);

    mProgress = (ViewGroup) root.findViewById(R.id.progress_bar_layout);
    mProgress.setVisibility(View.VISIBLE);
    return root;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(LOG_TAG, "onLoadFinished");
    // Swap the new cursor in.  (The framework will take care of closing the
    // old cursor once we return.)
    //Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));
    if (Utils.isCursorEmpty(data)) {
      fetchSubreddits();
    } else {
      mProgress.setVisibility(View.GONE);
    }
    mAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(LOG_TAG, "onLoaderReset");
    mAdapter.swapCursor(null);
  }

  @Override
  public void onPause() {
    Log.d(LOG_TAG, "onPause");
    super.onPause();
  }

  @Override
  public void onResume() {
    Log.d(LOG_TAG, "onResume");
    getLoaderManager().restartLoader(SUBMISSIONS_LOADER, null, this);
    super.onResume();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    Log.d(LOG_TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
  }

  private void fetchSubreddits() {
    new FetchSubredditsTask(this.getContext(), new RedditDownloadCallback() {
      @Override
      public void onDownloadComplete(Object result) {
        if (mSwipeRefreshLayout.isRefreshing()) {
          mSwipeRefreshLayout.setRefreshing(false);
        }
        restartLoader();
      }
    }).execute();
  }

  private void restartLoader() {
    if (isAdded()) {
      getLoaderManager().restartLoader(SUBMISSIONS_LOADER, null, this);
    }
  }


}
