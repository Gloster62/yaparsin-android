package com.gloster.yaplan.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.ChecklistProgress
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.TaskRepository
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val results: List<Task> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val checklist: Map<Long, ChecklistProgress> = emptyMap(),
    val now: Long = System.currentTimeMillis(),
    val today: LocalDate = LocalDate.now(),
) {
    val hasQuery: Boolean get() = query.isNotBlank()
}

/** Title or note contains [query], open tasks first, then by due date. */
fun searchTasks(tasks: List<Task>, query: String): List<Task> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return tasks
        .filter { it.title.contains(q, ignoreCase = true) || it.note.contains(q, ignoreCase = true) }
        .sortedWith(compareBy<Task> { it.isDone }.thenBy(nullsLast()) { it.dueAt }.thenBy { it.priority.ordinal })
}

class SearchViewModel(repository: TaskRepository) : ViewModel() {
    private val query = MutableStateFlow("")

    val state: StateFlow<SearchUiState> =
        combine(repository.allTasks, repository.allCategories, repository.checklistProgress, query) { tasks, categories, checklist, q ->
            val now = System.currentTimeMillis()
            SearchUiState(
                query = q,
                results = searchTasks(tasks, q),
                categoriesById = categories.associateBy { it.id },
                checklist = checklist,
                now = now,
                today = now.toLocalDateTime().toLocalDate(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SearchViewModel((this[APPLICATION_KEY] as YapLanApp).repository) }
        }
    }
}
