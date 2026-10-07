package com.xtine.habbitrabbit.ui.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.data.repo.HabbitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReorderUiState(
    val habbits: List<Habbit> = emptyList(),
    val initialized: Boolean = false
)

@HiltViewModel
class ReorderHabbitsViewModel @Inject constructor(
    private val repository: HabbitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReorderUiState())
    val state: StateFlow<ReorderUiState> = _state.asStateFlow()

    init {
        val active = repository.store.value.habbits.filterNot { it.isArchived }
        _state.value = ReorderUiState(habbits = active, initialized = true)
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val list = current.habbits.toMutableList()
            if (fromIndex !in list.indices || toIndex !in list.indices) return@update current
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            current.copy(habbits = list)
        }
    }

    fun commit() {
        val orderedIds = _state.value.habbits.map { it.id }
        viewModelScope.launch {
            repository.reorderActive(orderedIds)
        }
    }
}
