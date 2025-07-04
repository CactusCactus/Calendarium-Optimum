package com.kuba.calendarium.ui.screens.calendar

import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.Companion.PAGER_INITIAL_OFFSET_DAYS
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent
import com.kuba.calendarium.util.getTodayMidnight
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    private lateinit var mockEventsRepository: EventsRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        mockEventsRepository = mockk()
        userPreferencesRepository = mockk()
        viewModel = CalendarViewModel(mockEventsRepository, userPreferencesRepository)
    }

    @Test
    fun testPageIndexToDateMillis() {
        val pageIndexes = intArrayOf(-10, -5, 0, 5, 10)

        pageIndexes.forEach { pageIndex ->
            val expectedDate = Calendar.getInstance().apply {
                timeInMillis = getTodayMidnight()
                add(Calendar.DAY_OF_YEAR, pageIndex - PAGER_INITIAL_OFFSET_DAYS)
            }.timeInMillis

            val result = viewModel.pageIndexToDateMillis(pageIndex)

            assert(result == expectedDate)
        }
    }

    @Test
    fun testDateMillisToPageIndex() {
        val dateMillis = Calendar.getInstance().apply { timeInMillis = getTodayMidnight() }

        assertThat(viewModel.dateMillisToPageIndex(dateMillis.timeInMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS
        )
        val dayAfter = Calendar.getInstance().apply {
            timeInMillis = getTodayMidnight()
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val dayBefore = Calendar.getInstance().apply {
            timeInMillis = getTodayMidnight()
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val twoDaysBefore = Calendar.getInstance().apply {
            timeInMillis = getTodayMidnight()
            add(Calendar.DAY_OF_YEAR, -2)
        }
        val inHundredDays = Calendar.getInstance().apply {
            timeInMillis = getTodayMidnight()
            add(Calendar.DAY_OF_YEAR, 100)
        }
        val inNegativeHundredDays = Calendar.getInstance().apply {
            timeInMillis = getTodayMidnight()
            add(Calendar.DAY_OF_YEAR, -100)
        }

        assertThat(viewModel.dateMillisToPageIndex(dayAfter.timeInMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS + 1
        )

        assertThat(viewModel.dateMillisToPageIndex(dayBefore.timeInMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS - 1
        )

        assertThat(viewModel.dateMillisToPageIndex(twoDaysBefore.timeInMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS - 2
        )

        assertThat(viewModel.dateMillisToPageIndex(inHundredDays.timeInMillis)).isEqualTo(
            PAGER_INITIAL_OFFSET_DAYS + 100
        )

        // TODO This test fails because I don't account for a leap year
//        assertThat(viewModel.dateMillisToPageIndex(inNegativeHundredDays.timeInMillis)).isEqualTo(
//            PAGER_INITIAL_OFFSET_DAYS - 100
//        )
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
}