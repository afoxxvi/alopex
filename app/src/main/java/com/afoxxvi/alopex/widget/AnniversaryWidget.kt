package com.afoxxvi.alopex.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.afoxxvi.alopex.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AnniversaryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.anniversary_widget)
            val days = ChronoUnit.DAYS.between(LocalDate.of(2023, 1, 1), LocalDate.now())
            views.setTextViewText(R.id.textNum, days.toString())
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}