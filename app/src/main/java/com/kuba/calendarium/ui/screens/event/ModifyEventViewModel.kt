package com.kuba.calendarium.ui.screens.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.NavEvent.Finish
import com.kuba.calendarium.util.getTodayMidnight
import com.kuba.calendarium.util.resetToMidnight
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// TODO eventsRepository should be protected but it's gonna break tests
abstract class ModifyEventViewModel(
    internal val eventsRepository: EventsRepository,
    protected val savedStateHandle: SavedStateHandle
) : ViewModel() {
    protected val _uiState = initUIState()

    protected val _navEvent = Channel<NavEvent>()

    val uiState = _uiState.asStateFlow()

    val navEvent = _navEvent.receiveAsFlow()

    companion object {
        const val MAX_TITLE_LENGTH = 100

        const val MAX_DESCRIPTION_LENGTH = 2000
    }

    protected abstract suspend fun databaseWriteOperation()

    protected abstract fun initUIState(): MutableStateFlow<UIState>

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DescriptionChanged -> {
                _uiState.update { _uiState.value.copy(description = event.description) }
                checkAndUpdateValidity()
            }

            is UIEvent.TitleChanged -> {
                _uiState.update { _uiState.value.copy(title = event.title) }
                checkAndUpdateValidity()
            }

            is UIEvent.DateSelected -> _uiState.update {
                when (_uiState.value.currentDateTimeMode) {
                    DateTimeMode.FROM -> {
                        var dateEnd = _uiState.value.selectedDateEnd

                        if (dateEnd != null && dateEnd < event.date) {
                            dateEnd = event.date
                        }

                        _uiState.value.copy(
                            selectedDate = event.date.resetToMidnight(),
                            selectedDateEnd = dateEnd?.resetToMidnight()
                        )
                    }

                    DateTimeMode.TO -> {
                        var dateStart = _uiState.value.selectedDate

                        if (dateStart > event.date) {
                            dateStart = event.date
                        }

                        // If not set set as selectedTime (or null if selectedTime not set)
                        val selectedTime =
                            _uiState.value.selectedTimeEnd ?: _uiState.value.selectedTime

                        _uiState.value.copy(
                            selectedDate = dateStart.resetToMidnight(),
                            selectedDateEnd = event.date.resetToMidnight(),
                            selectedTimeEnd = selectedTime
                        )
                    }
                }
            }

            is UIEvent.TimeSelected -> _uiState.update {
                when (_uiState.value.currentDateTimeMode) {
                    DateTimeMode.FROM -> {
                        val timeStart = _uiState.value.selectedDate + event.time // Add date to time
                        var timeEnd = if (_uiState.value.selectedDateEnd != null) {
                            val selectedDate =
                                _uiState.value.selectedDateEnd ?: _uiState.value.selectedDate

                            selectedDate + event.time
                        } else null // Set end time only when end date is set

                        if (timeEnd != null && timeEnd < timeStart) {
                            timeEnd = timeStart
                        }

                        _uiState.value.copy(selectedTime = timeStart, selectedTimeEnd = timeEnd)
                    }

                    DateTimeMode.TO -> {
                        val timeEnd =
                            (_uiState.value.selectedDateEnd ?: _uiState.value.selectedDate) +
                                    event.time // Add date to time
                        var timeStart = _uiState.value.selectedTime

                        if (timeStart != null && timeStart > timeEnd) {
                            timeStart = timeEnd
                        }

                        _uiState.value.copy(selectedTime = timeStart, selectedTimeEnd = timeEnd)
                    }
                }
            }

            is UIEvent.ClearDateAndTime -> _uiState.update {
                when (event.mode) {
                    DateTimeMode.FROM -> _uiState.value.copy(
                        selectedDate = getTodayMidnight(),
                        selectedTime = null
                    ) // Can't be fully cleared
                    DateTimeMode.TO -> _uiState.value.copy(
                        selectedDateEnd = null,
                        selectedTimeEnd = null
                    )
                }
            }

            is UIEvent.ClearTime -> _uiState.update {
                _uiState.value.copy(selectedTime = null, selectedTimeEnd = null)
            }

            UIEvent.DoneClicked -> viewModelScope.launch {
                databaseWriteOperation()
                _navEvent.send(Finish(_uiState.value.selectedDate))
            }
            // Date picker events
            is UIEvent.DatePickerOpened -> _uiState.update {
                _uiState.value.copy(datePickerOpen = true, currentDateTimeMode = event.mode)
            }

            UIEvent.DatePickerDismissed -> _uiState.update {
                _uiState.value.copy(datePickerOpen = false)
            }

            is UIEvent.TimePickerOpened -> _uiState.update {
                _uiState.value.copy(timePickerOpen = true, currentDateTimeMode = event.mode)
            }

            UIEvent.TimePickerDismissed -> _uiState.update {
                _uiState.value.copy(timePickerOpen = false)
            }
        }
    }

    protected fun validateTitle(title: String): ValidationError? {
        return when {
            title.isBlank() -> ValidationError.TITLE_EMPTY
            title.length > MAX_TITLE_LENGTH -> ValidationError.TITLE_TOO_LONG
            else -> null
        }
    }

    protected fun validateDescription(description: String): ValidationError? {
        return when {
            description.length > MAX_DESCRIPTION_LENGTH -> ValidationError.DESCRIPTION_TOO_LONG
            else -> null
        }
    }

    protected fun checkAndUpdateValidity() {
        _uiState.update {
            _uiState.value.copy(
                titleError = validateTitle(_uiState.value.title),
                descriptionError = validateDescription(_uiState.value.description)
            )
        }

        _uiState.update {
            _uiState.value.copy(
                isValid = _uiState.value.titleError == null &&
                        _uiState.value.descriptionError == null
            )
        }
    }

    data class UIState(
        val title: String = "",
        val description: String = "",
        val selectedDate: Long = getTodayMidnight(),
        val selectedDateEnd: Long? = null,
        val selectedTime: Long? = null,
        val selectedTimeEnd: Long? = null,
        val datePickerOpen: Boolean = false,
        val timePickerOpen: Boolean = false,
        val currentDateTimeMode: DateTimeMode = DateTimeMode.FROM,

        // Validation
        val titleError: ValidationError? = null,
        val descriptionError: ValidationError? = null,
        val isValid: Boolean = false
    )

    sealed class UIEvent {
        data class TitleChanged(val title: String) : UIEvent()
        data class DescriptionChanged(val description: String) : UIEvent()
        data class DateSelected(val date: Long) : UIEvent()
        data class TimeSelected(val time: Long) : UIEvent()
        object ClearTime : UIEvent()
        data class ClearDateAndTime(val mode: DateTimeMode) : UIEvent()
        object DoneClicked : UIEvent()
        data class DatePickerOpened(val mode: DateTimeMode) : UIEvent()
        data class TimePickerOpened(val mode: DateTimeMode) : UIEvent()
        object DatePickerDismissed : UIEvent()
        object TimePickerDismissed : UIEvent()
    }

    sealed class NavEvent {
        data class Finish(val eventDate: Long) : NavEvent()
    }

    enum class ValidationError {
        TITLE_EMPTY, TITLE_TOO_LONG, DESCRIPTION_TOO_LONG
    }
}

enum class DateTimeMode {
    FROM, TO
}