package com.wah.sadge.work

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wah.sadge.ui.SaddestWidgetEver
import com.wah.sadge.ui.updateAppWidget


class UpdateWidgetWork(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val name = ComponentName(context, SaddestWidgetEver::class.java)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(name).toList().forEach {
            updateAppWidget(context, appWidgetManager, it)
        }
        Log.i("WIDGET", "update work executed")
        return Result.success()
    }
}