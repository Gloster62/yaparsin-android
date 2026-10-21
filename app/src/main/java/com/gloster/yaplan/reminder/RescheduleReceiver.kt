package com.gloster.yaplan.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gloster.yaplan.YapLanApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Alarms are lost on reboot, weekly and daily ones are set in local time, and widgets show
 * "Bugün": all of them are set again after a reboot, an app update or a clock/time zone change.
 */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in handledActions) return
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.rescheduleAll()
                app.updateWidgets()
            } finally {
                pending.finish()
            }
        }
    }

    private val handledActions = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
    )
}
