package com.gloster.yaplan.reminder

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.AtRiskChain
import com.gloster.yaplan.data.nextMidnightAt
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * At [MINUTES] every evening, one notification for the chains that break at midnight unless
 * done today, counting down to midnight and gone by then.
 */
object ChainSaver {
    const val MINUTES = 22 * 60
    private const val TAG = "chain_saver"
    private const val ID = 0

    suspend fun show(context: Context) {
        val chains = (context.applicationContext as YapLanApp).habits.atRiskToday()
        if (chains.isEmpty() || !canNotify(context)) {
            NotificationManagerCompat.from(context).cancel(TAG, ID)
            return
        }
        NotificationManagerCompat.from(context).notify(TAG, ID, build(context, chains))
    }

    /** Keeps a showing notification in step with habits marked done since it went up. */
    suspend fun refreshIfShowing(context: Context) {
        if (isNotificationShowing(context, ID, TAG)) show(context)
    }

    private fun build(context: Context, chains: List<AtRiskChain>): Notification {
        val now = System.currentTimeMillis()
        val midnight = nextMidnightAt(now, ZoneId.systemDefault())
        val single = chains.singleOrNull()
        val link = if (single != null) DeepLinks.habit(single.habit.id) else DeepLinks.HABITS.toUri()
        val open = PendingIntent.getActivity(
            context,
            ID,
            DeepLinks.intent(context, link),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, NotificationChannels.CHAIN_SAVER)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setWhen(midnight)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setTimeoutAfter(midnight - now)
        if (single != null) {
            builder
                .setContentTitle("Zincir kırılmak üzere!")
                .setContentText("${single.habit.name} · ${single.streak} günlük zincir gece yarısı kırılır")
                .addAction(0, "Bugün yaptım", NotificationActionReceiver.habitDone(context, single.habit.id))
        } else {
            builder
                .setContentTitle("${chains.size} zincir kırılmak üzere!")
                .setContentText(chains.joinToString(", ") { it.habit.name })
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(chains.joinToString("\n") { "${it.habit.name} · ${it.streak} gün" }),
                )
        }
        return builder.build()
    }
}

class ChainSaverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.scheduler.scheduleChainSaver()
                ChainSaver.show(context)
            } finally {
                pending.finish()
            }
        }
    }
}
