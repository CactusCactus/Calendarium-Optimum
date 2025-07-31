package com.kuba.calendarium.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.EventDetailed
import com.kuba.calendarium.data.model.Reminder
import com.kuba.calendarium.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventTask: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Transaction
    suspend fun insertEventDetailed(event: Event, tasks: List<Task>, reminders: List<Reminder>) {
        val eventId = insert(event)
        tasks.forEach { task -> insert(task.copy(eventIdRef = eventId)) }
        reminders.forEach { reminder -> insert(reminder.copy(eventIdRef = eventId)) }
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: Event)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(eventTask: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventDetailed(event: Event, tasks: List<Task>, reminders: List<Reminder>) {
        update(event)
        deleteTasksByEventId(event.id)
        tasks.forEach { task -> insert(task.copy(eventIdRef = event.id)) }

        deleteRemindersByEventId(event.id)
        reminders.forEach { reminder -> insert(reminder.copy(eventIdRef = event.id)) }
    }

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM task WHERE event_id_ref = :eventId")
    suspend fun deleteTasksByEventId(eventId: Long)

    @Query("DELETE FROM reminder WHERE event_id_ref = :eventId")
    suspend fun deleteRemindersByEventId(eventId: Long)

    @Transaction
    @Query(
        "SELECT * FROM event WHERE (date_end IS NOT NULL AND :date BETWEEN date AND date_end) " +
                "OR :date == date ORDER BY is_done ASC, time ASC"
    )
    fun getEventDetailedListForDate(date: LocalDate): Flow<List<EventDetailed>>

    @Transaction
    @Query(
        "SELECT * FROM event WHERE repetition IS NOT NULL AND :date > date " +
                "ORDER BY is_done ASC, time ASC"
    )
    fun getPastRepeatingEventDetailedList(date: LocalDate): Flow<List<EventDetailed>>

    @Query(
        "SELECT * FROM event WHERE (date_end IS NOT NULL AND :date BETWEEN date AND date_end) " +
                "OR :date == date ORDER BY is_done ASC, time ASC"
    )
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>

    @Query(
        "SELECT * FROM event WHERE " +
                // Events that start and end within the range
                "(:startDate IS NULL OR date >= :startDate) AND " +
                "(:endDate IS NULL OR date_end <= :endDate) OR " +
                // Events that start before the range and end within or after the range
                "(:startDate IS NULL OR date < :startDate) AND " +
                "(:endDate IS NULL OR date_end >= :startDate) OR " +
                // Events that start within the range and end after the range
                "(:startDate IS NULL OR date <= :endDate) AND " +
                "(:endDate IS NULL OR date_end > :endDate) OR " +
                // Events that start before and end after the range (enclosing the range)
                "(:startDate IS NULL OR date < :startDate) AND " +
                "(:endDate IS NULL OR date_end > :endDate) OR " +
                // Single day events within the range (date_end is null)
                "(:startDate IS NULL OR date >= :startDate) AND " +
                "(:endDate IS NULL OR date <= :endDate) AND date_end IS NULL " +
                "ORDER BY is_done ASC, time ASC"
    )
    fun getEventsForDateRange(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<List<Event>>

    @Transaction
    @Query("SELECT * FROM event WHERE event_id = :id")
    fun getEventDetailedById(id: Long): Flow<EventDetailed?>

    @Query("SELECT * FROM event WHERE event_id = :id")
    fun getEventById(id: Long): Flow<Event?>
}
