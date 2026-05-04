package com.maestro.app.ui.dashboard

private external interface JsDate {
    fun getFullYear(): Int
    fun getMonth(): Int
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun newDate(): JsDate = js("new Date()")

actual fun getCurrentMonth(): String {
    val now = newDate()
    val year = now.getFullYear().toString()
    val month = (now.getMonth() + 1).toString().padStart(2, '0')
    return "$year-$month"
}
