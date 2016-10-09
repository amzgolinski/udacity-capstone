package com.amzgolinski.yara.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Vector;


public class FetchCommentsTask extends AsyncTask<String, Void, CommentNode> {

  private static final String LOG_TAG = FetchCommentsTask.class.getName();

  private Context mContext;
  private RedditDownloadCallback mCallback;

  public FetchCommentsTask(Context context, RedditDownloadCallback callback) {
    mContext = context;
    mCallback = callback;
  }

  public CommentNode doInBackground(String... params) {
    Log.d(LOG_TAG, "doInBackground");
    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    Submission fullSubmissionData = redditClient.getSubmission(params[0]);
    CommentNode rootNode = fullSubmissionData.getComments();
    //rootNode.loadFully(redditClient);
    Log.d(LOG_TAG, Integer.toString(rootNode.getTotalSize()));
    // By default, this Iterable will use pre-order traversal.
    // By passing a TraversalMethod in the walkTree() method,
    // you can change the way in which the comments will be iterated.


    if (rootNode != null) {
      //int inserted = addComments(rootNode);
      //Log.d(LOG_TAG, "Added: " + inserted + " comments for " + fullSubmissionData.getTitle());
    }

    return rootNode;
  }

  public void onPostExecute(CommentNode rootNode) {
    Log.d(LOG_TAG, "onPostExecute");
    mCallback.onDownloadComplete(rootNode);
  }

  private int addComments(CommentNode rootNode) {

    int numInserted = 0;
    Iterable<CommentNode> iterable = rootNode.walkTree();
    Vector<ContentValues> contentValuesVector = new Vector<>(0);

    for (CommentNode node : iterable) {
      ContentValues submissionValues = commentToContentValue(node);
      contentValuesVector.add(submissionValues);
    }

    ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
    contentValuesVector.toArray(contentValuesArray);

    numInserted = mContext.getContentResolver()
        .bulkInsert(RedditContract.CommentsEntry.CONTENT_URI, contentValuesArray);

    return numInserted;
  }

  private ContentValues commentToContentValue(CommentNode node) {
    ContentValues toReturn = new ContentValues();
    Comment comment = node.getComment();
    toReturn.put(
        RedditContract.CommentsEntry.COLUMN_COMMENT_ID,
        Utils.redditIdToLong(comment.getId())
    );
    toReturn.put(
        RedditContract.CommentsEntry.COLUMN_SUBMISSION_ID,
        Utils.redditParentIdToLong(comment.getSubmissionId())
    );
    toReturn.put(
        RedditContract.CommentsEntry.COLUMN_PARENT_ID,
        Utils.redditParentIdToLong(comment.getParentId())
    );
    toReturn.put(RedditContract.CommentsEntry.COLUMN_AUTHOR, comment.getAuthor());

    toReturn.put(
        RedditContract.CommentsEntry.COLUMN_BODY,
        StringEscapeUtils.unescapeHtml4(comment.data("body_html"))
    );

    toReturn.put(RedditContract.CommentsEntry.COLUMN_DEPTH, node.getDepth());
    toReturn.put(RedditContract.CommentsEntry.COLUMN_SCORE, comment.getScore());
    toReturn.put(RedditContract.CommentsEntry.COLUMN_IS_VISIBLE, 1);
    toReturn.put(RedditContract.CommentsEntry.COLUMN_VOTE, comment.getVote().getValue());

    if (node.hasMoreComments()) {
      toReturn.put(RedditContract.CommentsEntry.COLUMN_TYPE, Utils.CommentType.HAS_MORE_COMMENTS);
      toReturn.put(
          RedditContract.CommentsEntry.COLUMN_NUM_CHILDREN,
          node.getMoreChildren().getCount()
      );
    } else {
      toReturn.put(RedditContract.CommentsEntry.COLUMN_TYPE, Utils.CommentType.NO_REPLIES);
    }
    return toReturn;
  }

}
