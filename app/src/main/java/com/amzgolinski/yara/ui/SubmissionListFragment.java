package com.amzgolinski.yara.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.SubmissionListAdapter;
import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.service.YaraUtilityService;
import com.amzgolinski.yara.tasks.FetchSubredditsTask;
import com.amzgolinski.yara.tasks.SubmitVoteTask;
import com.amzgolinski.yara.util.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;


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
      RedditContract.SubmissionsEntry.COLUMN_TEXT,
      RedditContract.SubmissionsEntry.COLUMN_AUTHOR,
      RedditContract.SubmissionsEntry.COLUMN_THUMBNAIL,
      RedditContract.SubmissionsEntry.COLUMN_COMMENT_COUNT,
      RedditContract.SubmissionsEntry.COLUMN_SCORE,
      RedditContract.SubmissionsEntry.COLUMN_VOTE,
  };

  public static final int COL_ID = 0;
  public static final int COL_SUBMISSION_ID = 1;
  public static final int COL_SUBREDDIT_ID = 2;
  public static final int COL_SUBREDDIT_NAME = 3;
  public static final int COL_TITLE = 4;
  public static final int COL_TEXT = 5;
  public static final int COL_AUTHOR = 6;
  public static final int COL_THUMBNAIL = 7;
  public static final int COL_COMMENT_COUNT = 8;
  public static final int COL_SCORE = 9;
  public static final int COL_VOTE = 10;

  // Views
  @BindView(R.id.submission_list_progress_bar_layout) ViewGroup mProgress;
  @BindView(R.id.submission_list_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

  private SubmissionListAdapter mAdapter;
  private BroadcastReceiver mReceiver;


  public SubmissionListFragment() {
    // empty
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onActivityCreated");
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBMISSIONS_LOADER, null, this);
    mReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onActivityCreated");
        Log.d(LOG_TAG, intent.getAction());
        restartLoader();
        mProgress.setVisibility(View.GONE);
      }
    };
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
    ButterKnife.bind(this, root);
    RecyclerView submissionsList = (RecyclerView) root.findViewById(R.id.submission_list);
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
    mAdapter = new SubmissionListAdapter(getActivity(), null, new RedditDownloadCallback() {
      @Override
      public void onDownloadComplete(Object reslt) {
        restartLoader();
      }
    });
    submissionsList.setAdapter(mAdapter);
    mProgress.setVisibility(View.VISIBLE);

    MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
    AdView mAdView = (AdView) root.findViewById(R.id.ad_view);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);

    return root;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(LOG_TAG, "onLoadFinished");
    //Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));
    Log.d(LOG_TAG, "Logged in: " + Utils.isLoggedIn(getContext()));
    if (Utils.isCursorEmpty(data) && Utils.isLoggedIn(getContext())) {
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
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver((mReceiver));
  }

  @Override
  public void onResume() {
    Log.d(LOG_TAG, "onResume");
    getLoaderManager().restartLoader(SUBMISSIONS_LOADER, null, this);
    super.onResume();

    LocalBroadcastManager.getInstance(getContext()).registerReceiver(
        mReceiver, new IntentFilter(YaraUtilityService.ACTION_SUBMIT_VOTE));

    LocalBroadcastManager.getInstance(getContext()).registerReceiver(
        mReceiver, new IntentFilter(YaraUtilityService.ACTION_SUBREDDIT_UNSUBSCRIBE));
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
