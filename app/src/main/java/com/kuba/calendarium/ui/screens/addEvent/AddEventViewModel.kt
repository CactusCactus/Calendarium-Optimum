package com.kuba.calendarium.ui.screens.addEvent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kuba.calendarium.ui.navigation.ARG_SELECTED_DATE_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        UIState(
            selectedDate = savedStateHandle.get<Long>(ARG_SELECTED_DATE_MS)?.let {
                Date(it)
            } ?: Date()
        ))

    val uiState = _uiState.asStateFlow()

    data class UIState(
        val title: String = "",
        val description: String = "",
        val selectedDate: Date = Date()
    )
}