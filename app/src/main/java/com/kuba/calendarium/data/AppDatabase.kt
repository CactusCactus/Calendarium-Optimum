package com.kuba.calendarium.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event

@Database(entities = [Event::class], version = 1)
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