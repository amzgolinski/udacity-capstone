package com.amzgolinski.yara.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amzgolinski.yara.YaraApplication;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.LoggedInAccount;


public class SetRefreshTokenTask extends AsyncTask<String, Void, LoggedInAccount> {

  private static final String LOG_TAG = SetRefreshTokenTask.class.getName();

  public SetRefreshTokenTask() {
    // empty
  }

  @Override
  protected LoggedInAccount doInBackground(String... params) {
    try {
      Log.d(LOG_TAG, "Refresh token: " + params[0]);

      AuthenticationManager.get().getRedditClient().getOAuthHelper().setRefreshToken(params[0]);
      AuthenticationManager.get().refreshAccessToken(YaraApplication.CREDENTIALS);
      AuthenticationManager.get().getRedditClient().authenticate(AuthenticationManager.get().getRedditClient().getOAuthData());
      //AuthenticationManager.get().onAuthenticated(AuthenticationManager.get().getRedditClient().getOAuthData());
      Log.d(LOG_TAG, "Authed user: " + AuthenticationManager.get().getRedditClient().getAuthenticatedUser());
      return AuthenticationManager.get().getRedditClient().me();
    } catch (NoSuchTokenException | OAuthException e) {
      Log.e(LOG_TAG, "Could not set new access token", e);
    }
    return null;
  }

  @Override
  protected void onPostExecute(LoggedInAccount account) {
    Log.d(LOG_TAG, "Reauthenticated");
    //mCallback.onAccountRetrieved(account);
  }
}
