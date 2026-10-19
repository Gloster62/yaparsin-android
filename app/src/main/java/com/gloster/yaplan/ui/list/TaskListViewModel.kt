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
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryChip(val category: Category?, val openCount: Int)

data class Celebration(val taskTitle: String, val isToday: Boolean, val progress: DailyProgress) {
    val allDoneToday: Boolean get() = isToday && progress.left == 0
}

data class TaskListUiState(
    val loaded: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val now: Long = 0,
    val hasAnyTask: Boolean = false,
    val progress: DailyProgress = DailyProgress(0, 0),
    val filter: TaskFilter = TaskFilter(),
    val tabCounts: Map<TaskTab, Int> = emptyMap(),
    val chips: List<CategoryChip> = emptyList(),
    val sections: List<TaskSection> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val celebration: Celebration? = null,
)

class TaskListViewModel(
    private val repository: TaskRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val filter = MutableStateFlow(TaskFilter())
    private val celebratingId = MutableStateFlow<Long?>(null)
    private var celebrationJob: Job? = null

    /** Ticks every minute so "Gecikti" and the day sections stay correct while the screen is open. */
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    val state: StateFlow<TaskListUiState> =
        combine(repository.allTasks, repository.allCategories, filter, celebratingId, clock) { tasks, categories, f, pinned, now ->
            val validFilter =
                if (f.categoryId != null && categories.none { it.id == f.categoryId }) f.copy(categoryId = null) else f
            val today = now.toLocalDateTime(zone).toLocalDate()
            val progress = dailyProgress(tasks, now, zone)
            val open = tasks.filterNot { it.isDone }
            TaskListUiState(
                loaded = true,
                today = today,
                now = now,
                hasAnyTask = tasks.isNotEmpty(),
                progress = progress,
                filter = validFilter,
                tabCounts = mapOf(
                    TaskTab.ALL to tasks.size,
                    TaskTab.OPEN to open.size,
                    TaskTab.DONE to tasks.size - open.size,
                ),
                chips = if (categories.isEmpty()) {
                    emptyList()
                } else {
                    listOf(CategoryChip(null, open.size)) +
                        categories.map { c -> CategoryChip(c, open.count { it.categoryId == c.id }) }
                },
                sections = groupIntoSections(applyFilter(tasks, validFilter, pinned), now, zone, pinned),
                categoriesById = categories.associateBy { it.id },
                celebration = tasks.find { it.id == pinned && it.isDone }?.let { task ->
                    Celebration(
                        taskTitle = task.title,
                        isToday = task.dueAt?.toLocalDateTime(zone)?.toLocalDate() == today,
                        progress = progress,
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    fun setTab(tab: TaskTab) = filter.update { it.copy(tab = tab) }

    fun setCategory(categoryId: Long?) = filter.update { it.copy(categoryId = categoryId) }

    fun toggleDone(task: Task) {
        val done = !task.isDone
        celebrationJob?.cancel()
        celebratingId.value = if (done) task.id else null
        if (done) {
            celebrationJob = viewModelScope.launch {
                delay(CELEBRATION_MILLIS)
                celebratingId.value = null
            }
        }
        viewModelScope.launch { repository.setDone(task, done) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    companion object {
        const val CELEBRATION_MILLIS = 1_600L

        val Factory = viewModelFactory {
            initializer { TaskListViewModel((this[APPLICATION_KEY] as YapLanApp).repository) }
        }
    }
}
