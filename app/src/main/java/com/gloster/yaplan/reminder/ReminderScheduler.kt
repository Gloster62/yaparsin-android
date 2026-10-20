package com.gloster.yaplan.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.nextReminderAt
import java.time.ZoneId

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
        setAlarm(due, pendingIntent(task.id, task.title))
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(pendingIntent(taskId, null))
    }

    fun updateHabit(habit: Habit) {
        val minutes = habit.reminderMinutes ?: return cancelHabit(habit.id)
        setAlarm(nextReminderAt(System.currentTimeMillis(), minutes, ZoneId.systemDefault()), habitIntent(habit.id))
    }

    fun cancelHabit(habitId: Long) {
        alarmManager.cancel(habitIntent(habitId))
    }

    private fun setAlarm(at: Long, intent: PendingIntent) {
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
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

    private fun habitIntent(habitId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        Intent(context, HabitReminderReceiver::class.java).putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
