package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.ui.common.ConfirmDialog
import com.kuba.calendarium.ui.common.ContextMenuBottomSheet
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
                    EventRow(
                        event = it,
                        onLongClick = {
                            viewModel.onEvent(CalendarViewModel.UIEvent.ContextMenuOpen(it))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        ShowDialogsAndBottomSheets(viewModel)
    }
}

@Composable
private fun ShowDialogsAndBottomSheets(viewModel: CalendarViewModel) {
    val uiState = viewModel.uiState.collectAsState().value

    if (uiState.contextMenuOpen) {
        ContextMenuBottomSheet(
            title = uiState.contextMenuName,
            onDismissRequest = {
                viewModel.onEvent(CalendarViewModel.UIEvent.ContextMenuDismiss)
            },
            onOptionClick = {
                viewModel.onEvent(CalendarViewModel.UIEvent.ContextMenuOptionSelected(it))
            }
        )
    }

    if (uiState.deleteDialogShowing) {
        ShowDeleteDialog(
            onConfirm = { viewModel.onEvent(CalendarViewModel.UIEvent.ContextEventDelete) },
            onDismiss = { viewModel.onEvent(CalendarViewModel.UIEvent.DeleteDialogDismiss) }
        )
    }
}

@Composable
private fun ShowDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        title = stringResource(R.string.delete_dialog_title),
        text = stringResource(R.string.delete_dialog_text),
        icon = R.drawable.ic_delete_24
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventRow(event: Event, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(standardPadding)
            .combinedClickable(
                onClick = { /* NO-OP */ },
                onLongClick = onLongClick
            )
    ) {
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