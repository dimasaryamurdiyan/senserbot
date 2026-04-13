package com.dimasarya.senserbot.presentation.user

import com.dimasarya.senserbot.domain.model.User

data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)
