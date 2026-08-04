package com.maestro.app.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.dev.USE_MOCK
import com.maestro.app.dev.mockStudents
import com.maestro.shared.repository.MaestroRepository
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudentsState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

data class StudentDetailState(
    val student: Student? = null,
    val classes: List<ClassEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class StudentsViewModel(private val repository: MaestroRepository) : ViewModel() {
    private val _state = MutableStateFlow(StudentsState())
    val state: StateFlow<StudentsState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            if (USE_MOCK) {
                _state.value = _state.value.copy(students = mockStudents, isLoading = false)
                return@launch
            }
            try {
                val students = repository.getStudents()
                _state.value = _state.value.copy(students = students, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { _state.value = _state.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _state.value = _state.value.copy(showAddDialog = false) }

    fun createStudent(
        name: String, age: Int, level: Level, phone: String,
        email: String, monthlyFee: Long, notes: String, color: String
    ) {
        viewModelScope.launch {
            try {
                val req = StudentRequest(name, age, level, phone, email, monthlyFee, notes, color)
                val created = repository.createStudent(req)
                _state.value = _state.value.copy(
                    students = _state.value.students + created,
                    showAddDialog = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

class StudentDetailViewModel(private val repository: MaestroRepository) : ViewModel() {
    private val _state = MutableStateFlow(StudentDetailState())
    val state: StateFlow<StudentDetailState> = _state

    fun load(studentId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val students = repository.getStudents()
                val student = students.find { it.id == studentId }
                val allClasses = repository.getClasses()
                val studentClasses = allClasses.filter { it.studentId == studentId }
                    .sortedByDescending { it.date }
                _state.value = _state.value.copy(
                    student = student,
                    classes = studentClasses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun togglePaid(classEntry: ClassEntry) {
        viewModelScope.launch {
            try {
                val updated = repository.updateClass(
                    classEntry.id,
                    com.maestro.shared.dto.ClassEntryRequest(
                        studentId = classEntry.studentId,
                        date = classEntry.date,
                        topic = classEntry.topic,
                        paid = !classEntry.paid
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
