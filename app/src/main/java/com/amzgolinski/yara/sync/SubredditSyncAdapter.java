package com.amzgolinski.yara.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;

import com.amzgolinski.yara.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubredditSyncAdapter extends AbstractThreadedSyncAdapter {

  // log tag
  public final String LOG_TAG = SubredditSyncAdapter.class.getSimpleName();

  // widget action
  public static final String ACTION_DATA_UPDATED = "com.amzgolinski.yara.ACTION_DATA_UPDATED";

  // Interval at which to sync with the weather, in seconds.
  // 60 seconds (1 minute) * 180 = 3 hours
  public static final int SYNC_INTERVAL = 60 * 180;
  public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
  private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
  private static final int WEATHER_NOTIFICATION_ID = 3004;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
      LOCATION_STATUS_OK,
      LOCATION_STATUS_SERVER_DOWN,
      LOCATION_STATUS_SERVER_INVALID,
      LOCATION_STATUS_UNKNOWN,
      LOCATION_STATUS_INVALID})
  public @interface LocationStatus {
  }

  public static final int LOCATION_STATUS_OK = 0;
  public static final int LOCATION_STATUS_SERVER_DOWN = 1;
  public static final int LOCATION_STATUS_SERVER_INVALID = 2;
  public static final int LOCATION_STATUS_UNKNOWN = 3;
  public static final int LOCATION_STATUS_INVALID = 4;

  public SubredditSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
  }

  /**
   * Helper method to schedule the sync adapter periodic execution
   */
  public static void configurePeriodicSync(Context context, int syncInterval,
                                           int flexTime) {
    Account account = getSyncAccount(context);
    String authority = context.getString(R.string.content_authority);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // we can enable inexact timers in our periodic sync
      SyncRequest request = new SyncRequest.Builder().
          syncPeriodic(syncInterval, flexTime).
          setSyncAdapter(account, authority).
          setExtras(new Bundle()).build();
      ContentResolver.requestSync(request);
    } else {
      ContentResolver.addPeriodicSync(account,
          authority, new Bundle(), syncInterval);
    }
  }

  /**
   * Helper method to get the fake account to be used with SyncAdapter, or make
   * a new one if the fake account doesn't exist yet.  If we make a new account,
   * we call the onAccountCreated method so we can initialize things.
   *
   * @param context The context used to access the account service
   * @return a fake account.
   */
  public static Account getSyncAccount(Context context) {
    // Get an instance of the Android account manager
    AccountManager accountManager =
        (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

    // Create the account type and default account
    Account newAccount = new Account(
        context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

    // If the password doesn't exist, the account doesn't exist
    if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
      if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
        return null;
      }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

      onAccountCreated(newAccount, context);
    }
    return newAccount;
  }

  public static void initializeSyncAdapter(Context context) {
    getSyncAccount(context);
  }

  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
                            ContentProviderClient provider,
                            SyncResult syncResult) {

    Log.d(LOG_TAG, "Starting sync");
    return;
  }

  public static void syncImmediately(Context context) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    ContentResolver.requestSync(getSyncAccount(context),
        context.getString(R.string.content_authority), bundle);
  }



  private static void onAccountCreated(Account newAccount, Context context) {

    // Since we've created an account
    SubredditSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

    // Without calling setSyncAutomatically, our periodic sync will not be
    // enabled.
    ContentResolver.setSyncAutomatically(
        newAccount,
        context.getString(R.string.content_authority),
        true
    );

    //  Finally, let's do a sync to get things started
    syncImmediately(context);
  }


  /**
   *
   */
  private void updateWidgets() {
    Context context = getContext();
    // Setting the package ensures that only components in our app will receive
    // the broadcast
    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
        .setPackage(context.getPackageName());
    context.sendBroadcast(dataUpdatedIntent);

  }
}
