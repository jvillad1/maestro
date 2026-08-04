package com.maestro.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    val allowedOrigins = System.getenv("ALLOWED_ORIGINS")
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        if (allowedOrigins.isEmpty()) {
            this@configureCors.log.warn("ALLOWED_ORIGINS not set — allowing any host (dev fallback, do not use in production)")
            anyHost()
        } else {
            allowedOrigins.forEach { origin ->
                val url = Url(origin)
                val hostWithPort = if (url.specifiedPort != 0 && url.specifiedPort != url.protocol.defaultPort) {
                    "${url.host}:${url.specifiedPort}"
                } else {
                    url.host
                }
                allowHost(hostWithPort, schemes = listOf(url.protocol.name))
            }
        }
    }
}
