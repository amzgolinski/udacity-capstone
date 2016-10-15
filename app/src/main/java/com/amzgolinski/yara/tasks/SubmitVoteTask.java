package com.amzgolinski.yara.tasks;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;


public class SubmitVoteTask extends AsyncTask<Void, Void, Void> {

  private static final String LOG_TAG = SubmitVoteTask.class.getName();

  private String mSubmissionId;
  private int mVote;
  private Context mContext;
  private RedditDownloadCallback mCallback;

  public SubmitVoteTask(Context context, String subissionId, int vote,
                        RedditDownloadCallback callback) {
    mSubmissionId = subissionId;
    mVote = vote;
    mCallback = callback;
    mContext = context;
  }

  @Override
  public Void doInBackground(Void... params) {

    VoteDirection direction = VoteDirection.NO_VOTE;
    if (mVote == Utils.UPVOTE) {
      direction = VoteDirection.UPVOTE;
    } else if (mVote == Utils.DOWNVOTE) {
      direction = VoteDirection.DOWNVOTE;
    }
    RedditClient reddit = AuthenticationManager.get().getRedditClient();
    Submission submission = reddit.getSubmission(mSubmissionId);
    Log.d(LOG_TAG, submission.toString());

    // submit vote to the server
    try {
      new AccountManager(reddit).vote(submission, direction);
      Log.d(LOG_TAG, submission.toString());
      //submission = reddit.getSubmission(mSubmissionId);
      int numupdated = updateSubmission(submission);
      Log.d(LOG_TAG, "NUM UPDATED: " + numupdated);
    } catch (NetworkException | ApiException networkException) {
      Log.d(LOG_TAG, networkException.getMessage());
    }
    return null;
  }

  @Override
  public void onPostExecute(Void param) {
    mCallback.onDownloadComplete(param);
  }

  private int updateSubmission(Submission submission) {
    ContentValues values = new ContentValues();
    values.put(RedditContract.SubmissionsEntry.COLUMN_VOTE, submission.getVote().getValue());
    values.put(RedditContract.SubmissionsEntry.COLUMN_SCORE, submission.getScore()+1);

    long id = Utils.redditIdToLong(submission.getId());
    Uri submissionUri = RedditContract.SubmissionsEntry.buildSubmissionUri(id);
    int numUpdated = mContext.getContentResolver().update(
        submissionUri,
        values,
        RedditContract.SubmissionsEntry.COLUMN_SUBMISSION_ID + " =  ? ",
        new String[]{submission.getId()});
    return numUpdated;

  }
}
