package com.kuba.calendarium.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateTest {
    @Test
    fun standardDateFormat() {
        val expectedDate = "01/01/2023"
        val actualDate = LocalDate.of(2023, 1, 1)

        assertThat(actualDate.standardDateFormat()).isEqualTo(expectedDate)
    }

    @Test
    fun shortDateFormat() {
        val expectedDate = "01/01"
        val actualDate = LocalDate.of(2023, 1, 1)

        assertThat(actualDate.shortDateFormat()).isEqualTo(expectedDate)
    }

    @Test
    fun standardTimeFormat() {
        val expectedTime = "21:37"
        val actualTime = LocalTime.of(21, 37)

        assertThat(actualTime.standardTimeFormat()).isEqualTo(expectedTime)
    }

    @Test
    fun isSameDay() {
        val date1 = LocalDateTime.of(2023, 1, 1, 21, 37)
        val date2 = LocalDateTime.of(2023, 1, 1, 16, 20)

        assertThat(date1.isSameDay(date2)).isTrue()

        val date3 = LocalDateTime.of(2023, 1, 2, 21, 37)

        assertThat(date1.isSameDay(date3)).isFalse()
    }
}