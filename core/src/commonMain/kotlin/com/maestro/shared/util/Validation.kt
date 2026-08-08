package com.maestro.shared.util

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val ISO_DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

fun isValidEmail(value: String): Boolean = EMAIL_REGEX.matches(value.trim())

/** Valida formato YYYY-MM-DD y que sea una fecha real (meses, días, bisiestos). */
fun isValidIsoDate(value: String): Boolean {
    if (!ISO_DATE_REGEX.matches(value)) return false
    val (year, month, day) = value.split("-").map { it.toInt() }
    if (year < 1900 || year > 2100) return false
    if (month !in 1..12) return false
    val leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if (leap) 29 else 28
    }
    return day in 1..daysInMonth
}

/** Edad plausible de un estudiante. */
fun isValidAge(value: String): Boolean = value.toIntOrNull()?.let { it in 1..120 } ?: false

/** Monto en COP: entero positivo. */
fun isValidFee(value: String): Boolean = value.toLongOrNull()?.let { it > 0 } ?: false
