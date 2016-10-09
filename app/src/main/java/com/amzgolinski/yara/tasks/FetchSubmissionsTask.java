package com.amzgolinski.yara.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.Vector;


public class FetchSubmissionsTask extends AsyncTask<Void, Void, Void> {

  private static final String LOG_TAG = FetchSubmissionsTask.class.getName();

  Context mContext;

  public FetchSubmissionsTask(Context context) {
    mContext = context;
  }

  public Void doInBackground(Void... params) {
    return null;
  }

  private int addSubmissions(ArrayList<Submission> submissions) {
    int numInserted = 0;

    Vector<ContentValues> contentValuesVector = new Vector<>(submissions.size());
    for (Submission submission : submissions) {
      ContentValues submissionValues = submissionToValue(submission);
      //Log.d(LOG_TAG, "Submission Values: " + submissionValues.toString());
      contentValuesVector.add(submissionValues);
      mContext.getContentResolver().insert(RedditContract.SubmissionsEntry.CONTENT_URI, submissionValues);

    }

    ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
    contentValuesVector.toArray(contentValuesArray);

    numInserted = mContext.getContentResolver()
        .bulkInsert(RedditContract.SubmissionsEntry.CONTENT_URI, contentValuesArray);

    return numInserted;

  }

  private ContentValues submissionToValue(Submission submission) {
    ContentValues toReturn = new ContentValues();
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_SUBMISSION_ID, Utils.redditIdToLong(submission.getId()));
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_ID, Utils.redditParentIdToLong(submission.getSubredditId()));
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_AUTHOR, submission.getAuthor());
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_TITLE, submission.getTitle());
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_URL, submission.getUrl());
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_COMMENT_COUNT, submission.getCommentCount());
    int readOnly = (Utils.isSubmissionReadOnly(submission) ? 1 : 0);
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_IS_READ_ONLY, readOnly);
    toReturn.put(RedditContract.SubmissionsEntry.COLUMN_THUMBNAIL, submission.getThumbnail());

    return toReturn;
  }


}
