package com.kuba.calendarium.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {
}