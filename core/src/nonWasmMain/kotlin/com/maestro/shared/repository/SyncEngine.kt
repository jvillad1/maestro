package com.maestro.shared.repository

import com.maestro.shared.db.MaestroDatabase
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.EventType
import com.maestro.shared.model.Level
import com.maestro.shared.model.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pushes dirty local rows (syncedAt = NULL) and pending deletes to the server
 * every 30s (movi pattern). Server POSTs are idempotent upserts keyed by the
 * client-generated id, so retries are safe.
 */
class SyncEngine(
    private val db: MaestroDatabase,
    private val remote: MaestroRepository,
    private val userId: () -> Long,
    private val intervalMillis: Long = 30_000L,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        scope.launch {
            while (true) {
                delay(intervalMillis)
                try { syncAll() } catch (_: Exception) {}
            }
        }
    }

    suspend fun syncAll() {
        // Creates/updates: students before classes (FK)
        pushStudents()
        pushClasses()
        pushTasks()
        pushEvents()
        // Deletes: classes before students (FK)
        pushClassDeletes()
        pushStudentDeletes()
        pushTaskDeletes()
        pushEventDeletes()
    }

    private suspend fun pushStudents() {
        for (row in db.studentQueries.selectUnsynced(userId()).executeAsList()) {
            try {
                remote.createStudent(
                    StudentRequest(
                        name = row.name, age = row.age.toInt(), level = Level.valueOf(row.level),
                        phone = row.phone, email = row.email, monthlyFee = row.monthlyFee,
                        notes = row.notes, color = row.color, id = row.id
                    )
                )
                db.studentQueries.markSynced(epochMillis(), row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushClasses() {
        for (row in db.classEntryQueries.selectUnsynced(userId()).executeAsList()) {
            try {
                remote.createClass(
                    ClassEntryRequest(
                        studentId = row.studentId, date = row.date, topic = row.topic,
                        paid = row.paid == 1L, attendance = Attendance.valueOf(row.attendance), id = row.id
                    )
                )
                db.classEntryQueries.markSynced(epochMillis(), row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushTasks() {
        for (row in db.taskQueries.selectUnsynced(userId()).executeAsList()) {
            try {
                remote.createTask(
                    TaskRequest(
                        text = row.text, priority = Priority.valueOf(row.priority),
                        done = row.done == 1L, id = row.id
                    )
                )
                db.taskQueries.markSynced(epochMillis(), row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushEvents() {
        for (row in db.eventQueries.selectUnsynced(userId()).executeAsList()) {
            try {
                remote.createEvent(
                    EventRequest(
                        title = row.title, date = row.date, type = EventType.valueOf(row.type),
                        description = row.description, id = row.id
                    )
                )
                db.eventQueries.markSynced(epochMillis(), row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushStudentDeletes() {
        for (row in db.studentQueries.selectPendingDelete(userId()).executeAsList()) {
            try {
                remote.deleteStudent(row.id)
                db.studentQueries.delete(row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushClassDeletes() {
        for (row in db.classEntryQueries.selectPendingDelete(userId()).executeAsList()) {
            try {
                remote.deleteClass(row.id)
                db.classEntryQueries.delete(row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushTaskDeletes() {
        for (row in db.taskQueries.selectPendingDelete(userId()).executeAsList()) {
            try {
                remote.deleteTask(row.id)
                db.taskQueries.delete(row.id)
            } catch (_: Exception) {}
        }
    }

    private suspend fun pushEventDeletes() {
        for (row in db.eventQueries.selectPendingDelete(userId()).executeAsList()) {
            try {
                remote.deleteEvent(row.id)
                db.eventQueries.delete(row.id)
            } catch (_: Exception) {}
        }
    }
}
