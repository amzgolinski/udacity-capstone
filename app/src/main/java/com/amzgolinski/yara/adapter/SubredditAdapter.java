package com.amzgolinski.yara.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.activity.SubredditActivityFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.ViewHolder> {

  private static final String LOG_TAG = SubredditAdapter.class.getName();

  // Store the context for easy access
  private Context mContext;
  private CursorAdapter mCursorAdapter;

  public SubredditAdapter(Context context, Cursor cursor) {

    mContext = context;
    mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {

      @Override
      public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View subredditView = inflater.inflate(R.layout.subreddit_item, parent, false);
        return subredditView;
      }

      @Override
      public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(cursor));
        TextView subredditName = (TextView) view.findViewById(R.id.subreddit_name);
        subredditName.setText(cursor.getString(SubredditActivityFragment.COL_NAME));
      }
    };
  }

  public void swapCursor(Cursor cursor) {
    Log.d(LOG_TAG, "swapCursor");
    mCursorAdapter.swapCursor(cursor);
    notifyDataSetChanged();
    //mEmpty.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // Passing the binding operation to cursor loader
    mCursorAdapter.getCursor().moveToPosition(position);
    mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());

  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // Passing the inflater job to the cursor-adapter
    View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
    return new ViewHolder(v);
  }

  @Override
  public int getItemCount() {
    Log.d(LOG_TAG, "swapCursor: " + mCursorAdapter.getCount());
    return mCursorAdapter.getCount();
  }

  // Provide a direct reference to each of the views within a data item
  // Used to cache the views within the item layout for fast access
  static class ViewHolder extends RecyclerView.ViewHolder {

    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    TextView mSubredditName;
    //@BindView(R.id.subreddit_name) TextView mSubredditName;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView) {

      // Stores the itemView in a public final member variable that can be used
      // to access the context from any ViewHolder instance.
      super(itemView);
      mSubredditName = (TextView) itemView.findViewById(R.id.subreddit_name);

    }
  }
}
