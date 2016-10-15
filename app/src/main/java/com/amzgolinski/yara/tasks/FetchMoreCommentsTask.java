package com.amzgolinski.yara.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.model.CommentItem;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FetchMoreCommentsTask extends AsyncTask<CommentItem, Void, ArrayList<CommentItem>> {

  private static final String LOG_TAG = FetchMoreCommentsTask.class.getName();

  private int mPosition;
  private ArrayList<CommentItem> mComments;
  private RedditDownloadCallback mCallback;

  public FetchMoreCommentsTask(ArrayList<CommentItem> comments, int position,
                               RedditDownloadCallback callback) {
    mComments = comments;
    mPosition = position;
    mCallback = callback;
  }

  public ArrayList<CommentItem> doInBackground(CommentItem... params) {
    Log.d(LOG_TAG, "doInBackground");
    ArrayList<CommentItem> toReturn;
    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    CommentItem wrapper = params[0];
    //Log.d(LOG_TAG, wrapper.toString());
    Submission fullSubmissionData = redditClient.getSubmission(wrapper.getSubmissionId());
    CommentNode rootNode = fullSubmissionData.getComments();
    toReturn = loadMoreComments(rootNode, wrapper);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(toReturn.size()));
    mComments.remove(mPosition);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(mComments.size()));
    mComments.addAll(mPosition, toReturn);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(mComments.size()));

    return mComments;
  }

  public void onPostExecute(ArrayList<CommentItem> comments) {
    Log.d(LOG_TAG, "onPostExecute");
    mCallback.onDownloadComplete(comments);
  }

  private ArrayList<CommentItem> loadMoreComments(CommentNode rootNode, CommentItem wrapper) {
    Log.d(LOG_TAG, "loadMoreComments");

    ArrayList<CommentItem> toReturn;
    Stack<CommentItem> moreComments = new Stack<>();

    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    CommentNode parent = rootNode.findChild(wrapper.getId()).orNull();
    parent.loadMoreComments(redditClient);
    Iterable<CommentNode> iterable = parent.walkTree();
    List<CommentNode> filteredList = new ArrayList<>();
    boolean found = false;
    for (CommentNode node : iterable) {
      if (found) {
        //Log.d(LOG_TAG, "found parent node: ");
        //Log.d(LOG_TAG, node.getComment().getBody());
        if (!isLoaded(node.getComment().getId())) {
          filteredList.add(node);
        }
      } else if (node.getComment().getId().equals(wrapper.getId().substring(3))) {
        found = true;
      }
    }
    toReturn = CommentItem.walkTree(filteredList);
    return toReturn;
  }

  private boolean isLoaded(String id) {

    boolean inList = false;
    int index = 0;
    while (!inList && index < mComments.size() ) {
      inList = id.equals((mComments.get(index).getId()));
      index++;
    }
    return inList;
  }
}