package com.kuba.calendarium.ui.screens.addEvent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.common.DatePickerModal
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.descriptionMinHeight
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.common.textFieldClickable
import com.kuba.calendarium.util.standardDateFormat
import kotlinx.coroutines.flow.collectLatest
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: AddEventViewModel,
    onNavigateUp: (Long) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.navEvent.collectLatest {
            when (it) {
                is AddEventViewModel.NavEvent.Finish -> onNavigateUp(it.eventDate)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.add_event_screen_title),
                    style = MaterialTheme.typography.displayMedium
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(AddEventViewModel.UIEvent.DoneClicked)
            }) {
                Icon(Icons.Filled.Done, "Done")
            }
        }
    ) { innerPadding ->
        MainColumn(viewModel, modifier = Modifier.padding(innerPadding))

        if (viewModel.uiState.collectAsState().value.datePickerOpen) {
            DatePickerModal(
                initialDate = viewModel.uiState.collectAsState().value.selectedDate,
                onDatePicked = {
                    viewModel.onEvent(AddEventViewModel.UIEvent.DateSelected(Date(it)))
                },
                onDismissRequest = {
                    viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerDismissed)
                }
            )
        }
    }
}

@Composable
private fun MainColumn(viewModel: AddEventViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(standardPadding)
    ) {
        Text("Event Information")

        StandardSpacer()

        OutlinedTextField(
            value = viewModel.uiState.collectAsState().value.title,
            onValueChange = { viewModel.onEvent(AddEventViewModel.UIEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.input_title_placeholder)) }
        )

        StandardSpacer()

        OutlinedTextField(
            value = viewModel.uiState.collectAsState().value.description,
            onValueChange = {
                viewModel.onEvent(AddEventViewModel.UIEvent.DescriptionChanged(it))
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = descriptionMinHeight),
            placeholder = { Text(stringResource(R.string.input_description_placeholder)) }
        )

        StandardSpacer()

        Text("Date")

        StandardSpacer()

        val dateString =
            viewModel.uiState.collectAsState().value.selectedDate.standardDateFormat()

        OutlinedTextField(
            value = dateString,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .textFieldClickable(viewModel.uiState.collectAsState().value.selectedDate) {
                    viewModel.onEvent(AddEventViewModel.UIEvent.DatePickerOpened)
                }
        )
    }
}
