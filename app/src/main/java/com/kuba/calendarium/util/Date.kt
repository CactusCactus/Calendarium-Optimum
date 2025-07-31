package com.kuba.calendarium.util

import android.content.Context
import com.kuba.calendarium.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

const val STANDARD_DATE_FORMAT = "dd/MM/yyyy"

const val SHORT_DATE_FORMAT = "dd/MM"

const val TITLE_DATE_FORMAT = "d MMM yyyy"

const val STANDARD_TIME_FORMAT = "HH:mm"

fun LocalDate.standardDateFormat(): String =
    format(DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT))

fun LocalDate.titleDateFormat(): String =
    format(DateTimeFormatter.ofPattern(TITLE_DATE_FORMAT))


fun LocalDate.shortDateFormat(): String =
    format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT))

fun LocalTime.standardTimeFormat(): String =
    format(DateTimeFormatter.ofPattern(STANDARD_TIME_FORMAT))

fun getTodayMidnight(): LocalDate = LocalDate.now()

fun LocalDate.toMillis() = this.atStartOfDay().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDateTime.isSameDay(other: LocalDateTime) = this.toLocalDate() == other.toLocalDate()

fun LocalDate.isSameWeek(other: LocalDate): Boolean =
    this.get(WeekFields.ISO.weekOfYear()) == other.get(WeekFields.ISO.weekOfYear()) &&
            this.year == other.year

fun getMonthNames(textStyle: TextStyle = TextStyle.SHORT) = Month.entries.map { month ->
    month.getDisplayName(textStyle, Locale.getDefault())
}

fun ChronoUnit.toReminderString(quantity: Int, context: Context) =
    context.resources.getQuantityString(
        when (this) {
            ChronoUnit.MINUTES -> R.plurals.reminder_unit_minutes
            ChronoUnit.HOURS -> R.plurals.reminder_unit_hours
            ChronoUnit.DAYS -> R.plurals.reminder_unit_days
            ChronoUnit.WEEKS -> R.plurals.reminder_unit_weeks
            ChronoUnit.MONTHS -> R.plurals.reminder_unit_months
            else -> throw NotImplementedError()
        }, quantity
    )

fun getReminderChronoUnits() = listOf(
    ChronoUnit.MINUTES,
    ChronoUnit.HOURS,
    ChronoUnit.DAYS,
    ChronoUnit.WEEKS,
    ChronoUnit.MONTHS
)
