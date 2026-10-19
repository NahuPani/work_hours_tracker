package com.example.workhourstracker.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("work_hours_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOURLY_RATE = "key_hourly_rate"
        private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
        private const val KEY_ACTIVE_SHIFT_START = "key_active_shift_start"
        private const val KEY_ACTIVE_SHIFT_RATE = "key_active_shift_rate"
    }

    var hourlyRate: Double
        get() = prefs.getFloat(KEY_HOURLY_RATE, 2500.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_HOURLY_RATE, value.toFloat()).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    var activeShiftStartTime: Long?
        get() {
            val time = prefs.getLong(KEY_ACTIVE_SHIFT_START, -1L)
            return if (time > 0L) time else null
        }
        set(value) {
            if (value != null) {
                prefs.edit().putLong(KEY_ACTIVE_SHIFT_START, value).apply()
            } else {
                prefs.edit().remove(KEY_ACTIVE_SHIFT_START).apply()
            }
        }

    var activeShiftHourlyRate: Double?
        get() {
            val rate = prefs.getFloat(KEY_ACTIVE_SHIFT_RATE, -1.0f)
            return if (rate > 0f) rate.toDouble() else null
        }
        set(value) {
            if (value != null) {
                prefs.edit().putFloat(KEY_ACTIVE_SHIFT_RATE, value.toFloat()).apply()
            } else {
                prefs.edit().remove(KEY_ACTIVE_SHIFT_RATE).apply()
            }
        }

    fun clearActiveShift() {
        prefs.edit()
            .remove(KEY_ACTIVE_SHIFT_START)
            .remove(KEY_ACTIVE_SHIFT_RATE)
            .apply()
    }
}
