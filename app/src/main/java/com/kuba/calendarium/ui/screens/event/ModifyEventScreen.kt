package com.kuba.calendarium.ui.screens.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.common.DatePickerModal
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.TimePickerModal
import com.kuba.calendarium.ui.common.dateTimeRowPrefixLabelWidth
import com.kuba.calendarium.ui.common.descriptionMinHeight
import com.kuba.calendarium.ui.common.fabContentPadding
import com.kuba.calendarium.ui.common.fabSize
import com.kuba.calendarium.ui.common.standardIconSize
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.common.textFieldClickable
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.UIEvent
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
                onClick = { viewModel.onEvent(UIEvent.DoneClicked) },
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
                    viewModel.onEvent(UIEvent.DateSelected(it))
                },
                onDismissRequest = {
                    viewModel.onEvent(UIEvent.DatePickerDismissed)
                }
            )
        }

        val time =
            viewModel.uiState.collectAsState().value.selectedTime ?: System.currentTimeMillis()

        if (viewModel.uiState.collectAsState().value.timePickerOpen) {
            TimePickerModal(
                initialTime = time,
                onTimePicked = {
                    viewModel.onEvent(UIEvent.TimeSelected(it))
                },
                onDismissRequest = {
                    viewModel.onEvent(UIEvent.TimePickerDismissed)
                }
            )
        }
    }
}

@Composable
private fun MainColumn(
    uiState: ModifyEventViewModel.UIState,
    onEvent: (UIEvent) -> Unit,
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
            onValueChange = { onEvent(UIEvent.TitleChanged(it)) },
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
            onValueChange = { onEvent(UIEvent.DescriptionChanged(it)) },
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

        StandardSpacer()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.add_event_date_label))

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
            )

            Text(
                stringResource(R.string.set_time_label),
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )

            Checkbox(
                checked = uiState.selectedTime != null,
                onCheckedChange = {
                    if (it) {
                        onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
                    } else {
                        onEvent(UIEvent.ClearTime)
                    }
                }
            )
        }

        StandardHalfSpacer()

        DateTimeRow(
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            labelStart = if (uiState.selectedDateEnd != null) {
                {
                    Text(
                        text = stringResource(R.string.time_selection_from),
                        modifier = Modifier.width(dateTimeRowPrefixLabelWidth)
                    )
                }
            } else null,
            labelEnd = if (uiState.selectedDateEnd != null) {
                { Spacer(Modifier.size(standardIconSize)) }
            } else null,
            onDateFieldClicked = {
                onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM))
            },
            onTimeFieldClicked = {
                onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
            },
        )

        uiState.selectedDateEnd?.let {
            StandardSpacer()

            DateTimeRow(
                selectedDate = it,
                selectedTime = uiState.selectedTimeEnd,
                labelStart = {
                    Text(
                        text = stringResource(R.string.time_selection_to),
                        modifier = Modifier.width(dateTimeRowPrefixLabelWidth)
                    )
                },
                labelEnd = {
                    IconButton(
                        onClick = { onEvent(UIEvent.ClearDateAndTime(DateTimeMode.TO)) },
                        modifier = Modifier.size(standardIconSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = "Remove end date",
                        )
                    }
                },
                onDateFieldClicked = {
                    onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
                },
                onTimeFieldClicked = {
                    onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO))
                }
            )
        } ?: Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.selectedDateEnd != null,
                onCheckedChange = {
                    if (it) {
                        onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
                    } else {
                        onEvent(UIEvent.ClearDateAndTime(DateTimeMode.TO))
                    }
                },
            )
            Text(stringResource(R.string.set_end_date_and_time_text))
        }
    }
}

@Composable
private fun DateTimeRow(
    selectedDate: Long,
    selectedTime: Long?,
    labelStart: @Composable (RowScope.() -> Unit)?,
    labelEnd: @Composable (RowScope.() -> Unit)?,
    onDateFieldClicked: () -> Unit,
    onTimeFieldClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        if (labelStart != null) {
            labelStart()
            StandardSpacer()
        }

        // Date
        OutlinedText(
            text = selectedDate.standardDateFormat(),
            maxLines = 1,
            label = "Date",
            modifier = Modifier
                .clickable { onDateFieldClicked() }
                .fillMaxWidth()
                .weight(1f)
        )

        StandardSpacer()

        // Time (start)
        AnimatedVisibility(selectedTime != null) {
            selectedTime?.let {
                OutlinedText(
                    text = it.standardTimeFormat(),
                    maxLines = 1,
                    label = "Time",
                    modifier = Modifier.textFieldClickable(selectedTime) {
                        onTimeFieldClicked()
                    }
                )
            }
        }

        if (labelEnd != null) {
            StandardSpacer()
            labelEnd()
        }
    }
}

@Composable
fun OutlinedText(
    text: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Box(modifier = modifier) {
        Text(
            text = text,
            maxLines = maxLines,
            modifier = modifier
                .border(
                    width = 1.dp,
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                .padding(standardPadding)
        )

        label?.let {
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp, x = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
