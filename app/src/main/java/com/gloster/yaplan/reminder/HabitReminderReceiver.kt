package com.gloster.yaplan.reminder

import android.Manifest
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
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.currentStreak
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
        val app = context.applicationContext as YapLanApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = app.habits.getHabit(habitId) ?: return@launch
                app.scheduler.updateHabit(habit)
                val done = app.habits.doneDays(habitId)
                val today = LocalDate.now()
                if (today !in done) notify(context, habit, currentStreak(done, today))
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, habit: Habit, streak: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val open = PendingIntent.getActivity(
            context,
            habit.id.toInt(),
            DeepLinks.intent(context, DeepLinks.habit(habit.id)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = if (streak > 0) {
            "${habit.name} · $streak günlük zincirin seni bekliyor"
        } else {
            "${habit.name} · bugün henüz yapılmadı"
        }
        val notification = NotificationCompat.Builder(context, ReminderReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Zinciri kırma!")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        // The tag keeps habit ids from replacing task notifications with the same number.
        NotificationManagerCompat.from(context).notify(NOTIFICATION_TAG, habit.id.toInt(), notification)
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        private const val NOTIFICATION_TAG = "habit"
    }
}
