package com.maestro.shared.repository

import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Event
import com.maestro.shared.model.Student
import com.maestro.shared.model.Task

/**
 * CRUD surface shared by the remote ApiClient (wasm) and the offline-first
 * LocalRepository (Android). Auth stays outside: it is online-only.
 */
interface MaestroRepository {
    suspend fun getStudents(): List<Student>
    suspend fun createStudent(req: StudentRequest): Student
    suspend fun updateStudent(id: String, req: StudentRequest): Student
    suspend fun deleteStudent(id: String)

    suspend fun getClasses(month: String? = null): List<ClassEntry>
    suspend fun createClass(req: ClassEntryRequest): ClassEntry
    suspend fun updateClass(id: String, req: ClassEntryRequest): ClassEntry
    suspend fun deleteClass(id: String)

    suspend fun getTasks(): List<Task>
    suspend fun createTask(req: TaskRequest): Task
    suspend fun updateTask(id: String, req: TaskRequest): Task
    suspend fun deleteTask(id: String)

    suspend fun getEvents(): List<Event>
    suspend fun createEvent(req: EventRequest): Event
    suspend fun updateEvent(id: String, req: EventRequest): Event
    suspend fun deleteEvent(id: String)
}
