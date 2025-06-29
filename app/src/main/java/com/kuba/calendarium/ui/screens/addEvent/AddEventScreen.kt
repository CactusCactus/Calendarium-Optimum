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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.common.StandardSpacer
import com.kuba.calendarium.ui.common.descriptionMinHeight
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.util.standardDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(viewModel: AddEventViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.add_event_screen_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(AddEventViewModel.UIEvent.DoneClicked)
            }) {
                Icon(Icons.Filled.Done, "Done")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(standardPadding)
        ) {
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

            val dateString =
                viewModel.uiState.collectAsState().value.selectedDate.standardDateFormat()

            OutlinedTextField(
                value = dateString,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
