package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.util.resetToMidnight
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
class CalendarViewModelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testViewModelInjection() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]
                assertThat(viewModel).isNotNull()
                assertThat(viewModel.eventsRepository).isNotNull()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testEventFlowChangeOnDateChange() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                val date1 = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time.time.resetToMidnight()

                val date2 = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 2)
                }.time.time.resetToMidnight()

                val event1 = Event(
                    id = 1,
                    title = "Test Event 1",
                    description = "Test Description 1",
                    date = date1
                )

                val event2 = Event(
                    id = 2,
                    title = "Test Event 2",
                    description = "Test Description 2",
                    date = date2.resetToMidnight()
                )

                runTest {
                    viewModel.eventsRepository.insertEvent(event1)
                    viewModel.eventsRepository.insertEvent(event2)
                    viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(date1))

                    viewModel._selectedDate.flatMapLatest {
                        viewModel.eventsRepository.getEventsForDate(it).catch { emit(emptyList()) }
                    }.test {
                        val events = awaitItem()
                        assertThat(events).hasSize(1)
                        assertThat(events.first()).isEqualTo(event1)

                        viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(date2))

                        val events2 = awaitItem()
                        assertThat(events2).hasSize(1)
                        assertThat(events2.first()).isEqualTo(event2)

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }

    @Test
    fun testContextMenuDeletion() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                val event = Event(
                    id = 1,
                    title = "Test Event",
                    description = "Test Description",
                    date = Calendar.getInstance().time.time.resetToMidnight()
                )

                runTest {
                    viewModel.eventsRepository.insertEvent(event)
                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextMenuOpen(event))

                    assertThat(viewModel.uiState.value.contextMenuOpen).isTrue()

                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextEventDelete)

                    viewModel.eventsRepository.getEventById(event.id).test {
                        val events = awaitItem()
                        assertThat(events).isNull()
                        cancelAndConsumeRemainingEvents()
                    }

                    assertThat(viewModel.uiState.value.contextMenuOpen).isFalse()
                }
            }
        }
    }
}
