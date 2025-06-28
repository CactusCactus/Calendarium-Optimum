package com.kuba.calendarium.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(viewModel.uiState.collectAsState().value.text)
    }
}