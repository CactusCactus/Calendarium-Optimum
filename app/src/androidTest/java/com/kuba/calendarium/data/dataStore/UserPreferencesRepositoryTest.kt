package com.kuba.calendarium.data.dataStore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        userPreferencesRepository = UserPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        runTest {
            userPreferencesRepository.clear()
        }
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
}