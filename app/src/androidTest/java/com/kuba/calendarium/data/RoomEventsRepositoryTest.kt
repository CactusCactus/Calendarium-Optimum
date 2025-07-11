package com.kuba.calendarium.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.repo.EventsRepository
import com.kuba.calendarium.util.resetToMidnight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RoomEventsRepositoryTest {
    private lateinit var eventDao: EventDao
    private lateinit var db: AppDatabase
    private lateinit var repository: EventsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        eventDao = db.eventDao()
        repository = EventsRepository(eventDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testInsertAndRetrieveSameEvent() = runTest {
        val eventId = "testInsertAndRetrieveSameEvent".hashCode().toLong()

        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",

            date = System.currentTimeMillis()
        )

        val insertedId = repository.insertEvent(event)
        assert(insertedId == eventId)

        val retrievedEvent = repository.getEventById(eventId).first()

        assertThat(retrievedEvent).isNotNull()
        assert(retrievedEvent == event)
    }

    @Test
    fun retrieveNonExistentEvent() = runTest {
        val nonExistentEventId = 2137L

        val retrievedEvent = eventDao.getEventById(nonExistentEventId).first()

        assertThat(retrievedEvent).isNull()
    }

    @Test
    fun addEventThenUpdateAndCheckFlow() = runTest {
        val eventId = "addEventThenUpdateAndCheckFlow".hashCode().toLong()

        val eventOriginal = Event(
            id = eventId,
            title = "Original Test Event",
            description = "This is a test event",
            date = System.currentTimeMillis(),
        )

        val updatedEvent = Event(
            id = eventId,
            title = "Updated Test Event",
            description = "This is a test event",
            date = System.currentTimeMillis()
        )

        repository.getEventById(eventId).test {
            assertThat(awaitItem()).isNull()

            repository.insertEvent(eventOriginal)
            val emittedOriginalEvent = awaitItem()
            assertThat(emittedOriginalEvent).isNotNull()
            assertThat(emittedOriginalEvent).isEqualTo(eventOriginal)

            repository.insertEvent(updatedEvent)
            val emittedUpdatedEvent = awaitItem()
            assertThat(emittedUpdatedEvent).isNotNull()
            assertThat(emittedUpdatedEvent).isEqualTo(updatedEvent)

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addEventThenDeleteAndCheckFlow() = runTest {
        val eventId = "addEventThenDeleteAndCheckFlow".hashCode().toLong()

        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",
            date = System.currentTimeMillis()
        )

        repository.insertEvent(event)
        repository.getEventById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent).isNotNull()
            assertThat(emittedEvent).isEqualTo(event)

            repository.deleteEvent(event)
            val deletedEvent = awaitItem()
            assertThat(deletedEvent).isNull()

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addEventsAndCheckFlowByDate() = runTest {
        val eventId1 = "addEventsAndCheckFlow1".hashCode().toLong()
        val eventId2 = "addEventsAndCheckFlow2".hashCode().toLong()
        val date = System.currentTimeMillis().resetToMidnight()

        val event1 = Event(
            id = eventId1,
            title = "Test Event 1",
            description = "This is a test event 1",
            date = date
        )

        val event2 = Event(
            id = eventId2,
            title = "Test Event 2",
            description = "This is a test event 2",
            date = date
        )

        repository.insertEvent(event1)
        repository.insertEvent(event2)

        repository.getEventsForDate(date).test {
            val emittedEvents = awaitItem()
            assertThat(emittedEvents).containsExactly(event1, event2)

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun updateExistingEvent() = runTest {
        val eventId = "updateExistingEvent".hashCode().toLong()
        val originalEvent = Event(
            id = eventId,
            title = "Original Test Event",
            description = "This is a test event",
            date = System.currentTimeMillis()
        )

        val updatedEvent = Event(
            id = eventId,
            title = "Updated Test Event",
            description = "This is an updated test event",
            date = System.currentTimeMillis()
        )

        repository.insertEvent(originalEvent)
        repository.updateEvent(updatedEvent)

        repository.getEventById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent).isEqualTo(updatedEvent)
            assertThat(emittedEvent?.id).isEqualTo(originalEvent.id)

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun updateNonExistingEvent() = runTest {
        val eventId = "updateNonExistingEvent".hashCode().toLong()
        val updatedEvent = Event(
            id = eventId,
            title = "Updated Test Event",
            description = "This is an updated test event",
            date = System.currentTimeMillis()
        )

        repository.updateEvent(updatedEvent)

        repository.getEventById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent).isNull()

            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun insertEventAndFetchItForMultipleDates() = runTest {
        val eventId = "insertEventAndFetchItForMultipleDates".hashCode().toLong()
        val dateStart = System.currentTimeMillis().resetToMidnight()
        val dateMiddle = Calendar.getInstance().apply {
            timeInMillis = dateStart
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val dateEnd = Calendar.getInstance().apply {
            timeInMillis = dateStart
            add(Calendar.DAY_OF_MONTH, 2)
        }.timeInMillis

        // Fail scenario
        val dateAfterEnd = Calendar.getInstance().apply {
            timeInMillis = dateStart
            add(Calendar.DAY_OF_MONTH, 3)
        }.timeInMillis


        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",
            date = dateStart,
            dateEnd = dateEnd
        )

        repository.insertEvent(event)

        repository.getEventsForDate(dateStart).test {
            val emittedEvents = awaitItem()
            assertThat(emittedEvents).containsExactly(event)
        }

        repository.getEventsForDate(dateMiddle).test {
            val emittedEvents = awaitItem()
            assertThat(emittedEvents).containsExactly(event)
        }

        repository.getEventsForDate(dateEnd).test {
            val emittedEvents = awaitItem()
            assertThat(emittedEvents).containsExactly(event)
        }

        repository.getEventsForDate(dateAfterEnd).test {
            val emittedEvents = awaitItem()
            assertThat(emittedEvents).isEmpty()
        }
    }
}