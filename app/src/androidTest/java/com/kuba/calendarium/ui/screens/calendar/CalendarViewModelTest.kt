package com.kuba.calendarium.ui.screens.calendar

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class CalendarViewModelTest {
    private lateinit var testDispatcher: TestDispatcher

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var eventsRepository: EventsRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        hiltRule.inject()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelInjection() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]
                assertThat(viewModel).isNotNull()
                assertThat(eventsRepository).isNotNull()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testEventFlowChangeOnDateChange() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                val date1 = LocalDate.now().withDayOfMonth(1)

                val date2 = LocalDate.now().withDayOfMonth(2)

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
                    date = date2
                )

                runTest {
                    eventsRepository.insertEvent(event1)
                    eventsRepository.insertEvent(event2)
                    viewModel.onEvent(UIEvent.DateSelected(date1))
                    advanceUntilIdle()

                    viewModel.selectedDate.flatMapLatest {
                        eventsRepository.getEventsForDate(it).catch { emit(emptyList()) }
                    }.test {
                        val events = awaitItem()
                        assertThat(events).hasSize(1)
                        assertThat(events.first()).isEqualTo(event1)

                        viewModel.onEvent(UIEvent.DateSelected(date2))

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
                    date = LocalDate.now()
                )

                runTest {
                    eventsRepository.insertEvent(event)
                    viewModel.onEvent(UIEvent.ContextMenuOpen(event))

                    assertThat(viewModel.uiState.value.contextMenuOpen).isTrue()

                    viewModel.onEvent(
                        UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE)
                    )

                    assertThat(viewModel.uiState.value.deleteDialogShowing).isTrue()

                    viewModel.onEvent(UIEvent.ContextEventDelete(false))
                    advanceUntilIdle()

                    eventsRepository.getEventTasksById(event.id).test {
                        val events = awaitItem()
                        assertThat(events).isNull()

                        cancelAndConsumeRemainingEvents()
                    }

                    assertThat(viewModel.uiState.value.contextMenuOpen).isFalse()
                    assertThat(viewModel.uiState.value.deleteDialogShowing).isFalse()
                }
            }
        }
    }

    @Test
    fun testContextMenuDeleteAndCancelDialog() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                val event = Event(
                    id = 1,
                    title = "Test Event",
                    description = "Test Description",
                    date = LocalDate.now()
                )

                runTest {
                    eventsRepository.insertEvent(event)
                    viewModel.onEvent(UIEvent.ContextMenuOpen(event))

                    assertThat(viewModel.uiState.value.contextMenuOpen).isTrue()

                    viewModel.onEvent(
                        UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE)
                    )

                    assertThat(viewModel.uiState.value.deleteDialogShowing).isTrue()

                    viewModel.onEvent(UIEvent.DeleteDialogDismiss)

                    eventsRepository.getEventTasksById(event.id).test {
                        val events = awaitItem()
                        assertThat(events).isNotNull()
                        cancelAndConsumeRemainingEvents()
                    }

                    assertThat(viewModel.uiState.value.contextMenuOpen).isFalse()
                    assertThat(viewModel.uiState.value.deleteDialogShowing).isFalse()
                }
            }
        }
    }

    @Test
    fun testUserCheckDontShowAgainDeleteDialogAndDataSaved() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]
                val event = Event(
                    id = 1,
                    title = "Test Event",
                    description = "Test Description",
                    date = LocalDate.now()
                )

                runTest {
                    viewModel.onEvent(UIEvent.ContextMenuOpen(event))
                    viewModel.onEvent(UIEvent.ContextEventDelete(dontShowAgain = true))
                    advanceUntilIdle()

                    userPreferencesRepository.getShowDialogDeletePreference().test {
                        skipItems(1) // Skip initial value

                        assertThat(awaitItem()).isFalse() // Value is flipped in viewModel.onEvent

                        viewModel.onEvent(UIEvent.ContextMenuOpen(event))
                        viewModel.onEvent(UIEvent.ContextEventDelete(dontShowAgain = false))
                        advanceUntilIdle()

                        assertThat(awaitItem()).isTrue() // Value is flipped in viewModel.onEvent

                        viewModel.onEvent(UIEvent.ContextMenuOpen(event))
                        viewModel.onEvent(UIEvent.ContextEventDelete(dontShowAgain = false))
                        advanceUntilIdle()

                        expectNoEvents() // Flow should only emit when data changes

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }

    @Test
    fun testUserSettingEventAsDone() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]
                val eventId = "testUserSettingEventAsDone".hashCode().toLong()

                val event = Event(
                    id = eventId,
                    title = "Test Event",
                    description = "Test Description",
                    date = LocalDate.now()
                )

                runTest {
                    eventsRepository.insertEvent(event)
                    viewModel.onEvent(UIEvent.DoneChanged(event, true))
                    advanceUntilIdle()

                    eventsRepository.getEventTasksById(eventId).test {
                        val event = awaitItem()

                        assertThat(event?.event?.done).isTrue()
                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }

    @Test
    fun testEventCountChanges() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                val date = LocalDate.of(2025, 2, 19)
                val dateMonthBefore = date.minusMonths(1)

                val event = Event(
                    id = 1,
                    title = "Test Event",
                    description = "Test Description",
                    date = date
                )

                val eventBefore = Event(
                    id = 2,
                    title = "Test Event",
                    description = "Test Description",
                    date = dateMonthBefore
                )

                // Init date
                viewModel.onEvent(UIEvent.DateSelected(date.plusMonths(1)))

                runTest {
                    eventsRepository.insertEvent(event)
                    eventsRepository.insertEvent(eventBefore)

                    viewModel.eventCountMap.test {
                        skipItems(1) // Skip initial value

                        viewModel.onEvent(
                            UIEvent.VisibleDatesChanged(date.minusDays(15), date.plusDays(15))
                        )
                        assertThat(awaitItem()).containsExactly(date, 1)

                        viewModel.onEvent(
                            UIEvent.VisibleDatesChanged(
                                dateMonthBefore.minusDays(15),
                                dateMonthBefore.plusDays(15)
                            )
                        )
                        assertThat(awaitItem()).containsExactly(dateMonthBefore, 1)

                        viewModel.onEvent(
                            UIEvent.VisibleDatesChanged(date.minusDays(20), date.minusDays(10))
                        )

                        assertThat(awaitItem()).isEmpty()

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }
}