package com.gloster.yaplan.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.HabitRepository
import com.gloster.yaplan.data.MonthScore
import com.gloster.yaplan.data.currentStreak
import com.gloster.yaplan.data.isAtRisk
import com.gloster.yaplan.data.longestStreak
import com.gloster.yaplan.data.monthScore
import com.gloster.yaplan.data.startDay
import com.gloster.yaplan.ui.minuteTicks
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitDetailUiState(
    val loaded: Boolean = false,
    /** Null once loaded means the habit was deleted. */
    val habit: Habit? = null,
    val today: LocalDate = LocalDate.now(),
    val month: YearMonth = YearMonth.from(today),
    val done: Set<LocalDate> = emptySet(),
) {
    /** The day the habit was added, or the forgotten yesterday marked on that first day. */
    val startDay: LocalDate get() = minOf(habit?.startDay() ?: today, done.minOrNull() ?: today)
    val doneToday: Boolean get() = today in done
    val streak: Int get() = currentStreak(done, today)
    val longest: Int get() = longestStreak(done)
    val atRisk: Boolean get() = isAtRisk(done, today)
    val thisMonth: MonthScore get() = monthScore(YearMonth.from(today), done, today, startDay)
    val canGoBack: Boolean get() = month > YearMonth.from(startDay)
    val canGoForward: Boolean get() = month < YearMonth.from(today)
}

class HabitDetailViewModel(
    private val habits: HabitRepository,
    savedState: SavedStateHandle,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val habitId: Long = savedState.get<Long>(ARG_HABIT_ID) ?: -1
    private val monthOffset = MutableStateFlow(0L)

    val state: StateFlow<HabitDetailUiState> =
        combine(habits.observeHabit(habitId), habits.observeDoneDays(habitId), minuteTicks(), monthOffset) { habit, done, now, offset ->
            val today = now.toLocalDateTime(zone).toLocalDate()
            HabitDetailUiState(
                loaded = true,
                habit = habit,
                today = today,
                month = YearMonth.from(today).plusMonths(offset),
                done = done,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitDetailUiState())

    fun previousMonth() = monthOffset.update { it - 1 }

    fun nextMonth() = monthOffset.update { minOf(0, it + 1) }

    fun setDone(day: LocalDate, done: Boolean) {
        val today = state.value.today
        viewModelScope.launch { habits.setDone(habitId, day, done, today) }
    }

    fun save(habit: Habit) {
        if (habit.name.isBlank()) return
        viewModelScope.launch { habits.saveHabit(habit.copy(name = habit.name.trim())) }
    }

    fun delete() {
        val habit = state.value.habit ?: return
        viewModelScope.launch { habits.deleteHabit(habit) }
    }

    companion object {
        const val ARG_HABIT_ID = "habitId"

        val Factory = viewModelFactory {
            initializer {
                HabitDetailViewModel((this[APPLICATION_KEY] as YapLanApp).habits, createSavedStateHandle())
            }
        }
    }
}
