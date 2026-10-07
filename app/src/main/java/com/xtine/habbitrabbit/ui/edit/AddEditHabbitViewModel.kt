package com.xtine.habbitrabbit.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtine.habbitrabbit.data.Clock
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.data.repo.HabbitRepository
import com.xtine.habbitrabbit.ui.common.HabbitColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val id: Long = 0L,
    val name: String = "",
    val colorArgb: Int = HabbitColor.defaultArgb,
    val targetEnabled: Boolean = false,
    val targetPerWeek: Int = 3,
    val loading: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false
) {
    val isEditing: Boolean get() = id != 0L
    val canSave: Boolean get() = name.isNotBlank() && !loading
}

@HiltViewModel
class AddEditHabbitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HabbitRepository,
    private val clock: Clock
) : ViewModel() {

    private val initialId: Long = savedStateHandle.get<Long>(ARG_HABIT_ID) ?: 0L

    private val _state = MutableStateFlow(AddEditUiState(id = initialId, loading = initialId != 0L))
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    init {
        if (initialId != 0L) loadExisting(initialId)
    }

    private fun loadExisting(id: Long) {
        val habbit = repository.store.value.habbits.firstOrNull { it.id == id }
        if (habbit == null) {
            _state.update { it.copy(loading = false) }
            return
        }
        _state.update {
            it.copy(
                id = habbit.id,
                name = habbit.name,
                colorArgb = habbit.colorArgb,
                targetEnabled = habbit.targetPerWeek != null,
                targetPerWeek = habbit.targetPerWeek ?: 3,
                loading = false
            )
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onColorSelected(argb: Int) = _state.update { it.copy(colorArgb = argb) }
    fun onTargetEnabledChange(enabled: Boolean) = _state.update { it.copy(targetEnabled = enabled) }
    fun onTargetValueChange(value: Int) =
        _state.update { it.copy(targetPerWeek = value.coerceIn(1, 7)) }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val target = if (s.targetEnabled) s.targetPerWeek else null
            val habbit = Habbit(
                id = s.id,
                name = s.name.trim(),
                colorArgb = s.colorArgb,
                targetPerWeek = target,
                createdAt = clock.now()
            )
            repository.upsert(habbit)
            _state.update { it.copy(loading = false, saved = true) }
        }
    }

    fun delete() {
        val id = _state.value.id
        if (id == 0L) return
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            repository.delete(id)
            _state.update { it.copy(loading = false, deleted = true) }
        }
    }

    fun archive() {
        val id = _state.value.id
        if (id == 0L) return
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            repository.archive(id)
            _state.update { it.copy(loading = false, deleted = true) }
        }
    }

    companion object {
        const val ARG_HABIT_ID = "habbitId"
    }
}
