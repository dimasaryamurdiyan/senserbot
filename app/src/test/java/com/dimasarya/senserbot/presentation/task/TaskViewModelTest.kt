package com.dimasarya.senserbot.presentation.task

import app.cash.turbine.test
import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.domain.model.Task
import com.dimasarya.senserbot.domain.repository.TaskRepository
import com.dimasarya.senserbot.domain.usecase.AddTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ObserveTasksUseCase
import com.dimasarya.senserbot.domain.usecase.RemoveTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ToggleTaskUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var observeTasksUseCase: ObserveTasksUseCase
    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var toggleTaskUseCase: ToggleTaskUseCase
    private lateinit var removeTaskUseCase: RemoveTaskUseCase
    private lateinit var taskRepository: TaskRepository

    private val connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        observeTasksUseCase = mockk()
        addTaskUseCase = mockk(relaxed = true)
        toggleTaskUseCase = mockk(relaxed = true)
        removeTaskUseCase = mockk(relaxed = true)
        taskRepository = mockk()
        every { taskRepository.connectionState } returns connectionStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TaskViewModel {
        return TaskViewModel(
            observeTasksUseCase,
            addTaskUseCase,
            toggleTaskUseCase,
            removeTaskUseCase,
            taskRepository
        )
    }

    @Test
    fun `initial state has empty tasks and disconnected state`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.tasks.isEmpty())
            assertEquals(ConnectionState.Disconnected, state.connectionState)
            assertEquals("", state.inputText)
            assertNull(state.error)
        }
    }

    @Test
    fun `tasks update when observeTasksUseCase emits`() = runTest {
        val tasks = listOf(createTask("1", "Task A"))
        every { observeTasksUseCase() } returns flowOf(tasks)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(tasks, state.tasks)
        }
    }

    @Test
    fun `connectionState propagates from repository`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial

            connectionStateFlow.value = ConnectionState.Connected

            val state = awaitItem()
            assertEquals(ConnectionState.Connected, state.connectionState)
        }
    }

    @Test
    fun `onInputChanged updates inputText`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onInputChanged("Hello world")

            val state = awaitItem()
            assertEquals("Hello world", state.inputText)
        }
    }

    @Test
    fun `onAddTask calls AddTaskUseCase and clears inputText`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.onInputChanged("Buy milk")
        viewModel.onAddTask()

        coVerify { addTaskUseCase("Buy milk") }

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `onAddTask does nothing when inputText is blank`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.onAddTask()

        coVerify(exactly = 0) { addTaskUseCase(any()) }
    }

    @Test
    fun `onToggleTask calls ToggleTaskUseCase with correct id`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.onToggleTask("abc-123")

        coVerify { toggleTaskUseCase("abc-123") }
    }

    @Test
    fun `onRemoveTask calls RemoveTaskUseCase with correct id`() = runTest {
        every { observeTasksUseCase() } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.onRemoveTask("xyz-456")

        coVerify { removeTaskUseCase("xyz-456") }
    }

    private fun createTask(id: String, title: String) = Task(
        id = id,
        title = title,
        isCompleted = false,
        createdAt = System.currentTimeMillis()
    )
}
