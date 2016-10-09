package com.amzgolinski.yara.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.CommentsAdapter;
import com.amzgolinski.yara.adapter.SubmissionDetailAdapter;
import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.tasks.FetchCommentsTask;
import com.amzgolinski.yara.util.Utils;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.common.collect.Lists;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import java.util.ArrayList;
import java.util.Vector;


public class SubmissionDetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, RedditDownloadCallback {

  private static final String LOG_TAG = SubmissionDetailFragment.class.getName();

  // loader
  private static final int SUBMISSION_LOADER_ID = 1;

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

  private Uri mSubmissionUri;
  private MergeAdapter mMergeAdapter;
  private SubmissionDetailAdapter mSubmissionAdapter;
  private CommentsAdapter mCommentsAdapter;
  private CommentNode mRootNode;

  public SubmissionDetailFragment() {
    // empty
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onActivityCreated");
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(SUBMISSION_LOADER_ID, null, this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreate");
    super.onCreate(savedInstanceState);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Log.d(LOG_TAG, "onCreateLoader");
    if (mSubmissionUri != null) {
      long submissionId = ContentUris.parseId(mSubmissionUri);
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
    mCommentsAdapter = new CommentsAdapter(getContext(), new ArrayList<CommentNode>());
    mMergeAdapter = new MergeAdapter();
    mMergeAdapter.addAdapter(mSubmissionAdapter);
    mMergeAdapter.addAdapter(mCommentsAdapter);

    ListView submissionListView = (ListView) root.findViewById(R.id.submission_detail_listview);
    submissionListView.setAdapter(mMergeAdapter);
    mSubmissionUri = getActivity().getIntent().getData();
    String id = Utils.longToRedditId(ContentUris.parseId(mSubmissionUri));
    new FetchCommentsTask(this.getContext(), this).execute(id);
    return root;

  }

  @Override
  public void onDownloadComplete(Object result) {
    mRootNode = (CommentNode) result;
    ArrayList<CommentNode> nodes = Lists.newArrayList(mRootNode.walkTree());
    mCommentsAdapter.setComments(nodes);
    mCommentsAdapter.addAll(nodes);

    //walkTree();
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
    //Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));
    switch (loader.getId()) {

      case SUBMISSION_LOADER_ID: {
        mSubmissionAdapter.swapCursor(data);
        break;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(LOG_TAG, "onResume");
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d(LOG_TAG, "onStart");
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

  private void walkTree(){
    Iterable<CommentNode> iterable = mRootNode.walkTree();

    for (CommentNode node : iterable) {
      Comment comment = node.getComment();

      String test =  new String(new char[node.getDepth()]).replace("\0", "\t");
      Log.d(LOG_TAG,
          test +
          comment.getId() + " score " +
              comment.getScore() + " parent " +
          comment.getParentId() + " depth: " +
              node.getDepth());

      Log.d(LOG_TAG, comment.getBody());
      if (node.hasMoreComments()) {
        Log.d(LOG_TAG, "Has " + node.getMoreChildren().getCount() + " more comments." );
      }
    }
  }


}
