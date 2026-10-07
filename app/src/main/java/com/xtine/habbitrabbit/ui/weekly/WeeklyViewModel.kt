package com.xtine.habbitrabbit.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtine.habbitrabbit.data.Clock
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.data.repo.HabbitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class WeeklyUiState(
    val today: LocalDate,
    val weekStart: LocalDate,
    val selectedDay: LocalDate,
    val habbits: List<Habbit> = emptyList(),
    val completions: Set<Pair<Long, LocalDate>> = emptySet()
) {
    val weekDays: List<LocalDate> get() = weekStart.daysOfWeek()

    fun isCompleted(habbitId: Long, date: LocalDate): Boolean =
        habbitId to date in completions
}

@HiltViewModel
class WeeklyViewModel @Inject constructor(
    private val repository: HabbitRepository,
    private val clock: Clock
) : ViewModel() {

    private val today = clock.today()
    private val selection = MutableStateFlow(
        Selection(weekStart = today.weekStart(), selectedDay = today)
    )

    val state: StateFlow<WeeklyUiState> = combine(
        selection,
        repository.store
    ) { sel, store ->
        val rangeStart = sel.weekStart
        val rangeEnd = rangeStart.plusDays(6)
        val zone = ZoneId.systemDefault()
        val visibleHabbits = store.habbits
            .filter { habbit ->
                val archivedOn = habbit.archivedAt?.atZone(zone)?.toLocalDate()
                archivedOn == null || !archivedOn.isBefore(rangeStart)
            }
            .sortedBy { it.isArchived }
        val visibleIds = visibleHabbits.mapTo(HashSet()) { it.id }
        val completions = store.entries
            .asSequence()
            .filter { it.date in rangeStart..rangeEnd && it.habbitId in visibleIds }
            .map { it.habbitId to it.date }
            .toSet()
        WeeklyUiState(
            today = today,
            weekStart = sel.weekStart,
            selectedDay = sel.selectedDay,
            habbits = visibleHabbits,
            completions = completions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WeeklyUiState(today = today, weekStart = today.weekStart(), selectedDay = today)
    )

    fun previousWeek() = selection.update {
        val newStart = it.weekStart.minusWeeks(1)
        it.copy(weekStart = newStart, selectedDay = newStart.sameDowAs(it.selectedDay))
    }

    fun nextWeek() = selection.update {
        val newStart = it.weekStart.plusWeeks(1)
        it.copy(weekStart = newStart, selectedDay = newStart.sameDowAs(it.selectedDay))
    }

    fun onDaySelected(date: LocalDate) = selection.update {
        it.copy(weekStart = date.weekStart(), selectedDay = date)
    }

    fun goToToday() = selection.update {
        Selection(weekStart = today.weekStart(), selectedDay = today)
    }

    fun toggle(habbitId: Long, date: LocalDate) {
        viewModelScope.launch {
            repository.toggle(habbitId, date)
        }
    }

    private data class Selection(val weekStart: LocalDate, val selectedDay: LocalDate)

    private fun LocalDate.sameDowAs(other: LocalDate): LocalDate =
        plusDays((other.dayOfWeek.value - this.dayOfWeek.value).toLong())
}
