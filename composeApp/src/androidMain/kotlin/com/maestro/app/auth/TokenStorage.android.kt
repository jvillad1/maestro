package com.maestro.app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "maestro_prefs")
private val TOKEN_KEY = stringPreferencesKey("maestro_token")
private val NAME_KEY = stringPreferencesKey("maestro_user_name")

actual class TokenStorage actual constructor() {
    private val context: Context get() = AppContextHolder.context

    actual fun getToken(): String? = runBlocking {
        context.dataStore.data.first()[TOKEN_KEY]
    }

    actual fun saveToken(token: String) { runBlocking {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }}

    actual fun clearToken() { runBlocking {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }}

    actual fun getUserName(): String? = runBlocking {
        context.dataStore.data.first()[NAME_KEY]
    }

    actual fun saveUserName(name: String) { runBlocking {
        context.dataStore.edit { it[NAME_KEY] = name }
    }}
}
