package com.example.workhourstracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workhourstracker.data.AppDatabase
import com.example.workhourstracker.data.UserPreferences
import com.example.workhourstracker.data.WorkShift
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
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

    private val _isClockedIn = MutableStateFlow(prefs.activeShiftStartTime != null)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    private val _activeShiftStartTime = MutableStateFlow(prefs.activeShiftStartTime)
    val activeShiftStartTime: StateFlow<Long?> = _activeShiftStartTime.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _currentSessionEarnings = MutableStateFlow(0.0)
    val currentSessionEarnings: StateFlow<Double> = _currentSessionEarnings.asStateFlow()

    private var timerJob: Job? = null

    // Combined earnings summary
    val earningsSummary: StateFlow<EarningsSummary> = combine(
        allShifts,
        currentSessionEarnings,
        elapsedSeconds,
        isClockedIn
    ) { shifts, activeEarnings, activeSeconds, clockedIn ->
        calculateEarningsSummary(shifts, if (clockedIn) activeEarnings else 0.0, if (clockedIn) activeSeconds else 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsSummary())

    init {
        // Restore active shift if app was closed while clocked in
        val activeStart = prefs.activeShiftStartTime
        if (activeStart != null) {
            _isClockedIn.value = true
            _activeShiftStartTime.value = activeStart
            startTimer(activeStart)
        }
    }

    fun clockIn() {
        val now = System.currentTimeMillis()
        prefs.activeShiftStartTime = now
        prefs.activeShiftHourlyRate = _hourlyRate.value
        _activeShiftStartTime.value = now
        _isClockedIn.value = true
        startTimer(now)
    }

    fun clockOut(note: String = "") {
        val startTime = _activeShiftStartTime.value ?: return
        val endTime = System.currentTimeMillis()
        val rate = prefs.activeShiftHourlyRate ?: _hourlyRate.value

        stopTimer()
        prefs.clearActiveShift()
        _isClockedIn.value = false
        _activeShiftStartTime.value = null
        _elapsedSeconds.value = 0L
        _currentSessionEarnings.value = 0.0

        viewModelScope.launch {
            val shift = WorkShift(
                startTimeMillis = startTime,
                endTimeMillis = endTime,
                hourlyRate = rate,
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
        // Also update current session earnings calculation
        if (_isClockedIn.value) {
            _currentSessionEarnings.value = (_elapsedSeconds.value / 3600.0) * validRate
        }
    }

    private fun startTimer(startTimeMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val seconds = ((now - startTimeMillis) / 1000).coerceAtLeast(0)
                _elapsedSeconds.value = seconds
                val rate = prefs.activeShiftHourlyRate ?: _hourlyRate.value
                _currentSessionEarnings.value = (seconds / 3600.0) * rate
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun calculateEarningsSummary(
        shifts: List<WorkShift>,
        activeEarnings: Double,
        activeSeconds: Long
    ): EarningsSummary {
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

        val activeHours = activeSeconds / 3600.0
        todayH += activeHours
        todayE += activeEarnings
        weekH += activeHours
        weekE += activeEarnings
        monthH += activeHours
        monthE += activeEarnings
        totalH += activeHours
        totalE += activeEarnings

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

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
