package android.learn.habitapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.habitPrefsDataStore by preferencesDataStore(name = "habit_prefs")

class HabitPreferences(private val context: Context) {
    private val hasSeenSwipeHintKey = booleanPreferencesKey("has_seen_swipe_hint")

    val hasSeenSwipeHint: Flow<Boolean> = context.habitPrefsDataStore.data
        .map { prefs -> prefs[hasSeenSwipeHintKey] ?: false }

    suspend fun setSwipeHintSeen() {
        context.habitPrefsDataStore.edit { prefs ->
            prefs[hasSeenSwipeHintKey] = true
        }
    }
}