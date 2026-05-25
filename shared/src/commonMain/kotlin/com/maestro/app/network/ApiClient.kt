package com.maestro.app.network

import com.maestro.app.auth.TokenStorage
import com.maestro.shared.dto.*
import com.maestro.shared.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String,
    private val tokenStorage: TokenStorage
) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun HttpRequestBuilder.auth() {
        tokenStorage.getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    // Auth
    suspend fun login(req: LoginRequest): AuthResponse =
        client.post("$baseUrl/api/auth/login") { contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun register(req: RegisterRequest): AuthResponse =
        client.post("$baseUrl/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }.body()

    // Students
    suspend fun getStudents(): List<Student> =
        client.get("$baseUrl/api/students") { auth() }.body()

    suspend fun createStudent(req: StudentRequest): Student =
        client.post("$baseUrl/api/students") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateStudent(id: Long, req: StudentRequest): Student =
        client.put("$baseUrl/api/students/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteStudent(id: Long) =
        client.delete("$baseUrl/api/students/$id") { auth() }

    // Classes
    suspend fun getClasses(month: String? = null): List<ClassEntry> =
        client.get("$baseUrl/api/classes") { auth(); month?.let { parameter("month", it) } }.body()

    suspend fun createClass(req: ClassEntryRequest): ClassEntry =
        client.post("$baseUrl/api/classes") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateClass(id: Long, req: ClassEntryRequest): ClassEntry =
        client.put("$baseUrl/api/classes/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteClass(id: Long) =
        client.delete("$baseUrl/api/classes/$id") { auth() }

    // Tasks
    suspend fun getTasks(): List<Task> =
        client.get("$baseUrl/api/tasks") { auth() }.body()

    suspend fun createTask(req: TaskRequest): Task =
        client.post("$baseUrl/api/tasks") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateTask(id: Long, req: TaskRequest): Task =
        client.put("$baseUrl/api/tasks/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteTask(id: Long) =
        client.delete("$baseUrl/api/tasks/$id") { auth() }

    // Events
    suspend fun getEvents(): List<Event> =
        client.get("$baseUrl/api/events") { auth() }.body()

    suspend fun createEvent(req: EventRequest): Event =
        client.post("$baseUrl/api/events") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateEvent(id: Long, req: EventRequest): Event =
        client.put("$baseUrl/api/events/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteEvent(id: Long) =
        client.delete("$baseUrl/api/events/$id") { auth() }
}
