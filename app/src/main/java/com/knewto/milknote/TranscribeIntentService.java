package com.knewto.milknote;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TranscribeIntentService extends IntentService {
    private static final String TAG = "TranscribeIntentService";
    Handler mHandler;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RECOGNIZE = "com.knewto.milknote.action.RECOGNIZE";
    private static final String ACTION_STOP = "com.knewto.milknote.action.STOP";
    private static final String ACTION_CANCEL = "com.knewto.milknote.action.STOP";
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_UIUPDATE = "com.knewto.milknote.action.UIUPDATE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.knewto.milknote.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.knewto.milknote.extra.PARAM2";

    public TranscribeIntentService() {
        super("TranscribeIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Used by Toast feature
        mHandler = new Handler();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TranscribeIntentService.class);
        intent.setAction(ACTION_STOP);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TranscribeIntentService.class);
        intent.setAction(ACTION_CANCEL);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    // Method to handle incoming requests
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            Log.v(TAG, "onHandleIntent action = " + action);
            if (ACTION_RECOGNIZE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                // Do the Recognize
            } else if (ACTION_STOP.equals(action)) {
                // Do the Stop
            } else if (ACTION_CANCEL.equals(action)) {
                // Do the cancel
            } else if (ACTION_TOAST.equals(action)){
                String ToastText = "Text sent: " + intent.getStringExtra("TEXT_EXTRA");
                makeToast(ToastText);
            }
        }
    }

    private void makeToast(String toastText){
        final String finalToast = toastText;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), finalToast, Toast.LENGTH_SHORT).show();
            }
        });
        Intent UIUpdateIntent = new Intent(ACTION_UIUPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(UIUpdateIntent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
