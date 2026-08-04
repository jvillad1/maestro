package com.maestro.shared.repository

actual fun epochMillis(): Long = System.currentTimeMillis()
actual fun todayIsoDate(): String = java.time.LocalDate.now().toString()
