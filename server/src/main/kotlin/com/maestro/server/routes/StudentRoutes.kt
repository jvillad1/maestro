package com.maestro.server.routes

import com.maestro.server.plugins.Students
import com.maestro.server.plugins.userId
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student
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
import java.time.LocalDate

private fun ResultRow.toStudent() = Student(
    id = this[Students.id],
    userId = this[Students.userId],
    name = this[Students.name],
    age = this[Students.age],
    level = Level.valueOf(this[Students.level]),
    phone = this[Students.phone],
    email = this[Students.email],
    monthlyFee = this[Students.monthlyFee],
    notes = this[Students.notes],
    joinDate = this[Students.joinDate],
    color = this[Students.color]
)

fun Route.studentRoutes() {
    route("/api/students") {
        get {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val list = transaction {
                Students.selectAll().where { Students.userId eq uid }.map { it.toStudent() }
            }
            call.respond(list)
        }

        post {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val req = call.receive<StudentRequest>()
            val sid = req.id ?: java.util.UUID.randomUUID().toString()
            val student = transaction {
                val exists = Students.selectAll()
                    .where { (Students.id eq sid) and (Students.userId eq uid) }.count() > 0
                if (exists) {
                    // Idempotent sync retry: same client id upserts instead of duplicating
                    Students.update({ (Students.id eq sid) and (Students.userId eq uid) }) {
                        it[name] = req.name; it[age] = req.age; it[level] = req.level.name
                        it[phone] = req.phone; it[email] = req.email; it[monthlyFee] = req.monthlyFee
                        it[notes] = req.notes; it[color] = req.color
                    }
                } else {
                    Students.insert {
                        it[id] = sid
                        it[userId] = uid
                        it[name] = req.name
                        it[age] = req.age
                        it[level] = req.level.name
                        it[phone] = req.phone
                        it[email] = req.email
                        it[monthlyFee] = req.monthlyFee
                        it[notes] = req.notes
                        it[joinDate] = LocalDate.now().toString()
                        it[color] = req.color
                    }
                }
                Students.selectAll().where { Students.id eq sid }.single().toStudent()
            }
            call.respond(HttpStatusCode.Created, student)
        }

        get("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val sid = call.parameters["id"]!!
            val student = transaction {
                Students.selectAll().where { (Students.id eq sid) and (Students.userId eq uid) }.singleOrNull()?.toStudent()
            }
            if (student == null) call.respond(HttpStatusCode.NotFound) else call.respond(student)
        }

        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val sid = call.parameters["id"]!!
            val req = call.receive<StudentRequest>()
            val updated = transaction {
                val count = Students.update({ (Students.id eq sid) and (Students.userId eq uid) }) {
                    it[name] = req.name; it[age] = req.age; it[level] = req.level.name
                    it[phone] = req.phone; it[email] = req.email; it[monthlyFee] = req.monthlyFee
                    it[notes] = req.notes; it[color] = req.color
                }
                if (count == 0) null
                else Students.selectAll().where { Students.id eq sid }.single().toStudent()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }

        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val sid = call.parameters["id"]!!
            val count = transaction {
                Students.deleteWhere { (Students.id eq sid) and (Students.userId eq uid) }
            }
            if (count == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}
