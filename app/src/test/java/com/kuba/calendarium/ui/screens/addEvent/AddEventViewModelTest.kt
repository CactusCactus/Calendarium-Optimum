package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import com.kuba.calendarium.ui.screens.addEvent.AddEventViewModel.ValidationError
import com.kuba.calendarium.util.resetToMidnight
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Calendar
import java.util.Date

class AddEventViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

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

    @Test
    fun `Done button clicked - Finish event sent`() = runTest {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
        }.time

        coEvery { mockEventsRepository.insertEvent(any()) } returns 1
        // Set date of the Event (other fields are not important in this case)
        viewModel.onEvent(AddEventViewModel.UIEvent.DateSelected(date.time))

        viewModel.navEvent.test {
            viewModel.onEvent(AddEventViewModel.UIEvent.DoneClicked)

            val finishEvent = awaitItem()
            assert(finishEvent is AddEventViewModel.NavEvent.Finish)

            // Date is reset to midnight before being inserted into repository
            val expectedDate = date.time.resetToMidnight()
            assert((finishEvent as AddEventViewModel.NavEvent.Finish).eventDate == expectedDate)

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

        viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged("Test Title"))

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

        viewModel.onEvent(AddEventViewModel.UIEvent.DescriptionChanged("Test Description"))

        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description == "Test Description")
        // Error will be only set after user interacts with title TextField
        assert(viewModel.uiState.value.titleError == ValidationError.TITLE_EMPTY)
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

        viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged("Test Title"))
        viewModel.onEvent(AddEventViewModel.UIEvent.DescriptionChanged("Test Description"))

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

        val longTitle = "a".repeat(AddEventViewModel.MAX_TITLE_LENGTH + 1)
        viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged(longTitle))

        assert(viewModel.uiState.value.title == longTitle)
        assert(viewModel.uiState.value.titleError == ValidationError.TITLE_TOO_LONG)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User enters valid title but too long description - form is invalid`() {
        assert(viewModel.uiState.value.title.isEmpty())
        assert(viewModel.uiState.value.description.isEmpty())
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == null)
        assert(viewModel.uiState.value.isValid.not())

        viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged("Test Title"))
        val longDescription = "a".repeat(AddEventViewModel.MAX_DESCRIPTION_LENGTH + 1)
        viewModel.onEvent(AddEventViewModel.UIEvent.DescriptionChanged(longDescription))

        assert(viewModel.uiState.value.title == "Test Title")
        assert(viewModel.uiState.value.description == longDescription)
        assert(viewModel.uiState.value.titleError == null)
        assert(viewModel.uiState.value.descriptionError == ValidationError.DESCRIPTION_TOO_LONG)
        assert(viewModel.uiState.value.isValid.not())
    }

    @Test
    fun `User clicks date field - date picker opens, user dismissed it - date picker closes`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerOpened)

        assert(viewModel.uiState.value.datePickerOpen)

        viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerDismissed)

        assert(viewModel.uiState.value.datePickerOpen.not())
    }

    @Test
    fun `User selects date - date picker closes and changes displayed date`() {
        assert(viewModel.uiState.value.datePickerOpen.not())

        viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerOpened)

        assert(viewModel.uiState.value.datePickerOpen)

        viewModel.onEvent(AddEventViewModel.UIEvent.DateSelected(Date().time))
        viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerDismissed)

        // Date is reset to midnight before being saved
        assert(viewModel.uiState.value.selectedDate == Date().time.resetToMidnight())
        assert(viewModel.uiState.value.datePickerOpen.not())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)

        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)

        Dispatchers.resetMain()
    }
}