package com.maestro.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.network.ApiClient
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.model.Event
import com.maestro.shared.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EventsState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

class EventsViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val events = apiClient.getEvents()
                _state.value = _state.value.copy(
                    events = events.sortedBy { it.date },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { _state.value = _state.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _state.value = _state.value.copy(showAddDialog = false) }

    fun createEvent(title: String, date: String, type: EventType, description: String) {
        viewModelScope.launch {
            try {
                val req = EventRequest(title, date, type, description)
                val created = apiClient.createEvent(req)
                _state.value = _state.value.copy(
                    events = (_state.value.events + created).sortedBy { it.date },
                    showAddDialog = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
