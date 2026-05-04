package com.maestro.app.auth

import kotlinx.browser.localStorage

actual class TokenStorage actual constructor() {
    actual fun getToken(): String? = localStorage.getItem("maestro_token")
    actual fun saveToken(token: String) { localStorage.setItem("maestro_token", token) }
    actual fun clearToken() { localStorage.removeItem("maestro_token") }
    actual fun getUserName(): String? = localStorage.getItem("maestro_user_name")
    actual fun saveUserName(name: String) { localStorage.setItem("maestro_user_name", name) }
}
