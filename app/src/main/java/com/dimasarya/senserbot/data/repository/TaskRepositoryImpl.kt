package com.dimasarya.senserbot.data.repository

import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.core.websocket.WebSocketManager
import com.dimasarya.senserbot.data.remote.dto.TaskDto
import com.dimasarya.senserbot.data.remote.dto.WsMessageDto
import com.dimasarya.senserbot.data.remote.dto.toDomain
import com.dimasarya.senserbot.domain.model.Task
import com.dimasarya.senserbot.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class TaskRepositoryImpl(
    private val wsManager: WebSocketManager,
    private val json: Json
) : TaskRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    override val connectionState: StateFlow<ConnectionState> = wsManager.connectionState

    init {
        wsManager.connect()
        wsManager.messages
            .onEach { raw -> handleMessage(raw) }
            .launchIn(scope)
    }

    override fun observeTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun addTask(title: String) {
        val id = UUID.randomUUID().toString()
        val createdAt = System.currentTimeMillis()
        val message = """{"type":"ADD_TASK","payload":{"id":"$id","title":${Json.encodeToString(title)},"is_completed":false,"created_at":$createdAt}}"""
        withContext(Dispatchers.IO) { wsManager.send(message) }
    }

    override suspend fun toggleTask(id: String) {
        val message = """{"type":"TOGGLE_TASK","payload":{"id":"$id"}}"""
        withContext(Dispatchers.IO) { wsManager.send(message) }
    }

    override suspend fun removeTask(id: String) {
        val message = """{"type":"REMOVE_TASK","payload":{"id":"$id"}}"""
        withContext(Dispatchers.IO) { wsManager.send(message) }
    }

    private fun handleMessage(raw: String) {
        val message = runCatching { json.decodeFromString<WsMessageDto>(raw) }.getOrNull() ?: return
        when (message.type) {
            "SYNC_STATE" -> {
                val tasksArray = message.payload.jsonObject["tasks"]?.jsonArray ?: return
                _tasks.value = tasksArray.mapNotNull { parseTaskDto(it.jsonObject) }
            }
            "TASK_ADDED" -> {
                val task = parseTaskDto(message.payload.jsonObject) ?: return
                _tasks.update { it + task }
            }
            "TASK_TOGGLED" -> {
                val id = message.payload.jsonObject["id"]?.jsonPrimitive?.content ?: return
                val isCompleted = message.payload.jsonObject["is_completed"]?.jsonPrimitive?.boolean ?: return
                _tasks.update { list ->
                    list.map { if (it.id == id) it.copy(isCompleted = isCompleted) else it }
                }
            }
            "TASK_REMOVED" -> {
                val id = message.payload.jsonObject["id"]?.jsonPrimitive?.content ?: return
                _tasks.update { list -> list.filter { it.id != id } }
            }
        }
    }

    private fun parseTaskDto(obj: JsonObject): Task? = runCatching {
        json.decodeFromJsonElement(TaskDto.serializer(), obj).toDomain()
    }.getOrNull()
}
