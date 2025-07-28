package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.EventTasks
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.NavEvent.EditEvent
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent.ContextEventDelete
import com.kuba.calendarium.util.getTodayMidnight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayMidnight())

    val selectedDate = _selectedDate.asStateFlow()

    private val _navEvent = Channel<NavEvent>()

    val navEvent = _navEvent.receiveAsFlow()

    val visibleDatesRange: MutableStateFlow<Pair<LocalDate, LocalDate>> =
        MutableStateFlow(getTodayMidnight() to getTodayMidnight())

    val eventCountMap: StateFlow<Map<LocalDate, Int>> =
        visibleDatesRange.flatMapLatest {
            eventsRepository.getEventCountForDateRange(it.first, it.second)
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(),
            initialValue = emptyMap()
        )

    private var contextMenuEvent: Event? = null

    companion object {
        const val PAGER_VIRTUAL_PAGE_COUNT: Long = 365 * 10 // ~10 years

        const val PAGER_INITIAL_OFFSET_DAYS: Long = PAGER_VIRTUAL_PAGE_COUNT / 2
    }

    init {
        // Collect the user preferences
        viewModelScope.launch {
            userPreferencesRepository.getShowDialogDeletePreference().collect { value ->
                _uiState.update { _uiState.value.copy(showDialogDelete = value) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.getCalendarModePreference().collect { value ->
                _uiState.update { _uiState.value.copy(calendarDisplayMode = value) }
            }
        }
    }

    fun getEventsForDate(date: LocalDate): Flow<List<EventTasks>> =
        eventsRepository.getEventTasksListForDate(date).onEach {
            it.forEach {
                Timber.d("Event: ${it.event.title}, Tasks: ${it.tasks.map { it.title }}")
            }
        }

    fun onEvent(event: UIEvent) {
        when (event) {
            // Event manipulation events
            is UIEvent.DateSelected -> _selectedDate.update { event.date }

            is ContextEventDelete -> deleteEvent(event)

            is UIEvent.DoneChanged -> viewModelScope.launch {
                eventsRepository.updateEvent(event.event.copy(done = event.checked))
            }

            // Context menu events
            is UIEvent.ContextMenuOpen -> {
                _uiState.update {
                    _uiState.value.copy(contextMenuOpen = true, contextMenuName = event.event.title)
                }
                contextMenuEvent = event.event
            }

            is UIEvent.ContextMenuDismiss -> _uiState.update {
                _uiState.value.copy(contextMenuOpen = false)
            }

            is UIEvent.ContextMenuOptionSelected -> contextMenuOptionSelected(event)

            // Dialog events
            UIEvent.ShowMonthYearPickerDialog -> _uiState.update {
                _uiState.value.copy(datePickerDialogShowing = true)
            }

            UIEvent.DatePickerDialogDismiss -> _uiState.update {
                _uiState.value.copy(datePickerDialogShowing = false)
            }

            UIEvent.DeleteDialogDismiss -> _uiState.update {
                _uiState.value.copy(contextMenuOpen = false, deleteDialogShowing = false)
            }

            // Toolbar events
            UIEvent.CalendarModeClicked -> calendarModeChanged()

            UIEvent.SettingsClicked -> viewModelScope.launch { _navEvent.send(NavEvent.Settings) }

            // Misc
            is UIEvent.VisibleDatesChanged -> visibleDatesRange.update {
                event.startDate to event.endDate
            }

            is UIEvent.OnTaskDoneChanged -> viewModelScope.launch {
                eventsRepository.updateTask(event.task, event.checked)
            }
        }
    }

    fun localDateToPageIndex(localDate: LocalDate): Long {
        val startDateOfPager = getTodayMidnight()

        val daysDifference = localDate.toEpochDay() - startDateOfPager.toEpochDay()
        return PAGER_INITIAL_OFFSET_DAYS + daysDifference
    }

    fun pageIndexToLocalDate(pageIndex: Int): LocalDate =
        getTodayMidnight().plusDays(pageIndex - PAGER_INITIAL_OFFSET_DAYS)

    private fun calendarModeChanged() {
        viewModelScope.launch {
            val newDisplayMode =
                if (_uiState.value.calendarDisplayMode == CalendarDisplayMode.WEEK)
                    CalendarDisplayMode.MONTH
                else CalendarDisplayMode.WEEK
            userPreferencesRepository.setCalendarModePreference(newDisplayMode)
            _uiState.update { _uiState.value.copy(calendarDisplayMode = newDisplayMode) }
        }
    }

    private fun contextMenuOptionSelected(event: UIEvent.ContextMenuOptionSelected) {
        when (event.option) {
            ContextMenuOption.DELETE -> if (_uiState.value.showDialogDelete) {
                _uiState.update { _uiState.value.copy(deleteDialogShowing = true) }
            } else {
                onEvent(ContextEventDelete(dontShowAgain = true))
            }

            ContextMenuOption.EDIT -> {
                _uiState.update { _uiState.value.copy(contextMenuOpen = false) }

                contextMenuEvent?.let {
                    viewModelScope.launch { _navEvent.send(EditEvent(it.id)) }
                } ?: Timber.e("ContextMenuEvent is null and cannot be edited")
            }
        }
    }

    private fun deleteEvent(event: ContextEventDelete) {
        _uiState.update {
            _uiState.value.copy(
                contextMenuOpen = false,
                deleteDialogShowing = false,
                showDialogDelete = !event.dontShowAgain
            )
        }

        viewModelScope.launch {
            contextMenuEvent?.let {
                eventsRepository.deleteEvent(it)
                contextMenuEvent = null
            } ?: Timber.e("ContextMenuEvent is null and cannot be deleted")
        }

        viewModelScope.launch {
            userPreferencesRepository.setShowDialogDeletePreference(!event.dontShowAgain)
        }
    }

    data class UIState(
        var contextMenuOpen: Boolean = false,
        var contextMenuName: String = "",
        var deleteDialogShowing: Boolean = false,
        var datePickerDialogShowing: Boolean = false,
        var showDialogDelete: Boolean = UserPreferencesRepository.SHOW_DIALOG_DEFAULT,
        var calendarDisplayMode: CalendarDisplayMode = CalendarDisplayMode.UNDEFINED
    )

    sealed class UIEvent {
        data class DateSelected(val date: LocalDate) : UIEvent()
        data class ContextMenuOpen(val event: Event) : UIEvent()
        data class DoneChanged(val event: Event, val checked: Boolean) : UIEvent()
        data class ContextEventDelete(val dontShowAgain: Boolean) : UIEvent()
        data class ContextMenuOptionSelected(val option: ContextMenuOption) : UIEvent()
        data class VisibleDatesChanged(val startDate: LocalDate, val endDate: LocalDate) : UIEvent()
        data class OnTaskDoneChanged(val task: Task, val checked: Boolean) : UIEvent()
        object SettingsClicked : UIEvent()
        object CalendarModeClicked : UIEvent()
        object ShowMonthYearPickerDialog : UIEvent()
        object DeleteDialogDismiss : UIEvent()
        object DatePickerDialogDismiss : UIEvent()
        object ContextMenuDismiss : UIEvent()
    }

    sealed class NavEvent {
        data class EditEvent(val eventId: Long) : NavEvent()
        object Settings : NavEvent()
    }

    enum class CalendarDisplayMode {
        WEEK,
        MONTH,
        UNDEFINED // To avoid animating a default one before we get data from tha data store
    }
}
