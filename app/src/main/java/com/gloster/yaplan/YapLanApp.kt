package com.gloster.yaplan

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.gloster.yaplan.data.AppDatabase
import com.gloster.yaplan.data.HabitRepository
import com.gloster.yaplan.data.TaskRepository
import com.gloster.yaplan.reminder.ReminderReceiver
import com.gloster.yaplan.reminder.ReminderScheduler
import com.gloster.yaplan.widget.HabitWidget

class YapLanApp : Application() {
    val scheduler by lazy { ReminderScheduler(this) }
    private val database by lazy { AppDatabase.create(this) }
    val repository by lazy { TaskRepository(database, scheduler) }
    val habits by lazy { HabitRepository(database, scheduler) { HabitWidget().updateAll(this) } }

    override fun onCreate() {
        super.onCreate()
        ReminderReceiver.createChannel(this)
    }
}
