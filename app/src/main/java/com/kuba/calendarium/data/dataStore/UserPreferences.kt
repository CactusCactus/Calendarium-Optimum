package com.kuba.calendarium.data.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel
import com.kuba.calendarium.util.valueOfOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val USER_PREFERENCES_NAME = "user_preferences"

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

class UserPreferencesRepository @Inject constructor(private val appContext: Context) {
    companion object {
        private const val KEY_CALENDAR_MODE = "calendar_mode"

        private const val KEY_SHOW_DIALOG_DELETE = "show_dialog_delete"

        val CALENDAR_MODE_DEFAULT = CalendarViewModel.CalendarDisplayMode.MONTH

        const val SHOW_DIALOG_DEFAULT = true
    }

    fun getCalendarModePreference(): Flow<CalendarViewModel.CalendarDisplayMode> =
        appContext.dataStore.data.map { preferences ->
            valueOfOrNull<CalendarViewModel.CalendarDisplayMode>(
                preferences[stringPreferencesKey(KEY_CALENDAR_MODE)] ?: CALENDAR_MODE_DEFAULT.name
            ) ?: CALENDAR_MODE_DEFAULT
        }

    fun getShowDialogDeletePreference(): Flow<Boolean> =
        appContext.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(KEY_SHOW_DIALOG_DELETE)] ?: SHOW_DIALOG_DEFAULT
        }

    suspend fun setCalendarModePreference(mode: CalendarViewModel.CalendarDisplayMode) {
        appContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(KEY_CALENDAR_MODE)] = mode.name
        }
    }

    suspend fun setShowDialogDeletePreference(show: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(KEY_SHOW_DIALOG_DELETE)] = show
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
