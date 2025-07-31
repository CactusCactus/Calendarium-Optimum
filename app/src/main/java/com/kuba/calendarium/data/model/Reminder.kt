package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.temporal.ChronoUnit

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id_ref"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "reminder_id") val id: Long = 0,
    @ColumnInfo(name = "event_id_ref") val eventIdRef: Long = 0,
    @ColumnInfo(name = "value") val value: Long,
    @ColumnInfo(name = "unit") val unit: ChronoUnit
) {
    companion object {
        val default = Reminder(value = 1, unit = ChronoUnit.HOURS)
    }
}

class ChronoUnitConverter {
    @TypeConverter
    fun toChronoUnit(value: String) = enumValueOf<ChronoUnit>(value)

    @TypeConverter
    fun fromChronoUnit(value: ChronoUnit) = value.name
}
