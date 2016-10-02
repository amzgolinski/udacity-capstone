package com.amzgolinski.yara.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.amzgolinski.yara.data.RedditContract.SubredditEntry;


public class FetchSubredditsTask extends AsyncTask<Void, Void, HashMap<String, Subreddit>> {

  private Context mContext;

  public FetchSubredditsTask(Context context) {
    mContext = context;
  }

  private static final String LOG_TAG = FetchSubredditsTask.class.getName();

  public HashMap<String, Subreddit> doInBackground(Void... params) {

    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    UserSubredditsPaginator paginator = new UserSubredditsPaginator(redditClient, "subscriber");
    Log.i(LOG_TAG, "Paginator: " + paginator.toString());

    HashMap<String, Subreddit> latestSubreddits = new HashMap<>();

    while (paginator.hasNext()) {
      Listing<Subreddit> subreddits = paginator.next();
      for (Subreddit subreddit : subreddits) {
        Log.d(LOG_TAG, "Subreddit " + subreddit.toString());
        if (!subreddit.isNsfw()) {
          latestSubreddits.put(subreddit.getId(), subreddit);
        }
      }
    }
    int numInserted = addSubreddits(new ArrayList<Subreddit>(latestSubreddits.values()));
    Log.i(LOG_TAG, "Inserted " + numInserted +  " subreddits");
    return latestSubreddits;
  }

  @Override
  public void onPostExecute(HashMap<String, Subreddit> result) {
    for (Subreddit subreddit : result.values()) {
      Log.d(LOG_TAG, "Subreddit " + Long.parseLong(subreddit.getId(), 36));
    }
  }

  private int addSubreddits(ArrayList<Subreddit> subreddits) {

    int numInserted = 0;

    if (subreddits.size() > 0) {
      Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(subreddits.size());

      for (Subreddit subreddit : subreddits) {
        ContentValues subredditValues = subredditToValue(subreddit);
        Log.d(LOG_TAG, "Subreddit Values: " + subredditValues.toString());
        contentValuesVector.add(subredditValues);
      }

      ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
      contentValuesVector.toArray(contentValuesArray);

      numInserted = mContext.getContentResolver()
          .bulkInsert(SubredditEntry.CONTENT_URI, contentValuesArray);
    }
    return numInserted;
  }

  private ContentValues subredditToValue(Subreddit subreddit) {

    ContentValues toReturn = new ContentValues();
    Long subredditId = Long.parseLong(subreddit.getId(), 36);
    toReturn.put(SubredditEntry.COLUMN_SUBREDDIT_ID, subredditId.longValue());
    toReturn.put(SubredditEntry.COLUMN_NAME, subreddit.getDisplayName());
    toReturn.put(SubredditEntry.COLUMN_RELATIVE_LOCATION, subreddit.getRelativeLocation());
    toReturn.put(SubredditEntry.COLUMN_TITLE, subreddit.getTitle());
    toReturn.put(SubredditEntry.COLUMN_SELECTED, "1");

    return toReturn;
  }


}
