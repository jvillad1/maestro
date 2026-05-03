# Maestro — Diseño de arquitectura Compose Multiplatform

**Fecha:** 2026-05-02  
**Estado:** Aprobado

## Contexto

Maestro es una app para profesores de música que permite gestionar estudiantes, registrar clases, controlar finanzas, tareas y agenda. Existe un prototipo funcional en React JSX (`documentation/maestro-app.jsx`) con 8 módulos implementados que sirve como referencia de UI y lógica de negocio.

## Objetivo

Convertir el prototipo JSX en una aplicación de **Compose Multiplatform** (Web + Android) con backend **Ktor** en Kotlin. MVP: un profe por cuenta, con login propio. Escalable a academia multi-profe en iteración siguiente.

---

## Arquitectura general

**Monorepo** con tres módulos Gradle coordinados:

```
maestro/
├── composeApp/          # Compose Multiplatform — Web (Wasm) + Android
│   ├── src/commonMain/  # UI compartida + ViewModels + ApiClient
│   ├── src/androidMain/ # Entry point Android
│   └── src/wasmJsMain/  # Entry point Web (Kotlin/Wasm)
├── server/              # Ktor backend
│   └── src/main/kotlin/
│       ├── plugins/     # Auth, Routing, Serialization, CORS, Database
│       ├── routes/      # Endpoints REST por dominio
│       ├── models/      # Entidades Exposed (tablas DB)
│       └── services/    # Lógica de negocio
├── shared/              # KMP — modelos y DTOs compartidos
│   └── src/commonMain/  # Data classes, enums, request/response DTOs
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

**Beneficio clave del monorepo:** los modelos de datos (`Student`, `ClassEntry`, etc.) se definen una sola vez en `shared` y los usan tanto el frontend como el backend, eliminando duplicación y desincronización.

---

## Stack tecnológico

### Frontend — `composeApp`

| Tecnología | Versión | Rol |
|---|---|---|
| Kotlin | 2.1.x | Lenguaje |
| Compose Multiplatform | 1.8.x | UI compartida Web + Android |
| Kotlin/Wasm | — | Target Web |
| Ktor Client | 3.x | HTTP hacia el backend |
| kotlinx.serialization | — | Serialización JSON |
| Lifecycle ViewModel (CMP) | — | Estado por pantalla |
| Navigation 3 (CMP) | — | Navegación |

**Por qué Kotlin/Wasm:** es el target oficial de Compose para Web desde CMP 1.7+. Mejor rendimiento que Kotlin/JS y soporte nativo de Compose. Kotlin/JS para UI con Compose está deprecado.

### Backend — `server`

| Tecnología | Versión | Rol |
|---|---|---|
| Ktor | 3.x | Framework HTTP |
| Exposed ORM | — | Acceso a base de datos |
| H2 | — | DB embebida para desarrollo local |
| PostgreSQL | — | DB en producción (Railway) |
| JWT | — | Autenticación stateless |
| BCrypt | — | Hash de contraseñas |
| kotlinx.serialization | — | Serialización JSON |

### Shared — `shared`

Kotlin Multiplatform puro. Contiene data classes, enums y DTOs de request/response. Sin dependencias de plataforma.

### Infraestructura

- **Local:** H2 embebido, sin instalación
- **Producción:** Railway (PostgreSQL gestionado + deploy desde Git + HTTPS automático)

---

## Modelos de datos (`shared/commonMain`)

### Entidades del dominio

```kotlin
data class User(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: String
)

data class Student(
    val id: Long,
    val userId: Long,       // FK → User
    val name: String,
    val age: Int,
    val level: Level,
    val phone: String,
    val email: String,
    val monthlyFee: Long,   // COP
    val notes: String,
    val joinDate: String,   // ISO: "2026-05-02"
    val color: String       // Hex: "#C9A84C"
)

data class ClassEntry(
    val id: Long,
    val studentId: Long,    // FK → Student
    val date: String,       // ISO: "2026-05-02"
    val topic: String,
    val paid: Boolean
)

data class Task(
    val id: Long,
    val userId: Long,       // FK → User
    val text: String,
    val priority: Priority,
    val done: Boolean
)

data class Event(
    val id: Long,
    val userId: Long,       // FK → User
    val title: String,
    val date: String,       // ISO: "2026-06-15"
    val type: EventType,
    val description: String
)
```

### Enumeraciones

```kotlin
enum class Level { INICIAL, ELEMENTAL, INTERMEDIO, AVANZADO }
enum class Priority { ALTA, MEDIA, BAJA }
enum class EventType { RECITAL, MASTERCLASS, EVALUACION, OTRO }
```

**Nota:** `passwordHash` existe solo en el modelo de base de datos del servidor (`server/models/`), nunca en `shared` ni viaja al cliente.

**Nota sobre `userId`:** cada entidad lleva `userId` como FK desde el inicio. Esto no agrega complejidad ahora pero hace trivial la migración a academia multi-profe: agregar tabla `organizations` y FK, sin reescribir el modelo.

---

## API REST (`server`)

Todos los endpoints bajo `/api/`. Todos excepto auth requieren `Authorization: Bearer <token>`. Cada endpoint valida que el recurso pertenezca al `userId` del JWT.

`ClassEntry` no lleva `userId` directo — el servidor verifica ownership vía `classEntry.studentId → student.userId == jwtUserId`.

### Autenticación

```
POST /api/auth/register   → { token: String, user: User }
POST /api/auth/login      → { token: String, user: User }
```

### Estudiantes

```
GET    /api/students          → List<Student>
POST   /api/students          → Student
GET    /api/students/{id}     → Student
PUT    /api/students/{id}     → Student
DELETE /api/students/{id}     → 204
```

### Clases

```
GET    /api/classes               → List<ClassEntry>
GET    /api/classes?month=2026-05 → List<ClassEntry>  (filtrado por mes)
POST   /api/classes               → ClassEntry
PUT    /api/classes/{id}          → ClassEntry
DELETE /api/classes/{id}          → 204
```

### Tareas

```
GET    /api/tasks       → List<Task>
POST   /api/tasks       → Task
PUT    /api/tasks/{id}  → Task
DELETE /api/tasks/{id}  → 204
```

### Eventos

```
GET    /api/events       → List<Event>
POST   /api/events       → Event
PUT    /api/events/{id}  → Event
DELETE /api/events/{id}  → 204
```

---

## Arquitectura frontend (`composeApp/commonMain`)

### Pantallas

| Pantalla | ViewModel | Notas |
|---|---|---|
| `LoginScreen` | `AuthViewModel` | Formulario email/contraseña |
| `RegisterScreen` | `AuthViewModel` | Compartido con Login |
| `DashboardScreen` | `DashboardViewModel` | Stats + frase motivacional rotativa |
| `StudentsScreen` | `StudentsViewModel` | Lista de tarjetas con color |
| `StudentDetailScreen` | `StudentsViewModel` | Detalle + historial de clases |
| `ClassesScreen` | `ClassesViewModel` | Registro con toggle de pago |
| `FinancesScreen` | `FinancesViewModel` | Resumen mensual por estudiante |
| `TasksScreen` | `TasksViewModel` | Lista por prioridad |
| `EventsScreen` | `EventsViewModel` | Tarjetas visuales por tipo |
| `MetronomeScreen` | _(sin VM)_ | Estado local puro. Audio via `expect/actual`: Web Audio API (Wasm/JS interop) en Web, `AudioTrack` en Android |
| `GuideScreen` | _(sin VM)_ | Datos estáticos de niveles |

### Estructura de directorios (`commonMain`)

```
ui/
├── auth/          AuthScreen.kt + AuthViewModel.kt
├── dashboard/     DashboardScreen.kt + DashboardViewModel.kt
├── students/      StudentsScreen.kt + StudentDetailScreen.kt + StudentsViewModel.kt
├── classes/       ClassesScreen.kt + ClassesViewModel.kt
├── finances/      FinancesScreen.kt + FinancesViewModel.kt
├── tasks/         TasksScreen.kt + TasksViewModel.kt
├── events/        EventsScreen.kt + EventsViewModel.kt
├── metronome/     MetronomeScreen.kt
├── guide/         GuideScreen.kt
└── components/    Componentes reutilizables (tarjetas, modales, etc.)
network/
└── ApiClient.kt   Ktor Client configurado con baseUrl + JWT header
navigation/
└── AppNavigation.kt   Navigation 3, incluye guard de auth
auth/
└── TokenStorage.kt    expect/actual: localStorage (Web) / DataStore (Android)
theme/
└── MaestroTheme.kt    Paleta: cream #F5F0E8, espresso #2C1810, gold #C9A84C, etc.
```

### Flujo de datos

```
Screen (Composable)
  └── observa StateFlow<UiState> del ViewModel
       └── ViewModel llama ApiClient
            └── ApiClient hace HTTP con Ktor Client
                 └── Ktor backend consulta DB con Exposed
                      └── Respuesta sube la cadena de vuelta
```

### Autenticación en el cliente

1. Al arrancar: `TokenStorage.getToken()` — si es válido → navegar a Dashboard, si no → Login
2. Login exitoso: guardar JWT en `TokenStorage`, navegar a Dashboard
3. `ApiClient` inyecta `Authorization: Bearer <token>` en cada request
4. 401 del servidor → limpiar token → navegar a Login

### Persistencia del token

- **Web:** `localStorage` via `expect/actual`
- **Android:** Jetpack `DataStore` via `expect/actual`

---

## Paleta de colores (del prototipo JSX)

```kotlin
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

---

## Fuera de alcance del MVP

- Academia multi-profe (iteración 2)
- Notificaciones push
- Exportación a PDF/Excel
- Modo offline con sync
- iOS (iteración 2 — requiere Xcode + Apple Developer)
