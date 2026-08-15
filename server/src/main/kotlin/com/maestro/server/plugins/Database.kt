package com.maestro.server.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = varchar("created_at", 30)
    override val primaryKey = PrimaryKey(id)
}

object Students : Table("students") {
    val id = varchar("id", 36)
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val age = integer("age")
    val level = varchar("level", 20)
    val phone = varchar("phone", 50)
    val email = varchar("email", 255)
    val monthlyFee = long("monthly_fee")
    val notes = text("notes").default("")
    val joinDate = varchar("join_date", 10)
    val color = varchar("color", 7)
    override val primaryKey = PrimaryKey(id)
}

object ClassEntries : Table("class_entries") {
    val id = varchar("id", 36)
    val studentId = varchar("student_id", 36).references(Students.id, onDelete = ReferenceOption.CASCADE)
    val date = varchar("date", 10)
    val topic = varchar("topic", 500)
    val paid = bool("paid").default(false)
    val attendance = varchar("attendance", 10).default("PENDIENTE")
    val notes = text("notes").default("")
    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table("tasks") {
    val id = varchar("id", 36)
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val text = varchar("text", 500)
    val priority = varchar("priority", 10)
    val done = bool("done").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Events : Table("events") {
    val id = varchar("id", 36)
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val date = varchar("date", 10)
    val type = varchar("type", 20)
    val description = varchar("description", 500)
    override val primaryKey = PrimaryKey(id)
}

fun Application.configureDatabase() {
    val config = environment.config
    val rawUrl = config.property("database.url").getString()

    // Railway/Heroku-style URLs (postgres://user:pass@host:port/db) carry
    // credentials that JDBC can't read from the URL itself
    val (url, user, password) = if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
        val uri = java.net.URI(rawUrl.replaceFirst("postgres://", "postgresql://"))
        val creds = (uri.userInfo ?: "").split(":", limit = 2)
        val port = if (uri.port == -1) 5432 else uri.port
        Triple(
            "jdbc:postgresql://${uri.host}:$port${uri.path}",
            creds.getOrElse(0) { "" },
            creds.getOrElse(1) { "" }
        )
    } else {
        Triple(rawUrl, "", "")
    }

    val driver = when {
        url.startsWith("jdbc:postgresql") -> "org.postgresql.Driver"
        else -> "org.h2.Driver"
    }

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        this.password = password
        maximumPoolSize = 10
    })
    Database.connect(dataSource)

    // One-shot migration (2026-08): entity ids moved from BIGINT autoincrement to
    // client-generated varchar(36). SchemaUtils.create can't alter column types,
    // so a pre-UUID schema gets its entity tables dropped (test data only).
    if (driver == "org.postgresql.Driver") {
        transaction {
            val idType = exec(
                "SELECT data_type FROM information_schema.columns WHERE table_name = 'students' AND column_name = 'id'"
            ) { rs -> if (rs.next()) rs.getString(1) else null }
            if (idType == "bigint") {
                exec("DROP TABLE IF EXISTS class_entries, tasks, events, students CASCADE")
            }
        }
    }

    transaction {
        SchemaUtils.create(Users, Students, ClassEntries, Tasks, Events)
        // SchemaUtils.create no agrega columnas a tablas existentes (2026-08: asistencia, notas)
        exec("ALTER TABLE class_entries ADD COLUMN IF NOT EXISTS attendance VARCHAR(10) DEFAULT 'PENDIENTE' NOT NULL")
        exec("ALTER TABLE class_entries ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT '' NOT NULL")
    }
}
