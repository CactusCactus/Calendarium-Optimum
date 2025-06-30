package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.util.getTodayMidnight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    val _selectedDate = MutableStateFlow(getTodayMidnight())

    val selectedDate = _selectedDate.asStateFlow()

    val eventList = _selectedDate.flatMapLatest {
        eventsRepository.getEventsForDate(it).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DateSelected -> _selectedDate.value = event.date
        }
    }

    data class UIState(
        val selectedDate: Long = getTodayMidnight() // Unused
    )

    sealed class UIEvent {
        data class DateSelected(val date: Long) : UIEvent()
    }
}