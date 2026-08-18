package com.maestro.shared.util

/**
 * Corre un mes en formato YYYY-MM [delta] meses (negativo = hacia atrás),
 * cruzando años correctamente. Devuelve el original si el formato no es válido.
 */
fun shiftMonth(month: String, delta: Int): String {
    val parts = month.split("-")
    if (parts.size != 2) return month
    val year = parts[0].toIntOrNull() ?: return month
    val m = parts[1].toIntOrNull() ?: return month
    if (m !in 1..12) return month
    val zeroBased = (year * 12 + (m - 1)) + delta
    if (zeroBased < 0) return month
    val newYear = zeroBased / 12
    val newMonth = zeroBased % 12 + 1
    return "$newYear-${newMonth.toString().padStart(2, '0')}"
}
