package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.model.Event
import com.maestro.shared.model.EventType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

private fun ResultRow.toEvent() = Event(
    id = this[Events.id],
    userId = this[Events.userId],
    title = this[Events.title],
    date = this[Events.date],
    type = EventType.valueOf(this[Events.type]),
    description = this[Events.description]
)

fun Route.eventRoutes() {
    route("/api/events") {
        get {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            call.respond(transaction { Events.selectAll().where { Events.userId eq uid }.map { it.toEvent() } })
        }
        post {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val req = call.receive<EventRequest>()
            val eid = req.id ?: java.util.UUID.randomUUID().toString()
            val event = transaction {
                val exists = Events.selectAll()
                    .where { (Events.id eq eid) and (Events.userId eq uid) }.count() > 0
                if (exists) {
                    Events.update({ (Events.id eq eid) and (Events.userId eq uid) }) {
                        it[title] = req.title; it[date] = req.date
                        it[type] = req.type.name; it[description] = req.description
                    }
                } else {
                    Events.insert {
                        it[id] = eid; it[userId] = uid; it[title] = req.title; it[date] = req.date
                        it[type] = req.type.name; it[description] = req.description
                    }
                }
                Events.selectAll().where { Events.id eq eid }.single().toEvent()
            }
            call.respond(HttpStatusCode.Created, event)
        }
        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val eid = call.parameters["id"]!!
            val req = call.receive<EventRequest>()
            val updated = transaction {
                val count = Events.update({ (Events.id eq eid) and (Events.userId eq uid) }) {
                    it[title] = req.title; it[date] = req.date
                    it[type] = req.type.name; it[description] = req.description
                }
                if (count == 0) null else Events.selectAll().where { Events.id eq eid }.single().toEvent()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }
        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val eid = call.parameters["id"]!!
            val count = transaction { Events.deleteWhere { (Events.id eq eid) and (Events.userId eq uid) } }
            if (count == 0) call.respond(HttpStatusCode.NotFound) else call.respond(HttpStatusCode.NoContent)
        }
    }
}
