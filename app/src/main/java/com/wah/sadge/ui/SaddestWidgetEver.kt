package com.wah.sadge.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wah.sadge.R
import com.wah.sadge.work.UpdateWidgetWork
import com.wah.sadge.web.getUSDExchangeRateBlocking
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val WORK_INTERVAL = 3L
const val WORK_NAME = "Update widget work"

class SaddestWidgetEver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.i("WIDGET", "update work added")
        val workManager = WorkManager.getInstance(context)
        val request = PeriodicWorkRequestBuilder<UpdateWidgetWork>(WORK_INTERVAL, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request)
    }

    override fun onDisabled(context: Context) { }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    thread {
        val widgetText = getUSDExchangeRateBlocking(context)

        val views = RemoteViews(context.packageName, R.layout.saddest_widget_ever)
        views.setTextViewText(R.id.appwidget_text, widgetText.toString())

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}