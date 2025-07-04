package com.kuba.calendarium.ui.screens.event.addEvent

import androidx.lifecycle.SavedStateHandle
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.util.Calendar

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
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
        }.time

        val mockSavedStateHandle = SavedStateHandle().apply {
            set(ARG_SELECTED_DATE_MS, date.time)
        }

        val viewModel = AddEventViewModel(mockEventsRepository, mockSavedStateHandle)
        assert(viewModel.uiState.value.selectedDate == date.time)
    }
}