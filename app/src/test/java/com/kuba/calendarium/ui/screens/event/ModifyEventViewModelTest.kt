package com.kuba.calendarium.ui.screens.event

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.UIEvent
import com.kuba.calendarium.ui.screens.event.addEvent.AddEventViewModel
import com.kuba.calendarium.util.resetToMidnight
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
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

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Done button clicked - Finish event sent`() = runTest {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
        }.time

        coEvery { mockEventsRepository.insertEvent(any()) } returns 1
        // Set date of the Event (other fields are not important in this case)
        viewModel.onEvent(UIEvent.DateSelected(date.time))

        viewModel.navEvent.test {
            viewModel.onEvent(UIEvent.DoneClicked)

            val finishEvent = awaitItem()
            assert(finishEvent is ModifyEventViewModel.NavEvent.Finish)

            // Date is reset to midnight before being inserted into repository
            val expectedDate = date.time
            assert((finishEvent as ModifyEventViewModel.NavEvent.Finish).eventDate == expectedDate.resetToMidnight())

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

        viewModel.onEvent(UIEvent.TitleChanged("Test Title"))

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

        viewModel.onEvent(UIEvent.DescriptionChanged("Test Description"))

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

        viewModel.onEvent(UIEvent.TitleChanged("Test Title"))
        viewModel.onEvent(UIEvent.DescriptionChanged("Test Description"))

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
        viewModel.onEvent(UIEvent.TitleChanged(longTitle))

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

        viewModel.onEvent(UIEvent.TitleChanged("Test Title"))
        val longDescription = "a".repeat(ModifyEventViewModel.MAX_DESCRIPTION_LENGTH + 1)
        viewModel.onEvent(UIEvent.DescriptionChanged(longDescription))

        assert(viewModel.uiState.value.title == "Test Title")
        assert(viewModel.uiState.value.description == longDescription)
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == ModifyEventViewModel.ValidationError.DESCRIPTION_TOO_LONG)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User clicks date field - date picker opens, user dismissed it - date picker closes`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))

        assert(viewModel.uiState.value.datePickerOpen)

        viewModel.onEvent(UIEvent.DatePickerDismissed)

        assert(viewModel.uiState.value.datePickerOpen.not())
    }

    @Test
    fun `User selects date - date picker closes and changes displayed date`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))

        assert(viewModel.uiState.value.datePickerOpen)

        val date = Date().time

        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerDismissed)

        // Date is reset to midnight before being saved
        assert(viewModel.uiState.value.selectedDate == date.resetToMidnight())
        assert(viewModel.uiState.value.datePickerOpen.not())
    }

    @Test
    fun `User clicks time field - time picker opens, user dismissed it - time picker closes`() {
        assert(viewModel.uiState.value.timePickerOpen.not())

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))

        assert(viewModel.uiState.value.timePickerOpen)

        viewModel.onEvent(UIEvent.TimePickerDismissed)

        assert(viewModel.uiState.value.timePickerOpen.not())
    }

    @Test
    fun `User selects time - time picker closes and changes displayed time`() {
        assert(viewModel.uiState.value.timePickerOpen.not())

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))

        assert(viewModel.uiState.value.timePickerOpen)

        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
            set(Calendar.YEAR, 0)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModel.onEvent(UIEvent.TimeSelected(time))
        viewModel.onEvent(UIEvent.TimePickerDismissed)

        // date timestamp should be added to the timestamp before being saved
        assert(viewModel.uiState.value.selectedTime == time + viewModel.uiState.value.selectedDate)
        assert(viewModel.uiState.value.timePickerOpen.not())
    }

    // End date tests
    @Test
    fun `User adds end date - date picker is displayed and changes displayed end date`() {
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))

        assertThat(viewModel.uiState.value.datePickerOpen).isTrue()
        assertThat(viewModel.uiState.value.currentDateTimeMode).isEqualTo(DateTimeMode.TO)

        val date = System.currentTimeMillis()
        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerDismissed)

        assertThat(viewModel.uiState.value.selectedDateEnd).isEqualTo(date.resetToMidnight())
        assertThat(viewModel.uiState.value.datePickerOpen).isFalse()
    }

    @Test
    fun `User opens end date picker and dismissed it - date is not set`() {
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))

        assertThat(viewModel.uiState.value.datePickerOpen).isTrue()
        assertThat(viewModel.uiState.value.currentDateTimeMode).isEqualTo(DateTimeMode.TO)

        viewModel.onEvent(UIEvent.DatePickerDismissed)

        assertThat(viewModel.uiState.value.datePickerOpen).isFalse()
        assertThat(viewModel.uiState.value.selectedDateEnd).isNull()
    }

    @Test
    fun `User sets end date and removes it - date is not set`() {
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))

        assertThat(viewModel.uiState.value.datePickerOpen).isTrue()
        assertThat(viewModel.uiState.value.currentDateTimeMode).isEqualTo(DateTimeMode.TO)

        val date = System.currentTimeMillis()
        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerDismissed)

        assertThat(viewModel.uiState.value.selectedDateEnd).isEqualTo(date.resetToMidnight())
        assertThat(viewModel.uiState.value.datePickerOpen).isFalse()

        viewModel.onEvent(UIEvent.ClearDateAndTime(DateTimeMode.TO))
        assertThat(viewModel.uiState.value.selectedDateEnd).isNull()
    }

    @Test
    fun `User picks end date before start date - start date is set to end date`() {
        val date = System.currentTimeMillis()
        val dateBefore = Calendar.getInstance().apply {
            timeInMillis = date
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.DateSelected(dateBefore))

        assertThat(viewModel.uiState.value.selectedDate).isEqualTo(dateBefore.resetToMidnight())
        assertThat(viewModel.uiState.value.selectedDateEnd).isEqualTo(dateBefore.resetToMidnight())
    }

    @Test
    fun `User pics start date after end date - end date is set ot start date`() {
        val date = System.currentTimeMillis()
        val dateAfter = Calendar.getInstance().apply {
            timeInMillis = date
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        // Initial date
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(dateAfter))

        assertThat(viewModel.uiState.value.selectedDate).isEqualTo(dateAfter.resetToMidnight())
        assertThat(viewModel.uiState.value.selectedDateEnd).isEqualTo(dateAfter.resetToMidnight())
    }

    @Test
    fun `Both dates are set, user sets time - time is set for both values`() {
        val date = System.currentTimeMillis().resetToMidnight()

        val time = getTestTime()

        // Initial date
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerDismissed)
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.DateSelected(date))
        viewModel.onEvent(UIEvent.DatePickerDismissed)

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.TimeSelected(time))
        viewModel.onEvent(UIEvent.TimePickerDismissed)

        assertThat(viewModel.uiState.value.datePickerOpen).isFalse()
        assertThat(viewModel.uiState.value.timePickerOpen).isFalse()
        assertThat(viewModel.uiState.value.selectedTime).isEqualTo(time + date)
        assertThat(viewModel.uiState.value.selectedTimeEnd).isEqualTo(time + date)
    }

    @Test
    fun `User picks end time before start time - start time is set to end time`() {
        val date = System.currentTimeMillis().resetToMidnight()
        val time = getTestTime()
        val timeBefore = Calendar.getInstance().apply {
            timeInMillis = time
            add(Calendar.HOUR_OF_DAY, -1)
        }.timeInMillis

        // Initial time and date
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.TimeSelected(time))
        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.TimeSelected(time))

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.TimeSelected(timeBefore))

        assertThat(viewModel.uiState.value.selectedTime).isEqualTo(timeBefore + date)
        assertThat(viewModel.uiState.value.selectedTimeEnd).isEqualTo(timeBefore + date)
    }

    @Test
    fun `User picks start time after end time - end time is set to start time`() {
        val date = System.currentTimeMillis().resetToMidnight()
        val time = getTestTime()
        val timeAfter = Calendar.getInstance().apply {
            timeInMillis = time
            add(Calendar.HOUR_OF_DAY, 1)
        }.timeInMillis

        // Initial time and date
        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.DateSelected(date))

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.TimeSelected(time))
        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
        viewModel.onEvent(UIEvent.TimeSelected(time))

        viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
        viewModel.onEvent(UIEvent.TimeSelected(timeAfter))

        assertThat(viewModel.uiState.value.selectedTime).isEqualTo(timeAfter + date)
        assertThat(viewModel.uiState.value.selectedTimeEnd).isEqualTo(timeAfter + date)
    }

    private fun getTestTime() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 21)
        set(Calendar.MINUTE, 37)
        set(Calendar.SECOND, 13)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.YEAR, 0)
        set(Calendar.MONTH, 0)
        set(Calendar.DAY_OF_MONTH, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}