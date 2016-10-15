package com.amzgolinski.yara.adapter;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.callbacks.RedditDownloadCallback;
import com.amzgolinski.yara.model.CommentItem;
import com.amzgolinski.yara.service.YaraUtilityService;
import com.amzgolinski.yara.tasks.FetchMoreCommentsTask;
import com.amzgolinski.yara.util.Utils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;


public class CommentsAdapter extends ArrayAdapter<CommentItem>
    implements RedditDownloadCallback {

  private static final String LOG_TAG = CommentsAdapter.class.getName();

  private static class ViewHolder {
    LinearLayout commentContainer;
    TextView commentAuthor;
    TextView commentBody;
  }

  private Context mContext;
  private ArrayList<CommentItem> mComments;

  public CommentsAdapter(Context context, ArrayList<CommentItem> comments) {

    super(context, R.layout.comment_item, comments);
    Log.d(LOG_TAG, "CommentsAdapter");
    mContext = context;
    mComments = comments;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {

    final CommentItem comment = getItem(position);
    ViewHolder viewHolder;

    if (convertView == null) {
      // If there's no view to re-use, inflate a brand new view for row
      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.comment_item, parent, false);
      viewHolder.commentBody = (TextView) convertView.findViewById(R.id.comment_body);
      viewHolder.commentAuthor = (TextView) convertView.findViewById(R.id.comment_author);
      viewHolder.commentContainer = (LinearLayout) convertView.findViewById(R.id.comment_container);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    if (comment.getType() == CommentItem.CommentType.HAS_MORE_COMMENTS) {

      viewHolder.commentBody.setClickable(true);
      viewHolder.commentAuthor.setText(Utils.EMPTY_STRING);
      viewHolder.commentBody.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
          //new FetchMoreCommentsTask(mComments, position, CommentsAdapter.this).execute(comment);
          YaraUtilityService.fetchMoreComments(mContext, mComments, position);
        }
      });
      viewHolder.commentBody.setText(
          String.format(
              mContext.getResources().getString(R.string.more_comments),
              comment.getMoreCommentsCount()
          ));
      viewHolder.commentBody.setTextAppearance(R.style.comment_more_comments);

    } else {
      viewHolder.commentBody.setClickable(false);
      viewHolder.commentBody.setTextAppearance(R.style.comment_text);

      // author
      viewHolder.commentAuthor.setText(comment.getAuthor());

      // comment body
      String unescape = StringEscapeUtils.unescapeHtml4(comment.getBody());
      viewHolder.commentBody.setText(Html.fromHtml(Utils.removeHtmlSpacing(unescape)));
      viewHolder.commentBody.setMovementMethod(LinkMovementMethod.getInstance());

      // up vote arrow
    }

    int indent = Utils.convertDpToPixels(mContext, 16) * comment.getDepth();
    LinearLayout.LayoutParams llp
        = (LinearLayout.LayoutParams) viewHolder.commentContainer.getLayoutParams();
    llp.setMargins(indent, 0, 0, 0); //
    viewHolder.commentContainer.setLayoutParams(llp);

    return convertView;
  }

  public void setComments(ArrayList<CommentItem> comments) {
    mComments = comments;
  }

  public void onDownloadComplete(Object result) {
    ArrayList<CommentItem> comments = (ArrayList<CommentItem>) result;
    this.setComments(comments);
    this.clear();
    this.addAll(comments);
    this.notifyDataSetChanged();
  }

  public void reloadComments(ArrayList comments) {
    Log.d(LOG_TAG, "reloadComments");
    this.setComments(comments);
    this.clear();
    this.addAll(comments);
    this.notifyDataSetChanged();
  }

}
