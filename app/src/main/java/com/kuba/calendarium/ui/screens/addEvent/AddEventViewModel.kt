package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        UIState(
            selectedDate = savedStateHandle.get<Long>(ARG_SELECTED_DATE_MS)?.let {
                Date(it)
            } ?: Date()
        ))

    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DescriptionChanged -> _uiState.value =
                _uiState.value.copy(description = event.description)

            is UIEvent.TitleChanged -> _uiState.value = _uiState.value.copy(title = event.title)
            is UIEvent.DateSelected -> _uiState.value =
                _uiState.value.copy(selectedDate = event.date)
        }
    }

    data class UIState(
        val title: String = "",
        val description: String = "",
        val selectedDate: Date = Date()
    )

    sealed class UIEvent {
        data class TitleChanged(val title: String) : UIEvent()
        data class DescriptionChanged(val description: String) : UIEvent()
        data class DateSelected(val date: Date) : UIEvent()
    }
}