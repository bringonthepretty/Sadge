package com.wah.sadge.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
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
const val ACTION_COPY_TO_CLIPBOARD = "action_copy_to_clipboard"
const val EXCHANGE_RATE_INTENT_EXTRA_NAME = "exchange_rate"

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

    override fun onReceive(context: Context?, intent: Intent?) {
        if (ACTION_COPY_TO_CLIPBOARD == intent?.action) {
            context?.let {
                val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("exchange rate",
                    intent.getDoubleExtra(EXCHANGE_RATE_INTENT_EXTRA_NAME, .0).toString())
                clipboard.setPrimaryClip(clip)
                Toast
                    .makeText(it, "Exchange rate copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    thread {
        val exchangeRate = getUSDExchangeRateBlocking(context)

        val views = RemoteViews(context.packageName, R.layout.saddest_widget_ever)
        views.setTextViewText(R.id.appwidget_text, exchangeRate.toString())

        val clickIntent = Intent(context, SaddestWidgetEver::class.java).apply {
            action = ACTION_COPY_TO_CLIPBOARD
            putExtra(EXCHANGE_RATE_INTENT_EXTRA_NAME, exchangeRate)
        }

        val clickPendingIntent =
            PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_MUTABLE)

        views.setOnClickPendingIntent(R.id.appwidget_text, clickPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}