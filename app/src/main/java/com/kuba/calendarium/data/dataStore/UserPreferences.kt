package com.kuba.calendarium.data.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class UserPreferencesRepository @Inject constructor(
    private val appContext: Context,
    dataStoreName: String = USER_PREFERENCES_NAME
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

    companion object {
        private const val USER_PREFERENCES_NAME = "user_preferences"

        private const val KEY_SHOW_DIALOG_DELETE = "show_dialog_delete"

        const val SHOW_DIALOG_DEFAULT = true
    }

    fun getShowDialogDeletePreference(): Flow<Boolean> =
        appContext.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(KEY_SHOW_DIALOG_DELETE)] ?: SHOW_DIALOG_DEFAULT
        }

    suspend fun setShowDialogDeletePreference(show: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(KEY_SHOW_DIALOG_DELETE)] = show
        }
    }
}

