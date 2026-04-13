package com.dimasarya.senserbot.domain.usecase

import app.cash.turbine.test
import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.domain.model.Task
import com.dimasarya.senserbot.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TaskUseCasesTest {

    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        every { taskRepository.connectionState } returns MutableStateFlow(ConnectionState.Disconnected)
    }

    @Test
    fun `ObserveTasksUseCase delegates to repository observeTasks`() = runTest {
        val tasks = listOf(createTask("1", "Task A"), createTask("2", "Task B"))
        every { taskRepository.observeTasks() } returns flowOf(tasks)

        val useCase = ObserveTasksUseCase(taskRepository)

        useCase().test {
            assertEquals(tasks, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `AddTaskUseCase calls repository addTask with correct title`() = runTest {
        val useCase = AddTaskUseCase(taskRepository)

        useCase("Buy groceries")

        coVerify { taskRepository.addTask("Buy groceries") }
    }

    @Test
    fun `ToggleTaskUseCase calls repository toggleTask with correct id`() = runTest {
        val useCase = ToggleTaskUseCase(taskRepository)

        useCase("abc-123")

        coVerify { taskRepository.toggleTask("abc-123") }
    }

    @Test
    fun `RemoveTaskUseCase calls repository removeTask with correct id`() = runTest {
        val useCase = RemoveTaskUseCase(taskRepository)

        useCase("xyz-456")

        coVerify { taskRepository.removeTask("xyz-456") }
    }

    private fun createTask(id: String, title: String) = Task(
        id = id,
        title = title,
        isCompleted = false,
        createdAt = System.currentTimeMillis()
    )
}
