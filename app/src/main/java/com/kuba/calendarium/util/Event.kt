package com.kuba.calendarium.util

import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.internal.Repetition
import java.time.LocalDate

fun Event.isHappeningOnDate(selectedDate: LocalDate): Boolean = if (dateEnd == null) {
    date == selectedDate
} else {
    selectedDate.isAfter(date) && selectedDate.isBefore(dateEnd)
}

fun Event.isRepeatingOnDate(selectedDate: LocalDate): Boolean {
    if (date > selectedDate || repetition == null) return false

    return when (repetition) {
        Repetition.DAILY -> true
        Repetition.WEEKLY -> if (dateEnd == null) {
            selectedDate.dayOfWeek == date.dayOfWeek
        } else if (!dateEnd.isSameWeek(date)) {
            selectedDate.dayOfWeek >= date.dayOfWeek ||
                    selectedDate.dayOfWeek <= dateEnd.dayOfWeek
        } else {
            selectedDate.dayOfWeek >= date.dayOfWeek &&
                    selectedDate.dayOfWeek <= dateEnd.dayOfWeek
        }

        Repetition.MONTHLY -> if (dateEnd == null) {
            selectedDate.dayOfMonth == date.dayOfMonth
        } else if (dateEnd.month != date.month) {
            selectedDate.dayOfMonth >= date.dayOfMonth ||
                    selectedDate.dayOfMonth <= dateEnd.dayOfMonth
        } else {
            selectedDate.dayOfMonth >= date.dayOfMonth &&
                    selectedDate.dayOfMonth <= dateEnd.dayOfMonth

        }

        Repetition.YEARLY -> if (dateEnd == null) {
            selectedDate.month == date.month && selectedDate.dayOfMonth == date.dayOfMonth
        } else {
            selectedDate.dayOfYear >= date.dayOfYear && selectedDate.dayOfYear <= dateEnd.dayOfYear
        }
    }
}
