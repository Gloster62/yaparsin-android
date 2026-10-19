package com.gloster.yaplan.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskCounts(val open: Int, val done: Int) {
    val total: Int get() = open + done
    val doneFraction: Float get() = if (total == 0) 0f else done.toFloat() / total
}

data class CategoryRow(val category: Category, val counts: TaskCounts)

data class CategoriesUiState(
    val loaded: Boolean = false,
    val rows: List<CategoryRow> = emptyList(),
    val uncategorized: TaskCounts = TaskCounts(0, 0),
)

private fun List<Task>.counts() = TaskCounts(open = count { !it.isDone }, done = count { it.isDone })

class CategoriesViewModel(private val repository: TaskRepository) : ViewModel() {
    val state: StateFlow<CategoriesUiState> =
        combine(repository.allCategories, repository.allTasks) { categories, tasks ->
            val byCategory = tasks.groupBy { it.categoryId }
            CategoriesUiState(
                loaded = true,
                rows = categories.map { CategoryRow(it, byCategory[it.id].orEmpty().counts()) },
                uncategorized = byCategory[null].orEmpty().counts(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    fun save(category: Category) {
        if (category.name.isBlank()) return
        viewModelScope.launch { repository.saveCategory(category.copy(name = category.name.trim())) }
    }

    fun delete(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CategoriesViewModel((this[APPLICATION_KEY] as YapLanApp).repository) }
        }
    }
}
