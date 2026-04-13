package com.dimasarya.senserbot.data.repository

import app.cash.turbine.test
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.remote.dto.AddressDto
import com.dimasarya.senserbot.data.remote.dto.CompanyDto
import com.dimasarya.senserbot.data.remote.dto.GeoDto
import com.dimasarya.senserbot.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var userApi: UserApi
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {
        userApi = mockk()
        repository = UserRepositoryImpl(userApi)
    }

    @Test
    fun `getUsers emits Loading then Success with mapped users`() = runTest {
        val dtoList = listOf(createUserDto(id = 1, name = "Alice"), createUserDto(id = 2, name = "Bob"))
        coEvery { userApi.getUsers() } returns dtoList

        repository.getUsers().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            val users = (success as Result.Success).data
            assertEquals(2, users.size)
            assertEquals("Alice", users[0].name)
            assertEquals("Bob", users[1].name)

            awaitComplete()
        }
    }

    @Test
    fun `getUsers emits Loading then Error when api throws`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { userApi.getUsers() } throws exception

        repository.getUsers().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).exception.message)

            awaitComplete()
        }
    }

    @Test
    fun `getUsers returns empty list when api returns empty`() = runTest {
        coEvery { userApi.getUsers() } returns emptyList()

        repository.getUsers().test {
            awaitItem() // Loading
            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(0, (success as Result.Success).data.size)
            awaitComplete()
        }
    }

    @Test
    fun `getUserById emits Loading then Success with mapped user`() = runTest {
        val dto = createUserDto(id = 5, name = "Charlie")
        coEvery { userApi.getUserById(5) } returns dto

        repository.getUserById(5).test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            val user = (success as Result.Success).data
            assertEquals(5, user.id)
            assertEquals("Charlie", user.name)

            awaitComplete()
        }
    }

    @Test
    fun `getUserById emits Loading then Error when api throws`() = runTest {
        coEvery { userApi.getUserById(99) } throws RuntimeException("Not found")

        repository.getUserById(99).test {
            awaitItem() // Loading
            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Not found", (error as Result.Error).exception.message)
            awaitComplete()
        }
    }

    private fun createUserDto(
        id: Int = 1,
        name: String = "Test User"
    ): UserDto = UserDto(
        id = id,
        name = name,
        username = "user$id",
        email = "user$id@test.com",
        phone = "123-456",
        website = "test.com",
        address = AddressDto(
            street = "Street",
            suite = "Suite",
            city = "City",
            zipcode = "00000",
            geo = GeoDto(lat = "0.0", lng = "0.0")
        ),
        company = CompanyDto(
            name = "Company",
            catchPhrase = "Phrase",
            bs = "bs"
        )
    )
}