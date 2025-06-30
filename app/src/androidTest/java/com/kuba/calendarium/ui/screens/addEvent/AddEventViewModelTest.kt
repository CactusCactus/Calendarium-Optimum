package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.DummyHiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
}
