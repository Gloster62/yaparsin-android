package com.gloster.yaplan.ui.edit

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
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null,
    val dueAt: Long? = null,
    val remind: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank()
}

class TaskEditViewModel(
    private val repository: TaskRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val taskId: Long = savedState.get<Long>(ARG_TASK_ID) ?: NEW_TASK
    private var original: Task? = null

    private val _state = MutableStateFlow(TaskEditState(isNew = taskId == NEW_TASK, loaded = taskId == NEW_TASK))
    val state: StateFlow<TaskEditState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> =
        repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (taskId != NEW_TASK) {
            viewModelScope.launch {
                val task = repository.getTask(taskId) ?: return@launch
                original = task
                _state.value = TaskEditState(
                    isNew = false,
                    loaded = true,
                    title = task.title,
                    note = task.note,
                    priority = task.priority,
                    categoryId = task.categoryId,
                    dueAt = task.dueAt,
                    remind = task.remind,
                )
            }
        }
    }

    fun setTitle(value: String) = _state.update { it.copy(title = value) }
    fun setNote(value: String) = _state.update { it.copy(note = value) }
    fun setPriority(value: Priority) = _state.update { it.copy(priority = value) }
    fun setCategory(value: Long?) = _state.update { it.copy(categoryId = value) }
    fun setDueAt(value: Long?) = _state.update { it.copy(dueAt = value, remind = it.remind && value != null) }
    fun setRemind(value: Boolean) = _state.update { it.copy(remind = value && it.dueAt != null) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        val base = original ?: Task(title = "")
        val task = base.copy(
            title = s.title.trim(),
            note = s.note.trim(),
            priority = s.priority,
            categoryId = s.categoryId,
            dueAt = s.dueAt,
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
