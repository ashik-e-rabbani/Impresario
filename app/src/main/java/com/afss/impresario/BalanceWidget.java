package com.afss.impresario;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class BalanceWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences myPrefs;
        myPrefs = context.getSharedPreferences("SING_IN_CREDS", Context.MODE_PRIVATE);

        SimpleDateFormat sdf = new SimpleDateFormat("hh.mm aa");
        String currentTime = sdf.format(new Date());


        String balance = myPrefs.getString("BALANCE", null);
//        Log.d("NOW","Got balance"+ balance);


        CharSequence widgetTextBalance = "$" + balance;
        Log.d("NOW", "Got balance" + widgetTextBalance);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
        views.setTextViewText(R.id.currentTime, "Last Update: " + currentTime);
        if (balance.contains("-")) {
            views.setTextColor(R.id.appwidget_text, Color.parseColor("#B71C1C"));

        } else {
            views.setTextColor(R.id.appwidget_text, Color.parseColor("#2196F3"));
        }
        views.setTextViewText(R.id.appwidget_text, widgetTextBalance);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}