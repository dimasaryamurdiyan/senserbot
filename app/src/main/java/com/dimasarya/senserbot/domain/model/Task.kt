package com.dimasarya.senserbot.domain.model

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long
)
