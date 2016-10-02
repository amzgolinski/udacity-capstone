package com.amzgolinski.yara.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class RedditProvider extends ContentProvider {

  private static final String LOG_TAG = RedditProvider.class.getSimpleName();

  private static final UriMatcher sUriMatcher = buildUriMatcher();
  private RedditDbHelper mRedditDbHelper;

  private static final int SUBREDDIT = 100;
  private static final int SUBREDDIT_WITH_ID = 101;

  private static UriMatcher buildUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = RedditContract.CONTENT_AUTHORITY;

    // Movies
    matcher.addURI(authority, RedditContract.SubredditEntry.TABLE_NAME, SUBREDDIT);

    matcher.addURI(authority, RedditContract.SubredditEntry.TABLE_NAME + "/#", SUBREDDIT_WITH_ID);
    return matcher;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mRedditDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsDeleted = 0;

    switch (match) {

      case SUBREDDIT: {
        rowsDeleted = db.delete(
            RedditContract.SubredditEntry.TABLE_NAME,
            selection,
            selectionArgs
        );
        break;
      }

      case SUBREDDIT_WITH_ID: {

        rowsDeleted = db.delete(
            RedditContract.SubredditEntry.TABLE_NAME,
            RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID+ " = ?",
            new String[]{String.valueOf(ContentUris.parseId(uri))}
        );

        break;
      }
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);

    }
    if (rowsDeleted > 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsDeleted;
  }

  @Override
  public String getType(Uri uri) {
    final int match = sUriMatcher.match(uri);

    switch (match) {
      case SUBREDDIT:
        return RedditContract.SubredditEntry.CONTENT_TYPE;

      case SUBREDDIT_WITH_ID:
        return RedditContract.SubredditEntry.CONTENT_ITEM_TYPE;

      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    final SQLiteDatabase db = mRedditDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri uriToReturn;

    switch (match) {
      case SUBREDDIT: {
        long _id = db.insert(RedditContract.SubredditEntry.TABLE_NAME, null, values);
        // insert unless it is already contained in the database
        if (_id > 0) {
          uriToReturn = RedditContract.SubredditEntry.buildSubredditUri(_id);
        } else {
          throw new android.database.SQLException("Failed to insert row into: " + uri);
        }
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return uriToReturn;
  }

  @Override
  public boolean onCreate() {
    mRedditDbHelper = new RedditDbHelper(getContext());
    return true;
  }

  private Cursor getSubreddit (String[] projection, String selection, String[] selectionArgs,
                               String sortOrder) {

    return mRedditDbHelper.getReadableDatabase().query(
        RedditContract.SubredditEntry.TABLE_NAME,
        projection,
        selection,
        selectionArgs,
        null, // GROUP BY
        null, // HAVING
        sortOrder
    );
  }

  private Cursor getSubredditById (Uri uri, String[] projection, String sortOrder) {

    String selection = RedditContract.SubredditEntry.TABLE_NAME + "."
        + RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID + " = ?";
    String[] args = new String[] {String.valueOf(ContentUris.parseId(uri))};
    return getSubreddit(projection, selection, args, sortOrder);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Cursor results = null;
    switch(sUriMatcher.match(uri)) {

      case SUBREDDIT: {
        results = getSubreddit(projection, selection, selectionArgs, sortOrder);
        break;
      }

      case SUBREDDIT_WITH_ID: {
        results = getSubredditById(uri, projection, sortOrder);
        break;
      }

      default:{
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }

    }
    return results;
  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] values) {

    final SQLiteDatabase db = mRedditDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case SUBREDDIT: {
        Log.d(LOG_TAG, "MATCHED SUBREDDIT");
        db.beginTransaction();
        int rowsInserted = 0;

        try {
          for (ContentValues value : values) {
            if (value == null) {
              throw new IllegalArgumentException("Null content values not allowed");
            }

            long _id = db.insert(
                RedditContract.SubredditEntry.TABLE_NAME,
                null,
                value
            );

            if (_id != -1) {
              rowsInserted++;
            }
          }
          if (rowsInserted > 0) {
            db.setTransactionSuccessful();
          }
        } finally {
          db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsInserted;
      }
      default:
        return super.bulkInsert(uri, values);
    }
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    Log.d(LOG_TAG, "update");
    final SQLiteDatabase db = mRedditDbHelper.getWritableDatabase();
    int rowsUpdated = 0;

    if (values == null){
      throw new IllegalArgumentException("Null content values not allowed");
    }

    int match = sUriMatcher.match(uri);
    switch(match){

      case SUBREDDIT: {
        rowsUpdated = db.update(
            RedditContract.SubredditEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs);
        break;
      }

      case SUBREDDIT_WITH_ID: {
        Log.d(LOG_TAG, "MOVIE_WITH_ID");
        Log.d(LOG_TAG, values.toString());
        rowsUpdated = db.update(
            RedditContract.SubredditEntry.TABLE_NAME,
            values,
            RedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID + " = ?",
            new String[] {String.valueOf(ContentUris.parseId(uri))});
        break;
      }
      default:{
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }

    if (rowsUpdated > 0){
      Log.d(LOG_TAG, uri.toString());
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return rowsUpdated;
  }
}
