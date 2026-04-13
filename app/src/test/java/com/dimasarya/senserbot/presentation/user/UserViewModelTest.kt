package com.dimasarya.senserbot.presentation.user

import app.cash.turbine.test
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getUsersUseCase: GetUsersUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUsersUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state triggers getUsers and shows loading then success`() = runTest {
        val users = listOf(createUser(1, "Alice"), createUser(2, "Bob"))
        every { getUsersUseCase() } returns flowOf(
            Result.Loading,
            Result.Success(users)
        )

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(users, state.users)
            assertNull(state.error)
        }
    }

    @Test
    fun `getUsers sets error state when use case returns error`() = runTest {
        every { getUsersUseCase() } returns flowOf(
            Result.Loading,
            Result.Error(RuntimeException("Network error"))
        )

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Network error", state.error)
            assertTrue(state.users.isEmpty())
        }
    }

    @Test
    fun `getUsers sets error with Unknown error when exception message is null`() = runTest {
        every { getUsersUseCase() } returns flowOf(
            Result.Loading,
            Result.Error(RuntimeException())
        )

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Unknown error", state.error)
        }
    }

    @Test
    fun `getUsers sets loading state`() = runTest {
        every { getUsersUseCase() } returns flow {
            emit(Result.Loading)
            // Don't emit further to capture loading state
        }

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `getUsers clears error on retry success`() = runTest {
        // First call: error
        every { getUsersUseCase() } returns flowOf(
            Result.Loading,
            Result.Error(RuntimeException("Failed"))
        )

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val errorState = awaitItem()
            assertEquals("Failed", errorState.error)

            // Second call: success
            val users = listOf(createUser(1, "Alice"))
            every { getUsersUseCase() } returns flowOf(
                Result.Loading,
                Result.Success(users)
            )

            viewModel.getUsers()

            val successState = awaitItem()
            assertNull(successState.error)
            assertEquals(users, successState.users)
        }
    }

    @Test
    fun `getUsers handles empty user list`() = runTest {
        every { getUsersUseCase() } returns flowOf(
            Result.Loading,
            Result.Success(emptyList())
        )

        val viewModel = UserViewModel(getUsersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.users.isEmpty())
            assertNull(state.error)
        }
    }

    private fun createUser(id: Int, name: String): User = User(
        id = id,
        name = name,
        username = "user$id",
        email = "user$id@test.com",
        phone = "123-456",
        website = "test.com",
        city = "City",
        companyName = "Company"
    )
}
