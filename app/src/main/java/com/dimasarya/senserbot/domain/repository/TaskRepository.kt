package com.dimasarya.senserbot.domain.repository

import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    val connectionState: StateFlow<ConnectionState>
    suspend fun addTask(title: String)
    suspend fun toggleTask(id: String)
    suspend fun removeTask(id: String)
}
