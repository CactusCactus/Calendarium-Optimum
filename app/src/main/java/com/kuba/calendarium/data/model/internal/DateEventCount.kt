package com.kuba.calendarium.data.model.internal

import java.time.LocalDate

data class DateEventCount(
    val eventDate: LocalDate,
    val eventCount: Int
)