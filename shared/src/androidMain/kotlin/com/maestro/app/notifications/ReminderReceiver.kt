package com.maestro.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.maestro.app.AppContextHolder
import com.maestro.app.auth.TokenStorage
import com.maestro.shared.db.MaestroDatabase
import com.maestro.shared.db.createAndroidDriver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val ACTION_DAILY_REMINDER = "com.maestro.app.DAILY_REMINDER"
private const val CHANNEL_ID = "clases_del_dia"
private const val NOTIFICATION_ID = 1001

/** Se dispara con la alarma diaria y avisa cuántas clases hay hoy. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // El proceso puede haber arrancado por la alarma, sin pasar por MainActivity
        AppContextHolder.context = context.applicationContext

        // La alarma es de un solo disparo: dejamos programada la de mañana
        if (reminderEnabled(context)) {
            scheduleReminder(context, reminderHour(context), reminderMinute(context))
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val names = todayStudentNames(context, today)
        // Sin clases hoy no molestamos
        if (names.isEmpty()) return

        ensureChannel(context)
        val title = if (names.size == 1) "Tienes 1 clase hoy" else "Tienes ${names.size} clases hoy"
        val body = names.joinToString(" · ")

        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = open?.let {
            android.app.PendingIntent.getActivity(
                context, 0, it,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // El usuario revocó el permiso después de programar; no hay nada que hacer
        }
    }
}

/** Reprograma la alarma tras reiniciar el teléfono; si no, se pierde. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AppContextHolder.context = context.applicationContext
        if (reminderEnabled(context)) {
            scheduleReminder(context, reminderHour(context), reminderMinute(context))
        }
    }
}

private fun todayStudentNames(context: Context, today: String): List<String> = try {
    val userId = TokenStorage().getUserId()
    if (userId == null) emptyList()
    else {
        val db = MaestroDatabase(createAndroidDriver(context))
        val students = db.studentQueries.selectAll(userId).executeAsList().associate { it.id to it.name }
        db.classEntryQueries.selectAll(userId).executeAsList()
            .filter { it.date == today }
            .mapNotNull { students[it.studentId] }
    }
} catch (e: Exception) {
    Log.e("MaestroReminder", "fallo al consultar", e)
    emptyList()
}

private fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (manager.getNotificationChannel(CHANNEL_ID) != null) return
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, "Clases del día", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Aviso diario con las clases que tienes programadas"
        }
    )
}
