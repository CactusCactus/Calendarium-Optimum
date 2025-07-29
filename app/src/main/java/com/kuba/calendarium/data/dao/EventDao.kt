package com.kuba.calendarium.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.EventTasks
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.data.model.internal.DateEventCount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(eventTask: Task): Long

    @Transaction
    suspend fun insertEventWithTasks(event: Event, tasks: List<Task>) {
        val eventId = insert(event)
        tasks.forEach { task -> insertTask(task.copy(eventIdRef = eventId)) }
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: Event)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(eventTask: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventWithTasks(event: Event, tasks: List<Task>) {
        update(event)
        deleteTasksByEventId(event.id)
        tasks.forEach { task -> insertTask(task.copy(eventIdRef = event.id)) }
    }

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM task WHERE event_id_ref = :eventId")
    suspend fun deleteTasksByEventId(eventId: Long)


    @Transaction
    @Query(
        "SELECT * FROM event WHERE (date_end IS NOT NULL AND :date BETWEEN date AND date_end) " +
                "OR :date == date ORDER BY is_done ASC, time ASC"
    )
    fun getEventTasksListForDate(date: LocalDate): Flow<List<EventTasks>>

    @Transaction
    @Query(
        "SELECT * FROM event WHERE repetition IS NOT NULL AND :date > date " +
                "ORDER BY is_done ASC, time ASC"
    )
    fun getPastRepeatingEventTasksList(date: LocalDate): Flow<List<EventTasks>>

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
                // COUNT(e.event_id) is correct as it only counts non-NULL event IDs
                "    COUNT(e.event_id) AS eventCount " +
                "FROM DateSeries ds " +
                "LEFT JOIN event e ON " +
                // Condition for events that span multiple days
                "    (e.date_end IS NOT NULL AND ds.generated_date BETWEEN e.date AND e.date_end) " +
                // Condition for single-day events (where date_end might be NULL or same as date)
                "    OR (e.date_end IS NULL AND e.date = ds.generated_date) " +
                "GROUP BY ds.generated_date " +
                "HAVING COUNT(e.event_id) > 0"
    )
    fun getEventCountForDateRange(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Flow<List<DateEventCount>>

    @Transaction
    @Query("SELECT * FROM event WHERE event_id = :id")
    fun getEventTasksById(id: Long): Flow<EventTasks?>

    @Query("SELECT * FROM event WHERE event_id = :id")
    fun getEventById(id: Long): Flow<Event?>
}
