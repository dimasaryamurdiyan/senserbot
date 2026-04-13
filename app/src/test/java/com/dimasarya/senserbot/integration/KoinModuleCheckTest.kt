package com.dimasarya.senserbot.integration

import com.dimasarya.senserbot.core.di.networkModule
import com.dimasarya.senserbot.core.di.repositoryModule
import com.dimasarya.senserbot.core.di.useCaseModule
import com.dimasarya.senserbot.core.di.viewModelModule
import com.dimasarya.senserbot.core.di.wsModule
import com.dimasarya.senserbot.core.websocket.WebSocketManager
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.domain.repository.TaskRepository
import com.dimasarya.senserbot.domain.repository.UserRepository
import com.dimasarya.senserbot.domain.usecase.AddTaskUseCase
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.domain.usecase.ObserveTasksUseCase
import com.dimasarya.senserbot.domain.usecase.RemoveTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ToggleTaskUseCase
import okhttp3.OkHttpClient
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * Verifies that all Koin modules have their dependencies satisfied.
 * This catches DI wiring issues at test time rather than at runtime.
 */
class KoinModuleCheckTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `networkModule dependencies are satisfied`() {
        networkModule.verify()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `wsModule dependencies are satisfied`() {
        wsModule.verify(
            extraTypes = listOf(OkHttpClient::class)
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `repositoryModule dependencies are satisfied`() {
        repositoryModule.verify(
            extraTypes = listOf(UserApi::class, WebSocketManager::class)
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `useCaseModule dependencies are satisfied`() {
        useCaseModule.verify(
            extraTypes = listOf(UserRepository::class, TaskRepository::class)
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `viewModelModule dependencies are satisfied`() {
        viewModelModule.verify(
            extraTypes = listOf(
                GetUsersUseCase::class,
                ObserveTasksUseCase::class,
                AddTaskUseCase::class,
                ToggleTaskUseCase::class,
                RemoveTaskUseCase::class,
                TaskRepository::class
            )
        )
    }
}
