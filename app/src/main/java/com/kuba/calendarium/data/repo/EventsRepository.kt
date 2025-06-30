package com.kuba.calendarium.data.repo

import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.util.standardDateFormat
import timber.log.Timber
import javax.inject.Inject

class EventsRepository @Inject constructor(private val dao: EventDao) {
    fun getEventsForDate(date: Long) = dao.getEventsForDate(date).also {
        Timber.d("Fetched Events flow for date: ${date.standardDateFormat()}")
    }

    fun getEventById(id: Long) = dao.getEventById(id).also {
        Timber.d("Fetched Event flow for id: $id")
    }

    suspend fun insertEvent(event: Event) = dao.insert(event).also {
        Timber.d("Inserted event: ${event.title}")
    }

    suspend fun deleteEvent(event: Event) = dao.delete(event).also {
        Timber.d("Deleted event: ${event.title}")
    }
}