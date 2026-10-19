package com.example.workhourstracker.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("work_hours_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOURLY_RATE = "key_hourly_rate"
        private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
    }

    var hourlyRate: Double
        get() = prefs.getFloat(KEY_HOURLY_RATE, 2500.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_HOURLY_RATE, value.toFloat()).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()
}
