package com.knewto.milknote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class TranscribeService extends Service {
    private final String transcribeIntentName = "com.knewto.milknote.TRANSCRIBE";

    public TranscribeService() {
    }

    @Override
    public void onCreate(){
        // Do stuff when created
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                transcribeAudio,
                new IntentFilter(transcribeIntentName));
    }

    // Broadcast receiver for transcribe request
    private BroadcastReceiver transcribeAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ToastText = "Transcribe service called " + intent.getStringExtra("TEXT_EXTRA");
            Toast.makeText(getApplicationContext(), ToastText, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
