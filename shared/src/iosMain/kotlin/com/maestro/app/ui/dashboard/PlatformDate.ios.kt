package com.maestro.app.ui.dashboard

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

private fun format(pattern: String): String =
    NSDateFormatter().apply { dateFormat = pattern }.stringFromDate(NSDate())

actual fun getCurrentMonth(): String = format("yyyy-MM")
actual fun getCurrentDate(): String = format("yyyy-MM-dd")
