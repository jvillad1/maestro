package com.maestro.server

import com.maestro.shared.dto.AuthResponse
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRoutesTest {
    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    @Test
    fun `register returns 201 with token and user`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = jsonClient()
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@maestro.com", "password123", "Test Teacher"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertNotNull(body.token)
        assertEquals("test@maestro.com", body.user.email)
    }

    @Test
    fun `register duplicate email returns 409`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = jsonClient()
        val req = RegisterRequest("dup@maestro.com", "pass", "Dup")
        client.post("/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }
        val response = client.post("/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `login with valid credentials returns token`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = jsonClient()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("login@maestro.com", "mypassword", "Login Teacher"))
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("login@maestro.com", "mypassword"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.body<AuthResponse>().token)
    }

    @Test
    fun `login with wrong password returns 401`() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = jsonClient()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("bad@maestro.com", "correct", "Bad"))
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("bad@maestro.com", "wrong"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
