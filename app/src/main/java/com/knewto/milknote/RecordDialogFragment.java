package com.knewto.milknote;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

public class RecordDialogFragment extends DialogFragment {
    private IntentFilter setStateFilter;
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */
    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        setRetainInstance(true);

        // Broadcast listener for state changes
        setStateFilter = new IntentFilter(ACTION_SETSTATE);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(onStateUpdate, setStateFilter);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.fragment_record_dialog, null));

        builder.setMessage(R.string.recorder_dialog_title)
                .setPositiveButton(R.string.recorder_dialog_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(RecordDialogFragment.this);
                    }
                });
        return builder.create();

    }

    // Broadcast receiver for State updates
    private BroadcastReceiver onStateUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State newState = State.valueOf(intent.getStringExtra("newState"));
            if(newState.equals(State.IDLE)){
            RecordDialogFragment.this.dismiss();}
           }
    };

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface RecordDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        // Not used but I kept it in case of future use
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    RecordDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (RecordDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement RecordDialogListener");
        }
    }
}