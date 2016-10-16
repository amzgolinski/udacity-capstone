package com.amzgolinski.yara.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amzgolinski.yara.R;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.HashSet;

public class Utils {

  public static final String EMPTY_STRING = "";
  private static final String LOG_TAG = Utils.class.getName();

  public static final int UPVOTE = 1;
  public static final int NOVOTE = 0;
  public static final int DOWNVOTE = -1;

  public static int convertDpToPixels(Context context, int dp) {
      return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
  }

  public static void logOutCurrentUser(Context context) {
    Log.d(LOG_TAG, "logOutCurrentUser");
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    SharedPreferences.Editor editor = prefs.edit();
    editor.remove(context.getString(R.string.current_user_key));
    editor.apply();
  }

  public static String getOauthRefreshToken(Context context, String username) {

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    String oauthToken =  prefs.getString(username, EMPTY_STRING);
    Log.d(LOG_TAG, String.format("Username %s\nToken %s ", username, oauthToken));
    return oauthToken;
  }

  public static VoteDirection getVote(int currentVote, int newVote) {
    Log.d(LOG_TAG, "current " + currentVote + " new " + newVote);
    VoteDirection toReturn = VoteDirection.NO_VOTE;

    if (newVote > currentVote ) {
      toReturn = VoteDirection.UPVOTE;
    } else if (newVote < currentVote) {
      toReturn = VoteDirection.DOWNVOTE;
    }
    Log.d(LOG_TAG, toReturn.toString());
    return toReturn;
  }

  public static boolean isCursorEmpty(Cursor data) {
    return (data == null || !data.moveToNext());
  }

  public static String getCurrentUser(Context context) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);
    String currentUser
        = prefs.getString(context.getString(R.string.current_user_key), EMPTY_STRING);
    return currentUser;
  }

  public static boolean isLoggedIn(Context context) {
    return !Utils.getCurrentUser(context).equals(EMPTY_STRING);
  }

  public static boolean isMarkedNsfw(String toCheck) {
    return toCheck.toLowerCase().contains("nsfw");
  }

  public static boolean isNetworkAvailable(Context ctx) {
    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  public static boolean isStringEmpty(String toTest) {
    return (toTest == null || toTest.equals(EMPTY_STRING));
  }

  public static boolean isSubmissionReadOnly(Submission submission) {
    return (submission.isArchived() || submission.isLocked());
  }

  public static boolean isValidSubmission(Submission submission) {
    return (
        (!submission.isNsfw()) ||
            (Utils.isMarkedNsfw(submission.getTitle())) ||
            (!submission.isHidden()) ||
            (!submission.isStickied())
    );
  }

  public static String longToRedditId(long id) {
    return Long.toString(id, 36);
  }

  public static void setCurrentUser(Context context, String username) {
    Log.d(LOG_TAG, "Current user: " + username);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(context.getString(R.string.current_user_key), username);
    editor.apply();
  }

  public static void putOauthRefreshToken(Context context, String username, String oauthToken) {
    Log.d(LOG_TAG, String.format("Adding token: username %s\nToken %s ", username, oauthToken));
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(username, oauthToken);
    editor.apply();
  }

  public static long redditIdToLong(String redditId) {

    Long id = Long.parseLong(redditId, 36);
    //Log.d(LOG_TAG, "ID: " + redditId + " translated: " + Long.toString(id));
    return id;
  }

  public static long redditParentIdToLong(String parentId) {
    Long id = Long.parseLong(parentId.substring(3), 36);
    return id;
  }

  public static String removeHtmlSpacing(String html) {

    html = html.replace("<div class=\"md\">", "");
    html = html.replace("</div>", "");
    html = html.replace("<p>", "");
    html = html.replace("</p>", "");

    return html;
  }

}

