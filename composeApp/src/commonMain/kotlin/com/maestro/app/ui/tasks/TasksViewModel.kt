package com.maestro.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.network.ApiClient
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.Priority
import com.maestro.shared.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

class TasksViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val tasks = apiClient.getTasks()
                _state.value = _state.value.copy(tasks = tasks, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { _state.value = _state.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _state.value = _state.value.copy(showAddDialog = false) }

    fun createTask(text: String, priority: Priority) {
        viewModelScope.launch {
            try {
                val req = TaskRequest(text, priority, false)
                val created = apiClient.createTask(req)
                _state.value = _state.value.copy(
                    tasks = _state.value.tasks + created,
                    showAddDialog = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            try {
                val updated = apiClient.updateTask(task.id, TaskRequest(task.text, task.priority, !task.done))
                _state.value = _state.value.copy(
                    tasks = _state.value.tasks.map { if (it.id == updated.id) updated else it }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                apiClient.deleteTask(task.id)
                _state.value = _state.value.copy(
                    tasks = _state.value.tasks.filter { it.id != task.id }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
