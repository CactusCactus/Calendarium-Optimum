package com.kuba.calendarium.ui.screens.event.addEvent

import androidx.lifecycle.SavedStateHandle
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_EPOCH_DAY
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    eventsRepository: EventsRepository,
    savedStateHandle: SavedStateHandle
) : ModifyEventViewModel(eventsRepository, savedStateHandle) {
    override suspend fun databaseWriteOperation() {
        // Nullify endTime if it's exact same as time (on the same day)
        val endTime = if (_uiState.value.selectedTimeEnd == _uiState.value.selectedTime
            && _uiState.value.selectedDateEnd == _uiState.value.selectedDate
        ) null else _uiState.value.selectedTimeEnd

        val tasks = _uiState.value.taskList.map { Task(title = it.title) }

        eventsRepository.insertEventWithTasks(
            Event(
                title = _uiState.value.title,
                description = _uiState.value.description,
                date = _uiState.value.selectedDate,
                time = _uiState.value.selectedTime,
                dateEnd = _uiState.value.selectedDateEnd,
                timeEnd = endTime
            ),
            tasks
        )
    }

    override fun initUIState(): MutableStateFlow<UIState> {
        val epochDays = savedStateHandle.get<Long>(ARG_SELECTED_DATE_EPOCH_DAY)
            ?: LocalDate.now().toEpochDay()

        return MutableStateFlow(
            UIState(selectedDate = LocalDate.ofEpochDay(epochDays))
        )
    }
}
