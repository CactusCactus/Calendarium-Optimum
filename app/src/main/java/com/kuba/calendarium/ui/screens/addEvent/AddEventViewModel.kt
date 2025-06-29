package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import com.kuba.calendarium.util.getDayStartMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        UIState(selectedDate = savedStateHandle.get<Long>(ARG_SELECTED_DATE_MS) ?: Date().time)
    )

    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DescriptionChanged -> _uiState.value =
                _uiState.value.copy(description = event.description)

            is UIEvent.TitleChanged -> _uiState.value = _uiState.value.copy(title = event.title)
            is UIEvent.DateSelected -> _uiState.value =
                _uiState.value.copy(selectedDate = event.date.getDayStartMillis())

            UIEvent.DoneClicked -> viewModelScope.launch {
                eventsRepository.insertEvent(
                    Event(
                        title = _uiState.value.title,
                        description = _uiState.value.description,
                        date = _uiState.value.selectedDate
                    )
                )
            }
            // Date picker events
            UIEvent.DatePickerOpened -> _uiState.value = _uiState.value.copy(
                datePickerOpen = true
            )

            UIEvent.DatePickerDismissed -> _uiState.value = _uiState.value.copy(
                datePickerOpen = false
            )
        }
    }

    data class UIState(
        val title: String = "",
        val description: String = "",
        val selectedDate: Long = Date().getDayStartMillis(),
        val datePickerOpen: Boolean = false
    )

    sealed class UIEvent {
        data class TitleChanged(val title: String) : UIEvent()
        data class DescriptionChanged(val description: String) : UIEvent()
        data class DateSelected(val date: Date) : UIEvent()
        object DoneClicked : UIEvent()
        object DatePickerOpened : UIEvent()
        object DatePickerDismissed : UIEvent()
    }
}