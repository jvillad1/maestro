package com.maestro.server

import com.maestro.shared.dto.*
import com.maestro.shared.model.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentRoutesTest {
    private suspend fun ApplicationTestBuilder.authedClient(
        email: String = "teacher-${UUID.randomUUID()}@test.com"
    ): Pair<io.ktor.client.HttpClient, String> {
        val client = createClient { install(ContentNegotiation) { json() } }
        val token = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, "pass123", "Teacher"))
        }.body<AuthResponse>().token
        return client to token
    }

    private fun studentReq() = StudentRequest(
        "Isabella García", 10, Level.ELEMENTAL, "300-123", "isa@mail.com", 250000L, "Dedicada", "#C9A84C"
    )

    @Test
    fun `create student returns 201`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val (client, token) = authedClient()
        val response = client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Isabella García", response.body<Student>().name)
    }

    @Test
    fun `list students returns only own students`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val (client, token) = authedClient()
        client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }
        val response = client.get("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, response.body<List<Student>>().size)
    }

    @Test
    fun `delete student returns 204`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val (client, token) = authedClient()
        val student = client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }.body<Student>()
        val del = client.delete("/api/students/${student.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, del.status)
    }
}
