package com.kuba.calendarium.util

import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val STANDARD_DATE_FORMAT = "dd/MM/yyyy"

const val SHORT_DATE_FORMAT = "dd/MM"

const val TITLE_DATE_FORMAT = "d MMM yyyy"

const val STANDARD_TIME_FORMAT = "HH:mm"

fun Long.standardDateFormat(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(this))

fun Long.titleDateFormat(): String =
    SimpleDateFormat(TITLE_DATE_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(this))

fun Long.shortDateFormat(): String =
    SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(this))

fun Long.standardTimeFormat(): String =
    SimpleDateFormat(STANDARD_TIME_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(this))

fun getTodayMidnight(timeZone: TimeZone = TimeZone.getTimeZone("UTC")) =
    Calendar.getInstance(timeZone).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

fun Long.resetToMidnight(timeZone: TimeZone = TimeZone.getTimeZone("UTC")) =
    Calendar.getInstance(timeZone).apply {
        timeInMillis = this@resetToMidnight
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

fun Long.isSameDay(other: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Boolean {
    val thisCalendar = Calendar.getInstance(timeZone).apply { timeInMillis = this@isSameDay }
    val otherCalendar = Calendar.getInstance(timeZone).apply { timeInMillis = other }

    return thisCalendar.get(Calendar.DAY_OF_YEAR) == otherCalendar.get(Calendar.DAY_OF_YEAR) &&
            thisCalendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR)
}

fun getMonthNames(textStyle: TextStyle = TextStyle.SHORT) = Month.entries.map { month ->
    month.getDisplayName(textStyle, Locale.getDefault())
}
