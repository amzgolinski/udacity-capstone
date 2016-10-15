package com.amzgolinski.yara.ui;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.CommentsAdapter;
import com.amzgolinski.yara.adapter.SubmissionDetailAdapter;
import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.model.CommentItem;
import com.amzgolinski.yara.service.YaraUtilityService;
import com.amzgolinski.yara.tasks.FetchCommentsTask;
import com.amzgolinski.yara.util.Utils;
import com.commonsware.cwac.merge.MergeAdapter;

import java.util.ArrayList;


public class SubmissionDetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, RedditDownloadCallback {

  private static final String LOG_TAG = SubmissionDetailFragment.class.getName();
  private static final String URI = "uri";

  // loader
  private static final int SUBMISSION_LOADER_ID = 1;

  private static final String[] SUBMISSION_COLUMNS = {
      RedditContract.SubmissionsEntry.COLUMN_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBMISSION_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_ID,
      RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_NAME,
      RedditContract.SubmissionsEntry.COLUMN_TITLE,
      RedditContract.SubmissionsEntry.COLUMN_TEXT,
      RedditContract.SubmissionsEntry.COLUMN_THUMBNAIL,
      RedditContract.SubmissionsEntry.COLUMN_COMMENT_COUNT,
      RedditContract.SubmissionsEntry.COLUMN_SCORE,
  };

  public static final int COL_ID = 0;
  public static final int COL_SUBMISSION_ID = 1;
  public static final int COL_SUBREDDIT_ID = 2;
  public static final int COL_SUBREDDIT_NAME = 3;
  public static final int COL_TITLE = 4;
  public static final int COL_TEXT = 5;
  public static final int COL_THUMBNAIL = 6;
  public static final int COL_COMMENT_COUNT = 7;
  public static final int COL_SCORE = 8;

  private Uri mSubmissionUri;
  private MergeAdapter mMergeAdapter;
  private SubmissionDetailAdapter mSubmissionAdapter;
  private CommentsAdapter mCommentsAdapter;
  private ArrayList<CommentItem> mComments;
  private BroadcastReceiver mLoadMoreCommentsReceiver;

  public SubmissionDetailFragment() {
    // empty
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onActivityCreated");
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBMISSION_LOADER_ID, null, this);
    mSubmissionUri = getActivity().getIntent().getData();
    String id = Utils.longToRedditId(ContentUris.parseId(mSubmissionUri));
    mLoadMoreCommentsReceiver =  new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final ArrayList comments =
            intent.getParcelableArrayListExtra(YaraUtilityService.PARAM_COMMENTS);
        mCommentsAdapter.reloadComments(comments);
        //restartLoader();
      }
    };
    new FetchCommentsTask(this.getContext(), this).execute(id);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreate");
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Log.d(LOG_TAG, "onCreateLoader");
    if (mSubmissionUri != null) {
      Log.v(LOG_TAG, mSubmissionUri.toString());
      return getCursorLoader(mSubmissionUri, SUBMISSION_COLUMNS);
    }
    return null;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreateView");
    View root = inflater.inflate(R.layout.fragment_submission_detail, container, false);
    mSubmissionAdapter = new SubmissionDetailAdapter(getContext(), null, 0);
    mCommentsAdapter = new CommentsAdapter(getContext(), new ArrayList<CommentItem>());
    mMergeAdapter = new MergeAdapter();
    mMergeAdapter.addAdapter(mSubmissionAdapter);
    mMergeAdapter.addAdapter(mCommentsAdapter);
    ListView commentsView = (ListView) root.findViewById(R.id.submission_detail_listview);
    commentsView.setAdapter(mMergeAdapter);
    commentsView.setEmptyView(root.findViewById(R.id.empty));
    return root;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_submission_detail_fragment, menu);
    finishCreatingMenu(menu);
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    Log.d(LOG_TAG, "onSaveInstanceState");
    outState.putParcelable(URI, mSubmissionUri);
  }

  @Override
  public void onDownloadComplete(Object result) {
    mComments = (ArrayList<CommentItem>) result;
    mCommentsAdapter.setComments(mComments);
    mCommentsAdapter.addAll(mComments);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(LOG_TAG, "onLoaderReset");
    switch (loader.getId()) {

      case (SUBMISSION_LOADER_ID):
        mSubmissionAdapter.swapCursor(null);
        break;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    Log.d(LOG_TAG, "onLoadFinished");
    Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));
    switch (loader.getId()) {
      case SUBMISSION_LOADER_ID: {
        mSubmissionAdapter.swapCursor(data);
        break;
      }
    }
  }

  @Override
  public void onPause() {
    Log.d(LOG_TAG, "onPause");
    super.onPause();

    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLoadMoreCommentsReceiver);
  }

  @Override
  public void onResume() {
    Log.d(LOG_TAG, "onResume");
    super.onResume();

    LocalBroadcastManager.getInstance(getContext()).registerReceiver(
        mLoadMoreCommentsReceiver, new IntentFilter(YaraUtilityService.ACTION_LOAD_MORE_COMMENTS));
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d(LOG_TAG, "onStart");
  }

  private Intent createShareSubmissionIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, "This is my share");
    return shareIntent;
  }

  private void finishCreatingMenu(Menu menu) {
    // Retrieve the share menu item
    MenuItem menuItem = menu.findItem(R.id.action_share);
    menuItem.setIntent(createShareSubmissionIntent());
  }

  private CursorLoader getCursorLoader(Uri uri, String[] columns) {
    return new CursorLoader(
        getActivity(),
        uri,
        columns,
        null,
        null,
        null
    );
  }
}
