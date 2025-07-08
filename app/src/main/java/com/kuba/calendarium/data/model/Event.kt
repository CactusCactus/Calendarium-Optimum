package com.kuba.calendarium.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "time") val time: Long? = null,
    @ColumnInfo(name = "date_end") val dateEnd: Long? = null,
    @ColumnInfo(name = "time_end") val timeEnd: Long? = null
)