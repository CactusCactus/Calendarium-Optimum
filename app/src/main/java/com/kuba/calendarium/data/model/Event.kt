package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "event_id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "time") val time: LocalTime? = null,
    @ColumnInfo(name = "date_end") val dateEnd: LocalDate? = null,
    @ColumnInfo(name = "time_end") val timeEnd: LocalTime? = null,
    @ColumnInfo(name = "is_done") val done: Boolean = false,
    @ColumnInfo(name = "repetition") val repetition: Repetition? = null
) {
    fun toEventTasks() = EventDetailed(this, emptyList(), emptyList())

    fun getNextOccurrence(from: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val localDateTask = LocalDateTime.of(date, time ?: LocalTime.MIDNIGHT)
        val timeFromNow = from.until(localDateTask, ChronoUnit.MILLIS)

        return if (timeFromNow > 0) {
            localDateTask
        } else if (repetition != null) {
            val unitInPast = localDateTask.until(from, repetition.toChronoUnit())

            localDateTask.plus(unitInPast + 1, repetition.toChronoUnit())
        } else {
            null
        }
    }

    fun getNextOccurrenceEnd(from: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (dateEnd == null) return null

        val localDateTask = LocalDateTime.of(dateEnd, timeEnd ?: LocalTime.MIDNIGHT)
        val timeFromNow = from.until(localDateTask, ChronoUnit.MILLIS)

        return if (timeFromNow > 0) {
            localDateTask
        } else if (repetition != null) {
            val unitInPast = localDateTask.until(from, repetition.toChronoUnit())

            localDateTask.plus(unitInPast + 1, repetition.toChronoUnit())
        } else {
            null
        }
    }
}

data class EventDetailed(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "event_id_ref",
        entity = Task::class
    )
    val tasks: List<Task>,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "event_id_ref",
        entity = Reminder::class
    )
    val reminders: List<Reminder>
)

class TimeConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromTime(value: Long?): LocalTime? {
        return value?.let { LocalTime.ofNanoOfDay(it) }
    }

    @TypeConverter
    fun timeToLong(time: LocalTime?): Long? {
        return time?.toNanoOfDay()
    }
}
