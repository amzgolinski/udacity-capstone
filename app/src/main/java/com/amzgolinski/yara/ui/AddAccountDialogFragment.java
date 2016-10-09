package com.amzgolinski.yara.ui;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;



import com.amzgolinski.yara.R;


public class AddAccountDialogFragment extends DialogFragment {

  private static final String DIALOG_TITLE_KEY = "dialog_title";
  private static final String LOG_TAG = AddAccountDialogFragment.class.getName();

  public interface AddAccountDialogListener {
    public void onAddAccount(String username);
  }

  private EditText mEditText;
  private Button mCancelButton;
  private Button mAddButton;

  private AddAccountDialogListener mListener;

  public AddAccountDialogFragment() {
    // Empty constructor is required for DialogFragment
    // Make sure not to add arguments to the constructor
    // Use `newInstance` instead as shown below
  }

  public static AddAccountDialogFragment newInstance(String title) {
    AddAccountDialogFragment frag = new AddAccountDialogFragment();
    Bundle args = new Bundle();
    args.putString(DIALOG_TITLE_KEY, title);
    frag.setArguments(args);
    return frag;
  }

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      mListener = (AddAccountDialogListener) activity;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(activity.toString()
          + " must implement NoticeDialogListener");
    }
  }

  /*
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    builder.setView(inflater.inflate(R.layout.add_user_diaog, null))
        // Add action buttons
        .setPositiveButton(R.string.dialog_add_user_button, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            mListener.onAddAccount(user);

          }
        })
        .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            AddAccountDialogFragment.this.getDialog().cancel();
          }
        });

    return builder.create();
  }
  */

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.add_user_diaog, container);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Get field from view
    mEditText = (EditText) view.findViewById(R.id.reddit_account);
    mCancelButton = (Button) view.findViewById(R.id.button_cancel);
    mCancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getDialog().dismiss();
      }
    });

    mAddButton = (Button) view.findViewById(R.id.button_add);
    mAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Return input text back to activity through the implemented listener
        mListener.onAddAccount(mEditText.getText().toString());
        getDialog().dismiss();
      }
    });

    // Show soft keyboard automatically and request focus to field
    mEditText.requestFocus();
    String title =
        getArguments().getString(DIALOG_TITLE_KEY, "Enter Name");
    getDialog().setTitle(title);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

  }

}
