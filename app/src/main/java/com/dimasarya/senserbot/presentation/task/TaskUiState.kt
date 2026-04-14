package com.dimasarya.senserbot.presentation.task

import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.domain.model.Task

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val inputText: String = "",
    val error: String? = null
)
