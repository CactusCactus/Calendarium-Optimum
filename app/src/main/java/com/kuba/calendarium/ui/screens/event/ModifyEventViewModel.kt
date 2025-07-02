package com.kuba.calendarium.ui.screens.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.repo.EventsRepository
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
                _uiState.value.copy(selectedDate = event.date.resetToMidnight())
            }

            UIEvent.DoneClicked -> viewModelScope.launch {
                databaseWriteOperation()
                _navEvent.send(NavEvent.Finish(_uiState.value.selectedDate))
            }
            // Date picker events
            UIEvent.DatePickerOpened -> _uiState.update {
                _uiState.value.copy(datePickerOpen = true)
            }

            UIEvent.DatePickerDismissed -> _uiState.update {
                _uiState.value.copy(datePickerOpen = false)
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
        val datePickerOpen: Boolean = false,

        // Validation
        val titleError: ValidationError? = null,
        val descriptionError: ValidationError? = null,
        val isValid: Boolean = false
    )

    sealed class UIEvent {
        data class TitleChanged(val title: String) : UIEvent()
        data class DescriptionChanged(val description: String) : UIEvent()
        data class DateSelected(val date: Long) : UIEvent()
        object DoneClicked : UIEvent()
        object DatePickerOpened : UIEvent()
        object DatePickerDismissed : UIEvent()
    }

    sealed class NavEvent {
        data class Finish(val eventDate: Long) : NavEvent()
    }

    enum class ValidationError {
        TITLE_EMPTY, TITLE_TOO_LONG, DESCRIPTION_TOO_LONG
    }
}