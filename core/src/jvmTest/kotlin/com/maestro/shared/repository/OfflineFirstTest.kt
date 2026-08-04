package com.maestro.shared.repository

import com.maestro.shared.db.MaestroDatabase
import com.maestro.shared.db.createInMemoryDriver
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Event
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student
import com.maestro.shared.model.Task
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** In-memory stand-in for the server: upserts by client id, like the real routes. */
private class FakeRemote : MaestroRepository {
    var online = true
    val students = mutableMapOf<String, Student>()
    val classes = mutableMapOf<String, ClassEntry>()
    val tasks = mutableMapOf<String, Task>()
    val events = mutableMapOf<String, Event>()

    private fun check() { if (!online) throw RuntimeException("offline") }

    override suspend fun getStudents(): List<Student> { check(); return students.values.toList() }
    override suspend fun createStudent(req: StudentRequest): Student {
        check()
        val s = Student(req.id!!, 1L, req.name, req.age, req.level, req.phone, req.email, req.monthlyFee, req.notes, "2026-08-04", req.color)
        students[s.id] = s; return s
    }
    override suspend fun updateStudent(id: String, req: StudentRequest): Student {
        check(); return createStudent(req.copy(id = id))
    }
    override suspend fun deleteStudent(id: String) { check(); students.remove(id) }

    override suspend fun getClasses(month: String?): List<ClassEntry> {
        check(); return classes.values.filter { month == null || it.date.startsWith(month) }
    }
    override suspend fun createClass(req: ClassEntryRequest): ClassEntry {
        check()
        val c = ClassEntry(req.id!!, req.studentId, req.date, req.topic, req.paid)
        classes[c.id] = c; return c
    }
    override suspend fun updateClass(id: String, req: ClassEntryRequest): ClassEntry {
        check(); return createClass(req.copy(id = id))
    }
    override suspend fun deleteClass(id: String) { check(); classes.remove(id) }

    override suspend fun getTasks(): List<Task> { check(); return tasks.values.toList() }
    override suspend fun createTask(req: TaskRequest): Task {
        check()
        val t = Task(req.id!!, 1L, req.text, req.priority, req.done)
        tasks[t.id] = t; return t
    }
    override suspend fun updateTask(id: String, req: TaskRequest): Task {
        check(); return createTask(req.copy(id = id))
    }
    override suspend fun deleteTask(id: String) { check(); tasks.remove(id) }

    override suspend fun getEvents(): List<Event> { check(); return events.values.toList() }
    override suspend fun createEvent(req: EventRequest): Event {
        check()
        val e = Event(req.id!!, 1L, req.title, req.date, req.type, req.description)
        events[e.id] = e; return e
    }
    override suspend fun updateEvent(id: String, req: EventRequest): Event {
        check(); return createEvent(req.copy(id = id))
    }
    override suspend fun deleteEvent(id: String) { check(); events.remove(id) }
}

class OfflineFirstTest {
    private fun setup(): Triple<LocalRepository, SyncEngine, FakeRemote> {
        val db = MaestroDatabase(createInMemoryDriver())
        val remote = FakeRemote()
        val userId = { 1L }
        return Triple(LocalRepository(db, remote, userId), SyncEngine(db, remote, userId), remote)
    }

    private fun studentReq() = StudentRequest("Ana", 10, Level.ELEMENTAL, "300", "ana@mail.com", 120_000L, "", "#2D5016")

    @Test
    fun `offline create is served from cache and stays off the server`() = runBlocking {
        val (repo, _, remote) = setup()
        remote.online = false
        val created = repo.createStudent(studentReq())
        val listed = repo.getStudents()
        assertEquals(listOf(created), listed)
        assertTrue(remote.students.isEmpty())
    }

    @Test
    fun `sync pushes offline writes to the server`() = runBlocking {
        val (repo, sync, remote) = setup()
        remote.online = false
        val student = repo.createStudent(studentReq())
        val task = repo.createTask(TaskRequest("Preparar recital", com.maestro.shared.model.Priority.ALTA, false))
        remote.online = true
        sync.syncAll()
        assertEquals(student.name, remote.students[student.id]?.name)
        assertEquals(task.text, remote.tasks[task.id]?.text)
        // A second sync is a no-op (rows are marked synced)
        remote.students.clear()
        sync.syncAll()
        assertTrue(remote.students.isEmpty())
    }

    @Test
    fun `offline delete tombstones locally then propagates`() = runBlocking {
        val (repo, sync, remote) = setup()
        val student = repo.createStudent(studentReq())
        sync.syncAll()
        assertTrue(remote.students.containsKey(student.id))

        remote.online = false
        repo.deleteStudent(student.id)
        assertTrue(repo.getStudents().isEmpty())          // hidden immediately
        assertTrue(remote.students.containsKey(student.id)) // still on server

        remote.online = true
        sync.syncAll()
        assertTrue(remote.students.isEmpty())             // propagated
        assertTrue(repo.getStudents().isEmpty())
    }

    @Test
    fun `refresh does not clobber dirty local edits`() = runBlocking {
        val (repo, sync, remote) = setup()
        val student = repo.createStudent(studentReq())
        sync.syncAll()

        remote.online = false
        repo.updateStudent(student.id, studentReq().copy(name = "Ana Editada"))
        remote.online = true
        // Server still has the old name; refresh must keep the dirty local edit
        val listed = repo.getStudents()
        assertEquals("Ana Editada", listed.single().name)

        sync.syncAll()
        assertEquals("Ana Editada", remote.students[student.id]?.name)
    }

    @Test
    fun `refresh drops rows deleted on the server and month scoping is respected`() = runBlocking {
        val (repo, sync, remote) = setup()
        val student = repo.createStudent(studentReq())
        val mayClass = repo.createClass(ClassEntryRequest(student.id, "2026-05-02", "Escalas", false))
        val augClass = repo.createClass(ClassEntryRequest(student.id, "2026-08-01", "Arpegios", false))
        sync.syncAll()

        // Another device deletes the May class on the server
        remote.classes.remove(mayClass.id)
        val may = repo.getClasses("2026-05")
        assertTrue(may.isEmpty())
        // August cache must be untouched by the May-scoped reconciliation
        val aug = repo.getClasses("2026-08")
        assertEquals(listOf(augClass), aug)
    }
}
