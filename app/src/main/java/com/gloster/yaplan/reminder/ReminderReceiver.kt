package com.gloster.yaplan.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.ChecklistItem
import com.gloster.yaplan.data.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Due, weekly, snoozed and follow-up reminders of one task; the intent action says which. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.getTask(taskId) ?: return@launch
                if (intent.action == ACTION_REPEAT) app.scheduler.update(task)
                if (task.isDone || !canNotify(context)) return@launch
                val items = app.repository.getItems(taskId)
                if (intent.action == ACTION_FOLLOW_UP) {
                    // Tapped, ticked, snoozed or swiped away means it was seen; only an ignored one gets a second nudge.
                    if (isNotificationShowing(context, task.id.toInt())) {
                        notify(context, task, items, intent.getStringExtra(EXTRA_CHANNEL) ?: NotificationChannels.TASKS, followUp = true)
                    }
                    return@launch
                }
                val channel = if (intent.action == ACTION_REPEAT) NotificationChannels.REPEAT else NotificationChannels.TASKS
                notify(context, task, items, channel, followUp = false)
                app.scheduler.followUp(task.id, channel)
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, task: Task, items: List<ChecklistItem>, channel: String, followUp: Boolean) {
        val open = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            DeepLinks.intent(context, DeepLinks.task(task.id)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val left = items.filterNot { it.isDone }
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (followUp) "Hâlâ bekliyor" else "YAPARSIN!")
            .setContentText(task.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, "Tamamlandı", NotificationActionReceiver.taskDone(context, task.id))
            .addAction(0, "10 dk ertele", NotificationActionReceiver.taskSnooze(context, task.id))
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
        const val EXTRA_CHANNEL = "channel"
        const val ACTION_REPEAT = "com.gloster.yaplan.REPEAT_REMINDER"
        const val ACTION_SNOOZE = "com.gloster.yaplan.SNOOZED_REMINDER"
        const val ACTION_FOLLOW_UP = "com.gloster.yaplan.FOLLOW_UP_REMINDER"
    }
}
