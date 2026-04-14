package com.dimasarya.senserbot.domain.usecase

import com.dimasarya.senserbot.domain.repository.TaskRepository

class AddTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(title: String) = taskRepository.addTask(title)
}
