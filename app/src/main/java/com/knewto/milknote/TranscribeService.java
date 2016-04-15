package com.knewto.milknote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import com.knewto.milknote.LocationService.LocalBinder;

public class TranscribeService extends Service {
    private static final String TAG = "TranscribeService";
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String ACTION_UIUPDATE = "com.knewto.milknote.action.UIUPDATE";
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";
    private static final String ACTION_STATUS = "com.knewto.milknote.action.STATUS";

    private final int RECOGNIZE = 100;
    private final int STOP = 200;
    private final int CANCEL = 300;

    private final int START_LOCATION = 0;
    private final int GET_LOCATION = 1;
    private final int STOP_LOCATION = 2;


    // Location Binding
    private LocationService mBoundService;
    boolean mIsBound = false; // Has binding been initiated
    boolean mIsConnected = false; // Are we connected

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

    public TranscribeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");

        //Create a Nuance recognition session
        speechSession = Session.Factory.session(this, ConfigurationNuance.SERVER_URI, ConfigurationNuance.APP_KEY);
        loadEarcons();
        setState(State.IDLE);

        // Create intent filers
        IntentFilter toastFilter = new IntentFilter(ACTION_TOAST);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(makeToast, toastFilter);
        IntentFilter transcribeFilter = new IntentFilter(ACTION_TRANSCRIBE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(transcriber, transcribeFilter);

        // Bind to Location Service
        doBindService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");

        makeNotification("Ready to record");
        //broadcastStatus(state);

        // Process requests from Notifications
        if (intent != null && intent.getAction() != null) {
            String intentAction = intent.getAction();
            if (intentAction.equals(ACTION_TRANSCRIBE)) {
                toggleReco();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    // Broadcast receiver for toast request - keep for testing
    private BroadcastReceiver makeToast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive toast");
            String ToastText = "Text sent: " + intent.getStringExtra("TEXT_EXTRA");
            Toast.makeText(getApplicationContext(), ToastText, Toast.LENGTH_SHORT).show();
        }
    };

    // Broadcast receiver for transcribe request
    private BroadcastReceiver transcriber = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive transcriber");
            toggleReco();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Storage Methods - Task 1 uses Shared Preference
    private void setSharedPreference(String string) {
        Log.v(TAG, "setSharedPreference:" + string);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_result_key), string);
        editor.commit();
        recordTranscription(string);
        updateUI();
    }
    private void recordTranscription(String string){
        Location currentLocation = null;
        if(mIsConnected){currentLocation = mBoundService.getCurrentLocation();}
        Uri mNewUri = DataUtility.insertRecord(getApplicationContext(), string, currentLocation);
    }

    // Update user interface if open
    private void updateUI() {
        Log.v(TAG, "updateUI");
        Intent UIUpdateIntent = new Intent(ACTION_UIUPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(UIUpdateIntent);
    }


    // BINDING SERVICE METHODS
    // Refer to http://developer.android.com/reference/android/app/Service.html#LocalServiceSample
    void doBindService() {
        Log.v(TAG, "doBindService");
        // Establish a connection with the service.
        Intent bindIntent = new Intent(this, LocationService.class);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.v(TAG, "onBindServiceConnected");
            // Called when connection with service is established
            LocalBinder mLocalBinder = (LocalBinder)service;
            mBoundService = mLocalBinder.getService();
            mIsConnected = true;
        }
        public void onServiceDisconnected(ComponentName className) {
            Log.v(TAG, "onBindServiceDisconnected");
            // Called when the service is unexpectedly disconnected -- that is, its process crashed.
            mBoundService = null;
            mIsBound = false;
            mIsConnected = false;
        }
    };

    void doUnbindService() {
        Log.v(TAG, "doUnbindService");
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            mIsConnected = false;
        }
    }

    private void locationHelper(int command){
        Log.v(TAG, "LocHelp - called with: " + command);
        if(mIsConnected){
            Log.v(TAG, "LocHelp - connected");
            switch (command){
                case START_LOCATION:
                    mBoundService.startLocationService();
                    break;
                case GET_LOCATION:
                    mBoundService.getCurrentLocation();
                    break;
                case STOP_LOCATION:
                    mBoundService.stopLocationService();
                    break;
            }
        } else {
            Log.v(TAG, "LocHelp - not connected");
        }
    }


    // Make sure we unbind when destroying transcribe service
    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    // NUANCE SPEECHKIT METHODS
    // State - defines states
    // loadEarcons - load beep sounds for start, stop, and error
    // toggleReco - can press button multiple times to start and stop <-- replaced by transcriber
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
        Log.v(TAG, "loadEarcons");
        startEarcon = new Audio(this, R.raw.sk_start, ConfigurationNuance.PCM_FORMAT);
        stopEarcon = new Audio(this, R.raw.sk_stop, ConfigurationNuance.PCM_FORMAT);
        errorEarcon = new Audio(this, R.raw.sk_error, ConfigurationNuance.PCM_FORMAT);
    }

     /* Reco transactions */

    private void toggleReco() {
        Log.v(TAG, "toggleReco");
        switch (state) {
            case IDLE:
                recognize();
                locationHelper(START_LOCATION);
                break;
            case LISTENING:
                stopRecording();
                break;
            case PROCESSING:
                cancel();
                break;
        }
    }

    /**
     * Start listening to the user and streaming their voice to the server.
     */
    private void recognize() {
        Log.v(TAG, "recognize");
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
            Log.v(TAG, "onStartedRecording");
            broadcastStatus("Recording...");
            //We have started recording the users voice.
            //We should update our state and start polling their volume.
            setState(State.LISTENING);
        }

        @Override
        public void onFinishedRecording(Transaction transaction) {
            Log.v(TAG, "onFinishedRecording");
            broadcastStatus("Processing...");
            //We have finished recording the users voice.
            //We should update our state and stop polling their volume.
            setState(State.PROCESSING);
        }

        @Override
        public void onRecognition(Transaction transaction, Recognition recognition) {
            Log.v(TAG, "onRecognition: " + recognition.getText());
            setSharedPreference(recognition.getText());
            //We have received a transcription of the users voice from the server.
            setState(State.IDLE);
        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            Log.v(TAG, "onSuccess");
            broadcastStatus("Success");
            locationHelper(STOP_LOCATION);
            //Notification of a successful transaction. Nothing to do here.
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            Log.v(TAG, "onError: " + e.getMessage() + ". " + s);
            broadcastStatus("Failed");
            locationHelper(STOP_LOCATION);
            //Something went wrong. Check ConfigurationNuance.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            //We will simply reset to the idle state.
            setState(State.IDLE);
        }
    };

    /**
     * Stop recording the user
     */
    private void stopRecording() {
        Log.v(TAG, "stopRecording");
        recoTransaction.stopRecording();
    }

    /**
     * Cancel the Reco transaction.
     * This will only cancel if we have not received a response from the server yet.
     */
    private void cancel() {
        Log.v(TAG, "cancel");
        recoTransaction.cancel();
        setState(State.IDLE);
    }

    /**
     * Set the state and update the button text <-- removed for now.
     */
    private void setState(State newState) {
        Log.v(TAG, "setState: " + newState);
        state = newState;
        Intent setStateIntent = new Intent(ACTION_SETSTATE);
        setStateIntent.putExtra("newState", newState.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(setStateIntent);
    }

    // Broadcast status
    private void broadcastStatus(String status) {
        Log.v(TAG, "New status: " + status);
        Intent sendStatusIntent = new Intent(ACTION_STATUS);
        sendStatusIntent.putExtra("Status", status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(sendStatusIntent);
        makeNotification(status);
        //updateWidget(status);
    }

    // Update App Widget
    private void updateWidget(String status) {
        Log.v(TAG, "updateWidget");
        Intent intent = new Intent(this, MilknoteAppWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MilknoteAppWidget.class));
        intent.putExtra("Status", status);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    // Create or Update notification
    private void makeNotification(String status) {
        Log.v(TAG, "makeNotification");
        // Check preferences before creating notification
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationPref = sharedPref.getBoolean(getString(R.string.pref_notification_key), true);
        if (notificationPref) {

            // Missing the notification activity
            // RESEARCH - creating artificial back stack

            Intent transcribeIntent = new Intent(this, TranscribeService.class);
            transcribeIntent.setAction(ACTION_TRANSCRIBE);
            PendingIntent transcribePendingIntent =
                    PendingIntent.getService(
                            this,
                            0,
                            transcribeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // Create action for notification button
            NotificationCompat.Action recAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_media_play,
                    "Transcribe",
                    transcribePendingIntent)
                    .build();

            // Create the notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this);

            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
//                .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_stat_milk)
                    .setTicker("ticker text")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("My BIG text"))
                    .setContentTitle("Milknote")
                    .setContentText(status)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                    .addAction(recAction);

            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }

}
