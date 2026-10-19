package com.gloster.yaplan.ui.edit

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.Priority
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.TaskRepository
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditState(
    val isNew: Boolean = true,
    val loaded: Boolean = false,
    val title: String = "",
    val note: String = "",
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime = DEFAULT_TIME,
    val remind: Boolean = false,
    val createdAt: Long? = null,
) {
    val canSave: Boolean get() = title.isNotBlank()
    val hasDate: Boolean get() = dueDate != null

    fun dueAt(zone: ZoneId): Long? = dueDate?.atTime(dueTime)?.atZone(zone)?.toInstant()?.toEpochMilli()

    companion object {
        val DEFAULT_TIME: LocalTime = LocalTime.of(18, 0)
    }
}

@Stable
interface TaskFormActions {
    fun setTitle(value: String)
    fun setNote(value: String)
    fun setDone(value: Boolean)
    fun setPriority(value: Priority)
    fun setCategory(value: Long?)
    fun setDueDate(value: LocalDate?)
    fun setDueTime(value: LocalTime)
    fun setRemind(value: Boolean)
    fun createCategory(category: Category)
}

class TaskEditViewModel(
    private val repository: TaskRepository,
    savedState: SavedStateHandle,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel(), TaskFormActions {
    private val taskId: Long = savedState.get<Long>(ARG_TASK_ID) ?: NEW_TASK
    private var original: Task? = null

    private val _state = MutableStateFlow(newTaskState())
    val state: StateFlow<TaskEditState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> =
        repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (taskId != NEW_TASK) {
            viewModelScope.launch {
                val task = repository.getTask(taskId) ?: return@launch
                original = task
                val due = task.dueAt?.toLocalDateTime(zone)
                _state.value = TaskEditState(
                    isNew = false,
                    loaded = true,
                    title = task.title,
                    note = task.note,
                    isDone = task.isDone,
                    priority = task.priority,
                    categoryId = task.categoryId,
                    dueDate = due?.toLocalDate(),
                    dueTime = due?.toLocalTime()?.withSecond(0)?.withNano(0) ?: TaskEditState.DEFAULT_TIME,
                    remind = task.remind,
                    createdAt = task.createdAt,
                )
            }
        }
    }

    private fun newTaskState() = TaskEditState(isNew = taskId == NEW_TASK, loaded = taskId == NEW_TASK)

    /** The "Yeni görev" sheet reuses one instance, so it starts clean each time it opens. */
    fun reset(title: String = "") {
        _state.value = newTaskState().copy(title = title)
    }

    override fun setTitle(value: String) = _state.update { it.copy(title = value) }
    override fun setNote(value: String) = _state.update { it.copy(note = value) }
    override fun setDone(value: Boolean) = _state.update { it.copy(isDone = value) }
    override fun setPriority(value: Priority) = _state.update { it.copy(priority = value) }
    override fun setCategory(value: Long?) = _state.update { it.copy(categoryId = value) }
    override fun setDueDate(value: LocalDate?) =
        _state.update { it.copy(dueDate = value, remind = it.remind && value != null) }
    override fun setDueTime(value: LocalTime) = _state.update { it.copy(dueTime = value) }
    override fun setRemind(value: Boolean) = _state.update { it.copy(remind = value && it.hasDate) }

    override fun createCategory(category: Category) {
        if (category.name.isBlank()) return
        viewModelScope.launch {
            val id = repository.saveCategory(category.copy(name = category.name.trim()))
            setCategory(id)
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        val base = original ?: Task(title = "")
        val task = base.copy(
            title = s.title.trim(),
            note = s.note.trim(),
            isDone = s.isDone,
            priority = s.priority,
            categoryId = s.categoryId,
            dueAt = s.dueAt(zone),
            remind = s.remind,
        )
        viewModelScope.launch {
            repository.saveTask(task)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val task = original ?: return onDone()
        viewModelScope.launch {
            repository.deleteTask(task)
            onDone()
        }
    }

    companion object {
        const val ARG_TASK_ID = "taskId"
        const val NEW_TASK = -1L

        val Factory = viewModelFactory {
            initializer {
                TaskEditViewModel(
                    (this[APPLICATION_KEY] as YapLanApp).repository,
                    createSavedStateHandle(),
                )
            }
        }
    }
}
