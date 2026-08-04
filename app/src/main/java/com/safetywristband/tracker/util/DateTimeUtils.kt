@file:Suppress("unused")

package com.safetywristband.tracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    private val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault())

    fun formatTime(epochMillis: Long): String =
        if (epochMillis <= 0L) "--" else timeFormatter.format(Date(epochMillis))

    fun formatDateTime(epochMillis: Long): String =
        if (epochMillis <= 0L) "--" else dateTimeFormatter.format(Date(epochMillis))

    fun timeAgo(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Never"
        val diff = System.currentTimeMillis() - epochMillis
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        }
    }
}
