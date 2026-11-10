package com.coder7475.focusguard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_guard_preferences")

class FocusPreferences(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_BATTERY_GUIDE_DISMISSED = booleanPreferencesKey("battery_guide_dismissed")
        val KEY_SELECTED_DURATION_MINUTES = intPreferencesKey("selected_duration_minutes")
        const val DEFAULT_DURATION_MINUTES = 25
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val isBatteryGuideDismissed: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_BATTERY_GUIDE_DISMISSED] ?: false
        }

    suspend fun setBatteryGuideDismissed(dismissed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BATTERY_GUIDE_DISMISSED] = dismissed
        }
    }

    val selectedDurationMinutes: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SELECTED_DURATION_MINUTES] ?: DEFAULT_DURATION_MINUTES
        }

    suspend fun setSelectedDurationMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_DURATION_MINUTES] = minutes
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
