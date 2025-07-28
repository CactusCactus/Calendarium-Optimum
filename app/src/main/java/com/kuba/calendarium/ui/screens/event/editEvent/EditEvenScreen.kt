package com.kuba.calendarium.ui.screens.event.editEvent

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.screens.event.ModifyEventScreen
import java.time.LocalDate

@Composable
fun EditEvenScreen(
    viewModel: EditEventViewModel,
    onNavigateUp: (LocalDate) -> Unit
) {
    ModifyEventScreen(
        title = stringResource(R.string.edit_event_screen_title),
        viewModel = viewModel,
        onNavigateUp = onNavigateUp
    )
}