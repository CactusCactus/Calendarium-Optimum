package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.util.getTodayMidnight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    internal val eventsRepository: EventsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())

    val uiState = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayMidnight())

    val selectedDate = _selectedDate.asStateFlow()

    private var contextMenuEvent: Event? = null

//    val eventList = _selectedDate.flatMapLatest {
//        eventsRepository.getEventsForDate(it).catch { emit(emptyList()) }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    companion object {
        const val PAGER_VIRTUAL_PAGE_COUNT = 365 * 10 // ~10 years
        const val PAGER_INITIAL_OFFSET_DAYS = PAGER_VIRTUAL_PAGE_COUNT / 2
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
                ContextMenuOption.DELETE -> _uiState.update {
                    _uiState.value.copy(deleteDialogShowing = true)
                }
            }

            UIEvent.DeleteDialogDismiss -> _uiState.update {
                _uiState.value.copy(contextMenuOpen = false, deleteDialogShowing = false)
            }

            UIEvent.ContextEventDelete -> viewModelScope.launch {
                _uiState.update {
                    _uiState.value.copy(contextMenuOpen = false, deleteDialogShowing = false)
                }

                contextMenuEvent?.let {
                    eventsRepository.deleteEvent(it)
                    contextMenuEvent = null
                } ?: run {
                    Timber.e("ContextMenuEvent is null and cannot be deleted")
                }
            }

        }
    }

    fun pageIndexToDateMillis(pageIndex: Int): Long {
        val calendar = Calendar.getInstance()
        // Assume selectedDate is the date for the initial "center" page
        // Or pick a fixed epoch/start date for page 0 if preferred.
        // For this example, let's use the current selectedDate as the reference for PAGER_INITIAL_OFFSET_DAYS
        calendar.timeInMillis = getTodayMidnight()
        calendar.add(Calendar.DAY_OF_YEAR, pageIndex - PAGER_INITIAL_OFFSET_DAYS)
        return calendar.timeInMillis
    }

    data class UIState(
        var contextMenuOpen: Boolean = false,
        var contextMenuName: String = "",
        var deleteDialogShowing: Boolean = false
    )

    sealed class UIEvent {
        data class DateSelected(val date: Long) : UIEvent()
        data class ContextMenuOpen(val event: Event) : UIEvent()
        object ContextMenuDismiss : UIEvent()
        object ContextEventDelete : UIEvent()
        data class ContextMenuOptionSelected(val option: ContextMenuOption) : UIEvent()
        object DeleteDialogDismiss : UIEvent()
    }
}