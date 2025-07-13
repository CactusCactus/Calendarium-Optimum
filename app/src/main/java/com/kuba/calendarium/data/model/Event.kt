package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

data class EventTasks(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "task_id"
    )
    val tasks: List<Task>
)



