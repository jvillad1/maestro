package com.maestro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.network.ApiClient
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Event
import com.maestro.shared.model.Student
import com.maestro.shared.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val students: List<Student> = emptyList(),
    val recentClasses: List<ClassEntry> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val totalIncome: Long = 0L,
    val pendingIncome: Long = 0L,
    val isLoading: Boolean = true,
    val currentPhraseIndex: Int = 0
)

class DashboardViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val students = apiClient.getStudents()
                val currentMonth = getCurrentMonth()
                val classes = apiClient.getClasses(currentMonth)
                val events = apiClient.getEvents()
                val tasks = apiClient.getTasks()

                val totalIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && it.paid }) s.monthlyFee else 0L
                }
                val pendingIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && !it.paid }) s.monthlyFee else 0L
                }

                _state.value = DashboardState(
                    students = students,
                    recentClasses = classes.takeLast(4).reversed(),
                    upcomingEvents = events.sortedBy { it.date }.take(4),
                    pendingTasks = tasks.filter { !it.done }.take(3),
                    totalIncome = totalIncome,
                    pendingIncome = pendingIncome,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun nextPhrase() { _state.value = _state.value.copy(currentPhraseIndex = (_state.value.currentPhraseIndex + 1) % PHRASES.size) }
}

expect fun getCurrentMonth(): String

data class Phrase(val text: String, val author: String)

val PHRASES = listOf(
    Phrase("Enseñar música es sembrar semillas que florecen por generaciones.", "Naty Ramírez"),
    Phrase("Cada clase es una obra maestra que nunca volverá a repetirse.", "Maestro anónimo"),
    Phrase("La paciencia del maestro es el compás que guía el alma del estudiante.", "Heinrich Neuhaus"),
    Phrase("No enseñas notas. Enseñas a escuchar el universo.", "Nadia Boulanger"),
    Phrase("Tu energía de hoy es el recuerdo musical de alguien mañana.", "Naty Ramírez"),
    Phrase("Un buen maestro abre puertas que el estudiante ni sabía que existían.", "Leon Fleisher"),
    Phrase("Cuídate. Un maestro agotado no puede encender llamas en otros.", "Reflexión docente"),
    Phrase("La excelencia no es perfección. Es presencia total en cada compás.", "Naty Ramírez")
)
