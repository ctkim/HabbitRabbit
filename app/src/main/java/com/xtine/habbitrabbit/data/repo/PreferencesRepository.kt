package com.xtine.habbitrabbit.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xtine.habbitrabbit.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val theme: Flow<ThemePreference> = dataStore.data.map { prefs ->
        prefs[THEME_KEY]?.let { raw ->
            runCatching { ThemePreference.valueOf(raw) }.getOrNull()
        } ?: ThemePreference.SYSTEM
    }

    suspend fun setTheme(preference: ThemePreference) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = preference.name
        }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_pref_v1")
    }
}
