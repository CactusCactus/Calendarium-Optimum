package com.kuba.calendarium.util

import android.content.Context
import com.kuba.calendarium.R
import com.kuba.calendarium.ui.screens.event.ModifyEventViewModel

fun ModifyEventViewModel.ValidationError.toLocalizedString(context: Context): String = when (this) {
    ModifyEventViewModel.ValidationError.TITLE_EMPTY -> context.getString(R.string.error_event_title_empty)
    ModifyEventViewModel.ValidationError.TITLE_TOO_LONG -> context.getString(
        R.string.error_event_title_too_long,
        ModifyEventViewModel.MAX_TITLE_LENGTH
    )

    ModifyEventViewModel.ValidationError.DESCRIPTION_TOO_LONG -> context.getString(
        R.string.error_event_description_too_long,
        ModifyEventViewModel.MAX_DESCRIPTION_LENGTH
    )
}

inline fun <reified T : Enum<T>> valueOfOrNull(type: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (_: IllegalArgumentException) {
        null
    }
}
