package com.kuba.calendarium.ui.screens.settings

import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        userPreferencesRepository = mockk()
        coEvery { userPreferencesRepository.getShowDialogDeletePreference() } returns flowOf(true)

        viewModel = SettingsViewModel(userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `user checks delete dialog confirmation switch - state changes`() = runTest {
        coJustRun { userPreferencesRepository.setShowDialogDeletePreference(any()) }

        viewModel.onEvent(UIEvent.DeleteDialogChanged(true))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showDeleteDialog).isTrue()

        viewModel.onEvent(UIEvent.DeleteDialogChanged(false))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showDeleteDialog).isFalse()
    }
}