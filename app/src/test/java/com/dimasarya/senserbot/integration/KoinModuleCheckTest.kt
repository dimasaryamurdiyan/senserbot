package com.dimasarya.senserbot.integration

import com.dimasarya.senserbot.core.di.networkModule
import com.dimasarya.senserbot.core.di.repositoryModule
import com.dimasarya.senserbot.core.di.useCaseModule
import com.dimasarya.senserbot.core.di.viewModelModule
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.domain.repository.UserRepository
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * Verifies that all Koin modules have their dependencies satisfied.
 * This catches DI wiring issues at test time rather than at runtime.
 *
 * Modules that depend on definitions from other modules use extraTypes
 * to declare those cross-module dependencies.
 */
class KoinModuleCheckTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `networkModule dependencies are satisfied`() {
        networkModule.verify()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `repositoryModule dependencies are satisfied`() {
        repositoryModule.verify(
            extraTypes = listOf(UserApi::class)
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `useCaseModule dependencies are satisfied`() {
        useCaseModule.verify(
            extraTypes = listOf(UserRepository::class)
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `viewModelModule dependencies are satisfied`() {
        viewModelModule.verify(
            extraTypes = listOf(GetUsersUseCase::class)
        )
    }
}
