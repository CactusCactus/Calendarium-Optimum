package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import com.kuba.calendarium.util.resetToMidnight
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class AddEventViewModelTest {
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
                val viewModel = ViewModelProvider(it)[AddEventViewModel::class.java]
                assertThat(viewModel).isNotNull()
                assertThat(viewModel.eventsRepository).isNotNull()
            }
        }
    }

    @Test
    fun testDoneEventInsertingDataToDatabase() {
        ActivityScenario.launch(DummyHiltActivity::class.java).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[AddEventViewModel::class.java]
                val date = System.currentTimeMillis()

                runTest {
                    viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged("Test Title"))
                    viewModel.onEvent(AddEventViewModel.UIEvent.DescriptionChanged("Test Description"))
                    viewModel.onEvent(AddEventViewModel.UIEvent.DateSelected(date))

                    viewModel.onEvent(AddEventViewModel.UIEvent.DoneClicked)

                    viewModel.eventsRepository.getEventsForDate(date.resetToMidnight()).test {
                        val events = awaitItem()
                        assertThat(events).hasSize(1)

                        val event1 = events.firstOrNull()

                        assertThat(event1).isNotNull()
                        assertThat(event1?.title).isEqualTo("Test Title")
                        assertThat(event1?.description).isEqualTo("Test Description")
                        assertThat(event1?.date).isEqualTo(date.resetToMidnight())

                        expectNoEvents()
                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }
}
