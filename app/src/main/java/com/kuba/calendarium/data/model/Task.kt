package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kuba.calendarium.data.model.internal.TaskCreationData

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id_ref"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["event_id_ref"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "task_id") val id: Long = 0,
    @ColumnInfo(name = "event_id_ref") val eventIdRef: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "position") val position: Int = 0,
    @ColumnInfo(name = "done") val done: Boolean = false
) {
    fun toTaskInternal() = TaskCreationData(id, title, done)
}
