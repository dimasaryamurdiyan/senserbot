package com.dimasarya.senserbot.domain.usecase

import com.dimasarya.senserbot.domain.model.Task
import com.dimasarya.senserbot.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeTasks()
}
