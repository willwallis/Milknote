package com.knewto.milknote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class TranscribeService extends Service {
    private static final String TAG = "TranscribeService";
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";

    private final int RECOGNIZE = 100;
    private final int STOP = 200;
    private final int CANCEL = 300;


    public TranscribeService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.v(TAG, "onCreate");
        // Create intent filers
        IntentFilter toastFilter = new IntentFilter(ACTION_TOAST);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(makeToast, toastFilter);
        IntentFilter transcribeFilter = new IntentFilter(ACTION_TRANSCRIBE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(transcriber, transcribeFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    // Broadcast receiver for toast request
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
            int actionCommand = intent.getIntExtra("ACTION_COMMAND", 0);
            Log.v(TAG, "Transcribe command: " + actionCommand);
            String ToastText = "Action sent: " + actionCommand ;
            Toast.makeText(getApplicationContext(), ToastText, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
