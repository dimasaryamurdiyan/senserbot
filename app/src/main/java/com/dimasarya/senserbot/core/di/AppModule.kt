package com.dimasarya.senserbot.core.di

import com.dimasarya.senserbot.core.network.RetrofitClient
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.repository.UserRepositoryImpl
import com.dimasarya.senserbot.domain.repository.UserRepository
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.presentation.user.UserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single<UserApi> { RetrofitClient.retrofit.create(UserApi::class.java) }
}

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { GetUsersUseCase(get()) }
}

val viewModelModule = module {
    viewModel { UserViewModel(get()) }
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
