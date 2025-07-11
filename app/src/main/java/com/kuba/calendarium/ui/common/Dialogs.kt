package com.kuba.calendarium.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import com.kuba.calendarium.util.getMonthNames
import timber.log.Timber
import java.time.Instant
import java.time.ZoneOffset
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDate: Long,
    onDatePicked: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate,
        initialDisplayedMonthMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDatePicked(it)
                    onDismissRequest()
                } ?: run {
                    Timber.e("DatePickerDialog: selectedDateMillis is null")
                }
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = modifier
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    initialTime: Long,
    onTimePicked: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeCalendar = Calendar.getInstance().apply { timeInMillis = initialTime }
    val timePickerState = rememberTimePickerState(
        initialHour = timeCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = timeCalendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(standardPadding)
            ) {
                TimePicker(state = timePickerState)

                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }

                    StandardSpacer()

                    TextButton(onClick = {
                        onTimePicked(timePickerState.hour * 3600000L + timePickerState.minute * 60000)
                        onDismissRequest()
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
fun MonthYearPickerModal(
    initialDate: Long,
    onDatePicked: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localDate by remember {
        mutableStateOf(Instant.ofEpochMilli(initialDate).atZone(ZoneOffset.UTC).toLocalDate())
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = modifier.widthIn(min = dialogMinWidth)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(standardPadding)
            ) {
                // Year picker
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        localDate = localDate.plusYears(-1)
                    }) {
                        Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Previous year")
                    }

                    StandardHalfSpacer()

                    Text(
                        text = localDate.year.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )

                    StandardHalfSpacer()

                    IconButton(onClick = {
                        localDate = localDate.plusYears(1)
                    }) {
                        Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Next year")
                    }
                }

                val months = remember { getMonthNames() }

                // Month picker
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalArrangement = Arrangement.spacedBy(standardHalfPadding)
                ) {
                    itemsIndexed(months) { index, monthName ->
                        val selected = localDate.month.value == index + 1

                        Button(
                            onClick = {
                                localDate = localDate.withMonth(index + 1)
                                val millis = localDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                                    .toEpochMilli()

                                onDatePicked(millis)
                            },
                            modifier = Modifier
                                .aspectRatio(1.5f)
                                .fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = monthName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    onOptionClick: (ContextMenuOption) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest, modifier = modifier
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(standardPadding)
            )

            LazyColumn {
                items(ContextMenuOption.entries.toTypedArray()) {
                    JourneyContextMenuRow(it, Modifier.clickable { onOptionClick.invoke(it) })
                }
            }

            StandardDoubleSpacer()
        }
    }
}

@Composable
fun JourneyContextMenuRow(option: ContextMenuOption, modifier: Modifier = Modifier) {
    StandardListRow(
        label = stringResource(option.label),
        icon = option.icon,
        iconTint = option.overrideTint,
        modifier = modifier,
    )
}

@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
    @DrawableRes icon: Int? = null,
    iconTint: Color = LocalContentColor.current,
    confirmButtonLabel: String = stringResource(R.string.confirm),
    cancelButtonLabel: String = stringResource(R.string.cancel),
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        icon = {
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = "$title + dialog icon",
                    tint = iconTint
                )
            }
        },
        title = { title?.let { Text(it) } },
        text = content,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onConfirm) {
                Text(confirmButtonLabel)
            }
        },
        dismissButton = {
            TextButton(onDismiss) {
                Text(cancelButtonLabel)
            }
        },
        modifier = modifier
    )
}