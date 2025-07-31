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

fun ChronoUnit.toReminderString(context: Context) = when (this) {
    ChronoUnit.MINUTES -> context.getString(R.string.reminder_unit_minutes)
    ChronoUnit.HOURS -> context.getString(R.string.reminder_unit_hours)
    ChronoUnit.DAYS -> context.getString(R.string.reminder_unit_days)
    ChronoUnit.WEEKS -> context.getString(R.string.reminder_unit_weeks)
    ChronoUnit.MONTHS -> context.getString(R.string.reminder_unit_months)
    else -> throw NotImplementedError()
}
