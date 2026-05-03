package com.maestro.shared.model

import kotlinx.serialization.Serializable

@Serializable enum class Level { INICIAL, ELEMENTAL, INTERMEDIO, AVANZADO }
@Serializable enum class Priority { ALTA, MEDIA, BAJA }
@Serializable enum class EventType { RECITAL, MASTERCLASS, EVALUACION, OTRO }

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
