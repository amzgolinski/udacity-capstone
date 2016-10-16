package com.amzgolinski.yara.adapter;


import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.YaraApplication;
import com.amzgolinski.yara.tasks.SetRefreshTokenTask;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;

import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder>{

  private static final String LOG_TAG = AccountsAdapter.class.getName();

  // Store a member variable for the accounts
  private List<String> mAccounts;

  // Store the context for easy access
  private Context mContext;

  // Pass in the contact array into the constructor
  public AccountsAdapter(Context context, List<String> accounts) {
    mAccounts = accounts;
    mContext = context;
  }

  // Easy access to the context object in the recyclerview
  private Context getContext() {
    return mContext;
  }

  // Usually involves inflating a layout from XML and returning the holder
  @Override
  public AccountsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    // Inflate the custom layout
    View accountView = inflater.inflate(R.layout.account_item, parent, false);

    // Return a new holder instance
    ViewHolder viewHolder = new ViewHolder(accountView);
    return viewHolder;
  }

  // Involves populating data into the item through holder
  @Override
  public void onBindViewHolder(AccountsAdapter.ViewHolder viewHolder, int position) {
    // Get the data model based on position
    String account = mAccounts.get(position);

    // Set item views based on your views and data model
    TextView textView = viewHolder.nameTextView;
    textView.setText(account);
    ImageButton delete = viewHolder.deleteButton;
    delete.setTag(position);
  }

  // Returns the total count of items in the list
  @Override
  public int getItemCount() {
    return mAccounts.size();
  }

  public void deleteUser(int userNum) {
    String user = mAccounts.get(userNum);
    String message = String.format("Deleting user %s ", user);
    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    String loggedIn = Utils.getCurrentUser(mContext);
    Log.d(LOG_TAG, "Logged in user: " + loggedIn);
    // user to delete is currently logged in
    mAccounts.remove(userNum);

    if (user.equals(loggedIn)) {

      if (mAccounts.size() >= 1) {
        String newUser = mAccounts.get(0);
        //Utils.putCurrentUser(mContext, newUser);
        String oauthToken = Utils.getOauthRefreshToken(mContext, newUser);
        new SetRefreshTokenTask().execute(oauthToken);

      }

    }

    Utils.logOutCurrentUser(mContext);
    this.notifyDataSetChanged();
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



  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    public TextView nameTextView;
    public ImageButton deleteButton;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      itemView.setOnClickListener(this);

      nameTextView = (TextView) itemView.findViewById(R.id.account_name);
      deleteButton = (ImageButton) itemView.findViewById(R.id.delete_account_button);
      deleteButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View view) {
          int userNum = (Integer) view.getTag();
          AccountsAdapter.this.deleteUser(userNum);
        }
      });
    }

    public void onClick(View view) {
      int pos = getAdapterPosition();

      Log.d(LOG_TAG, "onClick " + Integer.toString(pos));
      // Check if an item was deleted, but the user clicked it before the UI removed it
      if (pos != RecyclerView.NO_POSITION) {
        String newUser = mAccounts.get(pos);
        Log.d(LOG_TAG, "User: " + newUser);
        //tils.putCurrentUser(mContext, newUser);
        String oauthToken = Utils.getOauthRefreshToken(mContext, newUser);
        new SetRefreshTokenTask().execute(oauthToken);
        // We can access the data within the views
        Toast.makeText(mContext, nameTextView.getText(), Toast.LENGTH_SHORT).show();
      }
    }
  }

}
