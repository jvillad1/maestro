package com.maestro.app.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.dev.USE_MOCK
import com.maestro.app.dev.mockClasses
import com.maestro.app.dev.mockStudents
import com.maestro.shared.repository.MaestroRepository
import com.maestro.app.ui.dashboard.getCurrentMonth
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ClassesState(
    val classes: List<ClassEntry> = emptyList(),
    val students: List<Student> = emptyList(),
    val currentMonth: String = "",
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

class ClassesViewModel(private val repository: MaestroRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClassesState())
    val state: StateFlow<ClassesState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            val month = getCurrentMonth()
            _state.value = _state.value.copy(isLoading = true, error = null, currentMonth = month)
            if (USE_MOCK) {
                _state.value = _state.value.copy(
                    students = mockStudents,
                    classes = mockClasses,
                    isLoading = false
                )
                return@launch
            }
            try {
                val students = repository.getStudents()
                val classes = repository.getClasses(month)
                _state.value = _state.value.copy(
                    students = students,
                    classes = classes.sortedByDescending { it.date },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { _state.value = _state.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _state.value = _state.value.copy(showAddDialog = false) }

    fun createClass(studentId: String, date: String, topic: String, paid: Boolean) {
        viewModelScope.launch {
            try {
                val req = ClassEntryRequest(studentId, date, topic, paid)
                val created = repository.createClass(req)
                _state.value = _state.value.copy(
                    classes = (_state.value.classes + created).sortedByDescending { it.date },
                    showAddDialog = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun togglePaid(classEntry: ClassEntry) {
        update(classEntry.copy(paid = !classEntry.paid))
    }

    fun cycleAttendance(classEntry: ClassEntry) {
        val next = when (classEntry.attendance) {
            Attendance.PENDIENTE -> Attendance.ASISTIO
            Attendance.ASISTIO -> Attendance.FALTO
            Attendance.FALTO -> Attendance.PENDIENTE
        }
        update(classEntry.copy(attendance = next))
    }

    private fun update(entry: ClassEntry) {
        viewModelScope.launch {
            try {
                val updated = repository.updateClass(
                    entry.id,
                    ClassEntryRequest(
                        studentId = entry.studentId,
                        date = entry.date,
                        topic = entry.topic,
                        paid = entry.paid,
                        attendance = entry.attendance
                    )
                )
                _state.value = _state.value.copy(
                    classes = _state.value.classes.map { if (it.id == updated.id) updated else it }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
