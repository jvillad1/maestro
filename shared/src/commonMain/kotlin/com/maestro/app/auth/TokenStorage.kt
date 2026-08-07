package com.maestro.app.auth

expect class TokenStorage() {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
    fun getUserName(): String?
    fun saveUserName(name: String)
    fun getUserId(): Long?
    fun saveUserId(id: Long)
    fun clearSession()
}
