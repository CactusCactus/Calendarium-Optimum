package com.kuba.calendarium.ui.screens.calendar

import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.Companion.PAGER_INITIAL_OFFSET_DAYS
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    private lateinit var mockEventsRepository: EventsRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        mockEventsRepository = mockk()
        userPreferencesRepository = mockk(relaxed = true)

        viewModel = CalendarViewModel(mockEventsRepository, userPreferencesRepository)
    }

    @Test
    fun testPageIndexToDateMillis() {
        val pageIndexes = intArrayOf(-10, -5, 0, 5, 10)

        pageIndexes.forEach { pageIndex ->
            val expectedDate = LocalDate.now().plusDays(pageIndex - PAGER_INITIAL_OFFSET_DAYS)

            val result = viewModel.pageIndexToLocalDate(pageIndex)

            assert(result == expectedDate)
        }
    }

    @Test
    fun testDateMillisToPageIndex() {
        val dateMillis = LocalDate.now()

        assertThat(viewModel.localDateToPageIndex(dateMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS
        )
        val dayAfter = LocalDate.now().plusDays(1)
        val dayBefore = LocalDate.now().minusDays(1)
        val twoDaysBefore = LocalDate.now().minusDays(2)
        val inTwentyDays = LocalDate.now().plusDays(20)

        assertThat(viewModel.localDateToPageIndex(dayAfter)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS + 1
        )

        assertThat(viewModel.localDateToPageIndex(dayBefore)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS - 1
        )

        assertThat(viewModel.localDateToPageIndex(twoDaysBefore)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS - 2
        )

        assertThat(viewModel.localDateToPageIndex(inTwentyDays)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS + 20
        )
    }

    @Test
    fun testDeleteDialogShowing() = runTest {
        viewModel.onEvent(UIEvent.ContextEventDelete(false))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showDialogDelete).isTrue()

        viewModel.onEvent(UIEvent.ContextEventDelete(true))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showDialogDelete).isFalse()
    }

    @Test
    fun testDeleteDialogNotShowingWhenUserPrefersNotTo() = runTest {
        // Other event deleted with checkbox set
        viewModel.onEvent(UIEvent.ContextEventDelete(true))
        advanceUntilIdle()

        // User clicks delete
        viewModel.onEvent(UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deleteDialogShowing).isFalse()
    }

    @Test
    fun testDeleteDialogShowingWhenUserPrefersItTo() = runTest {
        // Other event deleted without checkbox set
        viewModel.onEvent(UIEvent.ContextEventDelete(false))
        advanceUntilIdle()

        // User clicks delete
        viewModel.onEvent(UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE))

        advanceUntilIdle()
        assertThat(viewModel.uiState.value.deleteDialogShowing).isTrue()
    }

    @Test
    fun testMonthYearDialogShowing() {
        viewModel.onEvent(UIEvent.ShowMonthYearPickerDialog)
        assertThat(viewModel.uiState.value.datePickerDialogShowing).isTrue()

        viewModel.onEvent(UIEvent.DatePickerDialogDismiss)
        assertThat(viewModel.uiState.value.datePickerDialogShowing).isFalse()
    }
}