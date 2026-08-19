package com.maestro.app.notifications

/**
 * La web no puede avisar con la pestaña cerrada sin un service worker y un
 * servidor de push, así que acá el recordatorio simplemente no se ofrece.
 */
actual class DailyReminder actual constructor() {
    actual fun isSupported(): Boolean = false
    actual fun isEnabled(): Boolean = false
    actual fun hour(): Int = 8
    actual fun minute(): Int = 0
    actual suspend fun enable(hour: Int, minute: Int): Boolean = false
    actual fun disable() {}
}
