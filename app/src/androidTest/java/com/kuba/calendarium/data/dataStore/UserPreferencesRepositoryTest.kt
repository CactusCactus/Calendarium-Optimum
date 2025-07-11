package com.kuba.calendarium.data.dataStore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        userPreferencesRepository = UserPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        runTest {
            userPreferencesRepository.clear()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun testShowDialogDeletePreference() = runTest {
        userPreferencesRepository.setShowDialogDeletePreference(true)
        userPreferencesRepository.getShowDialogDeletePreference().test {
            val result = awaitItem()
            assertThat(result).isTrue()

            cancelAndConsumeRemainingEvents()
        }

        userPreferencesRepository.setShowDialogDeletePreference(false)
        userPreferencesRepository.getShowDialogDeletePreference().test {
            val result = awaitItem()
            assertThat(result).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun testCalendarModePreference() = runTest {
        userPreferencesRepository.setCalendarModePreference(
            CalendarViewModel.CalendarDisplayMode.MONTH
        )
        userPreferencesRepository.getShowDialogDeletePreference().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(CalendarViewModel.CalendarDisplayMode.MONTH)

            cancelAndConsumeRemainingEvents()
        }

        userPreferencesRepository.setCalendarModePreference(
            CalendarViewModel.CalendarDisplayMode.WEEK
        )
        userPreferencesRepository.getShowDialogDeletePreference().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(CalendarViewModel.CalendarDisplayMode.WEEK)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun testCalendarModePreferenceDisallowSavingUndefined() = runTest {
        userPreferencesRepository.setCalendarModePreference(
            CalendarViewModel.CalendarDisplayMode.UNDEFINED
        )
        userPreferencesRepository.getShowDialogDeletePreference().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(UserPreferencesRepository.CALENDAR_MODE_DEFAULT)

            cancelAndConsumeRemainingEvents()
        }
    }
}
