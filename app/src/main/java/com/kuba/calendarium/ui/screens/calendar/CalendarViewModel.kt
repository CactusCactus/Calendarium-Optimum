package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DateSelected -> {
                _uiState.value = _uiState.value.copy(selectedDate = event.date)
            }
        }
    }

    data class UIState(
        val selectedDate: Date = Date()
    )

    sealed class UIEvent {
        data class DateSelected(val date: Date) : UIEvent()
    }
}