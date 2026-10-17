package com.gloster.yaplan.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gloster.yaplan.YapLanApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val repository = (context.applicationContext as YapLanApp).repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
