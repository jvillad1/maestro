package com.maestro.app.dev

import com.maestro.shared.model.*

const val USE_MOCK = false  // flip to true for demo mode with fake data

val mockStudents = listOf(
    Student("s1", 1L, "Sofía Martínez",   17, Level.AVANZADO,   "+57 310 000 0001", "sofia@mail.com",   180_000L, "Muy talentosa", "2023-03-01", "#B85C38"),
    Student("s2", 1L, "Carlos López",     14, Level.INTERMEDIO,  "+57 310 000 0002", "carlos@mail.com",  150_000L, "Buen ritmo",    "2023-08-15", "#C9A84C"),
    Student("s3", 1L, "Ana García",       10, Level.ELEMENTAL,   "+57 310 000 0003", "ana@mail.com",     120_000L, "Principiante",  "2024-02-10", "#2D5016"),
    Student("s4", 1L, "Miguel Torres",    22, Level.AVANZADO,    "+57 310 000 0004", "miguel@mail.com",  200_000L, "Universitario", "2022-11-20", "#2C1810"),
    Student("s5", 1L, "Laura Sánchez",    13, Level.INTERMEDIO,  "+57 310 000 0005", "laura@mail.com",   150_000L, "Dedicada",      "2024-01-05", "#8B4513"),
)

val mockClasses = listOf(
    ClassEntry("c1", "s1", "2026-05-02", "Escalas mayores y menores",    paid = true),
    ClassEntry("c2", "s2", "2026-05-02", "Arpegios en Sol mayor",         paid = true),
    ClassEntry("c3", "s3", "2026-05-03", "Posición de manos",             paid = false),
    ClassEntry("c4", "s4", "2026-05-03", "Bach: Invención No. 1",         paid = true),
    ClassEntry("c5", "s1", "2026-05-05", "Chopin: Nocturno Op. 9",        paid = false),
    ClassEntry("c6", "s5", "2026-05-05", "Lectura a primera vista",        paid = true),
    ClassEntry("c7", "s2", "2026-05-07", "Beethoven: Para Elisa",         paid = false),
    ClassEntry("c8", "s3", "2026-05-08", "Teoría: figuras rítmicas",      paid = false),
)

val mockTasks = listOf(
    Task("t1", 1L, "Preparar repertorio para recital de junio",   Priority.ALTA,  done = false),
    Task("t2", 1L, "Enviar facturas del mes de abril",            Priority.ALTA,  done = false),
    Task("t3", 1L, "Revisar progreso de Ana García",              Priority.MEDIA, done = false),
    Task("t4", 1L, "Imprimir partituras Chopin para Sofía",       Priority.MEDIA, done = true),
    Task("t5", 1L, "Organizar carpetas de partituras",            Priority.BAJA,  done = false),
)

val mockEvents = listOf(
    Event("e1", 1L, "Recital de Fin de Semestre",  "2026-06-14", EventType.RECITAL,     "Auditorio principal del conservatorio"),
    Event("e2", 1L, "Masterclass Piano Romántico", "2026-05-20", EventType.MASTERCLASS, "Invitado: Prof. Diego Reyes"),
    Event("e3", 1L, "Evaluación Intermedia",        "2026-05-28", EventType.EVALUACION,  "Evaluación de todos los estudiantes"),
    Event("e4", 1L, "Clase de Teoría Musical",      "2026-05-12", EventType.OTRO,        "Grupo de teoría del conservatorio"),
)
