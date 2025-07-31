package com.kuba.calendarium.util

import com.kuba.calendarium.data.model.Reminder
import java.time.temporal.ChronoUnit

const val CALENDAR_MAX_OFFSET_YEARS = 12L

const val MONTH_PICKER_ROWS = 3

const val MAX_DAYS_FOR_REPETITION_WEEKLY = 7

const val MAX_DAYS_FOR_REPETITION_MONTHLY = 30

const val MAX_DAYS_FOR_REPETITION_YEARLY = 365

val DEFAULT_REMINDER_OPTIONS = listOf(
    Reminder(value = 15, unit = ChronoUnit.MINUTES),
    Reminder(value = 30, unit = ChronoUnit.MINUTES),
    Reminder(value = 1, unit = ChronoUnit.HOURS),
    Reminder(value = 8, unit = ChronoUnit.HOURS),
    Reminder(value = 1, unit = ChronoUnit.DAYS),
    Reminder(value = 1, unit = ChronoUnit.WEEKS),
)
