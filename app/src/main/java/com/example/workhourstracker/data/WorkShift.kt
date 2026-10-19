package com.example.workhourstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "work_shifts")
data class WorkShift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val hourlyRate: Double,
    val breakMinutes: Int = 0,
    val isBreakPaid: Boolean = true,
    val standardDailyHours: Double = 8.0,
    val overtimeMultiplier: Double = 1.5,
    val note: String = ""
) {
    /**
     * Runtime-only overrides for cumulative daily overtime calculation.
     * WARNING: These fields are NOT preserved by data class copy().
     * Always use [withDailyOvertime] to set them on a new copy.
     */
    @androidx.room.Ignore
    var overrideRegularHours: Double? = null

    @androidx.room.Ignore
    var overrideOvertimeHours: Double? = null

    fun withDailyOvertime(regular: Double, overtime: Double): WorkShift {
        val shift = this.copy()
        shift.overrideRegularHours = regular
        shift.overrideOvertimeHours = overtime
        return shift
    }

    val grossDurationMillis: Long
        get() = (endTimeMillis - startTimeMillis).coerceAtLeast(0)

    val breakMillis: Long
        get() = breakMinutes * 60000L

    val durationMillis: Long
        get() = if (isBreakPaid) grossDurationMillis else (grossDurationMillis - breakMillis).coerceAtLeast(0)

    val durationHours: Double
        get() = durationMillis / 3600000.0

    val isOvertimeActive: Boolean
        get() = standardDailyHours > 0.0 && overtimeMultiplier > 1.0

    val regularHours: Double
        get() = overrideRegularHours ?: if (isOvertimeActive) minOf(durationHours, standardDailyHours) else durationHours

    val overtimeHours: Double
        get() = overrideOvertimeHours ?: if (isOvertimeActive) maxOf(0.0, durationHours - standardDailyHours) else 0.0

    val regularEarnings: Double
        get() = regularHours * hourlyRate

    val overtimeEarnings: Double
        get() = overtimeHours * (hourlyRate * overtimeMultiplier)

    val totalEarnings: Double
        get() = regularEarnings + overtimeEarnings

    fun formattedDate(): String = synchronized(DATE_FORMAT_FULL) {
        DATE_FORMAT_FULL.format(Date(startTimeMillis)).replaceFirstChar { it.uppercase() }
    }

    fun formattedShortDate(): String = synchronized(DATE_FORMAT_SHORT) {
        DATE_FORMAT_SHORT.format(Date(startTimeMillis))
    }

    fun formattedTimeRange(): String = synchronized(TIME_FORMAT) {
        val start = TIME_FORMAT.format(Date(startTimeMillis))
        val end = TIME_FORMAT.format(Date(endTimeMillis))
        "$start - $end"
    }

    fun formattedDuration(): String {
        val totalMinutes = (durationMillis / 60000).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    companion object {
        // Cached formatters to avoid creating expensive SimpleDateFormat objects on every call.
        // Note: SimpleDateFormat is not thread-safe, but in a single-threaded UI context this is fine.
        private val DATE_FORMAT_FULL = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES"))
        private val DATE_FORMAT_SHORT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
}
