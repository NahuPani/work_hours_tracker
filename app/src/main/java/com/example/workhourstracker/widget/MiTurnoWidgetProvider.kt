package com.example.workhourstracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.workhourstracker.MainActivity
import com.example.workhourstracker.R
import com.example.workhourstracker.data.AppDatabase
import com.example.workhourstracker.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MiTurnoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MiTurnoWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, MiTurnoWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }

        private suspend fun updateWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val prefs = UserPreferences(context)
            val currency = prefs.currencySymbol
            val startDay = prefs.startDayOfWeek
            val db = AppDatabase.getDatabase(context)
            val shifts = db.workShiftDao().getAllShiftsList()

            // Calculate current week metrics respecting the custom cycle (e.g. Wednesday to Tuesday)
            val weekCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                val currentDay = get(Calendar.DAY_OF_WEEK)
                val diff = (currentDay - startDay + 7) % 7
                add(Calendar.DAY_OF_MONTH, -diff)
            }
            val startOfWeek = weekCalendar.timeInMillis

            var weekHours = 0.0
            var weekEarnings = 0.0

            for (shift in shifts) {
                if (shift.startTimeMillis >= startOfWeek) {
                    weekHours += shift.durationHours
                    weekEarnings += shift.totalEarnings
                }
            }

            val totalMinutes = (weekHours * 60).toInt()
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            val hoursText = "${h}h ${m}m"
            val earningsText = "$currency${String.format(Locale.getDefault(), "%,.0f", weekEarnings)}"

            val dayName = when (startDay) {
                Calendar.MONDAY -> "Lun-Dom"
                Calendar.TUESDAY -> "Mar-Lun"
                Calendar.WEDNESDAY -> "Mié-Mar"
                Calendar.THURSDAY -> "Jue-Mié"
                Calendar.FRIDAY -> "Vie-Jue"
                Calendar.SATURDAY -> "Sáb-Vie"
                else -> "Dom-Sáb"
            }
            val cycleText = "Esta semana ($dayName)"

            // Intent to open MainActivity on click
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_mi_turno).apply {
                    setTextViewText(R.id.widget_hours_text, hoursText)
                    setTextViewText(R.id.widget_earnings_text, earningsText)
                    setTextViewText(R.id.widget_cycle_text, cycleText)
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    setOnClickPendingIntent(R.id.widget_action_btn, pendingIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
