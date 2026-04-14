package com.dimasarya.senserbot.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDtoMappingTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun `TaskDto toDomain maps all fields correctly`() {
        val dto = TaskDto(
            id = "abc-123",
            title = "Buy milk",
            isCompleted = true,
            createdAt = 1713000000000L
        )

        val domain = dto.toDomain()

        assertEquals("abc-123", domain.id)
        assertEquals("Buy milk", domain.title)
        assertTrue(domain.isCompleted)
        assertEquals(1713000000000L, domain.createdAt)
    }

    @Test
    fun `TaskDto deserializes from JSON with snake_case fields`() {
        val raw = """{"id":"xyz","title":"Walk dog","is_completed":false,"created_at":1234567890}"""
        val dto = json.decodeFromString<TaskDto>(raw)

        assertEquals("xyz", dto.id)
        assertEquals("Walk dog", dto.title)
        assertFalse(dto.isCompleted)
        assertEquals(1234567890L, dto.createdAt)
    }

    @Test
    fun `WsMessageDto deserializes type and payload`() {
        val raw = """{"type":"SYNC_STATE","payload":{"tasks":[]}}"""
        val message = json.decodeFromString<WsMessageDto>(raw)

        assertEquals("SYNC_STATE", message.type)
    }

    @Test
    fun `WsMessageDto deserializes with missing payload defaults to empty object`() {
        val raw = """{"type":"UNKNOWN"}"""
        val message = json.decodeFromString<WsMessageDto>(raw)

        assertEquals("UNKNOWN", message.type)
    }
}
