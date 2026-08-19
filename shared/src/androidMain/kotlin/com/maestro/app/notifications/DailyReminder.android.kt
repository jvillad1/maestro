package com.maestro.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.maestro.app.AppContextHolder
import java.util.Calendar
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Puente para pedir POST_NOTIFICATIONS: MainActivity registra acá su launcher,
 * porque el permiso solo se puede pedir desde una Activity.
 */
object NotificationPermission {
    /** Por defecto concedido: en Android 12 y anteriores no se pide nada. */
    var request: ((granted: Boolean) -> Unit) -> Unit = { callback -> callback(true) }
}

private const val PREFS = "maestro_reminder"
private const val KEY_ENABLED = "enabled"
private const val KEY_HOUR = "hour"
private const val KEY_MINUTE = "minute"

actual class DailyReminder actual constructor() {
    private val context: Context get() = AppContextHolder.context
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun isSupported(): Boolean = true
    actual fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    actual fun hour(): Int = prefs.getInt(KEY_HOUR, 8)
    actual fun minute(): Int = prefs.getInt(KEY_MINUTE, 0)

    actual suspend fun enable(hour: Int, minute: Int): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val granted = suspendCancellableCoroutine { cont ->
                NotificationPermission.request { cont.resume(it) }
            }
            if (!granted) return false
        }
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
        scheduleReminder(context, hour, minute)
        return true
    }

    actual fun disable() {
        prefs.edit().putBoolean(KEY_ENABLED, false).apply()
        alarmManager(context).cancel(reminderIntent(context))
    }
}

internal fun reminderPrefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
internal fun reminderEnabled(context: Context) = reminderPrefs(context).getBoolean(KEY_ENABLED, false)
internal fun reminderHour(context: Context) = reminderPrefs(context).getInt(KEY_HOUR, 8)
internal fun reminderMinute(context: Context) = reminderPrefs(context).getInt(KEY_MINUTE, 0)

private fun alarmManager(context: Context) =
    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

private fun reminderIntent(context: Context): PendingIntent =
    PendingIntent.getBroadcast(
        context, 0,
        Intent(context, ReminderReceiver::class.java).setAction(ACTION_DAILY_REMINDER),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

internal fun scheduleReminder(context: Context, hour: Int, minute: Int) {
    val firstFire = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis
    // setWindow en vez de setInexactRepeating: la repetitiva inexacta le deja a
    // Android una ventana de 18 horas (lo confirmé en dumpsys alarm), y un aviso
    // de las 7:30 que llega a las 15:00 no sirve. Con ventana de 15 minutos no
    // hace falta el permiso de alarma exacta y el aviso llega a tiempo.
    // Es de un solo disparo: el receptor reprograma el día siguiente.
    alarmManager(context).setWindow(
        AlarmManager.RTC_WAKEUP, firstFire, 15 * 60 * 1000L, reminderIntent(context)
    )
}
