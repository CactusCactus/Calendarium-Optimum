package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kuba.calendarium.data.model.internal.TaskInternal
import java.util.UUID

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["event_id"],
            childColumns = ["eventIdRef"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["eventIdRef"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "task_id") val id: Long = 0,
    val eventIdRef: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "done") val done: Boolean = false
) {
    fun toTaskInternal() = TaskInternal(UUID.fromString(id.toString()), title, done)
}