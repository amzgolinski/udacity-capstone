package com.amzgolinski.yara.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amzgolinski.yara.YaraApplication;
import com.amzgolinski.yara.data.RedditContract;
import com.amzgolinski.yara.model.CommentItem;
import com.amzgolinski.yara.model.YaraContribution;
import com.amzgolinski.yara.model.YaraThing;
import com.amzgolinski.yara.util.Utils;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class YaraUtilityService extends IntentService {

  private static final String LOG_TAG = YaraUtilityService.class.getName();

  // Actions
  public static final String ACTION_SUBMIT_VOTE = "com.amzgolinski.yara.service.action.VOTE";
  public static final String ACTION_SUBMIT_COMMENT = "com.amzgolinski.yara.service.action.COMMENT";
  public static final String ACTION_LOAD_MORE_COMMENTS
      = "com.amzgolinski.yara.service.action.LOAD_COMMENTS";
  public static final String ACTION_DELETE_ACCOUNT
      = "com.amzgolinski.yara.service.action.DELETE_ACCOUNT";
  public static final String ACTION_SUBMISSIONS_UPDATED
      = "com.amzgolinski.yara.service.action.SUBMISSIONS_UPDATED";
  public static final String ACTION_SUBREDDIT_UNSUBSCRIBE
      = "com.amzgolinski.yara.service.action.REDDIT_UNSUBSCRIBE";

  // parameters
  public static final String PARAM_COMMENTS = "com.amzgolinski.yara.service.extra.COMMENTS";
  private static final String COMMENT = "com.amzgolinski.yara.service.extra.COMMENT";
  private static final String CURRENT_VOTE = "com.amzgolinski.yara.service.extra.CURRENT_VOTE";
  private static final String NEW_VOTE = "com.amzgolinski.yara.service.extra.NEW_VOTE";
  private static final String POSITION = "com.amzgolinski.yara.service.extra.POSITION";
  private static final String SUBMISSION_ID = "com.amzgolinski.yara.service.extra.SUBMISSION_ID";
  private static final String SUBREDDITS = "com.amzgolinski.yara.service.extra.SUBREDDIT_ID";


  public YaraUtilityService() {
    super("YaraUtilityService");
  }


  public static void fetchMoreComments(Context context, ArrayList<CommentItem> comments,
                                       int position) {

    Log.d(LOG_TAG, "fetchMoreComments");
    Log.d(LOG_TAG, "comments: " + comments.size() + " position " + position);
    Intent intent = new Intent(context, YaraUtilityService.class);
    intent.setAction(ACTION_LOAD_MORE_COMMENTS);
    intent.putExtra(PARAM_COMMENTS, comments);
    intent.putExtra(POSITION, position);
    context.startService(intent);
  }

  public static void deleteAccount(Context context) {

    Log.d(LOG_TAG, "removeUser");
    Intent intent = new Intent(context, YaraUtilityService.class);
    intent.setAction(ACTION_DELETE_ACCOUNT);
    context.startService(intent);
  }


  public static void submitVote(Context context, long submissionId, int currentVote, int newVote) {
    Intent intent = new Intent(context, YaraUtilityService.class);
    intent.setAction(ACTION_SUBMIT_VOTE);
    intent.putExtra(SUBMISSION_ID, submissionId);
    intent.putExtra(CURRENT_VOTE, currentVote);
    intent.putExtra(NEW_VOTE, newVote);
    context.startService(intent);
  }

  public static void submitComment(Context context, long submissionId, String comment) {
    Log.d(LOG_TAG, "submitComment");
    Log.d(LOG_TAG, "submission: " + submissionId + " comment " + comment);
    Intent intent = new Intent(context, YaraUtilityService.class);
    intent.setAction(ACTION_SUBMIT_COMMENT);
    intent.putExtra(SUBMISSION_ID, submissionId);
    intent.putExtra(COMMENT, comment);
    context.startService(intent);
  }

  public static void subredditUnsubscribe(Context context, ArrayList<String> subreddits) {
    Log.d(LOG_TAG, "subredditUnsubscribe");
    Log.d(LOG_TAG, "subreddit: " + subreddits.size());
    Intent intent = new Intent(context, YaraUtilityService.class);
    intent.setAction(ACTION_SUBREDDIT_UNSUBSCRIBE);
    intent.putExtra(SUBREDDITS, subreddits);
    context.startService(intent);
  }


  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(LOG_TAG, "onHandleIntent");
    if (intent != null) {
      final String action = intent.getAction();

      // vote for submission
      if (ACTION_SUBMIT_VOTE.equals(action)) {
        final long submissionId = intent.getLongExtra(SUBMISSION_ID, 1L);
        final int currentVote = intent.getIntExtra(CURRENT_VOTE, 0);
        final int newVote = intent.getIntExtra(NEW_VOTE, 0);
        handleSubmitVote(submissionId, currentVote, newVote);
      } else if (ACTION_SUBMIT_COMMENT.equals(action)) {
        final long submissionId = intent.getLongExtra(SUBMISSION_ID, Long.MIN_VALUE);
        final String comment = intent.getStringExtra(COMMENT);
        handleSubmitComment(submissionId, comment);
      } else if (ACTION_LOAD_MORE_COMMENTS.equals(action)) {
        final ArrayList comments = intent.getParcelableArrayListExtra(PARAM_COMMENTS);
        final int position = intent.getIntExtra(POSITION, Integer.MIN_VALUE);
        handleLoadMoreComments(comments, position);
      } else if (ACTION_SUBREDDIT_UNSUBSCRIBE.equals(action)) {
        final ArrayList subreddits = intent.getParcelableArrayListExtra(SUBREDDITS);
        handleUnsubscribeSubreddit(subreddits);
      } else if (ACTION_DELETE_ACCOUNT.equals(action)) {
        handleDeleteAccount();
      }
    }
  }

  private void broadcastResult(Intent result, boolean status) {
    Log.d(LOG_TAG, "broadcastResult");
    result.putExtra("STATUS", status);
    LocalBroadcastManager.getInstance(this).sendBroadcast(result);
  }

  private void handleDeleteAccount() {

    AuthenticationManager.get()
        .getRedditClient()
        .getOAuthHelper()
        .revokeAccessToken(YaraApplication.CREDENTIALS);

    int numDeleted = this.getContentResolver()
        .delete(RedditContract.SubmissionsEntry.CONTENT_URI, null, null);
    Log.d(LOG_TAG, "Deleted " + numDeleted);

    numDeleted = this.getContentResolver()
        .delete(RedditContract.SubredditsEntry.CONTENT_URI, null, null);
    Log.d(LOG_TAG, "Deleted " + numDeleted);

  }

  private void handleLoadMoreComments(ArrayList<CommentItem> comments, int position) {
    Log.d(LOG_TAG, "handleLoadMoreComments");
    Intent result = new Intent(ACTION_LOAD_MORE_COMMENTS);
    CommentItem item = comments.get(position);

    ArrayList<CommentItem> toReturn;
    RedditClient redditClient = AuthenticationManager.get().getRedditClient();

    //Log.d(LOG_TAG, wrapper.toString());
    Submission fullSubmissionData = redditClient.getSubmission(item.getSubmissionId());
    CommentNode rootNode = fullSubmissionData.getComments();
    toReturn = loadMoreComments(rootNode, item, comments);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(toReturn.size()));
    comments.remove(position);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(comments.size()));
    comments.addAll(position, toReturn);
    Log.d(LOG_TAG, "Size is: " + Integer.toString(comments.size()));

    result.putExtra(PARAM_COMMENTS, comments);
    this.broadcastResult(result, true);
  }

  private void handleSubmitComment(long submissionId, String commentText) {
    Log.d(LOG_TAG, "handleSubmitComment");

    String id = Utils.longToRedditId(submissionId); // converting long to Reddit ID
    RedditClient reddit = AuthenticationManager.get().getRedditClient();
    // submit reply to the server
    // TODO: handle error
    try {
      YaraContribution contrib = new YaraContribution(id);
      new AccountManager(reddit).reply(contrib, commentText);
    } catch (NetworkException | ApiException exception) {
      Log.d(LOG_TAG, exception.toString());
    }

    Intent result = new Intent(ACTION_SUBMIT_COMMENT);
    broadcastResult(result, true);
  }

  private void handleSubmitVote(long submissionId, int currentVote, int newVote) {

    Intent result = new Intent(ACTION_SUBMIT_VOTE);

    VoteDirection direction = Utils.getVote(currentVote, newVote);

    RedditClient reddit = AuthenticationManager.get().getRedditClient();
    String redditId = Utils.longToRedditId(submissionId);
    YaraThing submission = new YaraThing(redditId, YaraThing.Type.SUBMISSION);

    // submit vote to the server
    try {
      new AccountManager(reddit).vote(submission, direction);
      Submission updated = reddit.getSubmission(submission.getId());
      Log.d(LOG_TAG, updated.toString());
      int numupdated = updateSubmission(updated);
      Log.d(LOG_TAG, "NUM UPDATED: " + numupdated);

    } catch (NetworkException | ApiException exception) {
      Log.d(LOG_TAG, exception.getMessage());
    }

    this.broadcastResult(result, true);
  }

  private void handleUnsubscribeSubreddit(ArrayList<String> subreddits) {

    Intent result = new Intent(ACTION_SUBREDDIT_UNSUBSCRIBE);

    RedditClient reddit = AuthenticationManager.get().getRedditClient();

    for (String subredditName : subreddits) {
      try {
        RedditClient client = AuthenticationManager.get().getRedditClient();
        Subreddit subreddit = client.getSubreddit(subredditName);
        new AccountManager(reddit).unsubscribe(subreddit);
        this.deleteSubreddit(subreddit.getId());
      } catch (NetworkException exception) {
        Log.d(LOG_TAG, exception.getMessage());
      }
    }

    this.broadcastResult(result, true);

  }

  private void deleteSubreddit(String subredditId) {

    Log.d(LOG_TAG, "Deleting " + subredditId);
    long id = Utils.redditIdToLong(subredditId);
    Log.d(LOG_TAG, "Deleting " + id);
    String selector = RedditContract.SubmissionsEntry.COLUMN_SUBREDDIT_ID + " = ?";
    String[] args = new String[] {Long.toString(id)};
    int numDeleted = this.getContentResolver()
        .delete(RedditContract.SubmissionsEntry.CONTENT_URI, selector, args);
    Log.d(LOG_TAG, "Deleted " + numDeleted);

    Uri subredditUri = RedditContract.SubredditsEntry.buildSubredditUri(id);
    this.getContentResolver().delete(subredditUri, null, null);
  }

  private ArrayList<CommentItem> loadMoreComments(CommentNode rootNode, CommentItem wrapper,
                                                  ArrayList<CommentItem> current) {
    Log.d(LOG_TAG, "loadMoreComments");

    ArrayList<CommentItem> toReturn;
    Stack<CommentItem> moreComments = new Stack<>();

    RedditClient redditClient = AuthenticationManager.get().getRedditClient();
    CommentNode parent = rootNode.findChild(wrapper.getId()).orNull();
    parent.loadMoreComments(redditClient);
    Iterable<CommentNode> iterable = parent.walkTree();
    List<CommentNode> filteredList = new ArrayList<>();
    boolean found = false;
    for (CommentNode node : iterable) {
      if (found) {
        if (!isCommentLoaded(node.getComment().getId(), current)) {
          filteredList.add(node);
        }
      } else if (node.getComment().getId().equals(wrapper.getId().substring(3))) {
        found = true;
      }
    }
    toReturn = CommentItem.walkTree(filteredList);
    return toReturn;
  }

  private boolean isCommentLoaded(String id, ArrayList<CommentItem> comments) {

    boolean inList = false;
    int index = 0;
    while (!inList && index < comments.size() ) {
      inList = id.equals((comments.get(index).getId()));
      index++;
    }
    return inList;
  }

  private int updateSubmission(Submission submission) {
    ContentValues values = new ContentValues();
    values.put(RedditContract.SubmissionsEntry.COLUMN_VOTE, submission.getVote().getValue());
    values.put(RedditContract.SubmissionsEntry.COLUMN_SCORE, submission.getScore());

    long id = Utils.redditIdToLong(submission.getId());
    Uri submissionUri = RedditContract.SubmissionsEntry.buildSubmissionUri(id);
    int numUpdated = this.getContentResolver().update(
        submissionUri,
        values,
        RedditContract.SubmissionsEntry.COLUMN_SUBMISSION_ID + " =  ? ",
        new String[]{submission.getId()});
    return numUpdated;
  }


}
