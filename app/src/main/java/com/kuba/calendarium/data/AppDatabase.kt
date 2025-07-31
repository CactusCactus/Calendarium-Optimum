package com.kuba.calendarium.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.ChronoUnitConverter
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.Reminder
import com.kuba.calendarium.data.model.RepetitionConverter
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.data.model.TimeConverters

@Database(
    entities = [Event::class, Task::class, Reminder::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TimeConverters::class, RepetitionConverter::class, ChronoUnitConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "calendar_database"

        fun builder(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration(true)
    }

    abstract fun eventDao(): EventDao
}
