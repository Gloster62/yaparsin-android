package com.gloster.yaplan.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gloster.yaplan.YapLanApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Redraws the widgets when the chain mood or the day changes by the clock alone. */
class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                app.scheduler.scheduleWidgetRefresh()
                app.updateWidgets()
            } finally {
                pending.finish()
            }
        }
    }
}
