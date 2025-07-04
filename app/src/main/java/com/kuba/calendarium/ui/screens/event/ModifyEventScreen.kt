package com.kuba.calendarium.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.common.DatePickerModal
import com.kuba.calendarium.ui.common.StandardDoubleSpacer
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.TimePickerModal
import com.kuba.calendarium.ui.common.descriptionMinHeight
import com.kuba.calendarium.ui.common.fabContentPadding
import com.kuba.calendarium.ui.common.fabSize
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.common.textFieldClickable
import com.kuba.calendarium.util.standardDateFormat
import com.kuba.calendarium.util.standardTimeFormat
import com.kuba.calendarium.util.toLocalizedString
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyEventScreen(
    title: String,
    viewModel: ModifyEventViewModel,
    onNavigateUp: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.navEvent.collectLatest {
            when (it) {
                is ModifyEventViewModel.NavEvent.Finish -> onNavigateUp(it.eventDate)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium
                )
            })
        },
        floatingActionButton = {
            // FAB doesn't have enable function, so it's replaced by a button styled to look like one
            Button(
                onClick = { viewModel.onEvent(ModifyEventViewModel.UIEvent.DoneClicked) },
                modifier = Modifier.size(width = fabSize, height = fabSize),
                contentPadding = PaddingValues(fabContentPadding),
                enabled = viewModel.uiState.collectAsState().value.isValid,
                shape = FloatingActionButtonDefaults.shape
            ) {
                Icon(Icons.Filled.Done, "Done")
            }
        }
    ) { innerPadding ->
        MainColumn(
            viewModel.uiState.collectAsState().value,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(innerPadding)
        )

        if (viewModel.uiState.collectAsState().value.datePickerOpen) {
            DatePickerModal(
                initialDate = viewModel.uiState.collectAsState().value.selectedDate,
                onDatePicked = {
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.DateSelected(it))
                },
                onDismissRequest = {
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.DatePickerDismissed)
                }
            )
        }

        val time =
            viewModel.uiState.collectAsState().value.selectedTime ?: System.currentTimeMillis()

        if (viewModel.uiState.collectAsState().value.timePickerOpen) {
            TimePickerModal(
                initialTime = time,
                onTimePicked = {
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.TimeSelected(it))
                },
                onDismissRequest = {
                    viewModel.onEvent(ModifyEventViewModel.UIEvent.TimePickerDismissed)
                }
            )
        }
    }
}

@Composable
private fun MainColumn(
    uiState: ModifyEventViewModel.UIState,
    onEvent: (ModifyEventViewModel.UIEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(standardPadding)
    ) {
        Text(stringResource(R.string.add_event_information_label))

        StandardSpacer()

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { onEvent(ModifyEventViewModel.UIEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.input_title_placeholder)) },
            isError = uiState.titleError != null,
            supportingText = {
                uiState.titleError?.let {
                    Text(
                        text = it.toLocalizedString(LocalContext.current),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        StandardSpacer()

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(ModifyEventViewModel.UIEvent.DescriptionChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = descriptionMinHeight),
            placeholder = { Text(stringResource(R.string.input_description_placeholder)) },
            isError = uiState.descriptionError != null,
            supportingText = {
                uiState.descriptionError?.let {
                    Text(
                        text = it.toLocalizedString(LocalContext.current),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        StandardHalfSpacer()

        Text(stringResource(R.string.add_event_date_label))

        StandardSpacer()

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Date
            OutlinedTextField(
                value = uiState.selectedDate.standardDateFormat(),
                onValueChange = { /* NO-OP */ },
                readOnly = true,
                maxLines = 1,
                modifier = Modifier
                    .textFieldClickable(uiState.selectedDate) {
                        onEvent(ModifyEventViewModel.UIEvent.DatePickerOpened)
                    }
                    .defaultMinSize(1.dp)
            )

            StandardDoubleSpacer()

            // Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                uiState.selectedTime?.let {
                    OutlinedTextField(
                        value = it.standardTimeFormat(),
                        onValueChange = { /* NO-OP */ },
                        readOnly = true,
                        maxLines = 1,
                        modifier = Modifier
                            .textFieldClickable(uiState.selectedDate) {
                                onEvent(ModifyEventViewModel.UIEvent.DatePickerOpened)
                            }
                            .defaultMinSize(1.dp)
                    )
                } ?: Text("Set time")

                Checkbox(
                    checked = uiState.selectedTime != null,
                    onCheckedChange = {
                        if (it) {
                            onEvent(ModifyEventViewModel.UIEvent.TimePickerOpened)
                        } else {
                            onEvent(ModifyEventViewModel.UIEvent.TimeSelected(null))
                        }
                    }
                )
            }
        }
    }
}
