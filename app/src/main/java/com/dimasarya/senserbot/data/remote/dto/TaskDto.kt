package com.dimasarya.senserbot.data.remote.dto

import com.dimasarya.senserbot.domain.model.Task
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("created_at") val createdAt: Long
)

fun TaskDto.toDomain(): Task = Task(
    id = id,
    title = title,
    isCompleted = isCompleted,
    createdAt = createdAt
)
