package com.amzgolinski.yara.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by azgolinski on 9/25/16.
 */

public class RedditDbHelper extends SQLiteOpenHelper {

  public static final String LOG_TAG = RedditDbHelper.class.getSimpleName();

  private static final String DATABASE_NAME = "reddit.db";
  private static final int DATABASE_VERSION = 1;

  public RedditDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {

    final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
        RedditContract.SubredditEntry.TABLE_NAME + " (" +
        RedditContract.SubredditEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID + " INTEGER DEFAULT 0, " +
        RedditContract.SubredditEntry.COLUMN_TITLE + " TEXT, " +
        RedditContract.SubredditEntry.COLUMN_NAME + " TEXT, " +
        RedditContract.SubredditEntry.COLUMN_SELECTED + " INTEGER NOT NULL DEFAULT 0," +
        RedditContract.SubredditEntry.COLUMN_RELATIVE_LOCATION + " TEXT, " +

        " UNIQUE (" + RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID +
        ") ON CONFLICT IGNORE);";

    Log.d(LOG_TAG, SQL_CREATE_MOVIE_TABLE);

    sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
                        int newVersion) {

    Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
        newVersion + ". OLD DATA WILL BE DESTROYED");

    // Drop the Subreddits table
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditContract.SubredditEntry.TABLE_NAME);

    // Drop the Subreddits ID Sequence
    sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"
        + RedditContract.SubredditEntry.TABLE_NAME + "'");

    onCreate(sqLiteDatabase);

  }

}
