package com.dimasarya.senserbot.data.repository

import app.cash.turbine.test
import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.core.websocket.WebSocketManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    private lateinit var wsManager: WebSocketManager
    private val messagesFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setup() {
        wsManager = mockk(relaxed = true)
        every { wsManager.messages } returns messagesFlow
        every { wsManager.connectionState } returns connectionStateFlow
        repository = TaskRepositoryImpl(wsManager, json)
    }

    @Test
    fun `SYNC_STATE message populates task list`() = runTest {
        val syncMessage = """
            {"type":"SYNC_STATE","payload":{"tasks":[
                {"id":"1","title":"Buy milk","is_completed":false,"created_at":1000},
                {"id":"2","title":"Walk dog","is_completed":true,"created_at":2000}
            ]}}
        """.trimIndent()

        repository.observeTasks().test {
            awaitItem() // initial empty list

            messagesFlow.emit(syncMessage)

            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            assertEquals("Buy milk", tasks[0].title)
            assertFalse(tasks[0].isCompleted)
            assertEquals("Walk dog", tasks[1].title)
            assertTrue(tasks[1].isCompleted)
        }
    }

    @Test
    fun `TASK_ADDED message appends task to list`() = runTest {
        val addMessage = """{"type":"TASK_ADDED","payload":{"id":"3","title":"Read book","is_completed":false,"created_at":3000}}"""

        repository.observeTasks().test {
            awaitItem() // empty

            messagesFlow.emit(addMessage)

            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Read book", tasks[0].title)
        }
    }

    @Test
    fun `TASK_TOGGLED message flips isCompleted`() = runTest {
        val sync = """{"type":"SYNC_STATE","payload":{"tasks":[{"id":"1","title":"Task","is_completed":false,"created_at":1000}]}}"""
        val toggle = """{"type":"TASK_TOGGLED","payload":{"id":"1","is_completed":true}}"""

        repository.observeTasks().test {
            awaitItem() // empty

            messagesFlow.emit(sync)
            awaitItem() // list with 1 task (isCompleted = false)

            messagesFlow.emit(toggle)
            val tasks = awaitItem()
            assertTrue(tasks[0].isCompleted)
        }
    }

    @Test
    fun `TASK_REMOVED message removes task from list`() = runTest {
        val sync = """{"type":"SYNC_STATE","payload":{"tasks":[{"id":"1","title":"Task","is_completed":false,"created_at":1000}]}}"""
        val remove = """{"type":"TASK_REMOVED","payload":{"id":"1"}}"""

        repository.observeTasks().test {
            awaitItem() // empty

            messagesFlow.emit(sync)
            awaitItem() // 1 task

            messagesFlow.emit(remove)
            val tasks = awaitItem()
            assertTrue(tasks.isEmpty())
        }
    }

    @Test
    fun `addTask sends correctly typed JSON message`() = runTest {
        repository.addTask("Buy groceries")

        verify {
            wsManager.send(match { msg ->
                msg.contains("\"type\":\"ADD_TASK\"") &&
                        msg.contains("\"title\":\"Buy groceries\"") &&
                        msg.contains("\"is_completed\":false")
            })
        }
    }

    @Test
    fun `toggleTask sends correct JSON with task id`() = runTest {
        repository.toggleTask("abc-123")

        verify {
            wsManager.send(match { msg ->
                msg.contains("\"type\":\"TOGGLE_TASK\"") &&
                        msg.contains("\"id\":\"abc-123\"")
            })
        }
    }

    @Test
    fun `removeTask sends correct JSON with task id`() = runTest {
        repository.removeTask("xyz-456")

        verify {
            wsManager.send(match { msg ->
                msg.contains("\"type\":\"REMOVE_TASK\"") &&
                        msg.contains("\"id\":\"xyz-456\"")
            })
        }
    }

    @Test
    fun `unknown message type is silently ignored`() = runTest {
        repository.observeTasks().test {
            awaitItem() // empty

            messagesFlow.emit("""{"type":"UNKNOWN","payload":{}}""")

            // No new emission expected
            expectNoEvents()
        }
    }
}
