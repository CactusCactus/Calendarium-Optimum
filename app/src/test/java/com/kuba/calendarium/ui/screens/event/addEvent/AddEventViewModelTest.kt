package com.kuba.calendarium.ui.screens.event.addEvent

import androidx.lifecycle.SavedStateHandle
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_EPOCH_DAY
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddEventViewModelTest {
    private lateinit var mockEventsRepository: EventsRepository
    private lateinit var mockSavedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddEventViewModel

    @Before
    fun setUp() {
        mockEventsRepository = mockk()
        mockSavedStateHandle = SavedStateHandle().apply {
            // You can set any necessary data here
        }

        viewModel = AddEventViewModel(mockEventsRepository, mockSavedStateHandle)
    }

    @Test
    fun `Date is passed in the SavedStateHandle - state is updated`() {
        val date = LocalDate.of(1995, 2, 19)

        val mockSavedStateHandle = SavedStateHandle().apply {
            set(ARG_SELECTED_DATE_EPOCH_DAY, date.toEpochDay())
        }

        val viewModel = AddEventViewModel(mockEventsRepository, mockSavedStateHandle)
        assert(viewModel.uiState.value.selectedDate == date)
    }
}