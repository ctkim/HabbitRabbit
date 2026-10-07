package com.xtine.habbitrabbit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtine.habbitrabbit.data.model.ThemePreference
import com.xtine.habbitrabbit.data.repo.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository
) : ViewModel() {

    val theme: StateFlow<ThemePreference> = preferences.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)

    fun onThemeSelected(preference: ThemePreference) {
        viewModelScope.launch {
            preferences.setTheme(preference)
        }
    }
}
