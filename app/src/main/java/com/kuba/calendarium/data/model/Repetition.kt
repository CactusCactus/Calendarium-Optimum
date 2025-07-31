package com.kuba.calendarium.data.model

import androidx.room.TypeConverter
import java.time.temporal.ChronoUnit

enum class Repetition {
    DAILY, WEEKLY, MONTHLY, YEARLY;

    fun toChronoUnit(): ChronoUnit = when (this) {
        DAILY -> ChronoUnit.DAYS
        WEEKLY -> ChronoUnit.WEEKS
        MONTHLY -> ChronoUnit.MONTHS
        YEARLY -> ChronoUnit.YEARS
    }
}

class RepetitionConverter {
    @TypeConverter
    fun toRepetition(value: String) = enumValueOf<Repetition>(value)

    @TypeConverter
    fun fromRepetition(value: Repetition) = value.name
}
