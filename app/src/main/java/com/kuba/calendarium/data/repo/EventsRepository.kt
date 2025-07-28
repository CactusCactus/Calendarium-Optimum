package com.kuba.calendarium.data.repo

import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.util.standardDateFormat
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class EventsRepository @Inject constructor(private val dao: EventDao) {
    fun getEventsForDate(date: LocalDate) = dao.getEventsForDate(date).map { eventList ->
        eventList.map { event -> event.copy(tasks = event.tasks.sortedBy { it.position }) }

    }.also {
        Timber.d("Fetched Events flow for date: ${date.standardDateFormat()}")
    }

    fun getEventById(id: Long) = dao.getEventById(id).map {
        it?.copy(tasks = it.tasks.sortedBy { it.position })
    }.also {
        Timber.d("Fetched Event flow for id: $id")
    }

    fun getEventCountForDateRange(startDate: LocalDate, endDate: LocalDate) =
        dao.getEventCountForDateRange(startDate, endDate).map {
            it.associate { it.eventDate to it.eventCount }
        }.also {
            Timber.d("Fetched EventCount flow for date range: $startDate - $endDate")
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
