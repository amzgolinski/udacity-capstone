package com.amzgolinski.yara.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amzgolinski.yara.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Utils {

  private static final String LOG_TAG = Utils.class.getName();
  private static final String EMPTY_STRING = "";

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
  }

  public static boolean isCursorEmpty(Cursor data) {
    return (data == null || !data.moveToNext());
  }

  public static boolean isNetworkAvailable(Context ctx) {
    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  public static boolean isStringEmpty(String toTest) {
    return (toTest == null || toTest.equals(EMPTY_STRING));
  }

  public static String getOauthRefreshToken(Context context, String username) {

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context);

    String oauthToken =  prefs.getString(username, EMPTY_STRING);
    Log.d(LOG_TAG, String.format("Username %s\nToken %s ", username, oauthToken));
    return oauthToken;
  }

  public static void putOauthRefreshToken(Context context, String username, String oauthToken) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(username, oauthToken);
    editor.apply();
  }

  public static ArrayList<String> getUsers(Context context) {
    return new ArrayList<>(Arrays.asList("user1", "user2", "user3", "user4"));
    //return new ArrayList<>(getUserHashMap(context));
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



}

