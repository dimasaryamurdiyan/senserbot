package com.dimasarya.senserbot.domain.usecase

import app.cash.turbine.test
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUsersUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var getUsersUseCase: GetUsersUseCase

    @Before
    fun setup() {
        userRepository = mockk()
        getUsersUseCase = GetUsersUseCase(userRepository)
    }

    @Test
    fun `invoke delegates to repository getUsers`() = runTest {
        val users = listOf(createUser(1, "Alice"), createUser(2, "Bob"))
        every { userRepository.getUsers() } returns flowOf(
            Result.Loading,
            Result.Success(users)
        )

        getUsersUseCase().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(users, (success as Result.Success).data)

            awaitComplete()
        }

        verify(exactly = 1) { userRepository.getUsers() }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val exception = RuntimeException("Failed")
        every { userRepository.getUsers() } returns flowOf(
            Result.Loading,
            Result.Error(exception)
        )

        getUsersUseCase().test {
            awaitItem() // Loading
            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Failed", (error as Result.Error).exception.message)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list on success with no users`() = runTest {
        every { userRepository.getUsers() } returns flowOf(
            Result.Loading,
            Result.Success(emptyList())
        )

        getUsersUseCase().test {
            awaitItem() // Loading
            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(0, (success as Result.Success).data.size)
            awaitComplete()
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