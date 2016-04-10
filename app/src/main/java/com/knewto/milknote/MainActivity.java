package com.knewto.milknote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Nuance functionality variables
    // NOTE - Nuance requires targetting 22, 23 won't work!
    private State state = State.IDLE;

    // Layout variables
    private TextView statusText;
    private TextView transcription;
    private Button recordNote;
    private static final String TAG = "MainActivity";

    // Service variables
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String ACTION_UIUPDATE = "com.knewto.milknote.action.UIUPDATE";
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";
    private static final String ACTION_STATUS = "com.knewto.milknote.action.STATUS";


    private IntentFilter uiUpdateFilter;
    private IntentFilter setStateFilter;
    private IntentFilter setStatusFilter;

    private final int RECOGNIZE = 100;
    private final int STOP = 200;
    private final int CANCEL = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView)findViewById(R.id.status);
        transcription = (TextView)findViewById(R.id.transcription1);

        // Code to run when Transcribe button is clicked
        recordNote = (Button) findViewById(R.id.button1);
        recordNote.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start, Stop, or Cancel transcription on click
                callRecognition();
            }
        });

        // Start the Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);

        // Refresh the UI in case transcription occurred from notification/widget
        updateUI();

        // Create Broadcast receiver for UI and State updates from Service
        uiUpdateFilter = new IntentFilter(ACTION_UIUPDATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onUIUpdate, uiUpdateFilter);
        setStateFilter = new IntentFilter(ACTION_SETSTATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStateUpdate, setStateFilter);
        setStatusFilter = new IntentFilter(ACTION_STATUS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStatusUpdate, setStatusFilter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure service is running and recreates notification when app is repopened
        // Start the Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);
    }

    // Storage Methods - Task 1 uses Shared Preference
    private void setSharedPreference(String string){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_main_text), string);
        editor.commit();
    }

    // Broadcast receiver for UI updates
    private BroadcastReceiver onUIUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onUIUpdate");
            updateUI();
        }
    };

    // Broadcast receiver for State updates
    private BroadcastReceiver onStateUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State newState = State.valueOf(intent.getStringExtra("newState"));
            setState(newState);
        }
    };

    // Broadcast receiver for Status updates
    private BroadcastReceiver onStatusUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("Status");
            statusText.setText(status);
        }
    };


    // Updates UI with value from Shared Preference
    private void updateUI(){
        Log.v(TAG, "updateUI");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String textString = sharedPref.getString(getString(R.string.pref_main_text), "No notes");
        transcription.setText(textString);
    }





    // NUANCE SPEECHKIT METHODS
     // State - defines states
     // setstate - sets state to new state (was used to update UI in sample)
    // All other methods moved to TranscribeService.java

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

     /* Reco transactions */

    private void callRecognition() {
        Intent transcribeIntent = new Intent(ACTION_TRANSCRIBE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(transcribeIntent);
    }

    /**
     * Set the state and update the button text <-- removed for now.
     */
    private void setState(State newState) {
        Log.v(TAG,"setState: " + newState);
        state = newState;
    }

}
