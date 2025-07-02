package com.kuba.calendarium.ui.screens.event.editEvent

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.screens.event.ModifyEventScreen

@Composable
fun EditEvenScreen(
    viewModel: EditEventViewModel,
    onNavigateUp: (Long) -> Unit
) {
    ModifyEventScreen(
        title = stringResource(R.string.edit_event_screen_title),
        viewModel = viewModel,
        onNavigateUp = onNavigateUp
    )
}