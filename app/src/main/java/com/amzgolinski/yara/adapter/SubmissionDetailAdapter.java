package com.amzgolinski.yara.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.ui.SubmissionDetailFragment;
import com.amzgolinski.yara.util.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SubmissionDetailAdapter extends CursorAdapter {

  private static final String LOG_TAG = SubmissionDetailAdapter.class.getName();

  @BindView(R.id.submission_detail_title) TextView mSubmissionTitle;
  @BindView(R.id.submission_detail_text) TextView mSubmissionText;
  @BindView(R.id.submission_detail_subreddit_name) TextView mSubredditName;
  @BindView(R.id.submission_detail_comments) TextView mSubmissionCommentCount;
  @BindView(R.id.submission_detail_score) TextView mSubmissionScore;
  @BindView(R.id.submission_detail_image) ImageView mThumbnail;

  public SubmissionDetailAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  @Override
  public View newView(Context context, Cursor data, ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View detailView = inflater.inflate(R.layout.submission_detail, parent, false);
    ButterKnife.bind(this, detailView);
    return detailView;
  }

  @Override
  public void bindView(View view, final Context context, final Cursor cursor) {
    Log.d(LOG_TAG, "bindView");
    //Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(cursor));

    // submission title
    mSubmissionTitle.setText(cursor.getString(SubmissionDetailFragment.COL_TITLE));

    // submission subreddit name
    String subredditText = String.format(
        mContext.getResources().getString(R.string.subreddit_name),
        cursor.getString(SubmissionDetailFragment.COL_SUBREDDIT_NAME)
    );
    mSubredditName.setText(subredditText);
    String selfText = cursor.getString(SubmissionDetailFragment.COL_TEXT);
    if (Utils.isStringEmpty(selfText)) {
      mSubmissionText.setVisibility(View.GONE);
    } else {
      mSubmissionText.setText(Html.fromHtml(selfText));
      mSubmissionText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // submission comment count
    mSubmissionCommentCount.setText(cursor.getString(SubmissionDetailFragment.COL_COMMENT_COUNT));

    // submission score
    mSubmissionScore.setText(cursor.getString(SubmissionDetailFragment.COL_SCORE));

    // submission thumbnail
    String thumbnail = cursor.getString(SubmissionDetailFragment.COL_THUMBNAIL);
    if (!Utils.isStringEmpty(thumbnail)) {

      Picasso.with(context)
          .load(thumbnail)
          .error(R.drawable.ic_do_not_distrub_black_24dp)
          .into(mThumbnail);
    } else {
      mThumbnail.setVisibility(View.GONE);
    }
  }
}
