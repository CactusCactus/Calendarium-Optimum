package com.kuba.calendarium.ui.screens.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.internal.TaskCreationData
import com.kuba.calendarium.ui.common.DatePickerModal
import com.kuba.calendarium.ui.common.OutlinedText
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardQuarterSpacer
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.TimePickerModal
import com.kuba.calendarium.ui.common.dateTimeRowPrefixLabelWidth
import com.kuba.calendarium.ui.common.fabContentPadding
import com.kuba.calendarium.ui.common.fabSize
import com.kuba.calendarium.ui.common.outlineBorder
import com.kuba.calendarium.ui.common.standardHalfPadding
import com.kuba.calendarium.ui.common.standardIconSize
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.common.textFieldClickable
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel.UIEvent
import com.kuba.calendarium.util.standardDateFormat
import com.kuba.calendarium.util.standardTimeFormat
import com.kuba.calendarium.util.toLocalizedString
import kotlinx.coroutines.flow.collectLatest
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyEventScreen(
    title: String,
    viewModel: ModifyEventViewModel,
    onNavigateUp: (eventDate: LocalDate) -> Unit
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
            viewModel.uiState.collectAsState().value.selectedTime ?: LocalTime.now()

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
        StandardSpacer()

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { onEvent(UIEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.input_title_placeholder)) },
            maxLines = 1,
            isError = uiState.titleError != null,
            supportingText = uiState.titleError?.let {
                {
                    Text(
                        text = it.toLocalizedString(LocalContext.current),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        StandardHalfSpacer()

        DescriptionRow(
            description = uiState.description,
            descriptionError = uiState.descriptionError,
            onDescriptionChanged = { onEvent(UIEvent.DescriptionChanged(it)) }
        )

        StandardSpacer()

        TaskListRow(
            taskList = uiState.taskList,
            errorList = uiState.taskError,
            onTaskAdded = {
                onEvent(UIEvent.AddTask(it.title))
            },
            onTaskChanged = { id, task ->
                onEvent(UIEvent.UpdateTask(id, task))
            },
            onTaskOrderChanged = { indexFrom, indexTo ->
                onEvent(UIEvent.TaskOrderChanged(indexFrom, indexTo))
            },
            onTaskRemoved = {
                onEvent(UIEvent.RemoveTask(it))
            })

        StandardSpacer()

        DateTimeHeaderRow(isTimeSet = uiState.selectedTime != null) { checked ->
            if (checked) {
                onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM))
            } else {
                onEvent(UIEvent.ClearTime)
            }
        }

        StandardHalfSpacer()

        DateTimeRowFrom(
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            isEndDateSet = uiState.selectedDateEnd != null,
            onDatePickerOpen = { onEvent(UIEvent.DatePickerOpened(DateTimeMode.FROM)) },
            onTimePickerOpen = { onEvent(UIEvent.TimePickerOpened(DateTimeMode.FROM)) }
        )

        StandardSpacer()

        uiState.selectedDateEnd?.let {
            DateTimeRowTo(
                selectedDate = it,
                selectedTime = uiState.selectedTimeEnd,
                onClearTime = { onEvent(UIEvent.ClearDateAndTime(DateTimeMode.TO)) },
                onDatePickerOpen = { onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO)) },
                onTimePickerOpen = { onEvent(UIEvent.TimePickerOpened(DateTimeMode.TO)) }
            )
        } ?: SetEndDateCheckbox(
            isEndDateSet = uiState.selectedDateEnd != null,
            onCheckedChange = { checked ->
                if (checked) {
                    onEvent(UIEvent.DatePickerOpened(DateTimeMode.TO))
                } else {
                    onEvent(UIEvent.ClearDateAndTime(DateTimeMode.TO))
                }
            }
        )
    }
}

@Composable
private fun DescriptionRow(
    description: String? = null,
    descriptionError: ModifyEventViewModel.ValidationError? = null,
    onDescriptionChanged: (String?) -> Unit
) {
    val descriptionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(description) {
        if (description != null) {
            descriptionFocusRequester.requestFocus()
        }
    }

    if (description != null) {
        OutlinedTextField(
            value = description,
            onValueChange = { onDescriptionChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(descriptionFocusRequester),
            placeholder = { Text(stringResource(R.string.input_description_placeholder)) },
            isError = descriptionError != null,
            supportingText = descriptionError?.let {
                {
                    Text(
                        text = it.toLocalizedString(LocalContext.current),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = { onDescriptionChanged(null) }) {
                    Icon(painterResource(R.drawable.ic_close_24), "Clear button")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )
    } else {
        DataPlaceholder(
            text = stringResource(R.string.input_description_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDescriptionChanged("") }
        )
    }
}

@Composable
private fun TaskListRow(
    taskList: List<TaskCreationData>,
    errorList: List<ModifyEventViewModel.ValidationError?>,
    onTaskAdded: (TaskCreationData) -> Unit,
    onTaskChanged: (Int, TaskCreationData) -> Unit,
    onTaskOrderChanged: (Int, Int) -> Unit,
    onTaskRemoved: (Int) -> Unit
) {
    val newTextFieldFocusRequester = remember { FocusRequester() }
    var previousTaskListSize by remember { mutableIntStateOf(taskList.size) }

    LaunchedEffect(taskList.size) {
        if (taskList.size > previousTaskListSize && taskList.isNotEmpty()) {
            newTextFieldFocusRequester.requestFocus()
        }
        previousTaskListSize = taskList.size
    }

    if (taskList.isNotEmpty()) {
        val hapticFeedback = LocalHapticFeedback.current
        val lazyListState = rememberLazyListState()

        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            // -1 to account for the header
            onTaskOrderChanged(from.index - 1, to.index - 1)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .outlineBorder()
                .padding(standardPadding)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(standardHalfPadding)
        ) {
            item {
                Text(
                    text = stringResource(R.string.task_list_header_label),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            itemsIndexed(taskList, key = { _, task -> task.id }) { index, task ->
                val modifier =
                    if (index == taskList.lastIndex && taskList.size > previousTaskListSize) {
                        Modifier.focusRequester(newTextFieldFocusRequester)
                    } else {
                        Modifier
                    }

                ReorderableItem(state = reorderableLazyListState, key = task.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                    Surface(shadowElevation = elevation) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_drag_handle_24),
                                "Reorder icon",
                                tint = LocalContentColor.current.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(start = standardPadding)
                                    .draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        }
                                    )
                            )

                            StandardHalfSpacer()

                            val interactionSource = remember { MutableInteractionSource() }
                            val error = errorList.getOrNull(index)
                            val isFocused by interactionSource.collectIsFocusedAsState()

                            TextField(
                                value = task.title,
                                onValueChange = { onTaskChanged(index, task.copy(title = it)) },
                                interactionSource = interactionSource,
                                maxLines = 1,
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                isError = error != null && !isFocused,
                                supportingText = if (error != null && !isFocused) {
                                    {
                                        Text(
                                            text = error.toLocalizedString(LocalContext.current),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else null,
                                trailingIcon = {
                                    IconButton(onClick = { onTaskRemoved(index) }) {
                                        Icon(
                                            painterResource(R.drawable.ic_close_24),
                                            "Clear task button"
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(stringResource(R.string.new_task_field_placeholder))
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction =
                                        if (index == taskList.lastIndex) ImeAction.Next
                                        else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onNext = {
                                    onTaskAdded(TaskCreationData(title = ""))
                                }),
                                modifier = modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onTaskAdded(TaskCreationData(title = "")) }
                        .fillMaxWidth()
                        .padding(horizontal = standardPadding, vertical = standardHalfPadding)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24),
                        contentDescription = "Add task icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StandardQuarterSpacer()
                    Text(
                        text = stringResource(R.string.add_task_label),
                        color = LocalContentColor.current.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        DataPlaceholder(
            text = stringResource(R.string.new_task_list_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTaskAdded(TaskCreationData(title = "")) }
                .animateContentSize())
    }
}

@Composable
private fun DateTimeHeaderRow(
    isTimeSet: Boolean,
    onSetTimeCheckedChanged: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.add_event_date_label),
            style = MaterialTheme.typography.titleMedium
        )

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
            checked = isTimeSet,
            onCheckedChange = onSetTimeCheckedChanged
        )
    }
}

@Composable
private fun DateTimeRowFrom(
    selectedDate: LocalDate,
    selectedTime: LocalTime?,
    isEndDateSet: Boolean,
    onDatePickerOpen: () -> Unit,
    onTimePickerOpen: () -> Unit
) {
    DateTimeRow(
        selectedDate = selectedDate,
        selectedTime = selectedTime,
        labelStart = if (isEndDateSet) {
            {
                Text(
                    text = stringResource(R.string.time_selection_from),
                    modifier = Modifier.width(dateTimeRowPrefixLabelWidth)
                )
            }
        } else null,
        labelEnd = if (isEndDateSet) {
            { Spacer(Modifier.size(standardIconSize)) }
        } else null,
        onDateFieldClicked = { onDatePickerOpen() },
        onTimeFieldClicked = { onTimePickerOpen() }
    )
}

@Composable
private fun DateTimeRowTo(
    selectedDate: LocalDate,
    selectedTime: LocalTime?,
    onClearTime: () -> Unit,
    onDatePickerOpen: () -> Unit,
    onTimePickerOpen: () -> Unit
) {
    DateTimeRow(
        selectedDate = selectedDate,
        selectedTime = selectedTime,
        labelStart = {
            Text(
                text = stringResource(R.string.time_selection_to),
                modifier = Modifier.width(dateTimeRowPrefixLabelWidth)
            )
        },
        labelEnd = {
            IconButton(
                onClick = { onClearTime() },
                modifier = Modifier.size(standardIconSize)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_24),
                    contentDescription = "Remove end date",
                )
            }
        },
        onDateFieldClicked = { onDatePickerOpen() },
        onTimeFieldClicked = { onTimePickerOpen() }
    )
}

@Composable
private fun SetEndDateCheckbox(isEndDateSet: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isEndDateSet,
            onCheckedChange = onCheckedChange,
        )
        Text(stringResource(R.string.set_end_date_and_time_text))
    }
}

@Composable
private fun DateTimeRow(
    selectedDate: LocalDate,
    selectedTime: LocalTime?,
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
            label = stringResource(R.string.date_time_row_date_label),
            modifier = Modifier
                .clickable { onDateFieldClicked() }
                .fillMaxWidth()
                .weight(1f)
        )

        StandardSpacer()

        // Time
        AnimatedVisibility(selectedTime != null) {
            selectedTime?.let {
                OutlinedText(
                    text = it.standardTimeFormat(),
                    maxLines = 1,
                    label = stringResource(R.string.date_time_row_time_label),
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
private fun DataPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .outlineBorder()
            .padding(standardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_24),
            contentDescription = "Add icon",
            tint = MaterialTheme.colorScheme.primary,
        )

        StandardHalfSpacer()

        Text(text)
    }
}
