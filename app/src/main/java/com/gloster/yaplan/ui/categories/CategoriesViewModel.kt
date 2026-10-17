package com.gloster.yaplan.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: TaskRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> =
        repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
