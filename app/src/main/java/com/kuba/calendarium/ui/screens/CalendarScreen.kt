package com.kuba.calendarium.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.util.Date

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Calendar(
            date = viewModel.uiState.collectAsState().value.selectedDate,
            onDateSelected = { viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Calendar(
    date: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.time
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .filterNotNull()
            .distinctUntilChanged()
            .map { Date(it) }
            .collect(onDateSelected)
    }

    DatePicker(
        state = datePickerState,
        modifier = modifier,
        title = { }
    )
}