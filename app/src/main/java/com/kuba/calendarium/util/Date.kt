package com.kuba.calendarium.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val STANDARD_DATE_FORMAT = "dd/MM/yyyy"

fun Long.standardDateFormat(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.getDefault()).format(Date(this))

fun Date.standardDateFormat(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.getDefault()).format(this)