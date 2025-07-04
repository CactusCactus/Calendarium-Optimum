package com.kuba.calendarium.ui.screens.event

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.event.addEvent.AddEventViewModel
import com.kuba.calendarium.util.resetToMidnight
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ModifyEventViewModelTest {
    private lateinit var mockEventsRepository: EventsRepository
    private lateinit var mockSavedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddEventViewModel

    @Before
    fun setUp() {
        mockEventsRepository = mockk()
        mockSavedStateHandle = SavedStateHandle().apply {
            // You can set any necessary data here
        }

        // AddEventViewModel is picked for implementation of the abstract class ModifyEventViewModel
        viewModel = AddEventViewModel(mockEventsRepository, mockSavedStateHandle)
    }

    @Test
    fun `Done button clicked - Finish event sent`() = runTest {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
        }.time

        coEvery { mockEventsRepository.insertEvent(any()) } returns 1
        // Set date of the Event (other fields are not important in this case)
        viewModel.onEvent(ModifyEventViewModel.UIEvent.DateSelected(date.time))

        viewModel.navEvent.test {
            viewModel.onEvent(ModifyEventViewModel.UIEvent.DoneClicked)

            val finishEvent = awaitItem()
            assert(finishEvent is ModifyEventViewModel.NavEvent.Finish)

            // Date is reset to midnight before being inserted into repository
            val expectedDate = date.time
            assert((finishEvent as ModifyEventViewModel.NavEvent.Finish).eventDate == expectedDate)

            // Should fire only once
            expectNoEvents()

            cancelAndConsumeRemainingEvents()
        }
    }

    // Form validation tests
    @Test
    fun `Initial state - title and description empty, errors null, form invalid`() {
        val uiState = viewModel.uiState.value

        assert(uiState.title.isEmpty())
        assert(uiState.description.isEmpty())
        assert(uiState.titleError == null)
        assert(uiState.descriptionError == null)

        // Initial empty title makes it invalid but keeps error null until user interaction
        assert(!uiState.isValid)

    }

    @Test
    fun `User enters only title - form is valid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.isValid.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TitleChanged("Test Title"))

        assert(viewModel.uiState.value.title == "Test Title")
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.isValid)
    }

    @Test
    fun `User enters only description - form is invalid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.DescriptionChanged("Test Description"))

        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description == "Test Description")
        // Error will be only set after user interacts with title TextField
        assert(viewModel.uiState.value.titleError == ModifyEventViewModel.ValidationError.TITLE_EMPTY)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User enters both title and description - form is valid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TitleChanged("Test Title"))
        viewModel.onEvent(ModifyEventViewModel.UIEvent.DescriptionChanged("Test Description"))

        assert(viewModel.uiState.value.title == "Test Title")
        assert(viewModel.uiState.value.description == "Test Description")
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid)
    }

    @Test
    fun `User enters too long title - form is invalid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.isValid.not())

        val longTitle = "a".repeat(ModifyEventViewModel.MAX_TITLE_LENGTH + 1)
        viewModel.onEvent(ModifyEventViewModel.UIEvent.TitleChanged(longTitle))

        assert(viewModel.uiState.value.title == longTitle)
        assert(viewModel.uiState.value.titleError == ModifyEventViewModel.ValidationError.TITLE_TOO_LONG)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User enters valid title but too long description - form is invalid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TitleChanged("Test Title"))
        val longDescription = "a".repeat(ModifyEventViewModel.MAX_DESCRIPTION_LENGTH + 1)
        viewModel.onEvent(ModifyEventViewModel.UIEvent.DescriptionChanged(longDescription))

        assert(viewModel.uiState.value.title == "Test Title")
        assert(viewModel.uiState.value.description == longDescription)
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == ModifyEventViewModel.ValidationError.DESCRIPTION_TOO_LONG)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User clicks date field - date picker opens, user dismissed it - date picker closes`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.DatePickerOpened)

        assert(viewModel.uiState.value.datePickerOpen)

        viewModel.onEvent(ModifyEventViewModel.UIEvent.DatePickerDismissed)

        assert(viewModel.uiState.value.datePickerOpen.not())
    }

    @Test
    fun `User selects date - date picker closes and changes displayed date`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.DatePickerOpened)

        assert(viewModel.uiState.value.datePickerOpen)

        val date = Date().time

        viewModel.onEvent(ModifyEventViewModel.UIEvent.DateSelected(date))
        viewModel.onEvent(ModifyEventViewModel.UIEvent.DatePickerDismissed)

        // Date is reset to midnight before being saved
        assert(viewModel.uiState.value.selectedDate == date)
        assert(viewModel.uiState.value.datePickerOpen.not())
    }

    @Test
    fun `User clicks time field - time picker opens, user dismissed it - time picker closes`() {
        assert(viewModel.uiState.value.timePickerOpen.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TimePickerOpened)

        assert(viewModel.uiState.value.timePickerOpen)

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TimePickerDismissed)

        assert(viewModel.uiState.value.timePickerOpen.not())
    }

    @Test
    fun `User selects time - time picker closes and changes displayed time`() {
        assert(viewModel.uiState.value.timePickerOpen.not())

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TimePickerOpened)

        assert(viewModel.uiState.value.timePickerOpen)

        val time = Date().time

        viewModel.onEvent(ModifyEventViewModel.UIEvent.TimeSelected(time))
        viewModel.onEvent(ModifyEventViewModel.UIEvent.TimePickerDismissed)

        // Date is reset to midnight before being saved
        assert(viewModel.uiState.value.selectedTime == time)
        assert(viewModel.uiState.value.timePickerOpen.not())
    }
}