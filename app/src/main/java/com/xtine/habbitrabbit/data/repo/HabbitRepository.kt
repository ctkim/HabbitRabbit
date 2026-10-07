package com.xtine.habbitrabbit.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xtine.habbitrabbit.data.Clock
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.data.model.HabbitEntry
import com.xtine.habbitrabbit.data.model.HabbitStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabbitRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
    private val storeChangeNotifier: StoreChangeNotifier,
    repoScope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }

    val store: StateFlow<HabbitStore> = dataStore.data
        .map { prefs -> prefs[STORE_KEY]?.let(::decode) ?: HabbitStore() }
        .distinctUntilChanged()
        .stateIn(repoScope, SharingStarted.Eagerly, HabbitStore())

    fun observeAll(): Flow<List<Habbit>> = store.map { it.habbits }

    fun observeEntriesInRange(from: LocalDate, to: LocalDate): Flow<List<HabbitEntry>> =
        store.map { s -> s.entries.filter { it.date in from..to } }

    suspend fun toggle(habbitId: Long, date: LocalDate) {
        val now = clock.now()
        mutate { s ->
            val present = s.entries.any { it.habbitId == habbitId && it.date == date }
            if (present) {
                s.copy(entries = s.entries.filterNot { it.habbitId == habbitId && it.date == date })
            } else {
                s.copy(entries = s.entries + HabbitEntry(habbitId, date, now))
            }
        }
    }

    suspend fun upsert(habbit: Habbit): Habbit {
        var result = habbit
        mutate { s ->
            if (habbit.id == 0L) {
                val created = habbit.copy(id = s.nextHabbitId)
                result = created
                s.copy(habbits = s.habbits + created, nextHabbitId = s.nextHabbitId + 1)
            } else {
                result = habbit
                s.copy(habbits = s.habbits.map { if (it.id == habbit.id) habbit else it })
            }
        }
        return result
    }

    suspend fun delete(habbitId: Long) {
        mutate { s ->
            s.copy(
                habbits = s.habbits.filterNot { it.id == habbitId },
                entries = s.entries.filterNot { it.habbitId == habbitId }
            )
        }
    }

    suspend fun archive(habbitId: Long) {
        val now = clock.now()
        mutate { s ->
            s.copy(
                habbits = s.habbits.map { if (it.id == habbitId) it.copy(archivedAt = now) else it }
            )
        }
    }

    suspend fun reorderActive(orderedIds: List<Long>) {
        mutate { s ->
            val byId = s.habbits.associateBy { it.id }
            val activeInOrder = orderedIds.mapNotNull { byId[it] }.filterNot { it.isArchived }
            val activeIdSet = activeInOrder.mapTo(HashSet()) { it.id }
            // Preserve any active habbits not referenced in the requested order
            // (defensive; shouldn't normally happen) by appending them in their
            // current relative order.
            val leftoverActive = s.habbits.filterNot { it.isArchived || it.id in activeIdSet }
            val archived = s.habbits.filter { it.isArchived }
            s.copy(habbits = activeInOrder + leftoverActive + archived)
        }
    }

    private suspend fun mutate(block: (HabbitStore) -> HabbitStore) {
        dataStore.edit { prefs ->
            val current = prefs[STORE_KEY]?.let(::decode) ?: HabbitStore()
            prefs[STORE_KEY] = json.encodeToString(HabbitStore.serializer(), block(current))
        }
        storeChangeNotifier.notifyChanged()
    }

    private fun decode(raw: String): HabbitStore =
        json.decodeFromString(HabbitStore.serializer(), raw)

    private companion object {
        val STORE_KEY = stringPreferencesKey("habbit_store_v1")
    }
}
