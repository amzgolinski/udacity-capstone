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

import java.util.ArrayList;
import java.util.HashSet;

public class Utils {

  private static final String LOG_TAG = Utils.class.getName();
  private static final String EMPTY_STRING = "";

  public interface CommentType {
    int HAS_MORE_COMMENTS = 0;
    int NO_REPLIES = 1;
  }

  public static void addUser(Context context, String username, String oauthToken) {
    Log.d(LOG_TAG, "addUser");
    Log.d(LOG_TAG, String.format("Username %s Token %s", username, oauthToken));
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    HashSet<String> users = Utils.getUserHashMap(context);

    if (!users.contains(username)) {
      users.add(username);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putStringSet(context.getString(R.string.accounts_key), users);
      editor.apply();
    }

    Utils.putOauthRefreshToken(context, username, oauthToken);
    Utils.putCurrentUser(context, username);
  }

  public static int convertDpToPixels(Context context, int dp) {
      return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
  }

  public static void deleteUser(Context context, String username) {
    Log.d(LOG_TAG, "deleteUser");
    Log.d(LOG_TAG, String.format("Username %s ", username));
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    HashSet<String> users = Utils.getUserHashMap(context);
    users.remove(username);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putStringSet(context.getString(R.string.accounts_key), users);
    editor.remove(username);
    editor.apply();
  }

  public static String getCurrentUser(Context context) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    String user =  prefs.getString(context.getString(R.string.current_user_key), EMPTY_STRING);
    Log.d(LOG_TAG, String.format("Username %s", user));
    return user;
  }

  public static String getOauthRefreshToken(Context context, String username) {

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    String oauthToken =  prefs.getString(username, EMPTY_STRING);
    Log.d(LOG_TAG, String.format("Username %s\nToken %s ", username, oauthToken));
    return oauthToken;
  }

  public static HashSet<String> getUserHashMap(Context context) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    HashSet<String> users = (HashSet<String>) prefs.getStringSet(
        context.getString(R.string.accounts_key),
        new HashSet<String>()
    );
    return users;
  }

  public static ArrayList<String> getUsers(Context context) {
    return new ArrayList<>(getUserHashMap(context));
  }

  public static boolean isCursorEmpty(Cursor data) {
    return (data == null || !data.moveToNext());
  }

  public static boolean isNetworkAvailable(Context ctx) {
    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  /**
   *
   */
  public static boolean isValidSubmission(Submission submission) {
    return (
        (!submission.isNsfw()) ||
            (Utils.isMarkedNsfw(submission.getTitle())) ||
            (!submission.isHidden()) ||
            (!submission.isStickied())
    );
  }

  /**
   *
   */
  public static boolean isMarkedNsfw(String toCheck) {
    return toCheck.toLowerCase().contains("nsfw");
  }

  /**
   *
   */
  public static boolean isStringEmpty(String toTest) {
    return (toTest == null || toTest.equals(EMPTY_STRING));
  }

  /**
   *
   */
  public static boolean isSubmissionReadOnly(Submission submission) {
    return (submission.isArchived() || submission.isLocked());
  }

  /**
   *
   */
  public static String longToRedditId(long id) {
    //Log.d(LOG_TAG, "ID: " + id + " translated " + Long.toString(id, 36));
    return Long.toString(id, 36);
  }

  /**
   *
   */
  public static void putCurrentUser(Context context, String username) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(context.getString(R.string.current_user_key), username);
    editor.apply();
  }

  /**
   *
   */
  public static void putOauthRefreshToken(Context context, String username, String oauthToken) {
    Log.d(LOG_TAG, String.format("Adding token: username %s\nToken %s ", username, oauthToken));
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(username, oauthToken);
    editor.apply();
  }

  /**
   *
   */
  public static long redditIdToLong(String redditId) {

    Long id = Long.parseLong(redditId, 36);
    //Log.d(LOG_TAG, "ID: " + redditId + " translated: " + Long.toString(id));
    return id;
  }

  /**
   *
   */
  public static long redditParentIdToLong(String parentId) {
    Long id = Long.parseLong(parentId.substring(3), 36);
    return id;
  }

  /**
   *
   */
  public static String removeHtmlSpacing(String html) {

    html = html.replace("<div class=\"md\">", "");
    html = html.replace("</div>", "");
    html = html.replace("<p>", "");
    html = html.replace("</p>", "");

    return html;
  }




}

