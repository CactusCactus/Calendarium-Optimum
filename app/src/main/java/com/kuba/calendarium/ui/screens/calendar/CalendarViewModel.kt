package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import com.kuba.calendarium.data.model.Event
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    internal val eventsRepository: EventsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayMidnight())

    private val _navEvent = Channel<NavEvent>()

    val navEvent = _navEvent.receiveAsFlow()

    val selectedDate = _selectedDate.asStateFlow()

    private var contextMenuEvent: Event? = null

    companion object {
        const val PAGER_VIRTUAL_PAGE_COUNT = 365 * 10 // ~10 years
        const val PAGER_INITIAL_OFFSET_DAYS = PAGER_VIRTUAL_PAGE_COUNT / 2
    }

    init {
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

    fun getEventsForDate(date: Long): Flow<List<Event>> = eventsRepository.getEventsForDate(date)

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.DateSelected -> _selectedDate.update { event.date }
            is UIEvent.ContextMenuOpen -> {
                _uiState.update {
                    _uiState.value.copy(contextMenuOpen = true, contextMenuName = event.event.title)
                }
                contextMenuEvent = event.event
            }

            is UIEvent.ContextMenuDismiss -> _uiState.update {
                _uiState.value.copy(contextMenuOpen = false)
            }

            is UIEvent.ContextMenuOptionSelected -> when (event.option) {
                ContextMenuOption.DELETE -> if (_uiState.value.showDialogDelete) {
                    _uiState.update { _uiState.value.copy(deleteDialogShowing = true) }
                } else {
                    onEvent(ContextEventDelete(!_uiState.value.showDialogDelete))
                }

                ContextMenuOption.EDIT -> {
                    _uiState.update { _uiState.value.copy(contextMenuOpen = false) }

                    contextMenuEvent?.let {
                        viewModelScope.launch { _navEvent.send(EditEvent(it.id)) }
                    } ?: Timber.e("ContextMenuEvent is null and cannot be edited")
                }
            }

            UIEvent.DeleteDialogDismiss -> _uiState.update {
                _uiState.value.copy(contextMenuOpen = false, deleteDialogShowing = false)
            }

            is ContextEventDelete -> {
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

                    userPreferencesRepository
                        .setShowDialogDeletePreference(!event.dontShowAgain)
                }
            }

            UIEvent.SettingsClicked -> viewModelScope.launch { _navEvent.send(NavEvent.Settings) }
            is UIEvent.DoneChanged -> viewModelScope.launch {
                eventsRepository.updateEvent(event.event.copy(done = event.checked))
            }

            UIEvent.CalendarModeClicked -> viewModelScope.launch {
                val newDisplayMode =
                    if (_uiState.value.calendarDisplayMode == CalendarDisplayMode.WEEK)
                        CalendarDisplayMode.MONTH
                    else CalendarDisplayMode.WEEK
                userPreferencesRepository.setCalendarModePreference(newDisplayMode)
                _uiState.update { _uiState.value.copy(calendarDisplayMode = newDisplayMode) }
            }
        }
    }

    fun pageIndexToDateMillis(pageIndex: Int): Long = Calendar.getInstance().apply {
        timeInMillis = getTodayMidnight()
        add(Calendar.DAY_OF_YEAR, pageIndex - PAGER_INITIAL_OFFSET_DAYS)
    }.timeInMillis

    fun dateMillisToPageIndex(dateMillis: Long): Int {
        val startDateOfPager = getTodayMidnight()

        val daysDifference = TimeUnit.MILLISECONDS.toDays(dateMillis - startDateOfPager)
        return PAGER_INITIAL_OFFSET_DAYS + daysDifference.toInt()
    }

    data class UIState(
        var contextMenuOpen: Boolean = false,
        var contextMenuName: String = "",
        var deleteDialogShowing: Boolean = false,
        var showDialogDelete: Boolean = UserPreferencesRepository.SHOW_DIALOG_DEFAULT,
        var calendarDisplayMode: CalendarDisplayMode = CalendarDisplayMode.UNDEFINED
    )

    sealed class UIEvent {
        data class DateSelected(val date: Long) : UIEvent()
        data class ContextMenuOpen(val event: Event) : UIEvent()
        data class DoneChanged(val event: Event, val checked: Boolean) : UIEvent()
        object ContextMenuDismiss : UIEvent()
        data class ContextEventDelete(val dontShowAgain: Boolean) : UIEvent()
        data class ContextMenuOptionSelected(val option: ContextMenuOption) : UIEvent()
        object DeleteDialogDismiss : UIEvent()
        object SettingsClicked : UIEvent()
        object CalendarModeClicked : UIEvent()
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