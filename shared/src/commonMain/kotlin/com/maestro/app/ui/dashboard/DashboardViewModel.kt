package com.maestro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.dev.USE_MOCK
import com.maestro.app.dev.mockClasses
import com.maestro.app.dev.mockEvents
import com.maestro.app.dev.mockStudents
import com.maestro.app.dev.mockTasks
import com.maestro.shared.repository.MaestroRepository
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.model.Attendance
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
    /** Clases con fecha de hoy — la agenda del día. */
    val todayClasses: List<ClassEntry> = emptyList(),
    /** Mes completo: de acá sale el promedio de asistencia. */
    val monthClasses: List<ClassEntry> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val totalIncome: Long = 0L,
    val pendingIncome: Long = 0L,
    /** % de asistencia del mes (asistió / marcadas); null si aún no hay clases marcadas. */
    val attendanceRate: Int? = null,
    val isLoading: Boolean = true,
    val currentPhraseIndex: Int = 0
)

class DashboardViewModel(private val repository: MaestroRepository) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            if (USE_MOCK) {
                val totalIncome = mockStudents.sumOf { it.monthlyFee }
                val pendingIncome = mockStudents.filter { s ->
                    mockClasses.none { it.studentId == s.id && it.paid }
                }.sumOf { it.monthlyFee }
                _state.value = DashboardState(
                    students = mockStudents,
                    recentClasses = mockClasses,
                    upcomingEvents = mockEvents,
                    pendingTasks = mockTasks.filter { !it.done },
                    totalIncome = totalIncome,
                    pendingIncome = pendingIncome,
                    isLoading = false
                )
                return@launch
            }
            try {
                val students = repository.getStudents()
                val currentMonth = getCurrentMonth()
                val classes = repository.getClasses(currentMonth)
                val events = repository.getEvents()
                val tasks = repository.getTasks()

                val totalIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && it.paid }) s.monthlyFee else 0L
                }
                val pendingIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && !it.paid }) s.monthlyFee else 0L
                }
                val marked = classes.count { it.attendance != Attendance.PENDIENTE }
                val attendanceRate = if (marked == 0) null
                else classes.count { it.attendance == Attendance.ASISTIO } * 100 / marked

                val today = getCurrentDate()
                _state.value = DashboardState(
                    students = students,
                    recentClasses = classes.takeLast(4).reversed(),
                    todayClasses = classes.filter { it.date == today }.sortedBy { it.studentId },
                    monthClasses = classes,
                    upcomingEvents = events.sortedBy { it.date }.take(4),
                    pendingTasks = tasks.filter { !it.done }.take(3),
                    totalIncome = totalIncome,
                    pendingIncome = pendingIncome,
                    attendanceRate = attendanceRate,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /** Marca asistencia sin salir del dashboard: pendiente → asistió → faltó. */
    fun cycleAttendance(entry: ClassEntry) {
        val next = when (entry.attendance) {
            Attendance.PENDIENTE -> Attendance.ASISTIO
            Attendance.ASISTIO -> Attendance.FALTO
            Attendance.FALTO -> Attendance.PENDIENTE
        }
        viewModelScope.launch {
            try {
                val updated = repository.updateClass(
                    entry.id,
                    ClassEntryRequest(
                        studentId = entry.studentId, date = entry.date, topic = entry.topic,
                        paid = entry.paid, attendance = next, notes = entry.notes
                    )
                )
                val month = _state.value.monthClasses.map { if (it.id == updated.id) updated else it }
                val marked = month.count { it.attendance != Attendance.PENDIENTE }
                _state.value = _state.value.copy(
                    todayClasses = _state.value.todayClasses.map { if (it.id == updated.id) updated else it },
                    recentClasses = _state.value.recentClasses.map { if (it.id == updated.id) updated else it },
                    monthClasses = month,
                    // el KPI se movía solo al reabrir la app; ahora sigue al chip
                    attendanceRate = if (marked == 0) null
                    else month.count { it.attendance == Attendance.ASISTIO } * 100 / marked
                )
            } catch (_: Exception) { /* el repo offline-first ya guardó local */ }
        }
    }

    fun nextPhrase() { _state.value = _state.value.copy(currentPhraseIndex = (_state.value.currentPhraseIndex + 1) % PHRASES.size) }
}

expect fun getCurrentMonth(): String
expect fun getCurrentDate(): String

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
