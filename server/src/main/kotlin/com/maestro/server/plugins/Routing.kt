package com.maestro.server.plugins

import com.maestro.server.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/ping") { call.respond(HttpStatusCode.OK, "pong") }
        get("/health") { call.respond(HttpStatusCode.OK, "OK") }
        authRoutes()
        authenticate("jwt-auth") {
            studentRoutes()
            classRoutes()
            taskRoutes()
            eventRoutes()
        }

        // Serve wasmJs web app — must be last so API routes take priority
        staticResources("/", "static") {
            default("index.html")
            // Ktor 3.x no registra estos tipos por su cuenta; sin el correcto el
            // navegador ignora el manifest y no ofrece instalar la PWA
            contentType { url ->
                when {
                    url.path.endsWith(".wasm") -> ContentType("application", "wasm")
                    url.path.endsWith(".webmanifest") -> ContentType("application", "manifest+json")
                    else -> null
                }
            }
        }
    }
}
