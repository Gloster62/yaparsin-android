package com.gloster.yaplan

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.gloster.yaplan.data.AppDatabase
import com.gloster.yaplan.data.HabitRepository
import com.gloster.yaplan.data.TaskRepository
import com.gloster.yaplan.reminder.ReminderReceiver
import com.gloster.yaplan.reminder.ReminderScheduler
import com.gloster.yaplan.widget.HabitWidget
import com.gloster.yaplan.widget.TaskListWidget
import com.gloster.yaplan.widget.TaskWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YapLanApp : Application() {
    val scheduler by lazy { ReminderScheduler(this) }
    private val database by lazy { AppDatabase.create(this) }
    val repository by lazy {
        TaskRepository(database, scheduler) {
            TaskWidget().updateAll(this)
            TaskListWidget().updateAll(this)
        }
    }
    val habits by lazy { HabitRepository(database, scheduler) { HabitWidget().updateAll(this) } }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ReminderReceiver.createChannel(this)
        // Alarms are lost on force-stop, and after an update tasks that had "Hatırlat" off still
        // need one; setting them again is cheap and idempotent.
        scope.launch {
            repository.rescheduleAll()
            habits.rescheduleReminders()
        }
    }
}
