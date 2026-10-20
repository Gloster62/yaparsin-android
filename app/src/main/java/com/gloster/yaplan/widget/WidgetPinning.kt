package com.gloster.yaplan.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Asks the launcher to place a calendar widget for [habitId]. False when the launcher can't pin
 * widgets; then the user has to add it from the home screen's widget list.
 */
fun requestHabitWidget(context: Context, habitId: Long): Boolean {
    val manager = AppWidgetManager.getInstance(context)
    if (!manager.isRequestPinAppWidgetSupported) return false
    // Mutable: the launcher fills in EXTRA_APPWIDGET_ID of the widget it placed.
    val onPinned = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        Intent(context, HabitWidgetPinnedReceiver::class.java).putExtra(EXTRA_HABIT_ID, habitId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )
    return manager.requestPinAppWidget(ComponentName(context, HabitWidgetReceiver::class.java), null, onPinned)
}

private const val EXTRA_HABIT_ID = "habit_id"

class HabitWidgetPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || habitId == -1L) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                HabitWidget.assign(context, appWidgetId, habitId)
            } finally {
                pending.finish()
            }
        }
    }
}
