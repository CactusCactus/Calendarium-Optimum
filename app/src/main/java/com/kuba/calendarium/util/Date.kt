package com.kuba.calendarium.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val STANDARD_DATE_FORMAT = "dd/MM/yyyy"

fun Long.standardDateFormat(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.getDefault()).format(Date(this))

fun Date.standardDateFormat(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.getDefault()).format(this)

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