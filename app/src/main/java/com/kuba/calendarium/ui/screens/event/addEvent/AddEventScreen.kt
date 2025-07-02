package com.kuba.calendarium.ui.screens.event.addEvent

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.screens.event.ModifyEventScreen

@Composable
fun AddEventScreen(
    viewModel: AddEventViewModel,
    onNavigateUp: (Long) -> Unit
) {
    ModifyEventScreen(
        title = stringResource(R.string.add_event_screen_title),
        viewModel = viewModel,
        onNavigateUp = onNavigateUp
    )
}