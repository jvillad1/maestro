package com.maestro.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.maestro.app.notifications.NotificationPermission

class MainActivity : ComponentActivity() {
    // El permiso de notificaciones solo se puede pedir desde una Activity;
    // el recordatorio llega hasta acá a través de NotificationPermission.
    private var pendingPermissionCallback: ((Boolean) -> Unit)? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            pendingPermissionCallback?.invoke(granted)
            pendingPermissionCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.context = applicationContext
        NotificationPermission.request = { callback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pendingPermissionCallback = callback
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Antes de Android 13 basta con que el usuario no las haya apagado
                callback(true)
            }
        }
        enableEdgeToEdge()
        setContent { App() }
    }
}
