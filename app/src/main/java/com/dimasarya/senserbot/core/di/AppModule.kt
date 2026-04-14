package com.dimasarya.senserbot.core.di

import com.dimasarya.senserbot.core.network.RetrofitClient
import com.dimasarya.senserbot.core.websocket.WebSocketManager
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.repository.TaskRepositoryImpl
import com.dimasarya.senserbot.data.repository.UserRepositoryImpl
import com.dimasarya.senserbot.domain.repository.TaskRepository
import com.dimasarya.senserbot.domain.repository.UserRepository
import com.dimasarya.senserbot.domain.usecase.AddTaskUseCase
import com.dimasarya.senserbot.domain.usecase.GetUsersUseCase
import com.dimasarya.senserbot.domain.usecase.ObserveTasksUseCase
import com.dimasarya.senserbot.domain.usecase.RemoveTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ToggleTaskUseCase
import com.dimasarya.senserbot.presentation.task.TaskViewModel
import com.dimasarya.senserbot.presentation.user.UserViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single { RetrofitClient.okHttpClient }
    single { RetrofitClient.retrofit }
    single<UserApi> { get<retrofit2.Retrofit>().create(UserApi::class.java) }
}

val wsModule = module {
    single { WebSocketManager(get()) }
}

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<TaskRepository> {
        TaskRepositoryImpl(
            wsManager = get(),
            json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        )
    }
}

val useCaseModule = module {
    factory { GetUsersUseCase(get()) }
    factory { ObserveTasksUseCase(get()) }
    factory { AddTaskUseCase(get()) }
    factory { ToggleTaskUseCase(get()) }
    factory { RemoveTaskUseCase(get()) }
}

val viewModelModule = module {
    viewModel { UserViewModel(get()) }
    viewModel { TaskViewModel(get(), get(), get(), get(), get()) }
}

val appModules = listOf(
    networkModule,
    wsModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
