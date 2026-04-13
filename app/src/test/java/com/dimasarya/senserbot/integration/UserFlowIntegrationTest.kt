package com.dimasarya.senserbot.integration

import app.cash.turbine.test
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.remote.dto.AddressDto
import com.dimasarya.senserbot.data.remote.dto.CompanyDto
import com.dimasarya.senserbot.data.remote.dto.GeoDto
import com.dimasarya.senserbot.data.remote.dto.UserDto
import com.dimasarya.senserbot.data.repository.UserRepositoryImpl
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.presentation.user.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/**
 * Integration tests that verify the full data flow:
 * FakeUserApi -> UserRepositoryImpl -> GetUsersUseCase -> UserViewModel
 *
 * These tests use a fake API implementation instead of mocks to verify
 * that all layers work together correctly, including DTO-to-domain mapping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserFlowIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `full flow from API to ViewModel with successful response`() = runTest {
        val fakeApi = FakeUserApi(
            usersResponse = listOf(
                createUserDto(1, "Leanne Graham", "Bret", "Sincere@april.biz", "Gwenborough", "Romaguera-Crona"),
                createUserDto(2, "Ervin Howell", "Antonette", "Shanna@melissa.tv", "Wisokyburgh", "Deckow-Crist")
            )
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(2, state.users.size)

            // Verify DTO-to-domain mapping worked through the full flow
            val firstUser = state.users[0]
            assertEquals(1, firstUser.id)
            assertEquals("Leanne Graham", firstUser.name)
            assertEquals("Bret", firstUser.username)
            assertEquals("Sincere@april.biz", firstUser.email)
            assertEquals("Gwenborough", firstUser.city)
            assertEquals("Romaguera-Crona", firstUser.companyName)

            val secondUser = state.users[1]
            assertEquals(2, secondUser.id)
            assertEquals("Ervin Howell", secondUser.name)
        }
    }

    @Test
    fun `full flow from API to ViewModel with error response`() = runTest {
        val fakeApi = FakeUserApi(
            error = RuntimeException("Server unavailable")
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals("Server unavailable", state.error)
            assertTrue(state.users.isEmpty())
        }
    }

    @Test
    fun `full flow retry recovers from error`() = runTest {
        val fakeApi = FakeUserApi(
            error = RuntimeException("Timeout")
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        viewModel.uiState.test {
            val errorState = awaitItem()
            assertEquals("Timeout", errorState.error)

            // Fix the API and retry
            fakeApi.error = null
            fakeApi.usersResponse = listOf(
                createUserDto(1, "Alice", "alice", "alice@test.com", "NYC", "Corp")
            )
            viewModel.getUsers()

            val successState = awaitItem()
            assertNull(successState.error)
            assertEquals(1, successState.users.size)
            assertEquals("Alice", successState.users[0].name)
        }
    }

    @Test
    fun `full flow with empty API response`() = runTest {
        val fakeApi = FakeUserApi(usersResponse = emptyList())
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertTrue(state.users.isEmpty())
        }
    }

    @Test
    fun `repository emits correct Result sequence`() = runTest {
        val fakeApi = FakeUserApi(
            usersResponse = listOf(createUserDto(1, "Alice", "alice", "a@b.com", "City", "Corp"))
        )
        val repository = UserRepositoryImpl(fakeApi)

        repository.getUsers().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(1, (success as Result.Success).data.size)

            awaitComplete()
        }
    }

    @Test
    fun `use case passes repository flow unchanged`() = runTest {
        val fakeApi = FakeUserApi(
            usersResponse = listOf(
                createUserDto(1, "User1", "u1", "u1@t.com", "C1", "Co1"),
                createUserDto(2, "User2", "u2", "u2@t.com", "C2", "Co2"),
                createUserDto(3, "User3", "u3", "u3@t.com", "C3", "Co3")
            )
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)

        useCase().test {
            awaitItem() // Loading
            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(3, (success as Result.Success).data.size)
            awaitComplete()
        }
    }
}

/**
 * Fake implementation of UserApi for integration testing.
 * Allows controlling responses without network calls or mocking frameworks.
 */
private class FakeUserApi(
    var usersResponse: List<UserDto> = emptyList(),
    var error: Exception? = null,
    private val userByIdResponses: MutableMap<Int, UserDto> = mutableMapOf()
) : UserApi {

    override suspend fun getUsers(): List<UserDto> {
        error?.let { throw it }
        return usersResponse
    }

    override suspend fun getUserById(id: Int): UserDto {
        error?.let { throw it }
        return userByIdResponses[id]
            ?: usersResponse.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("User with id $id not found")
    }
}

private fun createUserDto(
    id: Int,
    name: String,
    username: String,
    email: String,
    city: String,
    companyName: String
): UserDto = UserDto(
    id = id,
    name = name,
    username = username,
    email = email,
    phone = "123-456-7890",
    website = "example.com",
    address = AddressDto(
        street = "123 Main St",
        suite = "Apt 1",
        city = city,
        zipcode = "12345",
        geo = GeoDto(lat = "0.0", lng = "0.0")
    ),
    company = CompanyDto(
        name = companyName,
        catchPhrase = "Catchphrase",
        bs = "business stuff"
    )
)
