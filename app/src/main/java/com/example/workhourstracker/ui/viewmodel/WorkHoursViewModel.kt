package com.example.workhourstracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workhourstracker.data.AppDatabase
import com.example.workhourstracker.data.UserPreferences
import com.example.workhourstracker.data.WorkShift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class EarningsSummary(
    val todayHours: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val weekHours: Double = 0.0,
    val weekEarnings: Double = 0.0,
    val monthHours: Double = 0.0,
    val monthEarnings: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalEarnings: Double = 0.0
)

data class DayActivity(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val durationHours: Double,
    val earnings: Double,
    val overtimeHours: Double
)

class WorkHoursViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val shiftDao = db.workShiftDao()
    private val prefs = UserPreferences(application)

    val allShifts: StateFlow<List<WorkShift>> = shiftDao.getAllShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hourlyRate = MutableStateFlow(prefs.hourlyRate)
    val hourlyRate: StateFlow<Double> = _hourlyRate.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.currencySymbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _standardDailyHours = MutableStateFlow(prefs.standardDailyHours)
    val standardDailyHours: StateFlow<Double> = _standardDailyHours.asStateFlow()

    private val _overtimeMultiplier = MutableStateFlow(prefs.overtimeMultiplier)
    val overtimeMultiplier: StateFlow<Double> = _overtimeMultiplier.asStateFlow()

    private val _enableOvertime = MutableStateFlow(prefs.enableOvertime)
    val enableOvertime: StateFlow<Boolean> = _enableOvertime.asStateFlow()

    // Calculated summary of all recorded shifts
    val earningsSummary: StateFlow<EarningsSummary> = allShifts.map { shifts ->
        calculateEarningsSummary(shifts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsSummary())

    // Weekly activity for 7 days (Monday to Sunday)
    val weeklyActivity: StateFlow<List<DayActivity>> = allShifts.map { shifts ->
        calculateWeeklyActivity(shifts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShift(
        startTimeMillis: Long,
        endTimeMillis: Long,
        hourlyRate: Double = _hourlyRate.value,
        standardDailyHours: Double = if (_enableOvertime.value) _standardDailyHours.value else 0.0,
        overtimeMultiplier: Double = if (_enableOvertime.value) _overtimeMultiplier.value else 1.0,
        note: String = ""
    ) {
        viewModelScope.launch {
            val shift = WorkShift(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                hourlyRate = hourlyRate,
                breakMinutes = 0,
                isBreakPaid = true,
                standardDailyHours = standardDailyHours,
                overtimeMultiplier = overtimeMultiplier,
                note = note.trim()
            )
            shiftDao.insertShift(shift)
        }
    }

    fun updateShift(shift: WorkShift) {
        viewModelScope.launch {
            shiftDao.updateShift(shift)
        }
    }

    fun deleteShift(shift: WorkShift) {
        viewModelScope.launch {
            shiftDao.deleteShift(shift)
        }
    }

    fun updateHourlyRate(newRate: Double, newSymbol: String) {
        val validRate = newRate.coerceAtLeast(0.0)
        prefs.hourlyRate = validRate
        prefs.currencySymbol = newSymbol
        _hourlyRate.value = validRate
        _currencySymbol.value = newSymbol
    }

    fun updateOvertimeSettings(standardHours: Double, multiplier: Double, enabled: Boolean) {
        prefs.standardDailyHours = standardHours
        prefs.overtimeMultiplier = multiplier
        prefs.enableOvertime = enabled
        _standardDailyHours.value = standardHours
        _overtimeMultiplier.value = multiplier
        _enableOvertime.value = enabled
    }

    // Export report for WhatsApp / clipboard sharing
    fun generateShareableReport(shifts: List<WorkShift>, periodTitle: String): String {
        val currency = _currencySymbol.value
        val totalHours = shifts.sumOf { it.durationHours }
        val totalRegHours = shifts.sumOf { it.regularHours }
        val totalExtraHours = shifts.sumOf { it.overtimeHours }
        val totalEarnings = shifts.sumOf { it.totalEarnings }

        val sb = StringBuilder()
        sb.appendLine("📋 REPORTE DE HORAS Y GANANCIAS")
        sb.appendLine("Período: $periodTitle")
        sb.appendLine("----------------------------------")
        sb.appendLine("⏱️ Horas totales: ${String.format(Locale.US, "%.2f", totalHours)}h")
        if (totalExtraHours > 0.0) {
            sb.appendLine("  • Horas normales: ${String.format(Locale.US, "%.2f", totalRegHours)}h")
            sb.appendLine("  • Horas extras: ${String.format(Locale.US, "%.2f", totalExtraHours)}h")
        }
        sb.appendLine("💰 Total a cobrar: $currency${String.format(Locale.getDefault(), "%,.2f", totalEarnings)}")
        sb.appendLine("----------------------------------")
        sb.appendLine("Detalle de jornadas (${shifts.size}):")

        shifts.forEach { shift ->
            val date = shift.formattedShortDate()
            val time = shift.formattedTimeRange()
            val dur = shift.formattedDuration()
            val money = "$currency${String.format(Locale.getDefault(), "%,.2f", shift.totalEarnings)}"
            sb.appendLine("• $date | $time ($dur) ➔ $money")
            if (shift.note.isNotBlank()) {
                sb.appendLine("  Nota: ${shift.note}")
            }
        }
        sb.appendLine("----------------------------------")
        sb.appendLine("Generado con Control de Horas")
        return sb.toString()
    }

    // Export shifts to CSV for Excel
    fun exportToCsv(shifts: List<WorkShift>): String {
        val sb = StringBuilder()
        sb.appendLine("ID,Fecha,Entrada,Salida,Horas_Normales,Horas_Extras,Horas_Totales,Tarifa_Hora,Total_Cobrar,Nota")
        val timeFmt = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

        shifts.forEach { s ->
            val date = s.formattedShortDate()
            val start = timeFmt.format(Date(s.startTimeMillis))
            val end = timeFmt.format(Date(s.endTimeMillis))
            val noteClean = s.note.replace(",", " ").replace("\n", " ")
            sb.appendLine(
                "${s.id},\"$date\",$start,$end," +
                        "${String.format(Locale.US, "%.2f", s.regularHours)}," +
                        "${String.format(Locale.US, "%.2f", s.overtimeHours)}," +
                        "${String.format(Locale.US, "%.2f", s.durationHours)}," +
                        "${s.hourlyRate},${String.format(Locale.US, "%.2f", s.totalEarnings)},\"$noteClean\""
            )
        }
        return sb.toString()
    }

    // Backup: Export all data to JSON
    fun exportBackupJson(): String {
        val json = org.json.JSONObject()
        json.put("version", 2)
        json.put("exportDate", System.currentTimeMillis())
        json.put("hourlyRate", _hourlyRate.value)
        json.put("currencySymbol", _currencySymbol.value)
        json.put("standardDailyHours", _standardDailyHours.value)
        json.put("overtimeMultiplier", _overtimeMultiplier.value)

        val array = org.json.JSONArray()
        allShifts.value.forEach { s ->
            val item = org.json.JSONObject()
            item.put("id", s.id)
            item.put("startTimeMillis", s.startTimeMillis)
            item.put("endTimeMillis", s.endTimeMillis)
            item.put("hourlyRate", s.hourlyRate)
            item.put("breakMinutes", s.breakMinutes)
            item.put("isBreakPaid", s.isBreakPaid)
            item.put("standardDailyHours", s.standardDailyHours)
            item.put("overtimeMultiplier", s.overtimeMultiplier)
            item.put("note", s.note)
            array.put(item)
        }
        json.put("shifts", array)
        return json.toString(2)
    }

    // Restore data from JSON
    suspend fun restoreBackupJson(jsonString: String): Result<Int> {
        return try {
            val json = org.json.JSONObject(jsonString)
            if (json.has("hourlyRate")) updateHourlyRate(json.getDouble("hourlyRate"), json.optString("currencySymbol", "$"))
            if (json.has("standardDailyHours")) {
                updateOvertimeSettings(
                    json.optDouble("standardDailyHours", 8.0),
                    json.optDouble("overtimeMultiplier", 1.5),
                    true
                )
            }

            val array = json.getJSONArray("shifts")
            val list = mutableListOf<WorkShift>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    WorkShift(
                        startTimeMillis = item.getLong("startTimeMillis"),
                        endTimeMillis = item.getLong("endTimeMillis"),
                        hourlyRate = item.getDouble("hourlyRate"),
                        breakMinutes = item.optInt("breakMinutes", 0),
                        isBreakPaid = item.optBoolean("isBreakPaid", true),
                        standardDailyHours = item.optDouble("standardDailyHours", 8.0),
                        overtimeMultiplier = item.optDouble("overtimeMultiplier", 1.5),
                        note = item.optString("note", "")
                    )
                )
            }
            shiftDao.insertAll(list)
            Result.success(list.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateEarningsSummary(shifts: List<WorkShift>): EarningsSummary {
        val now = Calendar.getInstance()

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCalendar.timeInMillis

        val weekCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            if (firstDayOfWeek == Calendar.SUNDAY && get(Calendar.DAY_OF_WEEK) > now.get(Calendar.DAY_OF_WEEK)) {
                add(Calendar.WEEK_OF_YEAR, -1)
            }
        }
        val startOfWeek = weekCalendar.timeInMillis

        val monthCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = monthCalendar.timeInMillis

        var todayH = 0.0
        var todayE = 0.0
        var weekH = 0.0
        var weekE = 0.0
        var monthH = 0.0
        var monthE = 0.0
        var totalH = 0.0
        var totalE = 0.0

        for (shift in shifts) {
            val h = shift.durationHours
            val e = shift.totalEarnings

            totalH += h
            totalE += e

            if (shift.startTimeMillis >= startOfMonth) {
                monthH += h
                monthE += e
            }
            if (shift.startTimeMillis >= startOfWeek) {
                weekH += h
                weekE += e
            }
            if (shift.startTimeMillis >= startOfToday) {
                todayH += h
                todayE += e
            }
        }

        return EarningsSummary(
            todayHours = todayH,
            todayEarnings = todayE,
            weekHours = weekH,
            weekEarnings = weekE,
            monthHours = monthH,
            monthEarnings = monthE,
            totalHours = totalH,
            totalEarnings = totalE
        )
    }

    private fun calculateWeeklyActivity(shifts: List<WorkShift>): List<DayActivity> {
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDayOfYear = now.get(Calendar.DAY_OF_YEAR)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Go to Monday of current week
            var dayOfWeek = get(Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2, ...
            val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
            add(Calendar.DAY_OF_MONTH, diff)
        }

        val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val list = mutableListOf<DayActivity>()

        for (i in 0 until 7) {
            val startOfDay = cal.timeInMillis
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = (cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear)

            cal.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = cal.timeInMillis

            val dayShifts = shifts.filter { it.startTimeMillis in startOfDay until endOfDay }
            val hours = dayShifts.sumOf { it.durationHours }
            val earnings = dayShifts.sumOf { it.totalEarnings }
            val overtime = dayShifts.sumOf { it.overtimeHours }

            list.add(
                DayActivity(
                    dayOfWeek = dayNames[i],
                    dayOfMonth = dayOfMonth,
                    isToday = isToday,
                    durationHours = hours,
                    earnings = earnings,
                    overtimeHours = overtime
                )
            )
        }
        return list
    }
}
