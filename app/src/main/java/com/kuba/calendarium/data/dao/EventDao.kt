package com.kuba.calendarium.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kuba.calendarium.data.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query(
        "SELECT * FROM event WHERE (date_end IS NOT NULL AND :date BETWEEN date AND date_end) " +
                "OR :date == date ORDER BY is_done ASC, time ASC"
    )
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>

    @Query(
        "SELECT COUNT(*) FROM event WHERE" +
                " (date_end IS NOT NULL AND :date BETWEEN date AND date_end) OR :date == date"
    )
    fun getEventCountForDate(date: LocalDate): Flow<Int>

    @Query("SELECT * FROM event WHERE id = :id")
    fun getEventById(id: Long): Flow<Event?>
}