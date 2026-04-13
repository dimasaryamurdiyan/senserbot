package com.dimasarya.senserbot.presentation.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.ui.theme.BoilerplateCodeTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class UserScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsUserListWhenLoadSucceeds() {
        val users = listOf(
            createUser(1, "Alice Johnson", "alice", "alice@test.com", "New York", "Acme Corp"),
            createUser(2, "Bob Smith", "bob", "bob@test.com", "London", "Tech Inc")
        )
        val useCase = mockk<GetUsersUseCase>()
        every { useCase() } returns flowOf(Result.Success(users))

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = UserViewModel(useCase))
            }
        }

        composeTestRule.onNodeWithText("Users").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("@alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("alice@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("@bob").assertIsDisplayed()
    }

    @Test
    fun showsCityAndCompanyForUsers() {
        val users = listOf(
            createUser(1, "Alice Johnson", "alice", "alice@test.com", "New York", "Acme Corp")
        )
        val useCase = mockk<GetUsersUseCase>()
        every { useCase() } returns flowOf(Result.Success(users))

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = UserViewModel(useCase))
            }
        }

        composeTestRule.onNodeWithText("New York \u2022 Acme Corp").assertIsDisplayed()
    }

    @Test
    fun showsErrorMessageWhenLoadFails() {
        val useCase = mockk<GetUsersUseCase>()
        every { useCase() } returns flowOf(Result.Error(RuntimeException("Connection failed")))

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = UserViewModel(useCase))
            }
        }

        composeTestRule.onNodeWithText("Connection failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun retryButtonTriggersReload() {
        val useCase = mockk<GetUsersUseCase>()
        // First call returns error
        every { useCase() } returns flowOf(Result.Error(RuntimeException("Failed")))

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = UserViewModel(useCase))
            }
        }

        // Now set up success for retry
        val users = listOf(createUser(1, "Alice", "alice", "a@t.com", "City", "Corp"))
        every { useCase() } returns flowOf(Result.Success(users))

        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        // Verify use case was called multiple times (init + retry)
        verify(atLeast = 2) { useCase() }
    }

    @Test
    fun showsTopAppBarWithTitle() {
        val useCase = mockk<GetUsersUseCase>()
        every { useCase() } returns flowOf(Result.Success(emptyList()))

        composeTestRule.setContent {
            BoilerplateCodeTheme {
                UserScreen(viewModel = UserViewModel(useCase))
            }
        }

        composeTestRule.onNodeWithText("Users").assertIsDisplayed()
    }

    private fun createUser(
        id: Int,
        name: String,
        username: String,
        email: String,
        city: String,
        companyName: String
    ): User = User(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = "123-456",
        website = "test.com",
        city = city,
        companyName = companyName
    )
}
