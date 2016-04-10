package com.knewto.milknote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Implementation of App Widget functionality.
 */
public class MilknoteAppWidget extends AppWidgetProvider {
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String TAG = "MilknoteAppWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.v(TAG, "updateAppWidget");
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.milknote_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

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
        views.setOnClickPendingIntent(R.id.button, transcribePendingIntent);

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

