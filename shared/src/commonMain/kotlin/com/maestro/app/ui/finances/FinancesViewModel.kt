package com.maestro.app.ui.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.dev.USE_MOCK
import com.maestro.app.dev.mockClasses
import com.maestro.app.dev.mockStudents
import com.maestro.shared.repository.MaestroRepository
import com.maestro.app.ui.dashboard.getCurrentMonth
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Student
import com.maestro.shared.util.shiftMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudentFinanceRow(
    val student: Student,
    val classCount: Int,
    val attended: Int,
    val missed: Int,
    val status: FinanceStatus
)

enum class FinanceStatus { PAID, PENDING, NO_CLASSES }

data class FinancesState(
    val students: List<Student> = emptyList(),
    val classes: List<ClassEntry> = emptyList(),
    /** Mes del reporte en formato YYYY-MM; navegable hacia atrás. */
    val selectedMonth: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val expectedTotal: Long get() = students.sumOf { it.monthlyFee }
    val collected: Long get() {
        return students.sumOf { s ->
            if (classes.any { it.studentId == s.id && it.paid }) s.monthlyFee else 0L
        }
    }
    val pending: Long get() = expectedTotal - collected

    val rows: List<StudentFinanceRow> get() = students.map { s ->
        val studentClasses = classes.filter { it.studentId == s.id }
        val status = when {
            studentClasses.isEmpty() -> FinanceStatus.NO_CLASSES
            studentClasses.any { it.paid } -> FinanceStatus.PAID
            else -> FinanceStatus.PENDING
        }
        StudentFinanceRow(
            student = s,
            classCount = studentClasses.size,
            attended = studentClasses.count { it.attendance == Attendance.ASISTIO },
            missed = studentClasses.count { it.attendance == Attendance.FALTO },
            status = status
        )
    }
}

class FinancesViewModel(private val repository: MaestroRepository) : ViewModel() {
    private val _state = MutableStateFlow(FinancesState())
    val state: StateFlow<FinancesState> = _state

    /** No tiene sentido reportar meses que aún no ocurrieron. */
    val canGoForward: Boolean get() = _state.value.selectedMonth < getCurrentMonth()

    init { load(getCurrentMonth()) }

    fun previousMonth() = load(shiftMonth(_state.value.selectedMonth, -1))

    fun nextMonth() {
        if (canGoForward) load(shiftMonth(_state.value.selectedMonth, 1))
    }

    fun load(month: String = _state.value.selectedMonth.ifBlank { getCurrentMonth() }) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, selectedMonth = month)
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
                    classes = classes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
