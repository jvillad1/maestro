package com.maestro.server.plugins

import com.maestro.server.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/ping") { call.respond(HttpStatusCode.OK, "pong") }
        authRoutes()
        authenticate("jwt-auth") {
            studentRoutes()
            classRoutes()
            taskRoutes()
            eventRoutes()
        }
    }
}
