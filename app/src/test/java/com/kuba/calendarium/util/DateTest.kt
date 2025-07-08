package com.kuba.calendarium.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

private val timeZone = TimeZone.getTimeZone("UTC")

class DateTest {
    @Test
    fun standardDateFormat() {
        val expectedDate = "01/01/2023"
        val actualDate = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        assertThat(actualDate.standardDateFormat()).isEqualTo(expectedDate)
    }

    @Test
    fun shortDateFormat() {
        val expectedDate = "01/01"
        val actualDate = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        assertThat(actualDate.timeInMillis.shortDateFormat()).isEqualTo(expectedDate)
    }

    @Test
    fun standardTimeFormat() {
        val expectedTime = "21:37"
        val actualTime = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 37)
        }

        assertThat(actualTime.timeInMillis.standardTimeFormat()).isEqualTo(expectedTime)
    }

    @Test
    fun isSameDay() {
        val date1 = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        val date2 = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        assertThat(date1.isSameDay(date2)).isTrue()

        val date3 = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 2)
        }.timeInMillis

        assertThat(date1.isSameDay(date3)).isFalse()
    }

}