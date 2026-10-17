package com.gloster.yaplan

import android.app.Application
import com.gloster.yaplan.data.AppDatabase
import com.gloster.yaplan.data.TaskRepository
import com.gloster.yaplan.reminder.ReminderReceiver
import com.gloster.yaplan.reminder.ReminderScheduler

class YapLanApp : Application() {
    val scheduler by lazy { ReminderScheduler(this) }
    val repository by lazy { TaskRepository(AppDatabase.create(this), scheduler) }

    override fun onCreate() {
        super.onCreate()
        ReminderReceiver.createChannel(this)
    }
}
