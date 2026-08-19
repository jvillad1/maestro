package com.maestro.app.notifications

/**
 * Recordatorio diario de las clases del día.
 *
 * Cada plataforma lo resuelve a su manera: Android programa una alarma que al
 * dispararse consulta la base local y dice cuántas clases hay hoy; iOS deja
 * programada una notificación de calendario repetitiva con un texto fijo,
 * porque no puede calcular nada mientras la app está cerrada.
 */
expect class DailyReminder() {
    /** false donde no se puede avisar con la app cerrada (web). */
    fun isSupported(): Boolean
    fun isEnabled(): Boolean
    fun hour(): Int
    fun minute(): Int
    /** Pide permiso si hace falta y programa. false = el permiso fue negado. */
    suspend fun enable(hour: Int, minute: Int): Boolean
    fun disable()
}
