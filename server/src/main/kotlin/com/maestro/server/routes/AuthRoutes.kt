package com.maestro.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.maestro.server.plugins.JwtConfig
import com.maestro.server.plugins.Users
import com.maestro.shared.dto.AuthResponse
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import com.maestro.shared.model.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val existing = transaction { Users.selectAll().where { Users.email eq req.email }.singleOrNull() }
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, "Email already registered")
                return@post
            }
            val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
            val userId = transaction {
                Users.insert {
                    it[email] = req.email
                    it[name] = req.name
                    it[passwordHash] = hash
                    it[createdAt] = Instant.now().toString()
                }[Users.id]
            }
            val user = User(userId, req.email, req.name, Instant.now().toString())
            call.respond(HttpStatusCode.Created, AuthResponse(JwtConfig.makeToken(userId), user))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            data class UserRow(val id: Long, val email: String, val name: String, val createdAt: String, val passwordHash: String)
            val found = transaction {
                Users.selectAll().where { Users.email eq req.email }.singleOrNull()?.let {
                    UserRow(it[Users.id], it[Users.email], it[Users.name], it[Users.createdAt], it[Users.passwordHash])
                }
            }
            if (found == null || !BCrypt.verifyer().verify(req.password.toCharArray(), found.passwordHash).verified) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }
            val user = User(found.id, found.email, found.name, found.createdAt)
            call.respond(AuthResponse(JwtConfig.makeToken(user.id), user))
        }
    }
}
