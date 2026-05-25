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

    @Test
    fun `ClassEntry serializes with paid flag`() {
        val entry = ClassEntry(1L, 1L, "2026-05-02", "Escalas mayores", true)
        val json = Json.encodeToString(entry)
        val decoded = Json.decodeFromString<ClassEntry>(json)
        assertEquals(true, decoded.paid)
        assertEquals("Escalas mayores", decoded.topic)
    }

    @Test
    fun `Event type enum round-trips`() {
        val event = Event(1L, 1L, "Recital de fin de año", "2026-12-15", EventType.RECITAL, "Auditorio principal")
        val json = Json.encodeToString(event)
        assertEquals(EventType.RECITAL, Json.decodeFromString<Event>(json).type)
    }
}
