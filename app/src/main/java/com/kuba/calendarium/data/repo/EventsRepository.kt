package com.kuba.calendarium.data.repo

import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.EventTasks
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.util.isHappeningOnDate
import com.kuba.calendarium.util.isRepeatingOnDate
import com.kuba.calendarium.util.standardDateFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class EventsRepository @Inject constructor(private val dao: EventDao) {
    fun getEventTasksListForDate(date: LocalDate): Flow<List<EventTasks>> {
        val dateEvents = dao.getEventTasksListForDate(date).also {
            Timber.d("Fetched Events flow for date: ${date.standardDateFormat()}")
        }
        val pastEvents = dao.getPastRepeatingEventTasksList(date).map { eventList ->
            eventList.filter { event -> event.event.isRepeatingOnDate(date) }
        }.also {
            Timber.d("Fetched Past Events flow for date: ${date.standardDateFormat()}")
        }

        return combine(dateEvents, pastEvents) { dateEvents, pastEvents ->
            dateEvents + pastEvents
        }.map { it.distinctBy { it.event.id } }
    }

    fun getEventsForDate(date: LocalDate): Flow<List<Event>> =
        dao.getEventsForDate(date).also {
            Timber.d("Fetched Events flow for date: ${date.standardDateFormat()}")
        }

    fun getEventTasksById(id: Long): Flow<EventTasks?> = dao.getEventTasksById(id).map {
        it?.copy(tasks = it.tasks.sortedBy { it.position })
    }.also {
        Timber.d("Fetched Event flow for id: $id")
    }

    fun getEventById(id: Long): Flow<Event?> = dao.getEventById(id).also {
        Timber.d("Fetched Event flow for id: $id")
    }

    fun getEventCountForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Map<LocalDate, Int>> {
        val nonRepeatingEvents = dao.getEventsForDateRange(startDate, endDate).map {
            it.filter { it.repetition == null }
        }
        val repeatingEvents = dao.getEventsForDateRange(startDate = null, endDate = endDate).map {
            it.filter { it.repetition != null }
        }

        return combine(nonRepeatingEvents, repeatingEvents) { nonRepeatingEvents, repeatingEvents ->
            val daysToHandle = startDate.until(endDate, ChronoUnit.DAYS) + 1
            val startDate = startDate.minusDays(1)
            val dateEventCountList = mutableMapOf<LocalDate, Int>()

            for (i in 0..daysToHandle) {
                val currentDay = startDate.plusDays(i)

                val repeatingCount = repeatingEvents.count { it.isRepeatingOnDate(currentDay) }
                val currentCount = nonRepeatingEvents.count { it.isHappeningOnDate(currentDay) }

                dateEventCountList[currentDay] = currentCount + repeatingCount
            }

            dateEventCountList
        }
    }

    suspend fun insertEvent(event: Event) = dao.insert(event).also {
        Timber.d("Inserted event: ${event.title} with id: $it")
    }

    suspend fun insertEventWithTasks(event: Event, tasks: List<Task>) =
        dao.insertEventWithTasks(event, tasks).also {
            Timber.d("Inserted event: ${event.title} with id: ${event.id} and tasks $tasks")
        }

    suspend fun updateEvent(event: Event) = dao.update(event).also {
        Timber.d("Updated event: ${event.title} with id: ${event.id}")
    }

    suspend fun updateEventWithTasks(event: Event, tasks: List<Task>) =
        dao.updateEventWithTasks(event, tasks).also {
            Timber.d("Updated event: ${event.title} with id: ${event.id} and tasks: $tasks")
        }

    suspend fun deleteEvent(event: Event) = dao.delete(event).also {
        Timber.d("Deleted event: ${event.title} with id: ${event.id}")
    }

    suspend fun updateTask(task: Task, done: Boolean) =
        dao.updateTask(task.copy(done = done)).also {
            Timber.d("Updated task: ${task.title} with id: ${task.id} to done: $done")
        }
}
