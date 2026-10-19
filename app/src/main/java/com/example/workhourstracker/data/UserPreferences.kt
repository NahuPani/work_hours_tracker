package com.example.workhourstracker.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("work_hours_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOURLY_RATE = "key_hourly_rate"
        private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
        private const val KEY_STANDARD_DAILY_HOURS = "key_standard_daily_hours"
        private const val KEY_OVERTIME_MULTIPLIER = "key_overtime_multiplier"
        private const val KEY_ENABLE_OVERTIME = "key_enable_overtime"
        private const val KEY_START_OF_WEEK = "key_start_of_week"
    }

    private fun getDoubleCompat(key: String, default: Double): Double {
        return try {
            Double.fromBits(prefs.getLong(key, default.toRawBits()))
        } catch (e: ClassCastException) {
            // Migrates legacy Float value stored in earlier versions
            try {
                val legacyFloat = prefs.getFloat(key, default.toFloat())
                val doubleVal = legacyFloat.toDouble()
                prefs.edit().putLong(key, doubleVal.toRawBits()).apply()
                doubleVal
            } catch (e2: Exception) {
                default
            }
        }
    }

    var hourlyRate: Double
        get() = getDoubleCompat(KEY_HOURLY_RATE, 2500.0)
        set(value) = prefs.edit().putLong(KEY_HOURLY_RATE, value.toRawBits()).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    var standardDailyHours: Double
        get() = getDoubleCompat(KEY_STANDARD_DAILY_HOURS, 8.0)
        set(value) = prefs.edit().putLong(KEY_STANDARD_DAILY_HOURS, value.toRawBits()).apply()

    var overtimeMultiplier: Double
        get() = getDoubleCompat(KEY_OVERTIME_MULTIPLIER, 1.5)
        set(value) = prefs.edit().putLong(KEY_OVERTIME_MULTIPLIER, value.toRawBits()).apply()

    var enableOvertime: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_OVERTIME, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_OVERTIME, value).apply()

    var startDayOfWeek: Int
        get() = prefs.getInt(KEY_START_OF_WEEK, java.util.Calendar.WEDNESDAY)
        set(value) = prefs.edit().putInt(KEY_START_OF_WEEK, value).apply()

    var draftStartHour: Int
        get() = prefs.getInt("key_draft_start_hour", 8)
        set(value) = prefs.edit().putInt("key_draft_start_hour", value).apply()

    var draftStartMinute: Int
        get() = prefs.getInt("key_draft_start_minute", 0)
        set(value) = prefs.edit().putInt("key_draft_start_minute", value).apply()

    var draftEndHour: Int
        get() = prefs.getInt("key_draft_end_hour", 16)
        set(value) = prefs.edit().putInt("key_draft_end_hour", value).apply()

    var draftEndMinute: Int
        get() = prefs.getInt("key_draft_end_minute", 0)
        set(value) = prefs.edit().putInt("key_draft_end_minute", value).apply()
}
