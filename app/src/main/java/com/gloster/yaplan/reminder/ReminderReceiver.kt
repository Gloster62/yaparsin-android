package com.gloster.yaplan.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gloster.yaplan.MainActivity
import com.gloster.yaplan.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("YAP LAN!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val CHANNEL_ID = "reminders"

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hatırlatmalar",
                NotificationManager.IMPORTANCE_HIGH,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
