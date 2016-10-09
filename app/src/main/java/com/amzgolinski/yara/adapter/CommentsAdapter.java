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

import com.amzgolinski.yara.R;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.models.CommentNode;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;


public class CommentsAdapter extends ArrayAdapter<CommentNode> {

  private static final String LOG_TAG = CommentsAdapter.class.getName();

  // View lookup cache
  private static class ViewHolder {
    LinearLayout commentContainer;
    TextView commentBody;
  }

  private Context mContext;
  private ArrayList<CommentNode> mComments;

  public CommentsAdapter(Context context, ArrayList<CommentNode> comments) {

    super(context, R.layout.comment_item, comments);
    Log.d(LOG_TAG, "CommentsAdapter");
    mContext = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {


    // Get the data item for this position
    CommentNode commentNode = getItem(position);

    ViewHolder viewHolder; // view lookup cache stored in tag

    if (convertView == null) {
      // If there's no view to re-use, inflate a brand new view for row
      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(getContext());

      convertView = inflater.inflate(R.layout.comment_item, parent, false);
      viewHolder.commentBody = (TextView) convertView.findViewById(R.id.comment_body);
      viewHolder.commentContainer = (LinearLayout) convertView.findViewById(R.id.comment_container);
      // Cache the viewHolder object inside the fresh view
      convertView.setTag(viewHolder);
    } else {
      // View is being recycled, retrieve the viewHolder object from tag
      viewHolder = (ViewHolder) convertView.getTag();
    }
    // Populate the data into the template view using the data object
    String unescape = StringEscapeUtils.unescapeHtml4(commentNode.getComment().data("body_html"));
    viewHolder.commentBody.setText(Html.fromHtml(Utils.removeHtmlSpacing(unescape)));
    viewHolder.commentBody.setMovementMethod(LinkMovementMethod.getInstance());

    int indent =
        Utils.convertDpToPixels(mContext, R.integer.comment_indent_dp) * commentNode.getDepth();

    LinearLayout.LayoutParams llp
        = (LinearLayout.LayoutParams) viewHolder.commentContainer.getLayoutParams();
    llp.setMarginStart(indent);
    viewHolder.commentContainer.setLayoutParams(llp);

    // Return the completed view to render on screen
    return convertView;
  }

  public void setComments(ArrayList<CommentNode> comments) {
    mComments = comments;
  }

}
