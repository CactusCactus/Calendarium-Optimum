package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.util.getDayStartMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    eventsRepository: EventsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    val eventList = eventsRepository.getEventsForDate(_uiState.value.selectedDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DateSelected -> {
                _uiState.value = _uiState.value.copy(selectedDate = event.date)
            }
        }
    }

    data class UIState(
        val selectedDate: Long = Date().getDayStartMillis()
    )

    sealed class UIEvent {
        data class DateSelected(val date: Long) : UIEvent()
    }
}