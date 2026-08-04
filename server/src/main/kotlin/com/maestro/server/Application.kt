package com.maestro.server

import com.maestro.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureMonitoring()
    configureCors()
    configureAuth()
    configureRouting()
}
