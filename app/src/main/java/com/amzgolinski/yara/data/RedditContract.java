package com.amzgolinski.yara.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


public class RedditContract {

  public static final String CONTENT_AUTHORITY = "com.amzgolinski.yara.provider";

  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

  public static final String PATH_SUBREDDITS   = "subreddits";

  public static final class SubredditEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDITS).build();

    public static final String CONTENT_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;

    public static final String CONTENT_ITEM_TYPE =
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;

    // table name
    public static final String TABLE_NAME = "subreddits";

    // columns
    public static final String COLUMN_ID                = "_id";
    public static final String COLUMN_SUBREDDIT_ID      = "subreddit_id";
    public static final String COLUMN_NAME              = "name";
    public static final String COLUMN_TITLE             = "title";
    public static final String COLUMN_RELATIVE_LOCATION = "relative_location";
    public static final String COLUMN_SELECTED          = "selected";


    public static Uri buildSubredditUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

  }

}
