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
    val standardDailyHours: Double = 8.0,
    val overtimeMultiplier: Double = 1.5,
    val note: String = ""
) {
    val grossDurationMillis: Long
        get() = (endTimeMillis - startTimeMillis).coerceAtLeast(0)

    val breakMillis: Long
        get() = breakMinutes * 60000L

    val durationMillis: Long
        get() = (grossDurationMillis - breakMillis).coerceAtLeast(0)

    val durationHours: Double
        get() = durationMillis / 3600000.0

    val isOvertimeActive: Boolean
        get() = standardDailyHours > 0.0 && overtimeMultiplier > 1.0

    val regularHours: Double
        get() = if (isOvertimeActive) minOf(durationHours, standardDailyHours) else durationHours

    val overtimeHours: Double
        get() = if (isOvertimeActive) maxOf(0.0, durationHours - standardDailyHours) else 0.0

    val regularEarnings: Double
        get() = regularHours * hourlyRate

    val overtimeEarnings: Double
        get() = overtimeHours * (hourlyRate * overtimeMultiplier)

    val totalEarnings: Double
        get() = regularEarnings + overtimeEarnings

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES"))
        return sdf.format(Date(startTimeMillis)).replaceFirstChar { it.uppercase() }
    }

    fun formattedShortDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(startTimeMillis))
    }

    fun formattedTimeRange(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.format(Date(startTimeMillis))
        val end = sdf.format(Date(endTimeMillis))
        return "$start - $end"
    }

    fun formattedDuration(): String {
        val totalMinutes = (durationMillis / 60000).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val base = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        return if (breakMinutes > 0) "$base (descanso: ${breakMinutes}m)" else base
    }
}
