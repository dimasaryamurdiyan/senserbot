package com.dimasarya.senserbot.domain.usecase

import com.dimasarya.senserbot.core.common.Result
import com.dimasarya.senserbot.domain.model.User
import com.dimasarya.senserbot.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Result<List<User>>> {
        return userRepository.getUsers()
    }
}
