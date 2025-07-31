package com.kuba.calendarium.ui.screens.events.addEvent

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.ui.screens.event.DateTimeMode
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.UIEvent
import com.kuba.calendarium.ui.screens.event.addEvent.AddEventViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class AddEventViewModelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: EventsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
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
                val viewModel = ViewModelProvider(it)[AddEventViewModel::class.java]
                assertThat(viewModel).isNotNull()
                assertThat(viewModel.eventsRepository).isNotNull()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDoneEventInsertingDataToDatabase() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[AddEventViewModel::class.java]
                val date = LocalDate.now()

                runTest {
                    viewModel.onEvent(UIEvent.TitleChanged("Test Title"))
                    viewModel.onEvent(UIEvent.DescriptionChanged("Test Description"))
                    viewModel.onEvent(UIEvent.DateSelected(date))

                    viewModel.onEvent(UIEvent.DoneClicked)
                    advanceUntilIdle()

                    viewModel.eventsRepository.getEventDetailedListForDate(date).test {
                        val events = awaitItem()
                        assertThat(events).hasSize(1)

                        val event1 = events.firstOrNull()

                        assertThat(event1).isNotNull()
                        assertThat(event1?.event?.title).isEqualTo("Test Title")
                        assertThat(event1?.event?.description).isEqualTo("Test Description")
                        assertThat(event1?.event?.date).isEqualTo(date)

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }

    @Test
    fun testAddingEndTimeSameAsStartTimeOnTheSameDate() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[AddEventViewModel::class.java]
                val date = LocalDate.now()
                val time = LocalTime.now()

                // Initial setup
                viewModel.onEvent(UIEvent.TitleChanged("Test Title"))
                viewModel.onEvent(UIEvent.DescriptionChanged("Test Description"))
                viewModel.onEvent(UIEvent.DateSelected(date))
                viewModel.onEvent(UIEvent.TimeSelected(time))

                // End date picked
                viewModel.onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
                viewModel.onEvent(UIEvent.DateSelected(date))

                // End time picked
                viewModel.onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
                viewModel.onEvent(UIEvent.TimeSelected(time))
                viewModel.onEvent(UIEvent.DoneClicked)

                runTest {
                    repository.getEventDetailedListForDate(date).test {
                        val event = awaitItem().firstOrNull()

                        assertThat(event).isNotNull()
                        assertThat(event?.event?.time).isNotNull()
                        assertThat(event?.event?.timeEnd).isNull()

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }
}
