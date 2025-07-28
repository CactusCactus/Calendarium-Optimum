package com.kuba.calendarium.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kuba.calendarium.data.dao.EventDao
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.data.repo.EventsRepository
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
import java.time.LocalDate

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

            date = LocalDate.now()
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

        val retrievedEvent = eventDao.getEventTasksById(nonExistentEventId).first()

        assertThat(retrievedEvent).isNull()
    }

    @Test
    fun addEventThenUpdateAndCheckFlow() = runTest {
        val eventId = "addEventThenUpdateAndCheckFlow".hashCode().toLong()

        val eventOriginal = Event(
            id = eventId,
            title = "Original Test Event",
            description = "This is a test event",
            date = LocalDate.now(),
        )

        val updatedEvent = Event(
            id = eventId,
            title = "Updated Test Event",
            description = "This is a test event",
            date = LocalDate.now()
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
            date = LocalDate.now()
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
        val date = LocalDate.now()

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
            date = LocalDate.now()
        )

        val updatedEvent = Event(
            id = eventId,
            title = "Updated Test Event",
            description = "This is an updated test event",
            date = LocalDate.now()
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
            date = LocalDate.now()
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
        val dateStart = LocalDate.now()
        val dateMiddle = dateStart.plusDays(1)
        val dateEnd = dateStart.plusDays(2)

        // Fail scenario
        val dateAfterEnd = dateStart.plusDays(3)


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

    @Test
    fun insertEventsAndCountThem() = runTest {
        val dateStart = LocalDate.now().minusDays(10)
        val dateMiddle = LocalDate.now()
        val dateEnd = LocalDate.now().plusDays(10)

        val eventStartEdge = Event(
            id = 1,
            title = "Test Event",
            description = "This is a test event",
            date = dateStart
        )

        val eventMiddle1 = Event(
            id = 2,
            title = "Test Event",
            description = "This is a test event",
            date = dateMiddle
        )

        val eventMiddle2 = Event(
            id = 3,
            title = "Test Event",
            description = "This is a test event",
            date = dateMiddle
        )

        val eventEndEdge = Event(
            id = 4,
            title = "Test Event",
            description = "This is a test event",
            date = dateEnd
        )

        repository.insertEvent(eventStartEdge)
        repository.insertEvent(eventMiddle1)
        repository.insertEvent(eventMiddle2)
        repository.insertEvent(eventEndEdge)

        repository.getEventCountForDateRange(dateStart, dateEnd).test {
            val eventMap = awaitItem()

            assertThat(eventMap).containsExactly(
                dateStart, 1,
                dateMiddle, 2,
                dateEnd, 1
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun insertEventWithTasks() = runTest {
        val eventId = "insertEventWithTasks".hashCode().toLong()
        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",
            date = LocalDate.now()
        )

        val taskList = listOf<Task>(
            Task(id = 1, title = "Test Task 1"), Task(id = 2, title = "Test Task 2")
        )

        repository.insertEventWithTasks(event, taskList)

        repository.getEventTasksById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent).isNotNull()
            assertThat(emittedEvent?.event).isEqualTo(event)
            assertThat(emittedEvent?.tasks).hasSize(taskList.size)
            assertThat(emittedEvent?.tasks?.firstOrNull()?.eventIdRef).isEqualTo(eventId)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun editEventTasks() = runTest {
        val eventId = "editEventTasks".hashCode().toLong()
        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",
            date = LocalDate.now()
        )

        val taskList = listOf<Task>(
            Task(id = 1, title = "Test Task 1"), Task(id = 2, title = "Test Task 2")
        )

        repository.insertEventWithTasks(event, taskList)

        repository.getEventTasksById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent?.tasks).hasSize(taskList.size)

            cancelAndConsumeRemainingEvents()
        }

        val newTaskList = taskList.toMutableList().apply {
            removeLast()
            set(0, taskList[0].copy(title = "Changed Task"))
        }

        repository.updateEventWithTasks(event, newTaskList)

        repository.getEventTasksById(eventId).test {
            val emittedEvent = awaitItem()
            assertThat(emittedEvent?.tasks).hasSize(newTaskList.size)
            assertThat(emittedEvent?.tasks?.firstOrNull()?.title).isEqualTo("Changed Task")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun updateDoneStatusOfAnInsertedTask() = runTest {
        val eventId = "updateDoneStatusOfAnInsertedTask".hashCode().toLong()
        val event = Event(
            id = eventId,
            title = "Test Event",
            description = "This is a test event",
            date = LocalDate.now()
        )
        val taskList = listOf<Task>(
            Task(id = 1, title = "Test Task 1"), Task(id = 2, title = "Test Task 2")
        )

        repository.insertEventWithTasks(event, taskList)
        var fetchedTasks = emptyList<Task>()

        repository.getEventTasksById(eventId).test {
            val emittedEvent = awaitItem()

            emittedEvent?.tasks?.forEach {
                assertThat(it.done).isFalse()
            }

            fetchedTasks = emittedEvent?.tasks ?: emptyList()

            cancelAndConsumeRemainingEvents()
        }

        repository.updateTask(fetchedTasks[0], true)
        repository.updateTask(fetchedTasks[1], true)

        repository.getEventTasksById(eventId).test {
            val emittedEvent = awaitItem()

            emittedEvent?.tasks?.forEach {
                assertThat(it.done).isTrue()
            }

            cancelAndConsumeRemainingEvents()
        }
    }
}