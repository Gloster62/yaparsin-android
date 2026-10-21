package com.gloster.yaplan.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.nextReminderAt
import com.gloster.yaplan.data.nextRepeatAt
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Every dated task reminds at its due time; chosen weekdays add a repeating one until it is done. */
    fun update(task: Task) {
        val now = System.currentTimeMillis()
        val due = task.dueAt?.takeIf { !task.isDone && it > now }
        if (due == null) alarmManager.cancel(dueIntent(task.id)) else setAlarm(due, dueIntent(task.id))

        val repeat = if (task.isDone) null else nextRepeatAt(now, task.repeatDays, task.repeatMinutes, ZoneId.systemDefault())
        if (repeat == null) alarmManager.cancel(repeatIntent(task.id)) else setAlarm(repeat, repeatIntent(task.id))
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(dueIntent(taskId))
        alarmManager.cancel(repeatIntent(taskId))
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

    private fun dueIntent(taskId: Long): PendingIntent = taskIntent(taskId, action = null)

    /** The action keeps it a separate alarm from [dueIntent] for the same task. */
    private fun repeatIntent(taskId: Long): PendingIntent = taskIntent(taskId, ReminderReceiver.ACTION_REPEAT)

    private fun taskIntent(taskId: Long, action: String?): PendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun habitIntent(habitId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        habitId.toInt(),
        Intent(context, HabitReminderReceiver::class.java).putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
