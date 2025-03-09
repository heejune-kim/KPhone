package com.soundmind.kphone.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.soundmind.kphone.MainActivity
import com.soundmind.kphone.R
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoActivity

class KPhoneWidgetProvider : AppWidgetProvider(){
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        //super.onUpdate(context, appWidgetManager, appWidgetIds)
        // widget update logic
        appWidgetIds?.forEach { id ->
            // intent definition to run activity
            val pendingIntent1: PendingIntent = Intent(context, LingGoActivity::class.java)
                .let {
                    intent -> PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }
            val pendingIntent2: PendingIntent = Intent(context, ViewGoActivity::class.java)
                .let {
                        intent -> PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }
            val pendingIntent3: PendingIntent = Intent(context, FxGoActivity::class.java)
                .let {
                        intent -> PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }
            // widget layout definition
            val views: RemoteViews = RemoteViews(context?.packageName, R.layout.widget_layout).apply {
                setOnClickPendingIntent(R.id.buttonLingGo, pendingIntent1)
                setOnClickPendingIntent(R.id.buttonViewGo, pendingIntent2)
                setOnClickPendingIntent(R.id.buttonFxGo, pendingIntent3)
            }
            // Notification to AppWidgetManager to update on App Widget
            appWidgetManager?.updateAppWidget(id, views)
        }
    }
}