package com.amzgolinski.yara.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.YaraApplication;
import com.amzgolinski.yara.callbacks.AccountRetrievedCallback;
import com.amzgolinski.yara.tasks.FetchLoggedInAccountTask;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.LoggedInAccount;


import java.util.ArrayList;

import butterknife.OnClick;

public class AccountsActivity extends AppCompatActivity implements AccountRetrievedCallback {

  private static final String LOG_TAG = AccountsActivity.class.getName();

  private ArrayList<String> mUsers;
  private LoggedInAccount mRedditAccount;

  @OnClick(R.id.login_button)
  public void login(View view) {

    //this.revokeToken(YaraApplication.CREDENTIALS);
    //startActivity(new Intent(this, LoginActivity.class));

  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_accounts);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mUsers = Utils.getUsers(this.getApplicationContext());
    new FetchLoggedInAccountTask(this.getApplicationContext(), this).execute();
    Log.d(LOG_TAG, mUsers.toString());

  }

  public void onAccountRetrieved(LoggedInAccount account) {
    Log.d(LOG_TAG, "onAccountRetrieved");
    mRedditAccount = account;
  }

  private void revokeToken() {

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          AuthenticationManager
              .get()
              .getRedditClient()
              .getOAuthHelper()
              .revokeRefreshToken(YaraApplication.CREDENTIALS);

        } catch (NetworkException | NullPointerException networkException) {
          Log.e(LOG_TAG, "Could not log in", networkException);

        }
        return null;
      }

    }.execute();
  }
}

