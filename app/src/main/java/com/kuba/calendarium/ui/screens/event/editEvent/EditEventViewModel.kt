package com.kuba.calendarium.ui.screens.event.editEvent

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_EVENT_ID
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel
import com.kuba.calendarium.util.getTodayMidnight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class EditEventViewModel @Inject constructor(
    eventsRepository: EventsRepository,
    savedStateHandle: SavedStateHandle
) : ModifyEventViewModel(eventsRepository, savedStateHandle) {
    private val eventId: Long = savedStateHandle.get<Long>(ARG_EVENT_ID) ?: -1

    init {
        if (eventId == -1L) {
            Timber.e("Event ID is invalid")

            viewModelScope.launch { _navEvent.send(NavEvent.Finish(getTodayMidnight())) }
        } else {
            viewModelScope.launch {
                eventsRepository.getEventById(eventId).filterNotNull().collect { et ->
                    _uiState.update {
                        _uiState.value.copy(
                            title = et.event.title,
                            description = et.event.description,
                            selectedDate = et.event.date,
                            selectedTime = et.event.time,
                            selectedDateEnd = et.event.dateEnd,
                            selectedTimeEnd = et.event.timeEnd,
                            taskList = et.tasks.map { it.toTaskInternal() }.toMutableStateList()
                        )
                    }

                    checkAndUpdateValidity()
                }
            }
        }
    }

    override suspend fun databaseWriteOperation() {
        // Nullify endTime if it's exact same as time (on the same day)
        val endTime = if (_uiState.value.selectedTimeEnd == _uiState.value.selectedTime
            && _uiState.value.selectedDateEnd == _uiState.value.selectedDate
        ) null else _uiState.value.selectedTimeEnd

        eventsRepository.updateEventWithTasks(
            Event(
                id = eventId,
                title = _uiState.value.title,
                description = _uiState.value.description,
                date = _uiState.value.selectedDate,
                time = _uiState.value.selectedTime,
                dateEnd = _uiState.value.selectedDateEnd,
                timeEnd = endTime
            ),
            _uiState.value.taskList.mapIndexed { index, it ->
                it.toTask(index)
            }
        )
    }

    override fun initUIState(): MutableStateFlow<UIState> = MutableStateFlow(UIState())
}