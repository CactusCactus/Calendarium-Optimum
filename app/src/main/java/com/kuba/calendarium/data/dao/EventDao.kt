package com.kuba.calendarium.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.internal.DateEventCount
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
        "WITH RECURSIVE DateSeries(generated_date) AS ( " +
                "  SELECT :dateStart " +
                "  UNION ALL " +
                "  SELECT date(generated_date, '+1 day') FROM DateSeries WHERE generated_date < :dateEnd" +
                ") " +
                "SELECT " +
                "    ds.generated_date AS eventDate, " +
                "    COUNT(e.id) AS eventCount " + // COUNT(e.id) is correct as it only counts non-NULL event IDs
                "FROM DateSeries ds " +
                "LEFT JOIN event e ON " +
                // Condition for events that span multiple days
                "    (e.date_end IS NOT NULL AND ds.generated_date BETWEEN e.date AND e.date_end) " +
                // Condition for single-day events (where date_end might be NULL or same as date)
                "    OR (e.date_end IS NULL AND e.date = ds.generated_date) " +
                "GROUP BY ds.generated_date"
    )
    fun getEventCountForDateRange(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Flow<List<DateEventCount>>

    @Query("SELECT * FROM event WHERE id = :id")
    fun getEventById(id: Long): Flow<Event?>
}