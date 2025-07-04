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
import com.kuba.calendarium.di.TEST_PREFS_NAME
import com.kuba.calendarium.util.resetToMidnight
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
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class CalendarViewModelTest {
    private lateinit var testDispatcher: TestDispatcher

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var eventsRepository: EventsRepository

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

                    viewModel.selectedDate.flatMapLatest {
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

                    viewModel.onEvent(
                        CalendarViewModel.UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE)
                    )

                    assertThat(viewModel.uiState.value.deleteDialogShowing).isTrue()

                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextEventDelete(false))

                    viewModel.eventsRepository.getEventById(event.id).test {
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
                    date = Calendar.getInstance().time.time.resetToMidnight()
                )

                runTest {
                    viewModel.eventsRepository.insertEvent(event)
                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextMenuOpen(event))

                    assertThat(viewModel.uiState.value.contextMenuOpen).isTrue()

                    viewModel.onEvent(
                        CalendarViewModel.UIEvent.ContextMenuOptionSelected(ContextMenuOption.DELETE)
                    )

                    assertThat(viewModel.uiState.value.deleteDialogShowing).isTrue()

                    viewModel.onEvent(CalendarViewModel.UIEvent.DeleteDialogDismiss)

                    viewModel.eventsRepository.getEventById(event.id).test {
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

    @Test // FIXME: This test is not working
    fun testUserCheckDontShowAgainDeleteDialogAndDataSaved() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val testUserPrefs = UserPreferencesRepository(it, TEST_PREFS_NAME)
                val viewModel = ViewModelProvider(it)[CalendarViewModel::class.java]

                runTest {
                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextEventDelete(dontShowAgain = true))
                    advanceUntilIdle()

                    testUserPrefs.getShowDialogDeletePreference().test {
                        val item = awaitItem()

                        assertThat(item).isFalse()
                        cancelAndConsumeRemainingEvents()
                    }

                    viewModel.onEvent(CalendarViewModel.UIEvent.ContextEventDelete(dontShowAgain = false))
                    advanceUntilIdle()

                    testUserPrefs.getShowDialogDeletePreference().test {
                        val item = awaitItem()

                        assertThat(item).isTrue()
                        cancelAndConsumeRemainingEvents()
                    }

                }
            }
        }
    }
}