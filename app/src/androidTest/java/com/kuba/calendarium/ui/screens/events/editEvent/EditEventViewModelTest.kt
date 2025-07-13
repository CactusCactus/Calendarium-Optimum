package com.kuba.calendarium.ui.screens.events.editEvent

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.ui.navigation.ARG_EVENT_ID
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel
import com.kuba.calendarium.ui.screens.event.editEvent.EditEventViewModel
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


@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class EditEventViewModelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

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
                val viewModel = ViewModelProvider(it)[EditEventViewModel::class.java]
                assertThat(viewModel).isNotNull()
                assertThat(viewModel.eventsRepository).isNotNull()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDoneEventUpdatingDataToDatabase() {
        val testEventId = "testDoneEventUpdatingDataToDatabase".hashCode().toLong()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            DummyHiltActivity::class.java
        ).apply {
            putExtra(ARG_EVENT_ID, testEventId)
        }

        ActivityScenario.launch<DummyHiltActivity>(intent).use { scenario ->
            scenario.onActivity {
                val viewModel = ViewModelProvider(it)[EditEventViewModel::class.java]

                runTest {
                    viewModel.eventsRepository.insertEvent(
                        Event(
                            id = testEventId,
                            title = "Test Title",
                            description = "Test Description",
                            date = LocalDate.now()
                        )
                    )
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.TitleChanged("New Title"))
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.DoneClicked)

                    advanceUntilIdle()

                    viewModel.eventsRepository.getEventById(testEventId).test {
                        val event = awaitItem()

                        assertThat(event).isNotNull()
                        assertThat(event?.event?.title).isEqualTo("New Title")

                        cancelAndConsumeRemainingEvents()
                    }
                }
            }
        }
    }
}
