package com.kuba.calendarium.util

import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.internal.Repetition
import java.time.LocalDate

fun Event.isRepeatingOnDate(selectedDate: LocalDate): Boolean {
    if (date >= selectedDate || repetition == null) return false

    return when (repetition) {
        Repetition.DAILY -> true
        Repetition.WEEKLY -> selectedDate.dayOfWeek == date.dayOfWeek
        Repetition.MONTHLY -> selectedDate.dayOfMonth == date.dayOfMonth
        Repetition.YEARLY -> selectedDate.month == date.month &&
                selectedDate.dayOfMonth == date.dayOfMonth
    }
}
