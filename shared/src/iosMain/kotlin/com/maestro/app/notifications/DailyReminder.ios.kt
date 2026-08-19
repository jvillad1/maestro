package com.maestro.app.notifications

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDateComponents
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

private const val KEY_ENABLED = "reminder_enabled"
private const val KEY_HOUR = "reminder_hour"
private const val KEY_MINUTE = "reminder_minute"
private const val REQUEST_ID = "maestro_daily_reminder"

actual class DailyReminder actual constructor() {
    private val defaults get() = NSUserDefaults.standardUserDefaults

    actual fun isSupported(): Boolean = true
    actual fun isEnabled(): Boolean = defaults.boolForKey(KEY_ENABLED)
    actual fun hour(): Int = if (defaults.objectForKey(KEY_HOUR) == null) 8 else defaults.integerForKey(KEY_HOUR).toInt()
    actual fun minute(): Int = defaults.integerForKey(KEY_MINUTE).toInt()

    actual suspend fun enable(hour: Int, minute: Int): Boolean {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val granted = suspendCancellableCoroutine { cont ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound
            ) { ok, _ -> cont.resume(ok) }
        }
        if (!granted) return false

        defaults.setBool(true, KEY_ENABLED)
        defaults.setInteger(hour.toLong(), KEY_HOUR)
        defaults.setInteger(minute.toLong(), KEY_MINUTE)

        center.removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
        val content = UNMutableNotificationContent().apply {
            setTitle("Clases de hoy")
            // Fijo a propósito: iOS no puede contar las clases con la app cerrada
            setBody("Revisa tu agenda del día en Maestro.")
        }
        val components = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(
                REQUEST_ID, content,
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, true)
            )
        ) { }
        return true
    }

    actual fun disable() {
        defaults.setBool(false, KEY_ENABLED)
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
    }
}
