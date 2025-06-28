package com.kuba.calendarium.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    val uiState = _uiState.asStateFlow()

    data class UiState(
        val text: String = "ViewModel initialized"
    )
}