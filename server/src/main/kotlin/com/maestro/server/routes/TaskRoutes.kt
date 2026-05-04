package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.Priority
import com.maestro.shared.model.Task
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

private fun ResultRow.toTask() = Task(
    id = this[Tasks.id],
    userId = this[Tasks.userId],
    text = this[Tasks.text],
    priority = Priority.valueOf(this[Tasks.priority]),
    done = this[Tasks.done]
)

fun Route.taskRoutes() {
    route("/api/tasks") {
        get {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            call.respond(transaction { Tasks.selectAll().where { Tasks.userId eq uid }.map { it.toTask() } })
        }
        post {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val req = call.receive<TaskRequest>()
            val task = transaction {
                val id = Tasks.insert {
                    it[userId] = uid; it[text] = req.text
                    it[priority] = req.priority.name; it[done] = req.done
                }[Tasks.id]
                Tasks.selectAll().where { Tasks.id eq id }.single().toTask()
            }
            call.respond(HttpStatusCode.Created, task)
        }
        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val tid = call.parameters["id"]!!.toLong()
            val req = call.receive<TaskRequest>()
            val updated = transaction {
                val count = Tasks.update({ (Tasks.id eq tid) and (Tasks.userId eq uid) }) {
                    it[text] = req.text; it[priority] = req.priority.name; it[done] = req.done
                }
                if (count == 0) null else Tasks.selectAll().where { Tasks.id eq tid }.single().toTask()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }
        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val tid = call.parameters["id"]!!.toLong()
            val count = transaction { Tasks.deleteWhere { (Tasks.id eq tid) and (Tasks.userId eq uid) } }
            if (count == 0) call.respond(HttpStatusCode.NotFound) else call.respond(HttpStatusCode.NoContent)
        }
    }
}
