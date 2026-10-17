package com.gloster.yaplan.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gloster.yaplan.data.Task

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun update(task: Task) {
        val due = task.dueAt
        if (!task.remind || task.isDone || due == null || due <= System.currentTimeMillis()) {
            cancel(task.id)
            return
        }
        val intent = pendingIntent(task.id, task.title)
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, intent)
        }
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(pendingIntent(taskId, null))
    }

    private fun pendingIntent(taskId: Long, title: String?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
            .putExtra(ReminderReceiver.EXTRA_TITLE, title)
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
