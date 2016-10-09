package com.amzgolinski.yara.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.adapter.AccountsAdapter;
import com.amzgolinski.yara.util.Utils;

import java.util.ArrayList;

public class AccountsActivityFragment extends Fragment {

  private static final String LOG_TAG = AccountsActivityFragment.class.getName();

  private AccountsAdapter mAccountsAdapter;

  public AccountsActivityFragment() {
    // empty
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.fragment_accounts, container, false);

    // Lookup the recyclerview in activity layout
    RecyclerView accountsView = (RecyclerView) root.findViewById(R.id.accounts);

    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL_LIST);
    accountsView.addItemDecoration(itemDecoration);

    ArrayList<String> list = Utils.getUsers(this.getContext());
    Log.d(LOG_TAG, list.toString());

    // Create adapter passing in the sample user data
    mAccountsAdapter = new AccountsAdapter(this.getContext(), list);

    // Attach the adapter to the recyclerview to populate items
    accountsView.setAdapter(mAccountsAdapter);

    // Set layout manager to position the items
    accountsView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    return root;
  }

}
