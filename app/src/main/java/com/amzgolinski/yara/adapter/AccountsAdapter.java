package com.amzgolinski.yara.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amzgolinski.yara.R;

import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder>{

  private static final String LOG_TAG = AccountsAdapter.class.getName();

  // Store a member variable for the contacts
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
    Button button = viewHolder.messageButton;
    button.setText("Message");
  }

  // Returns the total count of items in the list
  @Override
  public int getItemCount() {
    return mAccounts.size();
  }

  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  public static class ViewHolder extends RecyclerView.ViewHolder {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    public TextView nameTextView;
    public Button messageButton;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView) {
      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);

      nameTextView = (TextView) itemView.findViewById(R.id.account_name);
      messageButton = (Button) itemView.findViewById(R.id.message_button);
    }
  }

}
