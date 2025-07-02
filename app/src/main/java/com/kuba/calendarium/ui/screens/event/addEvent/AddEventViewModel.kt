package com.kuba.calendarium.ui.screens.event.addEvent

import androidx.lifecycle.SavedStateHandle
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel
import com.kuba.calendarium.util.getTodayMidnight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    eventsRepository: EventsRepository,
    savedStateHandle: SavedStateHandle
) : ModifyEventViewModel(eventsRepository, savedStateHandle) {
    override suspend fun databaseWriteOperation() {
        eventsRepository.insertEvent(
            Event(
                title = _uiState.value.title,
                description = _uiState.value.description,
                date = _uiState.value.selectedDate
            )
        )
    }

    override fun initUIState(): MutableStateFlow<UIState> = MutableStateFlow(
        UIState(
            selectedDate = savedStateHandle.get<Long>(ARG_SELECTED_DATE_MS) ?: getTodayMidnight()
        )
    )
}
