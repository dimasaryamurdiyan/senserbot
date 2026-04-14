package com.dimasarya.senserbot.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimasarya.senserbot.domain.repository.TaskRepository
import com.dimasarya.senserbot.domain.usecase.AddTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ObserveTasksUseCase
import com.dimasarya.senserbot.domain.usecase.RemoveTaskUseCase
import com.dimasarya.senserbot.domain.usecase.ToggleTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskViewModel(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
    taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        observeTasksUseCase()
            .onEach { tasks -> _uiState.update { it.copy(tasks = tasks) } }
            .launchIn(viewModelScope)

        taskRepository.connectionState
            .onEach { state -> _uiState.update { it.copy(connectionState = state) } }
            .launchIn(viewModelScope)
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onAddTask() {
        val title = _uiState.value.inputText.trim()
        if (title.isBlank()) return
        _uiState.update { it.copy(inputText = "") }
        viewModelScope.launch {
            runCatching { addTaskUseCase(title) }
                .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        }
    }

    fun onToggleTask(id: String) {
        viewModelScope.launch {
            runCatching { toggleTaskUseCase(id) }
                .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        }
    }

    fun onRemoveTask(id: String) {
        viewModelScope.launch {
            runCatching { removeTaskUseCase(id) }
                .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }
}
