package com.dimasarya.senserbot.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class WsMessageDto(
    val type: String,
    val payload: JsonElement = JsonObject(emptyMap())
)
