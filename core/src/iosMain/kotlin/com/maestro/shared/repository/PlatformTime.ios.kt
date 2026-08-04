package com.maestro.shared.repository

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

actual fun epochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun todayIsoDate(): String {
    val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
    return formatter.stringFromDate(NSDate())
}
