package com.kuba.calendarium.util

import android.content.Context
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.screens.addEvent.AddEventViewModel

fun AddEventViewModel.ValidationError.toLocalizedString(context: Context): String = when (this) {
    AddEventViewModel.ValidationError.TITLE_EMPTY -> context.getString(R.string.error_event_title_empty)
    AddEventViewModel.ValidationError.TITLE_TOO_LONG -> context.getString(
        R.string.error_event_title_too_long,
        AddEventViewModel.MAX_TITLE_LENGTH
    )
    AddEventViewModel.ValidationError.DESCRIPTION_TOO_LONG -> context.getString(
        R.string.error_event_description_too_long,
        AddEventViewModel.MAX_DESCRIPTION_LENGTH
    )
}