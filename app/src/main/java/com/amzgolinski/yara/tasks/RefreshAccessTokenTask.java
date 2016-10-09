package com.amzgolinski.yara.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.YaraApplication;
import com.amzgolinski.yara.callbacks.AccountRetrievedCallback;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.LoggedInAccount;


public class RefreshAccessTokenTask extends AsyncTask<Void, Void, LoggedInAccount> {

  private static final String LOG_TAG = FetchSubredditsTask.class.getName();

  private Context mContext;
  private AccountRetrievedCallback mCallback;

  public RefreshAccessTokenTask(Context context, AccountRetrievedCallback callback) {
    mContext = context;
    mCallback = callback;
  }

  @Override
  protected LoggedInAccount doInBackground(Void... params) {
    try {
      AuthenticationManager.get().refreshAccessToken(YaraApplication.CREDENTIALS);
      String user = AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
      String token = AuthenticationManager.get().getRedditClient().getOAuthHelper().getRefreshToken();
      Utils.addUser(mContext, user, token);
      return AuthenticationManager.get().getRedditClient().me();
    } catch (NoSuchTokenException | OAuthException e) {
      Log.e(LOG_TAG, "Could not refresh access token", e);
    }
    return null;
  }

  @Override
  protected void onPostExecute(LoggedInAccount account) {
    Log.d(LOG_TAG, "Reauthenticated");
    mCallback.onAccountRetrieved(account);
  }
}
