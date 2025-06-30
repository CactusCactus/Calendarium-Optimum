package com.kuba.calendarium.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.internal.ContextMenuOption
import timber.log.Timber

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