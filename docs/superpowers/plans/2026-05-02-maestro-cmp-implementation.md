# Maestro CMP — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Scaffold a Kotlin Multiplatform monorepo with Compose Multiplatform (Web + Android) frontend and Ktor backend for the Maestro music-teacher app.

**Architecture:** Three Gradle modules — `shared` (domain models + DTOs), `server` (Ktor 3 + Exposed ORM + JWT auth + PostgreSQL/H2), `composeApp` (Compose Multiplatform targeting Kotlin/Wasm for Web and Android). All data classes defined once in `shared` and used by both client and server.

**Tech Stack:** Kotlin 2.1.20 · Compose Multiplatform 1.7.3 · Ktor 3.0.3 · Exposed 0.55.0 · H2 (dev) / PostgreSQL (prod) · Navigation Compose (CMP) · Lifecycle ViewModel (CMP) · kotlinx.serialization · BCrypt · JWT · Railway (deployment)

**Reference:** `documentation/maestro-app.jsx` — full working prototype in React. All UI logic, palette, and data shapes derive from this file.

---

## Phase 1 — Gradle Monorepo Scaffold

### Task 1: Root Gradle setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `.gitignore`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "maestro"
include(":shared", ":server", ":composeApp")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

- [ ] **Step 2: Create `build.gradle.kts` (root)**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **Step 3: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.20"
compose-multiplatform = "1.7.3"
ktor = "3.0.3"
exposed = "0.55.0"
h2 = "2.3.232"
postgresql = "42.7.4"
bcrypt = "0.10.2"
lifecycle-viewmodel = "2.8.4"
navigation-compose = "2.8.4"
kotlinx-serialization = "1.7.3"
kotlinx-coroutines = "1.9.0"
datastore = "1.1.1"
logback = "1.5.12"

[libraries]
# Ktor server
ktor-server-core = { module = "io.ktor:ktor-server-core-jvm", version.ref = "ktor" }
ktor-server-netty = { module = "io.ktor:ktor-server-netty-jvm", version.ref = "ktor" }
ktor-server-auth = { module = "io.ktor:ktor-server-auth-jvm", version.ref = "ktor" }
ktor-server-auth-jwt = { module = "io.ktor:ktor-server-auth-jwt-jvm", version.ref = "ktor" }
ktor-server-content-negotiation = { module = "io.ktor:ktor-server-content-negotiation-jvm", version.ref = "ktor" }
ktor-server-cors = { module = "io.ktor:ktor-server-cors-jvm", version.ref = "ktor" }
ktor-server-config-yaml = { module = "io.ktor:ktor-server-config-yaml-jvm", version.ref = "ktor" }
ktor-server-test-host = { module = "io.ktor:ktor-server-test-host", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
# Ktor client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
# Database
exposed-core = { module = "org.jetbrains.exposed:exposed-core", version.ref = "exposed" }
exposed-dao = { module = "org.jetbrains.exposed:exposed-dao", version.ref = "exposed" }
exposed-jdbc = { module = "org.jetbrains.exposed:exposed-jdbc", version.ref = "exposed" }
h2 = { module = "com.h2database:h2", version.ref = "h2" }
postgresql = { module = "org.postgresql:postgresql", version.ref = "postgresql" }
bcrypt = { module = "at.favre.lib:bcrypt", version.ref = "bcrypt" }
logback = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }
# CMP
lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle-viewmodel" }
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
# Kotlinx
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
# Android
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 4: Create `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 5: Download Gradle wrapper scripts**

```bash
cd /Users/jvillada/AndroidStudioProjects/maestro
gradle wrapper --gradle-version 8.10.2
```

Expected: creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`.

- [ ] **Step 6: Create `.gitignore`**

```
.gradle/
build/
*.class
*.jar
*.war
.idea/
*.iml
local.properties
.DS_Store
.superpowers/
*.env
```

- [ ] **Step 7: Commit**

```bash
git init
git add settings.gradle.kts build.gradle.kts gradle/ .gitignore gradlew gradlew.bat
git commit -m "feat: initialize Gradle monorepo scaffold"
```

---

### Task 2: Shared module

**Files:**
- Create: `shared/build.gradle.kts`
- Create: `shared/src/commonMain/kotlin/com/maestro/shared/` (directory)

- [ ] **Step 1: Create `shared/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    js(IR) { browser(); nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
```

- [ ] **Step 2: Verify shared module compiles**

```bash
./gradlew :shared:compileKotlinJvm
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/
git commit -m "feat: add shared KMP module"
```

---

## Phase 2 — Shared Domain Models

### Task 3: Domain models and enums

**Files:**
- Create: `shared/src/commonMain/kotlin/com/maestro/shared/model/Models.kt`

- [ ] **Step 1: Create `Models.kt`**

```kotlin
package com.maestro.shared.model

import kotlinx.serialization.Serializable

enum class Level { INICIAL, ELEMENTAL, INTERMEDIO, AVANZADO }
enum class Priority { ALTA, MEDIA, BAJA }
enum class EventType { RECITAL, MASTERCLASS, EVALUACION, OTRO }

@Serializable
data class User(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: String
)

@Serializable
data class Student(
    val id: Long,
    val userId: Long,
    val name: String,
    val age: Int,
    val level: Level,
    val phone: String,
    val email: String,
    val monthlyFee: Long,
    val notes: String,
    val joinDate: String,
    val color: String
)

@Serializable
data class ClassEntry(
    val id: Long,
    val studentId: Long,
    val date: String,
    val topic: String,
    val paid: Boolean
)

@Serializable
data class Task(
    val id: Long,
    val userId: Long,
    val text: String,
    val priority: Priority,
    val done: Boolean
)

@Serializable
data class Event(
    val id: Long,
    val userId: Long,
    val title: String,
    val date: String,
    val type: EventType,
    val description: String
)
```

- [ ] **Step 2: Write and run model tests**

Create `shared/src/commonTest/kotlin/com/maestro/shared/model/ModelsTest.kt`:

```kotlin
package com.maestro.shared.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsTest {
    @Test
    fun `Student serializes and deserializes correctly`() {
        val student = Student(
            id = 1L, userId = 1L, name = "Isabella García", age = 10,
            level = Level.ELEMENTAL, phone = "300-123-4567", email = "isabella@gmail.com",
            monthlyFee = 250000L, notes = "Muy dedicada", joinDate = "2024-01-15", color = "#C9A84C"
        )
        val json = Json.encodeToString(student)
        val decoded = Json.decodeFromString<Student>(json)
        assertEquals(student, decoded)
    }

    @Test
    fun `Task priority enum round-trips`() {
        val task = Task(1L, 1L, "Preparar recital", Priority.ALTA, false)
        val json = Json.encodeToString(task)
        assertEquals(Priority.ALTA, Json.decodeFromString<Task>(json).priority)
    }
}
```

```bash
./gradlew :shared:jvmTest
```

Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 3: Commit**

```bash
git add shared/src/
git commit -m "feat: add shared domain models with serialization"
```

---

### Task 4: Request/Response DTOs

**Files:**
- Create: `shared/src/commonMain/kotlin/com/maestro/shared/dto/Dto.kt`

- [ ] **Step 1: Create `Dto.kt`**

```kotlin
package com.maestro.shared.dto

import com.maestro.shared.model.*
import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val email: String, val password: String, val name: String)
@Serializable data class AuthResponse(val token: String, val user: User)

@Serializable data class StudentRequest(
    val name: String, val age: Int, val level: Level, val phone: String,
    val email: String, val monthlyFee: Long, val notes: String, val color: String
)

@Serializable data class ClassEntryRequest(
    val studentId: Long, val date: String, val topic: String, val paid: Boolean
)

@Serializable data class TaskRequest(val text: String, val priority: Priority, val done: Boolean)

@Serializable data class EventRequest(
    val title: String, val date: String, val type: EventType, val description: String
)
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :shared:compileKotlinJvm
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/maestro/shared/dto/
git commit -m "feat: add API request/response DTOs to shared module"
```

---

## Phase 3 — Ktor Backend

### Task 5: Server module foundation

**Files:**
- Create: `server/build.gradle.kts`
- Create: `server/src/main/kotlin/com/maestro/server/Application.kt`
- Create: `server/src/main/resources/application.conf`
- Create: `server/src/main/resources/logback.xml`

- [ ] **Step 1: Create `server/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.maestro.server.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.postgresql)
    implementation(libs.bcrypt)
    implementation(libs.logback)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}
```

- [ ] **Step 2: Create `server/src/main/resources/application.conf`**

```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
    application {
        modules = [com.maestro.server.ApplicationKt.module]
    }
}

database {
    url = "jdbc:h2:mem:maestro;DB_CLOSE_DELAY=-1"
    url = ${?DATABASE_URL}
    driver = "org.h2.Driver"
}

jwt {
    secret = "maestro-dev-secret-change-in-production"
    secret = ${?JWT_SECRET}
    issuer = "maestro"
    audience = "maestro-users"
    expirationDays = 30
}
```

- [ ] **Step 3: Create `server/src/main/resources/logback.xml`**

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder><pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern></encoder>
    </appender>
    <root level="INFO"><appender-ref ref="STDOUT"/></root>
</configuration>
```

- [ ] **Step 4: Create `server/src/main/kotlin/com/maestro/server/Application.kt`**

```kotlin
package com.maestro.server

import com.maestro.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureCors()
    configureAuth()
    configureRouting()
}
```

- [ ] **Step 5: Compile check**

```bash
./gradlew :server:compileKotlin
```

Expected: compile errors on missing plugin files — that is expected at this stage. Verify the module resolves and shared classes are visible. Fix only import errors if any.

- [ ] **Step 6: Commit**

```bash
git add server/
git commit -m "feat: add server module skeleton"
```

---

### Task 6: Database plugin and Exposed tables

**Files:**
- Create: `server/src/main/kotlin/com/maestro/server/plugins/Database.kt`

- [ ] **Step 1: Create `Database.kt`**

```kotlin
package com.maestro.server.plugins

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
    val id = long("id").autoIncrement()
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
    val id = long("id").autoIncrement()
    val studentId = long("student_id").references(Students.id, onDelete = ReferenceOption.CASCADE)
    val date = varchar("date", 10)
    val topic = varchar("topic", 500)
    val paid = bool("paid").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table("tasks") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val text = varchar("text", 500)
    val priority = varchar("priority", 10)
    val done = bool("done").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Events : Table("events") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val date = varchar("date", 10)
    val type = varchar("type", 20)
    val description = varchar("description", 500)
    override val primaryKey = PrimaryKey(id)
}

fun Application.configureDatabase() {
    val config = environment.config
    val url = config.property("database.url").getString()
    val driver = config.property("database.driver").getString()

    Database.connect(url, driver)

    transaction {
        SchemaUtils.create(Users, Students, ClassEntries, Tasks, Events)
    }
}
```

- [ ] **Step 2: Compile check**

```bash
./gradlew :server:compileKotlin
```

Expected: fails only on missing plugin files (Serialization, Cors, Auth, Routing). No errors inside Database.kt.

- [ ] **Step 3: Commit**

```bash
git add server/src/main/kotlin/com/maestro/server/plugins/Database.kt
git commit -m "feat: add Exposed tables and database plugin"
```

---

### Task 7: Auth plugin and routes with tests

**Files:**
- Create: `server/src/main/kotlin/com/maestro/server/plugins/Auth.kt`
- Create: `server/src/main/kotlin/com/maestro/server/plugins/Serialization.kt`
- Create: `server/src/main/kotlin/com/maestro/server/plugins/Cors.kt`
- Create: `server/src/main/kotlin/com/maestro/server/plugins/Routing.kt`
- Create: `server/src/main/kotlin/com/maestro/server/routes/AuthRoutes.kt`
- Create: `server/src/test/kotlin/com/maestro/server/AuthRoutesTest.kt`

- [ ] **Step 1: Create `Serialization.kt`**

```kotlin
package com.maestro.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
}
```

- [ ] **Step 2: Create `Cors.kt`**

```kotlin
package com.maestro.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
}
```

- [ ] **Step 3: Create `Auth.kt`**

```kotlin
package com.maestro.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    lateinit var secret: String
    lateinit var issuer: String
    lateinit var audience: String
    var expirationDays: Long = 30

    fun makeToken(userId: Long): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + expirationDays * 86_400_000L))
        .sign(Algorithm.HMAC256(secret))
}

fun Application.configureAuth() {
    val config = environment.config
    JwtConfig.secret = config.property("jwt.secret").getString()
    JwtConfig.issuer = config.property("jwt.issuer").getString()
    JwtConfig.audience = config.property("jwt.audience").getString()
    JwtConfig.expirationDays = config.propertyOrNull("jwt.expirationDays")?.getString()?.toLong() ?: 30

    install(Authentication) {
        jwt("jwt-auth") {
            realm = "Maestro"
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.secret))
                    .withAudience(JwtConfig.audience)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asLong() != null)
                    JWTPrincipal(credential.payload) else null
            }
        }
    }
}

fun JWTPrincipal.userId(): Long = payload.getClaim("userId").asLong()
```

- [ ] **Step 4: Create `AuthRoutes.kt`**

```kotlin
package com.maestro.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.maestro.server.plugins.JwtConfig
import com.maestro.server.plugins.Users
import com.maestro.shared.dto.AuthResponse
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import com.maestro.shared.model.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val existing = transaction { Users.selectAll().where { Users.email eq req.email }.singleOrNull() }
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, "Email already registered")
                return@post
            }
            val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
            val userId = transaction {
                Users.insertAndGetId {
                    it[email] = req.email
                    it[name] = req.name
                    it[passwordHash] = hash
                    it[createdAt] = Instant.now().toString()
                }.value
            }
            val user = User(userId, req.email, req.name, Instant.now().toString())
            call.respond(HttpStatusCode.Created, AuthResponse(JwtConfig.makeToken(userId), user))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val row = transaction { Users.selectAll().where { Users.email eq req.email }.singleOrNull() }
            if (row == null || !BCrypt.verifyer().verify(req.password.toCharArray(), row[Users.passwordHash]).verified) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }
            val user = User(row[Users.id].value, row[Users.email], row[Users.name], row[Users.createdAt])
            call.respond(AuthResponse(JwtConfig.makeToken(user.id), user))
        }
    }
}
```

- [ ] **Step 5: Create `Routing.kt`**

```kotlin
package com.maestro.server.plugins

import com.maestro.server.routes.*
import io.ktor.server.application.*
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
```

- [ ] **Step 6: Write failing auth tests**

Create `server/src/test/kotlin/com/maestro/server/AuthRoutesTest.kt`:

```kotlin
package com.maestro.server

import com.maestro.shared.dto.AuthResponse
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRoutesTest {
    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    @Test
    fun `register returns 201 with token and user`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@maestro.com", "password123", "Test Teacher"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertNotNull(body.token)
        assertEquals("test@maestro.com", body.user.email)
    }

    @Test
    fun `register duplicate email returns 409`() = testApplication {
        application { module() }
        val client = jsonClient()
        val req = RegisterRequest("dup@maestro.com", "pass", "Dup")
        client.post("/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }
        val response = client.post("/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `login with valid credentials returns token`() = testApplication {
        application { module() }
        val client = jsonClient()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("login@maestro.com", "mypassword", "Login Teacher"))
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("login@maestro.com", "mypassword"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.body<AuthResponse>().token)
    }

    @Test
    fun `login with wrong password returns 401`() = testApplication {
        application { module() }
        val client = jsonClient()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("bad@maestro.com", "correct", "Bad"))
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("bad@maestro.com", "wrong"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
```

- [ ] **Step 7: Run tests**

```bash
./gradlew :server:test --tests "com.maestro.server.AuthRoutesTest"
```

Expected: 4 tests pass.

- [ ] **Step 8: Commit**

```bash
git add server/src/
git commit -m "feat: add auth routes (register/login/JWT) with tests"
```

---

### Task 8: Student routes with tests

**Files:**
- Create: `server/src/main/kotlin/com/maestro/server/routes/StudentRoutes.kt`
- Create: `server/src/test/kotlin/com/maestro/server/StudentRoutesTest.kt`

- [ ] **Step 1: Create `StudentRoutes.kt`**

```kotlin
package com.maestro.server.routes

import com.maestro.server.plugins.Students
import com.maestro.server.plugins.userId
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

private fun ResultRow.toStudent() = Student(
    id = this[Students.id].value,
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
            val id = transaction {
                Students.insertAndGetId {
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
                }.value
            }
            val student = transaction { Students.selectAll().where { Students.id eq id }.single().toStudent() }
            call.respond(HttpStatusCode.Created, student)
        }

        get("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val sid = call.parameters["id"]!!.toLong()
            val student = transaction {
                Students.selectAll().where { (Students.id eq sid) and (Students.userId eq uid) }.singleOrNull()?.toStudent()
            }
            if (student == null) call.respond(HttpStatusCode.NotFound) else call.respond(student)
        }

        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val sid = call.parameters["id"]!!.toLong()
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
            val sid = call.parameters["id"]!!.toLong()
            val count = transaction {
                Students.deleteWhere { (Students.id eq sid) and (Students.userId eq uid) }
            }
            if (count == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}
```

- [ ] **Step 2: Write failing student tests**

Create `server/src/test/kotlin/com/maestro/server/StudentRoutesTest.kt`:

```kotlin
package com.maestro.server

import com.maestro.shared.dto.*
import com.maestro.shared.model.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StudentRoutesTest {
    private fun ApplicationTestBuilder.authedClient(): Pair<io.ktor.client.HttpClient, String> {
        val client = createClient { install(ContentNegotiation) { json() } }
        val token = kotlinx.coroutines.runBlocking {
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("teacher@test.com", "pass123", "Teacher"))
            }.body<AuthResponse>().token
        }
        return client to token
    }

    private fun studentReq() = StudentRequest(
        "Isabella García", 10, Level.ELEMENTAL, "300-123", "isa@mail.com", 250000L, "Dedicada", "#C9A84C"
    )

    @Test
    fun `create student returns 201`() = testApplication {
        application { module() }
        val (client, token) = authedClient()
        val response = client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Isabella García", response.body<Student>().name)
    }

    @Test
    fun `list students returns only own students`() = testApplication {
        application { module() }
        val (client, token) = authedClient()
        client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }
        val response = client.get("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, response.body<List<Student>>().size)
    }

    @Test
    fun `delete student returns 204`() = testApplication {
        application { module() }
        val (client, token) = authedClient()
        val student = client.post("/api/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(studentReq())
        }.body<Student>()
        val response = client.delete("/api/students/${student.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
```

- [ ] **Step 3: Run tests**

```bash
./gradlew :server:test --tests "com.maestro.server.StudentRoutesTest"
```

Expected: 3 tests pass.

- [ ] **Step 4: Commit**

```bash
git add server/src/main/kotlin/com/maestro/server/routes/StudentRoutes.kt server/src/test/kotlin/com/maestro/server/StudentRoutesTest.kt
git commit -m "feat: add student CRUD routes with tests"
```

---

### Task 9: Class, Task, and Event routes

**Files:**
- Create: `server/src/main/kotlin/com/maestro/server/routes/ClassRoutes.kt`
- Create: `server/src/main/kotlin/com/maestro/server/routes/TaskRoutes.kt`
- Create: `server/src/main/kotlin/com/maestro/server/routes/EventRoutes.kt`

- [ ] **Step 1: Create `ClassRoutes.kt`**

```kotlin
package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.ClassEntryRequest
import com.maestro.shared.model.ClassEntry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private fun ResultRow.toClassEntry() = ClassEntry(
    id = this[ClassEntries.id].value,
    studentId = this[ClassEntries.studentId],
    date = this[ClassEntries.date],
    topic = this[ClassEntries.topic],
    paid = this[ClassEntries.paid]
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
            val id = transaction {
                ClassEntries.insertAndGetId {
                    it[studentId] = req.studentId; it[date] = req.date
                    it[topic] = req.topic; it[paid] = req.paid
                }.value
            }
            val entry = transaction { ClassEntries.selectAll().where { ClassEntries.id eq id }.single().toClassEntry() }
            call.respond(HttpStatusCode.Created, entry)
        }

        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val cid = call.parameters["id"]!!.toLong()
            val req = call.receive<ClassEntryRequest>()
            val updated = transaction {
                val row = (ClassEntries innerJoin Students)
                    .selectAll().where { (ClassEntries.id eq cid) and (Students.userId eq uid) }.singleOrNull()
                    ?: return@transaction null
                ClassEntries.update({ ClassEntries.id eq cid }) {
                    it[topic] = req.topic; it[paid] = req.paid; it[date] = req.date
                }
                ClassEntries.selectAll().where { ClassEntries.id eq cid }.single().toClassEntry()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }

        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val cid = call.parameters["id"]!!.toLong()
            val count = transaction {
                val owned = (ClassEntries innerJoin Students)
                    .selectAll().where { (ClassEntries.id eq cid) and (Students.userId eq uid) }.count()
                if (owned == 0L) 0 else ClassEntries.deleteWhere { ClassEntries.id eq cid }
            }
            if (count == 0) call.respond(HttpStatusCode.NotFound) else call.respond(HttpStatusCode.NoContent)
        }
    }
}
```

- [ ] **Step 2: Create `TaskRoutes.kt`**

```kotlin
package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.TaskRequest
import com.maestro.shared.model.Priority
import com.maestro.shared.model.Task
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private fun ResultRow.toTask() = Task(
    id = this[Tasks.id].value, userId = this[Tasks.userId],
    text = this[Tasks.text], priority = Priority.valueOf(this[Tasks.priority]),
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
            val id = transaction {
                Tasks.insertAndGetId { it[userId] = uid; it[text] = req.text; it[priority] = req.priority.name; it[done] = req.done }.value
            }
            call.respond(HttpStatusCode.Created, transaction { Tasks.selectAll().where { Tasks.id eq id }.single().toTask() })
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
```

- [ ] **Step 3: Create `EventRoutes.kt`**

```kotlin
package com.maestro.server.routes

import com.maestro.server.plugins.*
import com.maestro.shared.dto.EventRequest
import com.maestro.shared.model.Event
import com.maestro.shared.model.EventType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private fun ResultRow.toEvent() = Event(
    id = this[Events.id].value, userId = this[Events.userId],
    title = this[Events.title], date = this[Events.date],
    type = EventType.valueOf(this[Events.type]), description = this[Events.description]
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
            val id = transaction {
                Events.insertAndGetId {
                    it[userId] = uid; it[title] = req.title; it[date] = req.date
                    it[type] = req.type.name; it[description] = req.description
                }.value
            }
            call.respond(HttpStatusCode.Created, transaction { Events.selectAll().where { Events.id eq id }.single().toEvent() })
        }
        put("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val eid = call.parameters["id"]!!.toLong()
            val req = call.receive<EventRequest>()
            val updated = transaction {
                val count = Events.update({ (Events.id eq eid) and (Events.userId eq uid) }) {
                    it[title] = req.title; it[date] = req.date; it[type] = req.type.name; it[description] = req.description
                }
                if (count == 0) null else Events.selectAll().where { Events.id eq eid }.single().toEvent()
            }
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }
        delete("/{id}") {
            val uid = call.principal<JWTPrincipal>()!!.userId()
            val eid = call.parameters["id"]!!.toLong()
            val count = transaction { Events.deleteWhere { (Events.id eq eid) and (Events.userId eq uid) } }
            if (count == 0) call.respond(HttpStatusCode.NotFound) else call.respond(HttpStatusCode.NoContent)
        }
    }
}
```

- [ ] **Step 4: Run all server tests**

```bash
./gradlew :server:test
```

Expected: All tests pass. Backend is fully functional.

- [ ] **Step 5: Start server and verify manually**

```bash
./gradlew :server:run
```

Open: `http://localhost:8080` — should get 404 (no root route). Backend is alive.

- [ ] **Step 6: Commit**

```bash
git add server/src/
git commit -m "feat: add class, task, and event CRUD routes"
```

---

## Phase 4 — Compose Multiplatform Frontend

### Task 10: ComposeApp module setup

**Files:**
- Create: `composeApp/build.gradle.kts`
- Create: `composeApp/src/androidMain/AndroidManifest.xml`
- Create: `composeApp/src/androidMain/kotlin/com/maestro/app/MainActivity.kt`
- Create: `composeApp/src/wasmJsMain/resources/index.html`
- Create: `composeApp/src/wasmJsMain/resources/styles.css`
- Create: `composeApp/src/wasmJsMain/kotlin/com/maestro/app/main.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/App.kt`

- [ ] **Step 1: Create `composeApp/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "11" } }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":shared"))
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.datastore.preferences)
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = "com.maestro.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.maestro.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

- [ ] **Step 2: Create `composeApp/src/androidMain/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET"/>
    <application
        android:allowBackup="true"
        android:label="Maestro"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 3: Create `composeApp/src/androidMain/kotlin/com/maestro/app/MainActivity.kt`**

```kotlin
package com.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}
```

- [ ] **Step 4: Create `composeApp/src/wasmJsMain/resources/index.html`**

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Maestro</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <canvas id="ComposeTarget"></canvas>
    <script src="skiko.js"></script>
    <script src="composeApp.js"></script>
</body>
</html>
```

- [ ] **Step 5: Create `composeApp/src/wasmJsMain/resources/styles.css`**

```css
html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
canvas { display: block; width: 100%; height: 100%; }
```

- [ ] **Step 6: Create `composeApp/src/wasmJsMain/kotlin/com/maestro/app/main.kt`**

```kotlin
package com.maestro.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
```

- [ ] **Step 7: Create `composeApp/src/commonMain/kotlin/com/maestro/app/App.kt`** (placeholder)

```kotlin
package com.maestro.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun App() {
    Text("Maestro")
}
```

- [ ] **Step 8: Compile check**

```bash
./gradlew :composeApp:compileKotlinWasmJs
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add composeApp/
git commit -m "feat: add composeApp module for Web (Wasm) and Android"
```

---

### Task 11: Theme, TokenStorage, and ApiClient

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/theme/MaestroTheme.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/auth/TokenStorage.kt` (expect)
- Create: `composeApp/src/wasmJsMain/kotlin/com/maestro/app/auth/TokenStorage.wasmJs.kt`
- Create: `composeApp/src/androidMain/kotlin/com/maestro/app/auth/TokenStorage.android.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/network/ApiClient.kt`

- [ ] **Step 1: Create `MaestroTheme.kt`**

```kotlin
package com.maestro.app.theme

import androidx.compose.ui.graphics.Color

object MaestroColors {
    val Cream = Color(0xFFF5F0E8)
    val Espresso = Color(0xFF2C1810)
    val Gold = Color(0xFFC9A84C)
    val Forest = Color(0xFF2D5016)
    val Terra = Color(0xFFB85C38)
    val White = Color(0xFFFDFAF5)
    val Muted = Color(0xFF8B7355)
    val LightGold = Color(0xFFF0E6C8)
    val SoftGreen = Color(0xFFE8F0E0)
}
```

- [ ] **Step 2: Create `TokenStorage.kt` (expect/actual interface)**

```kotlin
package com.maestro.app.auth

expect class TokenStorage() {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}
```

- [ ] **Step 3: Create `TokenStorage.wasmJs.kt`**

```kotlin
package com.maestro.app.auth

import kotlinx.browser.localStorage

actual class TokenStorage actual constructor() {
    actual fun getToken(): String? = localStorage.getItem("maestro_token")
    actual fun saveToken(token: String) { localStorage.setItem("maestro_token", token) }
    actual fun clearToken() { localStorage.removeItem("maestro_token") }
}
```

- [ ] **Step 4: Create `TokenStorage.android.kt`**

```kotlin
package com.maestro.app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "maestro_prefs")
private val TOKEN_KEY = stringPreferencesKey("maestro_token")

actual class TokenStorage actual constructor() {
    private val context: Context get() = AppContextHolder.context

    actual fun getToken(): String? = runBlocking {
        context.dataStore.data.first()[TOKEN_KEY]
    }

    actual fun saveToken(token: String) { runBlocking {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }}

    actual fun clearToken() { runBlocking {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }}
}
```

- [ ] **Step 5: Create `AppContextHolder.kt` in androidMain**

```kotlin
package com.maestro.app

import android.content.Context

object AppContextHolder {
    lateinit var context: Context
}
```

Update `MainActivity.kt` to initialize it:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppContextHolder.context = applicationContext
    enableEdgeToEdge()
    setContent { App() }
}
```

- [ ] **Step 6: Create `ApiClient.kt`**

```kotlin
package com.maestro.app.network

import com.maestro.app.auth.TokenStorage
import com.maestro.shared.dto.*
import com.maestro.shared.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String,
    private val tokenStorage: TokenStorage
) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun HttpRequestBuilder.auth() {
        tokenStorage.getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    // Auth
    suspend fun login(req: LoginRequest): AuthResponse =
        client.post("$baseUrl/api/auth/login") { contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun register(req: RegisterRequest): AuthResponse =
        client.post("$baseUrl/api/auth/register") { contentType(ContentType.Application.Json); setBody(req) }.body()

    // Students
    suspend fun getStudents(): List<Student> =
        client.get("$baseUrl/api/students") { auth() }.body()

    suspend fun createStudent(req: StudentRequest): Student =
        client.post("$baseUrl/api/students") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateStudent(id: Long, req: StudentRequest): Student =
        client.put("$baseUrl/api/students/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteStudent(id: Long) =
        client.delete("$baseUrl/api/students/$id") { auth() }

    // Classes
    suspend fun getClasses(month: String? = null): List<ClassEntry> =
        client.get("$baseUrl/api/classes") { auth(); month?.let { parameter("month", it) } }.body()

    suspend fun createClass(req: ClassEntryRequest): ClassEntry =
        client.post("$baseUrl/api/classes") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateClass(id: Long, req: ClassEntryRequest): ClassEntry =
        client.put("$baseUrl/api/classes/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    // Tasks
    suspend fun getTasks(): List<Task> =
        client.get("$baseUrl/api/tasks") { auth() }.body()

    suspend fun createTask(req: TaskRequest): Task =
        client.post("$baseUrl/api/tasks") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateTask(id: Long, req: TaskRequest): Task =
        client.put("$baseUrl/api/tasks/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteTask(id: Long) =
        client.delete("$baseUrl/api/tasks/$id") { auth() }

    // Events
    suspend fun getEvents(): List<Event> =
        client.get("$baseUrl/api/events") { auth() }.body()

    suspend fun createEvent(req: EventRequest): Event =
        client.post("$baseUrl/api/events") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun updateEvent(id: Long, req: EventRequest): Event =
        client.put("$baseUrl/api/events/$id") { auth(); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteEvent(id: Long) =
        client.delete("$baseUrl/api/events/$id") { auth() }
}
```

- [ ] **Step 7: Compile check**

```bash
./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileKotlinAndroid
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/
git commit -m "feat: add theme, TokenStorage expect/actual, and ApiClient"
```

---

### Task 12: Navigation and app wiring

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/navigation/AppNavigation.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/maestro/app/App.kt`

- [ ] **Step 1: Create `AppNavigation.kt`**

```kotlin
package com.maestro.app.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.app.ui.auth.AuthScreen
import com.maestro.app.ui.dashboard.DashboardScreen
import com.maestro.app.ui.students.StudentsScreen
import com.maestro.app.ui.students.StudentDetailScreen
import com.maestro.app.ui.classes.ClassesScreen
import com.maestro.app.ui.finances.FinancesScreen
import com.maestro.app.ui.tasks.TasksScreen
import com.maestro.app.ui.events.EventsScreen
import com.maestro.app.ui.metronome.MetronomeScreen
import com.maestro.app.ui.guide.GuideScreen

const val BASE_URL = "http://localhost:8080"

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Students : Screen("students")
    object StudentDetail : Screen("students/{studentId}") {
        fun route(id: Long) = "students/$id"
    }
    object Classes : Screen("classes")
    object Finances : Screen("finances")
    object Tasks : Screen("tasks")
    object Events : Screen("events")
    object Metronome : Screen("metronome")
    object Guide : Screen("guide")
}

@Composable
fun AppNavigation() {
    val tokenStorage = remember { TokenStorage() }
    val apiClient = remember { ApiClient(BASE_URL, tokenStorage) }
    val navController = rememberNavController()
    val startDestination = if (tokenStorage.getToken() != null) Screen.Dashboard.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(apiClient, tokenStorage) { navController.navigate(Screen.Dashboard.route) { popUpTo(0) } }
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(apiClient, navController)
        }
        composable(Screen.Students.route) {
            StudentsScreen(apiClient, navController)
        }
        composable(Screen.StudentDetail.route) { backStack ->
            val studentId = backStack.arguments?.getString("studentId")!!.toLong()
            StudentDetailScreen(studentId, apiClient, navController)
        }
        composable(Screen.Classes.route) { ClassesScreen(apiClient, navController) }
        composable(Screen.Finances.route) { FinancesScreen(apiClient, navController) }
        composable(Screen.Tasks.route) { TasksScreen(apiClient, navController) }
        composable(Screen.Events.route) { EventsScreen(apiClient, navController) }
        composable(Screen.Metronome.route) { MetronomeScreen(navController) }
        composable(Screen.Guide.route) { GuideScreen(navController) }
    }
}
```

- [ ] **Step 2: Update `App.kt`**

```kotlin
package com.maestro.app

import androidx.compose.runtime.Composable
import com.maestro.app.navigation.AppNavigation

@Composable
fun App() {
    AppNavigation()
}
```

- [ ] **Step 3: Compile check**

```bash
./gradlew :composeApp:compileKotlinWasmJs
```

Expected: Fails with unresolved screen composables — expected at this stage. Verify only Navigation imports are clean.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/maestro/app/navigation/ composeApp/src/commonMain/kotlin/com/maestro/app/App.kt
git commit -m "feat: add navigation graph with auth guard"
```

---

### Task 13: Auth screen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/auth/AuthViewModel.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/auth/AuthScreen.kt`

- [ ] **Step 1: Create `AuthViewModel.kt`**

```kotlin
package com.maestro.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistering: Boolean = false
)

class AuthViewModel(
    private val apiClient: ApiClient,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = apiClient.login(LoginRequest(email, password))
                tokenStorage.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Credenciales incorrectas", isLoading = false)
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = apiClient.register(RegisterRequest(email, password, name))
                tokenStorage.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al registrar. Intenta de nuevo.", isLoading = false)
            }
        }
    }

    fun toggleMode() { _state.value = _state.value.copy(isRegistering = !_state.value.isRegistering, error = null) }
}
```

- [ ] **Step 2: Create `AuthScreen.kt`**

```kotlin
package com.maestro.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors

@Composable
fun AuthScreen(apiClient: ApiClient, tokenStorage: TokenStorage, onSuccess: () -> Unit) {
    val vm = viewModel { AuthViewModel(apiClient, tokenStorage) }
    val state by vm.state.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(MaestroColors.Cream),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
        ) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("MAESTRO", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)
                Text(
                    if (state.isRegistering) "Crear cuenta" else "Iniciar sesión",
                    style = MaterialTheme.typography.titleMedium, color = MaestroColors.Muted
                )

                if (state.isRegistering) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth())

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (state.isRegistering) vm.register(email, password, name, onSuccess)
                        else vm.login(email, password, onSuccess)
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaestroColors.White)
                    else Text(if (state.isRegistering) "Registrarse" else "Entrar")
                }

                TextButton(onClick = { vm.toggleMode() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        if (state.isRegistering) "¿Ya tenés cuenta? Iniciá sesión" else "¿No tenés cuenta? Registrate",
                        color = MaestroColors.Gold
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/maestro/app/ui/auth/
git commit -m "feat: add login/register screen with AuthViewModel"
```

---

### Task 14: Dashboard screen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/dashboard/DashboardViewModel.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/dashboard/DashboardScreen.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/components/AppScaffold.kt`

- [ ] **Step 1: Create `AppScaffold.kt`** (shared nav shell used by all screens)

```kotlin
package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors

data class NavTab(val screen: Screen, val label: String, val icon: String)

val navTabs = listOf(
    NavTab(Screen.Dashboard, "Inicio", "♪"),
    NavTab(Screen.Students, "Estudiantes", "♬"),
    NavTab(Screen.Classes, "Clases", "📋"),
    NavTab(Screen.Finances, "Finanzas", "₿"),
    NavTab(Screen.Tasks, "Tareas", "✓"),
    NavTab(Screen.Events, "Agenda", "📅"),
    NavTab(Screen.Metronome, "Metrónomo", "𝅗𝅥"),
    NavTab(Screen.Guide, "Guía", "📖")
)

@Composable
fun AppScaffold(currentRoute: String, navController: NavHostController, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaestroColors.Cream)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(MaestroColors.Espresso).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MAESTRO", color = MaestroColors.Gold, fontSize = 18.sp, letterSpacing = 2.sp)
        }
        // Nav tabs
        LazyRow(modifier = Modifier.fillMaxWidth().background(MaestroColors.White)) {
            items(navTabs) { tab ->
                val selected = currentRoute == tab.screen.route
                Column(
                    modifier = Modifier
                        .clickable { navController.navigate(tab.screen.route) { launchSingleTop = true } }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.icon, fontSize = 16.sp)
                    Text(tab.label, fontSize = 12.sp,
                        color = if (selected) MaestroColors.Terra else MaestroColors.Muted)
                    if (selected) Box(modifier = Modifier.height(3.dp).width(40.dp).background(MaestroColors.Terra))
                }
            }
        }
        // Content
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
```

- [ ] **Step 2: Create `DashboardViewModel.kt`**

```kotlin
package com.maestro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.network.ApiClient
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Event
import com.maestro.shared.model.Student
import com.maestro.shared.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val students: List<Student> = emptyList(),
    val recentClasses: List<ClassEntry> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val totalIncome: Long = 0L,
    val pendingIncome: Long = 0L,
    val isLoading: Boolean = true,
    val currentPhraseIndex: Int = 0
)

class DashboardViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val students = apiClient.getStudents()
                val currentMonth = getCurrentMonth()
                val classes = apiClient.getClasses(currentMonth)
                val events = apiClient.getEvents()
                val tasks = apiClient.getTasks()

                val totalIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && it.paid }) s.monthlyFee else 0L
                }
                val pendingIncome = students.sumOf { s ->
                    if (classes.any { it.studentId == s.id && !it.paid }) s.monthlyFee else 0L
                }

                _state.value = DashboardState(
                    students = students,
                    recentClasses = classes.takeLast(4).reversed(),
                    upcomingEvents = events.sortedBy { it.date }.take(4),
                    pendingTasks = tasks.filter { !it.done }.take(3),
                    totalIncome = totalIncome,
                    pendingIncome = pendingIncome,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun nextPhrase() { _state.value = _state.value.copy(currentPhraseIndex = (_state.value.currentPhraseIndex + 1) % PHRASES.size) }
}

expect fun getCurrentMonth(): String

data class Phrase(val text: String, val author: String)

val PHRASES = listOf(
    Phrase("Enseñar música es sembrar semillas que florecen por generaciones.", "Naty Ramírez"),
    Phrase("Cada clase es una obra maestra que nunca volverá a repetirse.", "Maestro anónimo"),
    Phrase("La paciencia del maestro es el compás que guía el alma del estudiante.", "Heinrich Neuhaus"),
    Phrase("No enseñas notas. Enseñas a escuchar el universo.", "Nadia Boulanger"),
    Phrase("Tu energía de hoy es el recuerdo musical de alguien mañana.", "Naty Ramírez"),
    Phrase("Un buen maestro abre puertas que el estudiante ni sabía que existían.", "Leon Fleisher"),
    Phrase("Cuídate. Un maestro agotado no puede encender llamas en otros.", "Reflexión docente"),
    Phrase("La excelencia no es perfección. Es presencia total en cada compás.", "Naty Ramírez")
)
```

- [ ] **Step 3: Create `getCurrentMonth` expect/actual**

In `composeApp/src/wasmJsMain/kotlin/com/maestro/app/ui/dashboard/PlatformDate.wasmJs.kt`:

```kotlin
package com.maestro.app.ui.dashboard

actual fun getCurrentMonth(): String {
    val now = js("new Date()")
    val year = (now.getFullYear() as Int).toString()
    val month = ((now.getMonth() as Int) + 1).toString().padStart(2, '0')
    return "$year-$month"
}
```

In `composeApp/src/androidMain/kotlin/com/maestro/app/ui/dashboard/PlatformDate.android.kt`:

```kotlin
package com.maestro.app.ui.dashboard

import java.time.YearMonth

actual fun getCurrentMonth(): String = YearMonth.now().toString()
```

- [ ] **Step 4: Create `DashboardScreen.kt`**

```kotlin
package com.maestro.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.app.navigation.Screen

@Composable
fun DashboardScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { DashboardViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()
    val phrase = PHRASES[state.currentPhraseIndex]

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(8000)
            vm.nextPhrase()
        }
    }

    AppScaffold(Screen.Dashboard.route, navController) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = MaestroColors.Gold)
            }
            return@AppScaffold
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Frase motivacional
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.Espresso)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Frase del día", color = MaestroColors.Gold, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("\"${phrase.text}\"", color = MaestroColors.White, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("— ${phrase.author}", color = MaestroColors.Gold, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Estudiantes", state.students.size.toString(), MaestroColors.Forest, MaestroColors.SoftGreen, Modifier.weight(1f))
                StatCard("Cobrado", formatCOP(state.totalIncome), MaestroColors.Forest, MaestroColors.SoftGreen, Modifier.weight(1f))
                StatCard("Por cobrar", formatCOP(state.pendingIncome), Color(0xFFC0392B), Color(0xFFFDECEA), Modifier.weight(1f))
            }

            // Tareas pendientes
            if (state.pendingTasks.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaestroColors.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tareas Pendientes", style = MaterialTheme.typography.titleSmall, color = MaestroColors.Espresso)
                            TextButton(onClick = { navController.navigate(Screen.Tasks.route) }) {
                                Text("Ver todas →", color = MaestroColors.Terra)
                            }
                        }
                        state.pendingTasks.forEach { task ->
                            Text("• ${task.text}", color = MaestroColors.Espresso, style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, textColor: Color, bgColor: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, color = textColor, style = MaterialTheme.typography.titleMedium)
            Text(label, color = MaestroColors.Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatCOP(amount: Long): String = "$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"
```

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/
git commit -m "feat: add Dashboard screen with stats and motivational phrases"
```

---

### Task 15: Students, Classes, Finances, Tasks, Events screens

> **Note:** Each screen follows the same pattern as Dashboard. Use the JSX prototype (`documentation/maestro-app.jsx`) as the exact visual reference for each screen's layout and interactions.

**Files per screen:**
- `ui/students/StudentsViewModel.kt` + `StudentsScreen.kt` + `StudentDetailScreen.kt`
- `ui/classes/ClassesViewModel.kt` + `ClassesScreen.kt`
- `ui/finances/FinancesViewModel.kt` + `FinancesScreen.kt`
- `ui/tasks/TasksViewModel.kt` + `TasksScreen.kt`
- `ui/events/EventsViewModel.kt` + `EventsScreen.kt`

For each screen, follow this pattern:

- [ ] **Step 1: Create ViewModel with StateFlow**

Pattern (example for Students):

```kotlin
package com.maestro.app.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.network.ApiClient
import com.maestro.shared.dto.StudentRequest
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudentsState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val showAddForm: Boolean = false
)

class StudentsViewModel(private val apiClient: ApiClient) : ViewModel() {
    private val _state = MutableStateFlow(StudentsState())
    val state: StateFlow<StudentsState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                _state.value = _state.value.copy(students = apiClient.getStudents(), isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun addStudent(name: String, age: Int, level: Level, phone: String, email: String,
                   monthlyFee: Long, notes: String, color: String) {
        viewModelScope.launch {
            val req = StudentRequest(name, age, level, phone, email, monthlyFee, notes, color)
            val student = apiClient.createStudent(req)
            _state.value = _state.value.copy(
                students = _state.value.students + student,
                showAddForm = false
            )
        }
    }

    fun toggleAddForm() { _state.value = _state.value.copy(showAddForm = !_state.value.showAddForm) }
}
```

- [ ] **Step 2: Create Screen composable** — reference the JSX for each section's layout. Use `AppScaffold` as the wrapper, `LazyColumn` for lists, `Card` for items.

- [ ] **Step 3: Commit after each screen**

```bash
git add composeApp/src/commonMain/kotlin/com/maestro/app/ui/<screen>/
git commit -m "feat: add <screen> screen"
```

Repeat for: Students, StudentDetail, Classes, Finances, Tasks, Events.

---

### Task 16: Metronome screen with audio

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/audio/AudioPlayer.kt` (expect)
- Create: `composeApp/src/wasmJsMain/kotlin/com/maestro/app/audio/AudioPlayer.wasmJs.kt`
- Create: `composeApp/src/androidMain/kotlin/com/maestro/app/audio/AudioPlayer.android.kt`
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/metronome/MetronomeScreen.kt`

- [ ] **Step 1: Create `AudioPlayer.kt` expect**

```kotlin
package com.maestro.app.audio

expect class AudioPlayer() {
    fun playClick(isAccent: Boolean)
}
```

- [ ] **Step 2: Create `AudioPlayer.wasmJs.kt`**

```kotlin
package com.maestro.app.audio

actual class AudioPlayer actual constructor() {
    actual fun playClick(isAccent: Boolean) {
        val freq = if (isAccent) 1200 else 800
        val gain = if (isAccent) 0.4 else 0.25
        js("""
            try {
                var ctx = new (window.AudioContext || window.webkitAudioContext)();
                var osc = ctx.createOscillator();
                var g = ctx.createGain();
                osc.connect(g); g.connect(ctx.destination);
                osc.frequency.value = freq;
                g.gain.setValueAtTime(gain, ctx.currentTime);
                g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);
                osc.start(ctx.currentTime); osc.stop(ctx.currentTime + 0.08);
            } catch(e) {}
        """)
    }
}
```

- [ ] **Step 3: Create `AudioPlayer.android.kt`**

```kotlin
package com.maestro.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

actual class AudioPlayer actual constructor() {
    actual fun playClick(isAccent: Boolean) {
        val sampleRate = 44100
        val durationMs = 80
        val samples = sampleRate * durationMs / 1000
        val freq = if (isAccent) 1200.0 else 800.0
        val buffer = ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / samples)
            (sin(2 * PI * freq * t) * envelope * Short.MAX_VALUE * if (isAccent) 0.4 else 0.25).toInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        track.play()
    }
}
```

- [ ] **Step 4: Create `MetronomeScreen.kt`**

```kotlin
package com.maestro.app.ui.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.audio.AudioPlayer
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import kotlinx.coroutines.delay

@Composable
fun MetronomeScreen(navController: NavHostController) {
    var bpm by remember { mutableStateOf(80f) }
    var isPlaying by remember { mutableStateOf(false) }
    var beat by remember { mutableStateOf(0) }
    var timeSignature by remember { mutableStateOf(4) }
    var accent by remember { mutableStateOf(true) }
    val audioPlayer = remember { AudioPlayer() }

    LaunchedEffect(isPlaying, bpm, timeSignature, accent) {
        if (!isPlaying) { beat = 0; return@LaunchedEffect }
        while (isPlaying) {
            audioPlayer.playClick(accent && beat == 0)
            delay((60_000L / bpm.toLong()))
            beat = (beat + 1) % timeSignature
        }
    }

    AppScaffold(Screen.Metronome.route, navController) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)) {

            Text("Metrónomo", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)

            Card(modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.Espresso),
                shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Beat visualizer
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(timeSignature) { i ->
                            Box(modifier = Modifier.size(24.dp, if (i == beat && isPlaying) 64.dp else 40.dp)
                                .background(
                                    if (i == beat && isPlaying) (if (accent && i == 0) MaestroColors.Gold else MaestroColors.Terra) else MaestroColors.Muted.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                ))
                        }
                    }

                    Text("${bpm.toInt()}", color = MaestroColors.White, fontSize = 72.sp)
                    Text("BPM", color = MaestroColors.Muted, style = MaterialTheme.typography.bodyMedium)

                    Slider(value = bpm, onValueChange = { bpm = it }, valueRange = 20f..240f,
                        colors = SliderDefaults.colors(thumbColor = MaestroColors.Gold, activeTrackColor = MaestroColors.Gold))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { bpm = (bpm - 5).coerceAtLeast(20f) }) {
                            Text("−", color = MaestroColors.White, fontSize = 20.sp)
                        }
                        Button(onClick = { isPlaying = !isPlaying },
                            shape = CircleShape, modifier = Modifier.size(80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) MaestroColors.Terra else MaestroColors.Gold)) {
                            Text(if (isPlaying) "⏹" else "▶", color = MaestroColors.Espresso, fontSize = 20.sp)
                        }
                        IconButton(onClick = { bpm = (bpm + 5).coerceAtMost(240f) }) {
                            Text("+", color = MaestroColors.White, fontSize = 20.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4, 6).forEach { sig ->
                            Button(onClick = { timeSignature = sig; beat = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = if (timeSignature == sig) MaestroColors.Gold else MaestroColors.Muted.copy(alpha = 0.3f))) {
                                Text("$sig/4", color = MaestroColors.Espresso)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/
git commit -m "feat: add Metronome screen with platform audio (Web Audio API + Android AudioTrack)"
```

---

### Task 17: Guide screen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/maestro/app/ui/guide/GuideScreen.kt`

- [ ] **Step 1: Create `GuideScreen.kt`**

```kotlin
package com.maestro.app.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold

data class LevelGuide(val name: String, val color: Color, val duration: String,
                      val objectives: List<String>, val repertoire: List<String>)

val LEVEL_GUIDE = listOf(
    LevelGuide("Inicial", Color(0xFF7BC67E), "6-12 meses",
        listOf("Postura y posición al piano", "Lectura de notas básicas (Do-Sol)", "Ritmo con valores básicos", "Canciones simples con 5 dedos"),
        listOf("Pequeñas piezas de Bartók", "Canciones folclóricas adaptadas", "Método Suzuki Vol.1")),
    LevelGuide("Elemental", Color(0xFF64B5F6), "1-2 años",
        listOf("Escalas mayores (Do, Sol, Re)", "Lectura en clave de Fa", "Articulaciones básicas", "Piezas ABRSM 1-2"),
        listOf("Bach Anna Magdalena", "Clementi Sonatinas Op.36", "Bartók Mikrokosmos Vol.1-2")),
    LevelGuide("Intermedio", Color(0xFFFFB74D), "2-3 años",
        listOf("Escalas y arpegios completos", "Fraseo musical", "Pedal de resonancia", "Análisis formal básico"),
        listOf("Mozart Sonatas sencillas", "Beethoven Sonatinas", "Chopin Preludios fáciles")),
    LevelGuide("Avanzado", Color(0xFFF06292), "3+ años",
        listOf("Técnica avanzada", "Repertorio de concierto", "Interpretación estilística", "Preparación para audiciones"),
        listOf("Bach Invenciones / Partitas", "Beethoven Sonatas Op.13, 27", "Chopin Baladas, Nocturnos"))
)

@Composable
fun GuideScreen(navController: NavHostController) {
    AppScaffold(Screen.Guide.route, navController) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Guía de Niveles", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)
                Text("Referencia pedagógica por nivel", style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Muted)
                Spacer(Modifier.height(8.dp))
            }
            items(LEVEL_GUIDE) { level ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaestroColors.White)) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().background(level.color, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).padding(20.dp)) {
                            Column {
                                Text(level.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                                Text("Duración: ${level.duration}", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Objetivos", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            level.objectives.forEach { Text("→ $it", color = MaestroColors.Espresso, style = MaterialTheme.typography.bodySmall) }
                            Spacer(Modifier.height(4.dp))
                            Text("Repertorio sugerido", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            level.repertoire.forEach { Text("♩ $it", color = MaestroColors.Espresso, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Final compile check**

```bash
./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileKotlinAndroid :server:test
```

Expected: All compile, all server tests pass.

- [ ] **Step 3: Run the web app**

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Open `http://localhost:8080` for backend. Open browser URL printed by webpack dev server for the frontend (usually `http://localhost:8081`).

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "feat: complete Maestro CMP — Guide screen and full app wiring"
```

---

## Self-review notes

- All types defined in Task 3-4 (`Student`, `ClassEntry`, `Task`, `Event`, enums, DTOs) are referenced consistently in Tasks 8-17 using the same property names.
- `getCurrentMonth()` expect/actual covers both Wasm and Android.
- `TokenStorage` expect/actual covers both Wasm (`localStorage`) and Android (`DataStore`).
- `AudioPlayer` expect/actual covers both Wasm (Web Audio API via JS interop) and Android (AudioTrack).
- `ClassEntry` ownership is validated server-side via `studentId → student.userId` join (Task 9).
- `BASE_URL` in `AppNavigation.kt` is `http://localhost:8080` for local dev. Replace with Railway URL for production.
- Task 15 (5 feature screens) intentionally uses the JSX prototype as visual spec rather than repeating 500+ lines of near-identical Compose code. Each screen follows the same ViewModel + StateFlow + AppScaffold pattern established in Task 14.
