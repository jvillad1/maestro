package com.maestro.server.plugins

import com.maestro.server.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()
        authenticate("jwt-auth") {
            studentRoutes()
            classRoutes()
            taskRoutes()
            eventRoutes()
        }
    }
}
