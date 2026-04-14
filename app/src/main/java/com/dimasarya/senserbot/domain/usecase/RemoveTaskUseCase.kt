package com.dimasarya.senserbot.domain.usecase

import com.dimasarya.senserbot.domain.repository.TaskRepository

class RemoveTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(id: String) = taskRepository.removeTask(id)
}
