package com.maestro.shared.dto

import com.maestro.shared.model.*
import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val email: String, val password: String, val name: String)
@Serializable data class AuthResponse(val token: String, val user: User)

@Serializable data class StudentRequest(
    val name: String,
    val age: Int,
    val level: Level,
    val phone: String,
    val email: String,
    val monthlyFee: Long,
    val notes: String,
    val color: String,
    val id: String? = null
)

@Serializable data class ClassEntryRequest(
    val studentId: String,
    val date: String,
    val topic: String,
    val paid: Boolean,
    val attendance: Attendance = Attendance.PENDIENTE,
    val notes: String = "",
    val id: String? = null
)

@Serializable data class TaskRequest(
    val text: String,
    val priority: Priority,
    val done: Boolean,
    val id: String? = null
)

@Serializable data class EventRequest(
    val title: String,
    val date: String,
    val type: EventType,
    val description: String,
    val id: String? = null
)
