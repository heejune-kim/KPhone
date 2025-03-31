package com.soundmind.kphone.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.soundmind.kphone.MainActivity
import com.soundmind.kphone.R
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoActivity
import com.soundmind.kphone.util.ImageUtils

/*
class KPhoneWidgetProvider : AppWidgetProvider(){
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        updateAppWidget(context!!, appWidgetManager!!, R.id.linggo_flag)

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
                //setOnClickPendingIntent(R.id.buttonFxGo, pendingIntent3)
            }
            // Notification to AppWidgetManager to update on App Widget
            appWidgetManager?.updateAppWidget(id, views)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Get the RemoteViews for the layout
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Get the original Bitmap (from resources or another source)
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)

        // Create the rounded bitmap
        val roundedBitmap: Bitmap = ImageUtils.getRoundedCornerBitmap(originalBitmap, 50) // Adjust the corner radius (50) as needed

        // Set the rounded bitmap to the ImageView
        views.setImageViewBitmap(appWidgetId, roundedBitmap)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

 */