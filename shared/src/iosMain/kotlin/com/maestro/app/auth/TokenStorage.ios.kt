package com.maestro.app.auth

import platform.Foundation.NSUserDefaults

actual class TokenStorage actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getToken(): String? = defaults.stringForKey("maestro_token")
    actual fun saveToken(token: String) { defaults.setObject(token, "maestro_token") }
    actual fun clearToken() { defaults.removeObjectForKey("maestro_token") }
    actual fun getUserName(): String? = defaults.stringForKey("maestro_user_name")
    actual fun saveUserName(name: String) { defaults.setObject(name, "maestro_user_name") }
    actual fun getUserId(): Long? = defaults.stringForKey("maestro_user_id")?.toLongOrNull()
    actual fun saveUserId(id: Long) { defaults.setObject(id.toString(), "maestro_user_id") }
    actual fun clearSession() {
        defaults.removeObjectForKey("maestro_token")
        defaults.removeObjectForKey("maestro_user_name")
        defaults.removeObjectForKey("maestro_user_id")
    }
}
