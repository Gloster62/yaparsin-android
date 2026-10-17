package com.gloster.yaplan.data

import com.gloster.yaplan.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler,
) {
    private val tasks = db.taskDao()
    private val categories = db.categoryDao()

    val allTasks: Flow<List<Task>> = tasks.observeAll()
    val allCategories: Flow<List<Category>> = categories.observeAll()

    suspend fun getTask(id: Long): Task? = tasks.get(id)

    suspend fun saveTask(task: Task): Task {
        val id = tasks.upsert(task).let { if (it == -1L) task.id else it }
        val saved = task.copy(id = id)
        scheduler.update(saved)
        return saved
    }

    suspend fun setDone(task: Task, done: Boolean) = saveTask(task.copy(isDone = done))

    suspend fun deleteTask(task: Task) {
        tasks.delete(task)
        scheduler.cancel(task.id)
    }

    suspend fun saveCategory(category: Category) {
        categories.upsert(category)
    }

    suspend fun deleteCategory(category: Category) = categories.delete(category)

    suspend fun rescheduleAll() {
        tasks.pendingReminders(System.currentTimeMillis()).forEach(scheduler::update)
    }
}
