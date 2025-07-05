package com.kuba.calendarium.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.getShowDialogDeletePreference().collect { show ->
                _uiState.update { _uiState.value.copy(showDeleteDialog = show) }
            }
        }
    }

    fun onEvent(event: UIEvent) = when (event) {
        is UIEvent.DeleteDialogChanged -> viewModelScope.launch {
            userPreferencesRepository.setShowDialogDeletePreference(event.show)
            _uiState.update { _uiState.value.copy(showDeleteDialog = event.show) }
        }
    }
}

data class UIState(
    val showDeleteDialog: Boolean = UserPreferencesRepository.SHOW_DIALOG_DEFAULT
)

sealed class UIEvent {
    data class DeleteDialogChanged(val show: Boolean) : UIEvent()
}