package com.amzgolinski.yara.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.callbacks.AccountRetrievedCallback;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.LoggedInAccount;

public class FetchLoggedInAccountTask extends AsyncTask<Void, Void, LoggedInAccount> {

  private static final String LOG_TAG = FetchSubredditsTask.class.getName();

  private Context mContext;
  private AccountRetrievedCallback mCallback;

  public FetchLoggedInAccountTask(Context context, AccountRetrievedCallback callback) {
    mContext = context;
    mCallback = callback;
  }

  @Override
  protected LoggedInAccount doInBackground(Void... params) {
      return AuthenticationManager.get().getRedditClient().me();
  }

  @Override
  protected void onPostExecute(LoggedInAccount account) {
    Log.d(LOG_TAG, "Retrieved Valid user");
    Utils.setCurrentUser(mContext, account.getFullName());
    mCallback.onAccountRetrieved(account);
  }

}
