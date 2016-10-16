package com.amzgolinski.yara.tasks;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.data.RedditContract.SubredditsEntry;
import com.amzgolinski.yara.data.RedditContract.SubmissionsEntry;
import com.amzgolinski.yara.service.YaraUtilityService;
import com.amzgolinski.yara.util.Utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;


public class FetchSubredditsTask extends AsyncTask<Void, Void, HashMap<String, Subreddit>> {

  private Context mContext;
  private RedditDownloadCallback mCallback;

  public FetchSubredditsTask(Context context, RedditDownloadCallback callback) {
    mContext = context;
    mCallback = callback;
  }

  private static final String LOG_TAG = FetchSubredditsTask.class.getName();

  public HashMap<String, Subreddit> doInBackground(Void... params) {

    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    UserSubredditsPaginator paginator = new UserSubredditsPaginator(redditClient, "subscriber");

    HashMap<String, Subreddit> latestSubreddits = new HashMap<>();

    while (paginator.hasNext()) {
      Listing<Subreddit> subreddits = paginator.next();
      for (Subreddit subreddit : subreddits) {
        //Log.d(LOG_TAG, "Subreddit " + subreddit.toString());
        if (!subreddit.isNsfw() && subreddit.isUserSubscriber()) {
          latestSubreddits.put(subreddit.getId(), subreddit);
        }
      }
    }
    ArrayList<Subreddit> subreddits = new ArrayList<>(latestSubreddits.values());
    int numInserted = addSubreddits(subreddits);
    Log.i(LOG_TAG, "Inserted " + numInserted +  " subreddits");
    int numSubmissions = processSubreddits(subreddits);
    Log.i(LOG_TAG, "Inserted " + numSubmissions +  " submissions");
    return latestSubreddits;
  }

  private int processSubreddits(ArrayList<Subreddit> subreddits) {

    int processed = 0;
    Log.d(LOG_TAG, "processSubreddits");
    RedditClient redditClient = AuthenticationManager.get().getRedditClient();

    for (Subreddit subreddit : subreddits) {
      SubredditPaginator paginator
          = new SubredditPaginator(redditClient, subreddit.getDisplayName());

      List<Submission> submissions = null;
      submissions = paginator.next();

      if (submissions == null) {
        Log.d(LOG_TAG, "Submissions was null");
        return processed;
      }

      ArrayList<Submission> toAdd = new ArrayList<>();
      for (Submission submission : submissions) {
        Log.d(LOG_TAG, submission.toString());
        if (Utils.isValidSubmission(submission)) {
          toAdd.add(submission);
        }
      }
      processed = addSubmissions(toAdd);

    }
    return processed;
  }

  @Override
  public void onPostExecute(HashMap<String, Subreddit> result) {
    Log.d(LOG_TAG, "onPostExecute");
    Intent dataUpdated = new Intent();
    dataUpdated.setAction("com.amzgolinski.yara.widget.MANUAL_UPDATE");
    mContext.sendBroadcast(dataUpdated);
    mCallback.onDownloadComplete(result);
  }

  private int addSubmissions(ArrayList<Submission> submissions) {
    int numInserted = 0;

    Vector<ContentValues> contentValuesVector = new Vector<>(submissions.size());
    for (Submission submission : submissions) {
      ContentValues submissionValues = submissionToValue(submission);
      contentValuesVector.add(submissionValues);
      mContext.getContentResolver().insert(SubmissionsEntry.CONTENT_URI, submissionValues);
    }

    ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
    contentValuesVector.toArray(contentValuesArray);

    numInserted = mContext.getContentResolver()
        .bulkInsert(SubmissionsEntry.CONTENT_URI, contentValuesArray);

    return numInserted;
  }

  private int addSubreddits(ArrayList<Subreddit> subreddits) {

    int numInserted = 0;

    if (subreddits.size() > 0) {
      Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(subreddits.size());

      for (Subreddit subreddit : subreddits) {
        ContentValues subredditValues = subredditToValue(subreddit);
        //Log.d(LOG_TAG, "Subreddit Values: " + subredditValues.toString());
        contentValuesVector.add(subredditValues);
      }

      ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
      contentValuesVector.toArray(contentValuesArray);

      numInserted = mContext.getContentResolver()
          .bulkInsert(SubredditsEntry.CONTENT_URI, contentValuesArray);
    }
    return numInserted;
  }

  private ContentValues subredditToValue(Subreddit subreddit) {

    ContentValues toReturn = new ContentValues();
    toReturn.put(SubredditsEntry.COLUMN_SUBREDDIT_ID, Utils.redditIdToLong(subreddit.getId()));
    toReturn.put(SubredditsEntry.COLUMN_NAME, subreddit.getDisplayName());
    toReturn.put(SubredditsEntry.COLUMN_RELATIVE_LOCATION, subreddit.getRelativeLocation());
    toReturn.put(SubredditsEntry.COLUMN_TITLE, subreddit.getTitle());
    toReturn.put(SubredditsEntry.COLUMN_SELECTED, "1");
    return toReturn;
  }

  private ContentValues submissionToValue(Submission submission) {
    ContentValues toReturn = new ContentValues();
    toReturn.put(SubmissionsEntry.COLUMN_SUBMISSION_ID, Utils.redditIdToLong(submission.getId()));
    toReturn.put(
        SubmissionsEntry.COLUMN_SUBREDDIT_ID,
        Utils.redditParentIdToLong(submission.getSubredditId())
    );
    toReturn.put(SubmissionsEntry.COLUMN_SUBREDDIT_NAME, submission.getSubredditName());
    toReturn.put(SubmissionsEntry.COLUMN_AUTHOR, submission.getAuthor());
    toReturn.put(
        SubmissionsEntry.COLUMN_TITLE,
        StringEscapeUtils.unescapeHtml4(submission.getTitle())
    );
    toReturn.put(SubmissionsEntry.COLUMN_URL, submission.getUrl());
    toReturn.put(SubmissionsEntry.COLUMN_COMMENT_COUNT, submission.getCommentCount());
    toReturn.put(SubmissionsEntry.COLUMN_SCORE, submission.getScore());
    int readOnly = (Utils.isSubmissionReadOnly(submission) ? 1 : 0);
    toReturn.put(SubmissionsEntry.COLUMN_IS_READ_ONLY, readOnly);
    toReturn.put(SubmissionsEntry.COLUMN_THUMBNAIL, submission.getThumbnail());

    String selfText = submission.data("selftext_html");
    if (!Utils.isStringEmpty(selfText)) {
      selfText = StringEscapeUtils.unescapeHtml4(selfText);
      selfText = Utils.removeHtmlSpacing(selfText);
    }
    toReturn.put(SubmissionsEntry.COLUMN_TEXT, selfText);
    toReturn.put(SubmissionsEntry.COLUMN_VOTE, submission.getVote().getValue());

    return toReturn;
  }


}
