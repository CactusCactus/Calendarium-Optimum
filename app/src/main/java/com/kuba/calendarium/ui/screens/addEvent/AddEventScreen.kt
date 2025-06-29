package com.kuba.calendarium.ui.screens.addEvent

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat

@Composable
fun AddEventScreen(viewModel: AddEventViewModel) {
    Text(
        SimpleDateFormat("dd.MM.yyyy").format(viewModel.uiState.collectAsState().value.selectedDate)
            .toString()
    )
}