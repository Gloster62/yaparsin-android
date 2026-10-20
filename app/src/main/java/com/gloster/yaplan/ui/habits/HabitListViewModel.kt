package com.gloster.yaplan.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.HabitRepository
import com.gloster.yaplan.data.currentStreak
import com.gloster.yaplan.data.doneDaysByHabit
import com.gloster.yaplan.data.isAtRisk
import com.gloster.yaplan.ui.minuteTicks
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitRow(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int,
    val atRisk: Boolean,
    val everDone: Boolean,
)

data class HabitListUiState(
    val loaded: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val rows: List<HabitRow> = emptyList(),
) {
    val doneToday: Int get() = rows.count { it.doneToday }
}

fun habitProgressMessage(done: Int, total: Int): String {
    val left = total - done
    return when {
        left == 0 -> "Bugünün halkaları tamam. Zinciri kırmadın lan!"
        done == 0 -> "Bugün daha hiçbiri yapılmadı. Zinciri kırma!"
        left == 1 -> "Bir tane kaldı. Zinciri kırma lan!"
        else -> "$left tane kaldı. Zinciri kırma!"
    }
}

class HabitListViewModel(
    private val habits: HabitRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    val state: StateFlow<HabitListUiState> =
        combine(habits.allHabits, habits.allChecks, minuteTicks()) { list, checks, now ->
            val today = now.toLocalDateTime(zone).toLocalDate()
            val doneByHabit = checks.doneDaysByHabit()
            HabitListUiState(
                loaded = true,
                today = today,
                rows = list.map { habit ->
                    val done = doneByHabit[habit.id].orEmpty()
                    HabitRow(
                        habit = habit,
                        doneToday = today in done,
                        streak = currentStreak(done, today),
                        atRisk = isAtRisk(done, today),
                        everDone = done.isNotEmpty(),
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListUiState())

    fun toggleToday(row: HabitRow) {
        val today = state.value.today
        viewModelScope.launch { habits.setDone(row.habit.id, today, !row.doneToday, today) }
    }

    fun save(habit: Habit) {
        if (habit.name.isBlank()) return
        viewModelScope.launch { habits.saveHabit(habit.copy(name = habit.name.trim())) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HabitListViewModel((this[APPLICATION_KEY] as YapLanApp).habits) }
        }
    }
}
