package com.gloster.yaplan.reminder

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gloster.yaplan.R
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.ui.alarm.ChainAlarmActivity

/**
 * A habit reminder that behaves like an alarm clock: full screen over the lock screen, ringing
 * until it is answered or [RING_MILLIS] pass. When the phone is in use Android shows it as a
 * heads-up notification instead, with the same buttons.
 */
object ChainAlarm {
    private const val RING_MILLIS = 5 * 60_000L

    fun ring(context: Context, habit: Habit, streak: Int) {
        val screen = PendingIntent.getActivity(
            context,
            habit.id.toInt(),
            ChainAlarmActivity.intent(context, habit.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHAIN_ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Zincir alarmı")
            .setContentText(HabitReminderReceiver.reminderText(habit.name, streak))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(screen, true)
            .setContentIntent(screen)
            .setOngoing(true)
            .setTimeoutAfter(RING_MILLIS)
            .addAction(0, "Yaptım", NotificationActionReceiver.habitDone(context, habit.id))
            .addAction(0, "10 dk ertele", NotificationActionReceiver.habitSnooze(context, habit.id))
            .addAction(0, "Kapat", NotificationActionReceiver.habitDismiss(context, habit.id))
            .build()
        // Insistent: the alarm sound repeats until the notification goes away.
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        NotificationManagerCompat.from(context).notify(NotificationActionReceiver.HABIT_TAG, habit.id.toInt(), notification)
    }

    fun stop(context: Context, habitId: Long) {
        NotificationManagerCompat.from(context).cancel(NotificationActionReceiver.HABIT_TAG, habitId.toInt())
    }
}
