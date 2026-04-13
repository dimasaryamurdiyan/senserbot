package com.dimasarya.senserbot.data.repository

import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.data.remote.api.UserApi
import com.dimasarya.senserbot.data.remote.dto.toDomain
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {

    override fun getUsers(): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        try {
            val users = userApi.getUsers().map { it.toDomain() }
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getUserById(id: Int): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val user = userApi.getUserById(id).toDomain()
            emit(Result.Success(user))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
