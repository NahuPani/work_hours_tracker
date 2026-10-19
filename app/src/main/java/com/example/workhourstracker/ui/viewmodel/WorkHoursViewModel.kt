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

    // Calculated summary of all recorded shifts
    val earningsSummary: StateFlow<EarningsSummary> = allShifts.map { shifts ->
        calculateEarningsSummary(shifts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsSummary())

    fun addShift(
        startTimeMillis: Long,
        endTimeMillis: Long,
        hourlyRate: Double = _hourlyRate.value,
        note: String = ""
    ) {
        viewModelScope.launch {
            val shift = WorkShift(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                hourlyRate = hourlyRate,
                note = note.trim()
            )
            shiftDao.insertShift(shift)
        }
    }

    fun addManualShift(
        startTimeMillis: Long,
        endTimeMillis: Long,
        hourlyRate: Double,
        note: String
    ) {
        addShift(startTimeMillis, endTimeMillis, hourlyRate, note)
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

    private fun calculateEarningsSummary(shifts: List<WorkShift>): EarningsSummary {
        val now = Calendar.getInstance()

        // Start of Today (00:00:00)
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCalendar.timeInMillis

        // Start of Week (Monday 00:00:00)
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

        // Start of Month (1st day 00:00:00)
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
}
