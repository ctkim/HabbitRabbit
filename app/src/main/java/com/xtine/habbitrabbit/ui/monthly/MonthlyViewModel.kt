package com.xtine.habbitrabbit.ui.monthly

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
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class MonthlyUiState(
    val today: LocalDate,
    val anchorMonth: YearMonth,
    val habbits: List<Habbit> = emptyList(),
    val archivedHabbits: List<Habbit> = emptyList(),
    val completionsByHabbit: Map<Long, Set<LocalDate>> = emptyMap()
) {
    fun completionsFor(habbitId: Long): Set<LocalDate> =
        completionsByHabbit[habbitId] ?: emptySet()

    fun completionCount(habbitId: Long): Int =
        completionsByHabbit[habbitId]?.size ?: 0
}

@HiltViewModel
class MonthlyViewModel @Inject constructor(
    repository: HabbitRepository,
    clock: Clock
) : ViewModel() {

    private val today = clock.today()
    private val anchor = MutableStateFlow(YearMonth.from(today))

    val state: StateFlow<MonthlyUiState> = combine(
        anchor,
        repository.store
    ) { month, store ->
        val rangeStart = month.atDay(1)
        val rangeEnd = month.atEndOfMonth()
        val completions = store.entries
            .asSequence()
            .filter { it.date in rangeStart..rangeEnd }
            .groupBy({ it.habbitId }, { it.date })
            .mapValues { (_, dates) -> dates.toSet() }
        val (archived, active) = store.habbits.partition { it.isArchived }
        val archivedWithActivity = archived.filter {
            completions[it.id]?.isNotEmpty() == true
        }
        MonthlyUiState(
            today = today,
            anchorMonth = month,
            habbits = active,
            archivedHabbits = archivedWithActivity,
            completionsByHabbit = completions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MonthlyUiState(today = today, anchorMonth = YearMonth.from(today))
    )

    fun previousMonth() = anchor.update { it.minusMonths(1) }

    fun nextMonth() = anchor.update { it.plusMonths(1) }

    fun goToToday() = anchor.update { YearMonth.from(today) }
}
