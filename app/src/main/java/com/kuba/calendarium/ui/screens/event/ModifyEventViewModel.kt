package com.kuba.calendarium.ui.screens.event

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.model.internal.Repetition
import com.kuba.calendarium.data.model.internal.TaskCreationData
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.NavEvent.Finish
import com.kuba.calendarium.util.MAX_DAYS_FOR_REPETITION_MONTHLY
import com.kuba.calendarium.util.MAX_DAYS_FOR_REPETITION_WEEKLY
import com.kuba.calendarium.util.MAX_DAYS_FOR_REPETITION_YEARLY
import com.kuba.calendarium.util.getTodayMidnight
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// TODO eventsRepository should be protected but it's gonna break tests
abstract class ModifyEventViewModel(
    internal val eventsRepository: EventsRepository,
    protected val savedStateHandle: SavedStateHandle
) : ViewModel() {
    open val focusTitleOnStart: Boolean = true

    protected val _uiState = initUIState()

    protected val _navEvent = Channel<NavEvent>()

    val uiState = _uiState.asStateFlow()

    val navEvent = _navEvent.receiveAsFlow()

    companion object {

        const val MAX_TITLE_LENGTH = 100

        const val MAX_DESCRIPTION_LENGTH = 2000

        const val MAX_TASK_LENGTH = 80
        const val MAX_TASK_COUNT = 20
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

            is UIEvent.DateSelected -> dateSelected(event)
            is UIEvent.TimeSelected -> timeSelected(event)
            is UIEvent.ClearDateAndTime -> clearDateAndTime(event)

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

            is UIEvent.AddTask -> addTask(event)
            is UIEvent.UpdateTask -> updateTask(event)
            is UIEvent.RemoveTask -> removeTask(event)
            is UIEvent.ReorderTask -> reorderTask(event)
            is UIEvent.RepetitionChanged -> _uiState.update {
                _uiState.value.copy(currentRepetition = event.repetition)
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

    protected fun validateDescription(description: String?): ValidationError? {
        return if ((description?.length ?: 0) > MAX_DESCRIPTION_LENGTH)
            ValidationError.DESCRIPTION_TOO_LONG
        else null
    }

    protected fun validateTask(text: String, index: Int): ValidationError? {
        return when {
            index >= MAX_TASK_COUNT -> ValidationError.TASK_TOO_MANY
            text.isBlank() -> ValidationError.TASK_EMPTY
            text.length > MAX_TASK_LENGTH -> ValidationError.TASK_TOO_LONG
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

        val taskErrors = _uiState.value.taskList.mapIndexed { index, task ->
            validateTask(task.title, index)
        }

        _uiState.update {
            _uiState.value.copy(taskError = taskErrors.toMutableStateList())
        }

        _uiState.update {
            _uiState.value.copy(
                isValid = _uiState.value.titleError == null &&
                        _uiState.value.descriptionError == null &&
                        _uiState.value.taskError.filterNotNull().isEmpty()
            )
        }
    }

    private fun clearDateAndTime(event: UIEvent.ClearDateAndTime) {
        _uiState.update {
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

        updatePossibleRepetitions()
    }

    private fun dateSelected(event: UIEvent.DateSelected) {
        _uiState.update {
            when (_uiState.value.currentDateTimeMode) {
                DateTimeMode.FROM -> {
                    var dateEnd = _uiState.value.selectedDateEnd

                    if (dateEnd != null && dateEnd < event.date) {
                        dateEnd = event.date
                    }

                    _uiState.value.copy(
                        selectedDate = event.date,
                        selectedDateEnd = dateEnd
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
                        selectedDate = dateStart,
                        selectedDateEnd = event.date,
                        selectedTimeEnd = selectedTime
                    )
                }
            }
        }

        updatePossibleRepetitions()
    }

    private fun timeSelected(event: UIEvent.TimeSelected) {
        _uiState.update {
            when (_uiState.value.currentDateTimeMode) {
                DateTimeMode.FROM -> {
                    val timeStart = event.time
                    var timeEnd = if (_uiState.value.selectedDateEnd != null) {
                        event.time
                    } else null // Set end time only when end date is set

                    if (timeEnd != null && timeEnd < timeStart) {
                        timeEnd = timeStart
                    }

                    _uiState.value.copy(selectedTime = timeStart, selectedTimeEnd = timeEnd)
                }

                DateTimeMode.TO -> {
                    val timeEnd = event.time
                    var timeStart = _uiState.value.selectedTime

                    if (timeStart != null && timeStart > timeEnd) {
                        timeStart = timeEnd
                    }

                    _uiState.value.copy(selectedTime = timeStart, selectedTimeEnd = timeEnd)
                }
            }
        }
    }

    private fun addTask(event: UIEvent.AddTask) {
        _uiState.update {
            it.copy(taskList = it.taskList.apply {
                add(TaskCreationData(title = event.title))
            })
        }
        checkAndUpdateValidity()
    }

    private fun updateTask(event: UIEvent.UpdateTask) {
        _uiState.update {
            it.copy(taskList = it.taskList.apply {
                set(event.index, event.task)
            })
        }
        checkAndUpdateValidity()
    }

    private fun removeTask(event: UIEvent.RemoveTask) {
        _uiState.update {
            if (it.taskList.size == 1) {
                it.copy(taskList = mutableStateListOf())
            } else
                it.copy(taskList = it.taskList.apply { removeAt(event.index) })
        }
        checkAndUpdateValidity()
    }

    private fun reorderTask(event: UIEvent.ReorderTask) {
        _uiState.update {
            it.copy(taskList = it.taskList.apply {
                add(event.toIndex, removeAt(event.fromIndex))
            })
        }
    }

    private fun updatePossibleRepetitions() {
        val startDate = _uiState.value.selectedDate
        val endDate = _uiState.value.selectedDateEnd

        if (endDate == null) {
            _uiState.update { _uiState.value.copy(availableRepetitions = Repetition.entries) }

            return
        }

        val weeklyPossible =
            endDate.toEpochDay() - startDate.toEpochDay() < MAX_DAYS_FOR_REPETITION_WEEKLY
        val monthlyPossible =
            endDate.toEpochDay() - startDate.toEpochDay() < MAX_DAYS_FOR_REPETITION_MONTHLY
        val yearlyPossible =
            endDate.toEpochDay() - startDate.toEpochDay() < MAX_DAYS_FOR_REPETITION_YEARLY

        val possibleRepetitions = mutableListOf<Repetition>()

        if (weeklyPossible) possibleRepetitions.add(Repetition.WEEKLY)
        if (monthlyPossible) possibleRepetitions.add(Repetition.MONTHLY)
        if (yearlyPossible) possibleRepetitions.add(Repetition.YEARLY)

        _uiState.update { _uiState.value.copy(availableRepetitions = possibleRepetitions) }

        val currentRepetition = _uiState.value.currentRepetition

        if (currentRepetition != null && !possibleRepetitions.contains(currentRepetition)) {
            _uiState.update {
                _uiState.value.copy(currentRepetition = possibleRepetitions.firstOrNull())
            }
        }
    }

    data class UIState(
        val title: String = "",
        val description: String? = null,
        val taskList: SnapshotStateList<TaskCreationData> = mutableStateListOf(),
        val selectedDate: LocalDate = getTodayMidnight(),
        val selectedDateEnd: LocalDate? = null,
        val selectedTime: LocalTime? = null,
        val selectedTimeEnd: LocalTime? = null,
        val datePickerOpen: Boolean = false,
        val timePickerOpen: Boolean = false,
        val currentDateTimeMode: DateTimeMode = DateTimeMode.FROM,
        val currentRepetition: Repetition? = null,
        val availableRepetitions: List<Repetition> = Repetition.entries,
        val isDone: Boolean = false,

        // Validation
        val titleError: ValidationError? = null,
        val taskError: SnapshotStateList<ValidationError?> = mutableStateListOf(),
        val descriptionError: ValidationError? = null,
        val isValid: Boolean = false
    )

    sealed class UIEvent {
        data class TitleChanged(val title: String) : UIEvent()
        data class DescriptionChanged(val description: String?) : UIEvent()
        data class DateSelected(val date: LocalDate) : UIEvent()
        data class TimeSelected(val time: LocalTime) : UIEvent()
        data class ClearDateAndTime(val mode: DateTimeMode) : UIEvent()
        data class DatePickerOpened(val mode: DateTimeMode) : UIEvent()
        data class TimePickerOpened(val mode: DateTimeMode) : UIEvent()
        data class AddTask(val title: String) : UIEvent()
        data class UpdateTask(val index: Int, val task: TaskCreationData) : UIEvent()
        data class RemoveTask(val index: Int) : UIEvent()
        data class ReorderTask(val fromIndex: Int, val toIndex: Int) : UIEvent()
        data class RepetitionChanged(val repetition: Repetition?) : UIEvent()
        object ClearTime : UIEvent()
        object DoneClicked : UIEvent()
        object DatePickerDismissed : UIEvent()
        object TimePickerDismissed : UIEvent()
    }

    sealed class NavEvent {
        data class Finish(val eventDate: LocalDate) : NavEvent()
    }

    enum class ValidationError {
        TITLE_EMPTY, TITLE_TOO_LONG, DESCRIPTION_TOO_LONG, TASK_EMPTY, TASK_TOO_LONG, TASK_TOO_MANY
    }
}

enum class DateTimeMode {
    FROM, TO
}
