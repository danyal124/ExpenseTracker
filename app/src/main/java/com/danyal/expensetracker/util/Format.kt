package com.danyal.expensetracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 12345.5 -> "Rs 12,345.50" */
fun Double.money(symbol: String, decimals: Boolean = true): String {
    val pattern = if (decimals) "%,.2f" else "%,.0f"
    return "$symbol ${String.format(Locale.US, pattern, this)}"
}

/** Short number for charts: 1500 -> "1.5k" */
fun Double.compact(): String = when {
    this >= 1_000_000 -> String.format(Locale.US, "%.1fM", this / 1_000_000)
    this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
    else -> String.format(Locale.US, "%.0f", this)
}

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
private val fullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

/** "Today", "Yesterday" or "Mon, 12 Oct". */
fun LocalDate.pretty(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> format(dayFormatter)
    }
}

fun LocalDate.full(): String = format(fullFormatter)
