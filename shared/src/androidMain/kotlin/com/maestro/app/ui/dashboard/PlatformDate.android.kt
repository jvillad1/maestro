package com.maestro.app.ui.dashboard

import java.time.YearMonth

actual fun getCurrentMonth(): String = YearMonth.now().toString()
actual fun getCurrentDate(): String = java.time.LocalDate.now().toString()
