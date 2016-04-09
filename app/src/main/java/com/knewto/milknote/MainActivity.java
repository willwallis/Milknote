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
import android.support.v4.media.TransportStateListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

public class MainActivity extends AppCompatActivity {

    // Nuance functionality variables
    // NOTE - Nuance requires targetting 22, 23 won't work!
    private Audio startEarcon;
    private Audio stopEarcon;
    private Audio errorEarcon;

    private Session speechSession;
    private Transaction recoTransaction;
    private State state = State.IDLE;

    private RecognitionType recognitionType = RecognitionType.DICTATION;
    private DetectionType detectionType = DetectionType.Long;
    private String language = "eng-USA";

    // Layout variables
    private TextView transcription;
    private Button recordNote;
    private static final String TAG = "MainActivity";

    // Service variables
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String ACTION_UIUPDATE = "com.knewto.milknote.action.UIUPDATE";
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";

    private IntentFilter uiUpdateFilter;
    private IntentFilter setStateFilter;

    private final int RECOGNIZE = 100;
    private final int STOP = 200;
    private final int CANCEL = 300;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transcription = (TextView)findViewById(R.id.transcription1);

        // Code to run when Transcribe button is clicked
        recordNote = (Button) findViewById(R.id.button1);
        recordNote.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                toggleReco();
            }
        });

        // Start Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);

        updateUI();

        //Create a session
//        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);
//
//        loadEarcons();
//        setState(State.IDLE);
        Log.v(TAG,"onCreate");

        // Create Notification
        createNotification();

        // Create Broadcast receiver for UI updates from Service
        uiUpdateFilter = new IntentFilter(ACTION_UIUPDATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onUIUpdate, uiUpdateFilter);
        setStateFilter = new IntentFilter(ACTION_SETSTATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStateUpdate, setStateFilter);


    }

    @Override
    public void onPause() {
        // Don't receive UI updates when Activity not active
//        LocalBroadcastManager.getInstance(this)
//                .unregisterReceiver(onUIUpdate);

        super.onPause();
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

    // Broadcast receiver for state updates
    private BroadcastReceiver onStateUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State newState = State.valueOf(intent.getStringExtra("newState"));
            setState(newState);
        }
    };


    private void callTranscribeService (){
        Log.v(TAG, "callTranscribeService");
        Intent transcribeIntent = new Intent(ACTION_TOAST);
//        transcribeIntent.setAction(ACTION_TOAST);
        transcribeIntent.putExtra("TEXT_EXTRA", "calling from the Main Activity");
        LocalBroadcastManager.getInstance(this).sendBroadcast(transcribeIntent);
    }


    private void updateUI(){
        Log.v(TAG, "updateUI");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String textString = sharedPref.getString(getString(R.string.pref_main_text), "No notes");
        //Toast.makeText(getApplicationContext(), textString, Toast.LENGTH_SHORT).show();
        transcription.setText(textString);
    }

    private void createNotification(){

        // Missing the notification activity
        // RESEARCH - creating artificial back stack

        Intent transcribeIntent = new Intent(this, TranscribeIntentService.class);
        transcribeIntent.setAction(ACTION_TOAST);
        transcribeIntent.putExtra("TEXT_EXTRA", "calling from the Notification");
        PendingIntent transcribePendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        transcribeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Create the notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        // Show controls on lock screen even when user hides sensitive content.
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_stat_milk)
                        .setContentTitle("My notification")
                        .setContentText("Ding ol Dang!")
                        // Add media control buttons that invoke intents in your media service
                        .addAction(android.R.drawable.ic_media_play, "Transcribe", transcribePendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }



    // NUANCE SPEECHKIT METHODS
     // State - defines states
     // loadEarcons - load beep sounds for start, stop, and error
     // toggleReco - can press button multiple times to start and stop
     // recognize - sets options and starts recognition session
     // recoListener - listens to messages from the transcription service and acts
     // stopRecording - stops the recording and transcribes
     // cancel - stops and no transcription
     // setstate - sets state to new state (was used to update UI in sample)
     // audioPoller (REMOVED) - allows volume display in progress bar

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    /* Earcons */

    private void loadEarcons() {
        //Load all the earcons from disk
        Log.v(TAG,"loadEarcons");
        startEarcon = new Audio(this, R.raw.sk_start, Configuration.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, Configuration.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, Configuration.PCM_FORMAT);
    }

     /* Reco transactions */

    private void toggleReco() {
        Log.v(TAG,"toggleReco");
        int tranCommand = 0;
        switch (state) {
            case IDLE:
                //recognize();
                tranCommand = RECOGNIZE;
                break;
            case LISTENING:
                //stopRecording();
                tranCommand = STOP;
                break;
            case PROCESSING:
                //cancel();
                tranCommand = CANCEL;
                break;
        }

        Intent transcribeIntent = new Intent(ACTION_TRANSCRIBE);
        transcribeIntent.putExtra("ACTION_COMMAND", tranCommand);
        LocalBroadcastManager.getInstance(this).sendBroadcast(transcribeIntent);
    }

    /**
     * Start listening to the user and streaming their voice to the server.
     */
    private void recognize() {
        Log.v(TAG,"recognize");
        //Setup our Reco transaction options.
        Transaction.Options options = new Transaction.Options();
        options.setRecognitionType(recognitionType);
        options.setDetection(detectionType);
        options.setLanguage(new Language(language));
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        //Start listening
        recoTransaction = speechSession.recognize(options, recoListener);
    }

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onStartedRecording(Transaction transaction) {
            Log.v(TAG,"onStartedRecording");
            transcription.append("\nonStartedRecording");
            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
        }
        @Override
        public void onFinishedRecording(Transaction transaction) {
            Log.v(TAG,"onFinishedRecording");
            transcription.append("\nonFinishedRecording");
            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
        }
        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            Log.v(TAG,"onRecognition");
            transcription.append("\nonRecognition: " + recognition.getText());
            setSharedPreference(recognition.getText());
            updateUI();
            //We have received a transcription of the users voice from the server.
            setState(State.IDLE);
        }
        @Override
        public void onSuccess(Transaction transaction, String s) {
            Log.v(TAG,"onSuccess");
            transcription.append("\nonSuccess");
            //Notification of a successful transaction. Nothing to do here.
        }
        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            Log.v(TAG,"onError" + e);
            transcription.append("\nonError: " + e.getMessage() + ". " + s);
            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };

    /**
     * Stop recording the user
     */
    private void stopRecording() {
        Log.v(TAG,"stopRecording");
        recoTransaction.stopRecording();
    }

    /**
     * Cancel the Reco transaction.
     * This will only cancel if we have not received a response from the server yet.
     */
    private void cancel() {
        Log.v(TAG,"cancel");
        recoTransaction.cancel();
        setState(State.IDLE);
    }

    /**
     * Set the state and update the button text <-- removed for now.
     */
    private void setState(State newState) {
        Log.v(TAG,"setState: " + newState);
        state = newState;
    }

}
