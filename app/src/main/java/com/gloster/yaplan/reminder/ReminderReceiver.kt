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
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.ChecklistItem
import com.gloster.yaplan.data.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.getTask(taskId) ?: return@launch
                if (intent.action == ACTION_REPEAT) app.scheduler.update(task)
                if (!task.isDone) notify(context, task, app.repository.getItems(taskId))
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, task: Task, items: List<ChecklistItem>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val open = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            DeepLinks.intent(context, DeepLinks.task(task.id)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val left = items.filterNot { it.isDone }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("YAP LAN!")
            .setContentText(task.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
        if (left.isNotEmpty()) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${task.title}\n${left.size} madde kaldı: ${left.joinToString(", ") { it.text }}"),
            )
        }
        NotificationManagerCompat.from(context).notify(task.id.toInt(), builder.build())
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val ACTION_REPEAT = "com.gloster.yaplan.REPEAT_REMINDER"
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
