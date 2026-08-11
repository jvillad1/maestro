package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.ClassEntry
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

private fun ResultRow.toClassEntry() = ClassEntry(
    id = this[ClassEntries.id],
    studentId = this[ClassEntries.studentId],
    date = this[ClassEntries.date],
    topic = this[ClassEntries.topic],
    paid = this[ClassEntries.paid],
    attendance = Attendance.valueOf(this[ClassEntries.attendance])
)

fun Route.classRoutes() {
    route("/api/classes") {
        get {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val month = call.request.queryParameters["month"]
            val list = transaction {
                (ClassEntries innerJoin Students)
                    .selectAll()
                    .where { Students.userId eq uid }
                    .let { q -> if (month != null) q.andWhere { ClassEntries.date like "$month%" } else q }
                    .map { it.toClassEntry() }
            }
            call.respond(list)
        }

        post {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val req = call.receive<ClassEntryRequest>()
            val studentOwned = transaction {
                Students.selectAll().where { (Students.id eq req.studentId) and (Students.userId eq uid) }.count() > 0
            }
            if (!studentOwned) { call.respond(HttpStatusCode.Forbidden); return@post }
            val cid = req.id ?: java.util.UUID.randomUUID().toString()
            val entry = transaction {
                val exists = ClassEntries.selectAll().where { ClassEntries.id eq cid }.count() > 0
                if (exists) {
                    ClassEntries.update({ ClassEntries.id eq cid }) {
                        it[studentId] = req.studentId; it[date] = req.date
                        it[topic] = req.topic; it[paid] = req.paid
                        it[attendance] = req.attendance.name
                    }
                } else {
                    ClassEntries.insert {
                        it[id] = cid; it[studentId] = req.studentId; it[date] = req.date
                        it[topic] = req.topic; it[paid] = req.paid
                        it[attendance] = req.attendance.name
                    }
                }
                ClassEntries.selectAll().where { ClassEntries.id eq cid }.single().toClassEntry()
            }
            call.respond(HttpStatusCode.Created, entry)
        }

        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val cid = call.parameters["id"]!!
            val req = call.receive<ClassEntryRequest>()
            val updated = transaction {
                val owned = (ClassEntries innerJoin Students)
                    .selectAll().where { (ClassEntries.id eq cid) and (Students.userId eq uid) }.count() > 0
                if (!owned) return@transaction null
                ClassEntries.update({ ClassEntries.id eq cid }) {
                    it[topic] = req.topic; it[paid] = req.paid; it[date] = req.date
                    it[attendance] = req.attendance.name
                }
                ClassEntries.selectAll().where { ClassEntries.id eq cid }.single().toClassEntry()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }

        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val cid = call.parameters["id"]!!
            val count = transaction {
                val owned = (ClassEntries innerJoin Students)
                    .selectAll().where { (ClassEntries.id eq cid) and (Students.userId eq uid) }.count()
                if (owned == 0L) 0 else ClassEntries.deleteWhere { ClassEntries.id eq cid }
            }
            if (count == 0) call.respond(HttpStatusCode.NotFound) else call.respond(HttpStatusCode.NoContent)
        }
    }
}
