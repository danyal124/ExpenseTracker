package com.danyal.expensetracker.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** [themeMode]: 0 = follow system, 1 = light, 2 = dark. */
data class AppSettings(val currency: String = "Rs", val themeMode: Int = 0)

/** Currencies offered in Settings: label to symbol shown in the app. */
val CURRENCIES: List<Pair<String, String>> = listOf(
    "PKR" to "Rs",
    "USD" to "$",
    "EUR" to "€",
    "GBP" to "£",
    "SEK" to "kr",
    "INR" to "₹",
    "AED" to "AED"
)

/** Tiny wrapper around SharedPreferences that exposes settings as a StateFlow. */
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        AppSettings(
            currency = prefs.getString("currency", "Rs") ?: "Rs",
            themeMode = prefs.getInt("theme", 0)
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setCurrency(symbol: String) {
        prefs.edit().putString("currency", symbol).apply()
        _settings.update { it.copy(currency = symbol) }
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme", mode).apply()
        _settings.update { it.copy(themeMode = mode) }
    }
}
