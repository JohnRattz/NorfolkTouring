package com.example.john.norfolktouring.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.john.norfolktouring.MainActivity;
import com.example.john.norfolktouring.R;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the View hierarchy and contents for the widget based on its dimensions.
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews views;
        if (width < 200) {
            views = getSmallRemoteView(context);
        } else {
            views = getGridRemoteView(context);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Creates and returns the RemoteViews to be displayed in small widgets.
     */
    private static RemoteViews getSmallRemoteView(Context context) {
        // Construct the RemoteViews object.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        // Clicking on the widget background simply opens MainActivity.
        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.root_view, pendingIntent);

        return views;
    }

    /**
     * Creates and returns the RemoteViews to be displayed in large widget.
     */
    private static RemoteViews getGridRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid);
        // Set the GridWidgetService intent to act as the adapter for the GridView
        Intent gridWidgetServiceIntent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.grid_view, gridWidgetServiceIntent);
        // Clicking on the widget background simply opens MainActivity.
        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPendingIntent =
                PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.grid_view, appPendingIntent);
        return views;
    }

    /**
     * Updates all widget instances given the widget IDs.
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param appWidgetIds     Array of widget IDs to be updated
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Called when a widget's options change.
     * In this case, dimensional changes are relevant.
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param appWidgetId     Widget ID to be updated
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget_small is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget_small is disabled
    }
}

