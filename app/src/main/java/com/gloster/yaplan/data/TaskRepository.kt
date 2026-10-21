package com.gloster.yaplan.data

import androidx.room.withTransaction
import com.gloster.yaplan.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

/** A deleted task with its list, so "Geri al" can bring both back. */
data class DeletedTask(val task: Task, val items: List<ChecklistItem>)

class TaskRepository(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler,
    /** Refreshes the home-screen widgets; their sessions don't outlive a few seconds. */
    private val onChanged: suspend () -> Unit,
) {
    private val tasks = db.taskDao()
    private val categories = db.categoryDao()
    private val checklist = db.checklistDao()

    val allTasks: Flow<List<Task>> = tasks.observeAll()
    val allCategories: Flow<List<Category>> = categories.observeAll()
    val checklistProgress: Flow<Map<Long, ChecklistProgress>> =
        checklist.observeProgress().map { list -> list.associateBy { it.taskId } }

    private val _deletedTasks = MutableSharedFlow<DeletedTask>(extraBufferCapacity = 1)

    /** Every deleted task, so the app can offer "Geri al" wherever the deletion happened. */
    val deletedTasks: SharedFlow<DeletedTask> = _deletedTasks.asSharedFlow()

    fun observeTask(id: Long): Flow<Task?> = tasks.observe(id)

    fun observeItems(taskId: Long): Flow<List<ChecklistItem>> = checklist.observe(taskId)

    suspend fun getTask(id: Long): Task? = tasks.get(id)

    suspend fun getItems(taskId: Long): List<ChecklistItem> = checklist.forTask(taskId)

    /** [items] null leaves the list as it is; otherwise it becomes exactly [items], in that order. */
    suspend fun saveTask(task: Task, items: List<ChecklistItem>? = null): Task {
        val saved = db.withTransaction {
            val id = tasks.upsert(task).let { if (it == -1L) task.id else it }
            if (items != null) {
                checklist.deleteAllExcept(id, items.map { it.id }.filter { it > 0 })
                checklist.upsert(items.mapIndexed { i, item -> item.copy(taskId = id, position = i) })
            }
            task.copy(id = id)
        }
        scheduler.update(saved)
        onChanged()
        return saved
    }

    suspend fun setDone(task: Task, done: Boolean) = saveTask(task.copy(isDone = done))

    suspend fun setDone(taskId: Long, done: Boolean) {
        getTask(taskId)?.let { setDone(it, done) }
    }

    suspend fun setItemDone(itemId: Long, done: Boolean) {
        checklist.setDone(itemId, done)
        onChanged()
    }

    suspend fun deleteTask(task: Task) {
        val items = checklist.forTask(task.id)
        tasks.delete(task)
        scheduler.cancel(task.id)
        onChanged()
        _deletedTasks.emit(DeletedTask(task, items))
    }

    suspend fun restore(deleted: DeletedTask) {
        saveTask(deleted.task, deleted.items)
    }

    suspend fun saveCategory(category: Category): Long =
        categories.upsert(category).let { if (it == -1L) category.id else it }

    suspend fun deleteCategory(category: Category) = categories.delete(category)

    suspend fun rescheduleAll() {
        tasks.withReminders(System.currentTimeMillis()).forEach(scheduler::update)
    }
}
