package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.standardPadding

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToAddEvent: (selectedDate: Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onNavigateToAddEvent(viewModel.selectedDate.value)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add new event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val selectedDate = viewModel.selectedDate.collectAsState().value

            Calendar(
                date = selectedDate,
                onDateSelected = { viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(it)) }
            )

            StandardSpacer()

            val events = viewModel.eventList.collectAsState().value

            LazyColumn {
                items(events) {
                    EventRow(event = it, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: Event, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(standardPadding)) {
        Column(modifier = Modifier.padding(standardPadding)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)

            StandardHalfSpacer()

            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Calendar(
    date: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date,
        initialDisplayedMonthMillis = date
    )

    LaunchedEffect(date) {
        datePickerState.selectedDateMillis = date
        datePickerState.displayedMonthMillis = date
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { newSelectedMillis ->
            if (newSelectedMillis != date) {
                onDateSelected(newSelectedMillis)
            }
        }
    }

    DatePicker(
        state = datePickerState,
        modifier = modifier,
        title = null,
        colors = DatePickerDefaults.colors().copy(containerColor = Color.Transparent)
    )
}