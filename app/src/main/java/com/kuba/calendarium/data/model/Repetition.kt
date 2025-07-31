package com.kuba.calendarium.data.model

import androidx.room.TypeConverter

enum class Repetition {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

class RepetitionConverter {
    @TypeConverter
    fun toRepetition(value: String) = enumValueOf<Repetition>(value)

    @TypeConverter
    fun fromRepetition(value: Repetition) = value.name
}
