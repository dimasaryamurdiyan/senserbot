package com.dimasarya.senserbot.domain.repository

import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<Result<List<User>>>
    fun getUserById(id: Int): Flow<Result<User>>
}
