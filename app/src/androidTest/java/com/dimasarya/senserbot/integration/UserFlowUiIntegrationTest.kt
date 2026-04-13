package com.dimasarya.senserbot.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.remote.dto.AddressDto
import com.dimasarya.senserbot.data.remote.dto.CompanyDto
import com.dimasarya.senserbot.data.remote.dto.GeoDto
import com.dimasarya.senserbot.data.remote.dto.UserDto
import com.dimasarya.senserbot.data.repository.UserRepositoryImpl
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.repository.UserRepository
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.presentation.user.UserScreen
import com.dimasarya.senserbot.presentation.user.UserViewModel
import com.dimasarya.senserbot.ui.theme.BoilerplateCodeTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end integration test that renders UserScreen with a fake API
 * and verifies the full pipeline: FakeApi -> Repository -> UseCase -> ViewModel -> Compose UI.
 */
class UserFlowUiIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fullFlowDisplaysUsersFromFakeApi() {
        val fakeApi = FakeUserApi(
            users = listOf(
                createUserDto(1, "Leanne Graham", "Bret", "Sincere@april.biz", "Gwenborough", "Romaguera-Crona"),
                createUserDto(2, "Ervin Howell", "Antonette", "Shanna@melissa.tv", "Wisokyburgh", "Deckow-Crist")
            )
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Users").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leanne Graham").assertIsDisplayed()
        composeTestRule.onNodeWithText("@Bret").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sincere@april.biz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ervin Howell").assertIsDisplayed()
        composeTestRule.onNodeWithText("@Antonette").assertIsDisplayed()
    }

    @Test
    fun fullFlowDisplaysErrorFromFakeApi() {
        val fakeApi = FakeUserApi(error = RuntimeException("Server error"))
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Server error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun fullFlowRetryAfterError() {
        val fakeApi = FakeUserApi(error = RuntimeException("Network error"))
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()

        // Fix the API and retry
        fakeApi.error = null
        fakeApi.users = listOf(
            createUserDto(1, "Alice Johnson", "alice", "alice@test.com", "Boston", "Tech Co")
        )

        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("@alice").assertIsDisplayed()
    }

    @Test
    fun fullFlowDisplaysCityAndCompanyName() {
        val fakeApi = FakeUserApi(
            users = listOf(
                createUserDto(1, "Test User", "tuser", "t@t.com", "San Francisco", "Google")
            )
        )
        val repository = UserRepositoryImpl(fakeApi)
        val useCase = GetUsersUseCase(repository)
        val viewModel = UserViewModel(useCase)

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("San Francisco \u2022 Google").assertIsDisplayed()
    }
}

private class FakeUserApi(
    var users: List<UserDto> = emptyList(),
    var error: Exception? = null
) : UserApi {

    override suspend fun getUsers(): List<UserDto> {
        error?.let { throw it }
        return users
    }

    override suspend fun getUserById(id: Int): UserDto {
        error?.let { throw it }
        return users.first { it.id == id }
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
