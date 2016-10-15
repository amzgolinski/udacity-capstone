package com.amzgolinski.yara.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.model.CommentItem;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;


public class FetchCommentsTask extends AsyncTask<String, Void, ArrayList<CommentItem>> {

  private static final String LOG_TAG = FetchCommentsTask.class.getName();

  private Context mContext;
  private RedditDownloadCallback mCallback;

  public FetchCommentsTask(Context context, RedditDownloadCallback callback) {
    mContext = context;
    mCallback = callback;
  }

  public ArrayList<CommentItem> doInBackground(String... params) {
    //Log.d(LOG_TAG, "doInBackground");
    ArrayList<CommentItem> toReturn;
    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    //Log.d(LOG_TAG, "Submission: " + params[0]);
    Submission fullSubmissionData = redditClient.getSubmission(params[0]);
    CommentNode rootNode = fullSubmissionData.getComments();
    toReturn = CommentItem.walkTree(rootNode.walkTree().toList());
    //Log.d(LOG_TAG, Integer.toString(rootNode.getTotalSize()));
    return toReturn;
  }

  public void onPostExecute(ArrayList<CommentItem> comments) {
    Log.d(LOG_TAG, "onPostExecute");
    mCallback.onDownloadComplete(comments);
  }

}
