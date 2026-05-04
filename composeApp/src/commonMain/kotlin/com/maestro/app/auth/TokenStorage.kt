package com.maestro.app.auth

expect class TokenStorage() {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}
