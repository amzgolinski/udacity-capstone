package com.amzgolinski.yara.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import com.amzgolinski.yara.R;

import java.util.HashMap;

public class UserInfoActivity extends AppCompatActivity {

  private static final String LOG_TAG = UserInfoActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_info);

    new AsyncTask<Void, Void, LoggedInAccount>() {
      @Override
      protected LoggedInAccount doInBackground(Void... params) {
        RedditClient redditClient = AuthenticationManager.get().getRedditClient();


        UserSubredditsPaginator paginator = new UserSubredditsPaginator(redditClient, "subscriber");
        Log.d(LOG_TAG, "Paginator: " + paginator.toString());

        HashMap<String, Subreddit> latestSubreddits = new HashMap<>();
        try {
          while (paginator.hasNext()) {
            Listing<Subreddit> subreddits = paginator.next();
            for (Subreddit subreddit: subreddits) {
              Log.d(LOG_TAG, "Subreddit " + subreddit.getDisplayName());
              if (!subreddit.isNsfw()) {
                latestSubreddits.put(subreddit.getId(), subreddit);
              }
            }
          }
        } catch (Exception e) {
          Log.d(LOG_TAG, e.getMessage());
          //checkError(, e);
          return null;
        }
        return AuthenticationManager.get().getRedditClient().me();
      }

      @Override
      protected void onPostExecute(LoggedInAccount data) {
        ((TextView) findViewById(R.id.user_name)).setText("Name: " + data.getFullName());
        ((TextView) findViewById(R.id.user_created)).setText("Created: " + data.getCreated());
        ((TextView) findViewById(R.id.user_link_karma)).setText("Link karma: " + data.getLinkKarma());
        ((TextView) findViewById(R.id.user_comment_karma)).setText("Comment karma: " + data.getCommentKarma());
        ((TextView) findViewById(R.id.user_has_mail)).setText("Has mail? " + (data.getInboxCount() > 0));
        ((TextView) findViewById(R.id.user_inbox_count)).setText("Inbox count: " + data.getInboxCount());
        ((TextView) findViewById(R.id.user_is_mod)).setText("Is mod? " + data.isMod());
      }
    }.execute();
  }

  protected void checkError(Context ctx, Exception e) {
    Log.e(LOG_TAG, "Received error from JRAW", e);

    String message = e.getMessage();

    /*
    if (message.contains(Consts.NOT_AUTHORIZED)) {
      Log.w(LOG_TAG, "Looks like user is no longer authorized. Will have to re-authenticate");
      UtilityService.startActionRemoveUserData(ctx);
      Utils.clearUserState(ctx);
    } else {
      Log.d(LOG_TAG, "Sync status: " + SyncStatusUtils.SYNC_STATUS_SERVER_INVALID + "(" + e.getMessage() + ")");
      SyncStatusUtils.setSyncStatus(ctx, getSyncStatusKeyName(), SyncStatusUtils.SYNC_STATUS_SERVER_INVALID);
    }
    */
  }
}