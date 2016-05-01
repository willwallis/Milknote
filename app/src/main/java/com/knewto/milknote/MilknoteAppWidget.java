package com.knewto.milknote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Implementation of App Widget functionality.
 */
public class MilknoteAppWidget extends AppWidgetProvider {
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String TAG = "MilknoteAppWidget";
    private static String status;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.v(TAG, "updateAppWidget");
        if (status == null ){status = context.getString(R.string.message_warming_up);}
        CharSequence widgetText= status;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.milknote_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // set button image
        Bitmap icon;
        if(status.equals(context.getString(R.string.message_recording)) ||
                status.equals(context.getString(R.string.message_processing))){
            icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_stop_white_48dp);
        } else {
            icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_mic_white_48dp);
        }
        views.setImageViewBitmap(R.id.appwidget_button, icon);

        // Add an intent to the button
        Intent transcribeIntent = new Intent(context, TranscribeService.class);
        transcribeIntent.setAction(ACTION_TRANSCRIBE);
        PendingIntent transcribePendingIntent =
                PendingIntent.getService(
                        context,
                        0,
                        transcribeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        views.setOnClickPendingIntent(R.id.appwidget_button, transcribePendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Receive message from intent (working)
        super.onReceive(context, intent);
        status = intent.getStringExtra("Status");
        Log.v(TAG, "onReceive " + status);

        // Update Widget (not working)
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), MilknoteAppWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.v(TAG, "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.v(TAG, "onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }
}

