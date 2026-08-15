package com.maestro.shared.repository

import com.maestro.shared.db.MaestroDatabase
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Event
import com.maestro.shared.model.EventType
import com.maestro.shared.model.Level
import com.maestro.shared.model.Priority
import com.maestro.shared.model.Student
import com.maestro.shared.model.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Offline-first repository (movi pattern): reads serve the local SQLDelight
 * cache, refreshed from the server when the network allows; writes land
 * locally with syncedAt = NULL and the SyncEngine pushes them later.
 */
@OptIn(ExperimentalUuidApi::class)
class LocalRepository(
    private val db: MaestroDatabase,
    private val remote: MaestroRepository,
    private val userId: () -> Long,
) : MaestroRepository {

    private fun newId(): String = Uuid.random().toString()

    // ── Students ──────────────────────────────────────────────────────────

    override suspend fun getStudents(): List<Student> {
        try {
            val remoteList = remote.getStudents()
            val now = epochMillis()
            db.transaction {
                remoteList.forEach { s ->
                    // Never overwrite a dirty local row with server state
                    val local = db.studentQueries.selectById(s.id).executeAsOneOrNull()
                    if (local == null || local.syncedAt != null) {
                        db.studentQueries.upsert(
                            s.id, s.userId, s.name, s.age.toLong(), s.level.name, s.phone,
                            s.email, s.monthlyFee, s.notes, s.joinDate, s.color, now, 0
                        )
                    }
                }
                val ids = remoteList.map { it.id }
                if (ids.isEmpty()) db.studentQueries.deleteAllSynced(userId())
                else db.studentQueries.deleteSyncedAbsent(userId(), ids)
            }
        } catch (_: Exception) { /* offline: serve cache */ }
        return db.studentQueries.selectAll(userId()).executeAsList().map { it.toStudent() }
    }

    override suspend fun createStudent(req: StudentRequest): Student {
        val id = req.id ?: newId()
        db.studentQueries.upsert(
            id, userId(), req.name, req.age.toLong(), req.level.name, req.phone,
            req.email, req.monthlyFee, req.notes, todayIsoDate(), req.color, null, 0
        )
        return db.studentQueries.selectById(id).executeAsOne().toStudent()
    }

    override suspend fun updateStudent(id: String, req: StudentRequest): Student {
        val existing = db.studentQueries.selectById(id).executeAsOne()
        db.studentQueries.upsert(
            id, existing.userId, req.name, req.age.toLong(), req.level.name, req.phone,
            req.email, req.monthlyFee, req.notes, existing.joinDate, req.color, null, 0
        )
        return db.studentQueries.selectById(id).executeAsOne().toStudent()
    }

    override suspend fun deleteStudent(id: String) {
        db.studentQueries.markPendingDelete(id)
    }

    // ── Classes ───────────────────────────────────────────────────────────

    override suspend fun getClasses(month: String?): List<ClassEntry> {
        try {
            val remoteList = remote.getClasses(month)
            val now = epochMillis()
            db.transaction {
                remoteList.forEach { c ->
                    val local = db.classEntryQueries.selectById(c.id).executeAsOneOrNull()
                    if (local == null || local.syncedAt != null) {
                        db.classEntryQueries.upsert(
                            c.id, userId(), c.studentId, c.date, c.topic,
                            if (c.paid) 1 else 0, c.attendance.name, c.notes, now, 0
                        )
                    }
                }
                val ids = remoteList.map { it.id }
                if (month == null) {
                    if (ids.isEmpty()) db.classEntryQueries.deleteAllSynced(userId())
                    else db.classEntryQueries.deleteSyncedAbsent(userId(), ids)
                } else {
                    if (ids.isEmpty()) db.classEntryQueries.deleteAllSyncedInMonth(userId(), "$month%")
                    else db.classEntryQueries.deleteSyncedAbsentInMonth(userId(), "$month%", ids)
                }
            }
        } catch (_: Exception) { /* offline: serve cache */ }
        val rows = if (month == null) db.classEntryQueries.selectAll(userId()).executeAsList()
        else db.classEntryQueries.selectByMonth(userId(), "$month%").executeAsList()
        return rows.map { it.toClassEntry() }
    }

    override suspend fun createClass(req: ClassEntryRequest): ClassEntry {
        val id = req.id ?: newId()
        db.classEntryQueries.upsert(
            id, userId(), req.studentId, req.date, req.topic, if (req.paid) 1 else 0,
            req.attendance.name, req.notes, null, 0
        )
        return db.classEntryQueries.selectById(id).executeAsOne().toClassEntry()
    }

    override suspend fun updateClass(id: String, req: ClassEntryRequest): ClassEntry {
        db.classEntryQueries.upsert(
            id, userId(), req.studentId, req.date, req.topic, if (req.paid) 1 else 0,
            req.attendance.name, req.notes, null, 0
        )
        return db.classEntryQueries.selectById(id).executeAsOne().toClassEntry()
    }

    override suspend fun deleteClass(id: String) {
        db.classEntryQueries.markPendingDelete(id)
    }

    // ── Tasks ─────────────────────────────────────────────────────────────

    override suspend fun getTasks(): List<Task> {
        try {
            val remoteList = remote.getTasks()
            val now = epochMillis()
            db.transaction {
                remoteList.forEach { t ->
                    val local = db.taskQueries.selectById(t.id).executeAsOneOrNull()
                    if (local == null || local.syncedAt != null) {
                        db.taskQueries.upsert(
                            t.id, t.userId, t.text, t.priority.name, if (t.done) 1 else 0, now, 0
                        )
                    }
                }
                val ids = remoteList.map { it.id }
                if (ids.isEmpty()) db.taskQueries.deleteAllSynced(userId())
                else db.taskQueries.deleteSyncedAbsent(userId(), ids)
            }
        } catch (_: Exception) { /* offline: serve cache */ }
        return db.taskQueries.selectAll(userId()).executeAsList().map { it.toTask() }
    }

    override suspend fun createTask(req: TaskRequest): Task {
        val id = req.id ?: newId()
        db.taskQueries.upsert(id, userId(), req.text, req.priority.name, if (req.done) 1 else 0, null, 0)
        return db.taskQueries.selectById(id).executeAsOne().toTask()
    }

    override suspend fun updateTask(id: String, req: TaskRequest): Task {
        db.taskQueries.upsert(id, userId(), req.text, req.priority.name, if (req.done) 1 else 0, null, 0)
        return db.taskQueries.selectById(id).executeAsOne().toTask()
    }

    override suspend fun deleteTask(id: String) {
        db.taskQueries.markPendingDelete(id)
    }

    // ── Events ────────────────────────────────────────────────────────────

    override suspend fun getEvents(): List<Event> {
        try {
            val remoteList = remote.getEvents()
            val now = epochMillis()
            db.transaction {
                remoteList.forEach { e ->
                    val local = db.eventQueries.selectById(e.id).executeAsOneOrNull()
                    if (local == null || local.syncedAt != null) {
                        db.eventQueries.upsert(
                            e.id, e.userId, e.title, e.date, e.type.name, e.description, now, 0
                        )
                    }
                }
                val ids = remoteList.map { it.id }
                if (ids.isEmpty()) db.eventQueries.deleteAllSynced(userId())
                else db.eventQueries.deleteSyncedAbsent(userId(), ids)
            }
        } catch (_: Exception) { /* offline: serve cache */ }
        return db.eventQueries.selectAll(userId()).executeAsList().map { it.toEvent() }
    }

    override suspend fun createEvent(req: EventRequest): Event {
        val id = req.id ?: newId()
        db.eventQueries.upsert(id, userId(), req.title, req.date, req.type.name, req.description, null, 0)
        return db.eventQueries.selectById(id).executeAsOne().toEvent()
    }

    override suspend fun updateEvent(id: String, req: EventRequest): Event {
        db.eventQueries.upsert(id, userId(), req.title, req.date, req.type.name, req.description, null, 0)
        return db.eventQueries.selectById(id).executeAsOne().toEvent()
    }

    override suspend fun deleteEvent(id: String) {
        db.eventQueries.markPendingDelete(id)
    }
}

// ── Row mappers ───────────────────────────────────────────────────────────

internal fun com.maestro.shared.db.Student.toStudent() = Student(
    id = id, userId = userId, name = name, age = age.toInt(),
    level = Level.valueOf(level), phone = phone, email = email,
    monthlyFee = monthlyFee, notes = notes, joinDate = joinDate, color = color
)

internal fun com.maestro.shared.db.Class_entry.toClassEntry() = ClassEntry(
    id = id, studentId = studentId, date = date, topic = topic, paid = paid == 1L,
    attendance = Attendance.valueOf(attendance), notes = notes
)

internal fun com.maestro.shared.db.Task.toTask() = Task(
    id = id, userId = userId, text = text, priority = Priority.valueOf(priority), done = done == 1L
)

internal fun com.maestro.shared.db.Event.toEvent() = Event(
    id = id, userId = userId, title = title, date = date,
    type = EventType.valueOf(type), description = description
)
