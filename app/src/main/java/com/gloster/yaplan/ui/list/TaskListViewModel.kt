package com.gloster.yaplan.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskTab(val label: String) {
    ALL("Tümü"),
    OPEN("Yapılacak"),
    DONE("Tamamlanan"),
}

data class TaskFilter(
    val query: String = "",
    val tab: TaskTab = TaskTab.ALL,
    val categoryId: Long? = null,
)

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filter: TaskFilter = TaskFilter(),
)

fun applyFilter(tasks: List<Task>, filter: TaskFilter): List<Task> {
    val query = filter.query.trim()
    return tasks
        .filter {
            when (filter.tab) {
                TaskTab.ALL -> true
                TaskTab.OPEN -> !it.isDone
                TaskTab.DONE -> it.isDone
            }
        }
        .filter { filter.categoryId == null || it.categoryId == filter.categoryId }
        .filter {
            query.isEmpty() ||
                it.title.contains(query, ignoreCase = true) ||
                it.note.contains(query, ignoreCase = true)
        }
        .sortedWith(
            compareBy<Task> { it.isDone }
                .thenBy(nullsLast()) { it.dueAt }
                .thenBy { it.priority.ordinal }
                .thenByDescending { it.createdAt },
        )
}

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {
    private val filter = MutableStateFlow(TaskFilter())

    val state: StateFlow<TaskListState> =
        combine(repository.allTasks, repository.allCategories, filter) { tasks, categories, f ->
            val validFilter =
                if (f.categoryId != null && categories.none { it.id == f.categoryId }) f.copy(categoryId = null) else f
            TaskListState(applyFilter(tasks, validFilter), categories, validFilter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListState())

    fun setQuery(query: String) = filter.value.let { filter.value = it.copy(query = query) }

    fun setTab(tab: TaskTab) = filter.value.let { filter.value = it.copy(tab = tab) }

    fun setCategory(categoryId: Long?) = filter.value.let { filter.value = it.copy(categoryId = categoryId) }

    fun toggleDone(task: Task) {
        viewModelScope.launch { repository.setDone(task, !task.isDone) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun restore(task: Task) {
        viewModelScope.launch { repository.saveTask(task) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TaskListViewModel((this[APPLICATION_KEY] as YapLanApp).repository) }
        }
    }
}
