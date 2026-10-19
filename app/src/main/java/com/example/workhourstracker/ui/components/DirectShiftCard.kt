package com.example.workhourstracker.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentEmerald
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DirectShiftCard(
    hourlyRate: Double,
    currencySymbol: String,
    standardDailyHours: Double = 8.0,
    overtimeMultiplier: Double = 1.5,
    enableOvertime: Boolean = true,
    defaultBreakMinutes: Int = 0,
    onSaveShift: (
        startTimeMillis: Long,
        endTimeMillis: Long,
        hourlyRate: Double,
        breakMinutes: Int,
        standardDailyHours: Double,
        overtimeMultiplier: Double,
        note: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val is24Hour = DateFormat.is24HourFormat(context)

    // Current date state
    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    // Default start at 08:00 and end at 16:00 (or current hour)
    var startHour by remember { mutableIntStateOf(8) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(16) }
    var endMinute by remember { mutableIntStateOf(0) }

    var breakMinutes by remember { mutableIntStateOf(defaultBreakMinutes) }
    var noteText by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Date Picker Dialog
    val datePickerDialog = remember(selectedCalendar) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedCalendar = newCal
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Time Picker for Start Time
    val startTimePickerDialog = remember(startHour, startMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                startHour = hourOfDay
                startMinute = minute
                successMessage = null
            },
            startHour,
            startMinute,
            is24Hour
        )
    }

    // Time Picker for End Time
    val endTimePickerDialog = remember(endHour, endMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                endHour = hourOfDay
                endMinute = minute
                successMessage = null
            },
            endHour,
            endMinute,
            is24Hour
        )
    }

    // Calculate Milliseconds and Net Duration
    val startCal = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCal = (selectedCalendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, endMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // If end time is before or equal to start time, treat as overnight shift (ending next day)
        if (timeInMillis <= startCal.timeInMillis) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val grossDurationMillis = (endCal.timeInMillis - startCal.timeInMillis).coerceAtLeast(0)
    val breakMillis = breakMinutes * 60000L
    val netDurationMillis = (grossDurationMillis - breakMillis).coerceAtLeast(0)
    val netHours = netDurationMillis / 3600000.0

    val totalNetMinutes = (netDurationMillis / 60000).toInt()
    val hours = totalNetMinutes / 60
    val minutes = totalNetMinutes % 60

    // Overtime calculation
    val isOvertimeActive = enableOvertime && standardDailyHours > 0.0 && overtimeMultiplier > 1.0
    val regularHours = if (isOvertimeActive) minOf(netHours, standardDailyHours) else netHours
    val overtimeHours = if (isOvertimeActive) maxOf(0.0, netHours - standardDailyHours) else 0.0
    val estimatedEarnings = (regularHours * hourlyRate) + (overtimeHours * (hourlyRate * overtimeMultiplier))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Card Title & Date Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar Turno",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Date Selector Chip
                val isToday = isSameDay(selectedCalendar, Calendar.getInstance())
                val dateLabel = if (isToday) {
                    "Hoy, " + SimpleDateFormat("d MMM", Locale("es", "ES")).format(selectedCalendar.time)
                } else {
                    SimpleDateFormat("d MMM yyyy", Locale("es", "ES")).format(selectedCalendar.time)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Seleccionar fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time Selector Cards (Entrada and Salida)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeSelectorBox(
                    modifier = Modifier.weight(1f),
                    title = "ENTRADA",
                    timeText = String.format(Locale.US, "%02d:%02d", startHour, startMinute),
                    icon = Icons.Default.Login,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClickTime = { startTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        startHour = now.get(Calendar.HOUR_OF_DAY)
                        startMinute = now.get(Calendar.MINUTE)
                        successMessage = null
                    }
                )

                TimeSelectorBox(
                    modifier = Modifier.weight(1f),
                    title = "SALIDA",
                    timeText = String.format(Locale.US, "%02d:%02d", endHour, endMinute),
                    icon = Icons.Default.Logout,
                    iconTint = AccentEmerald,
                    onClickTime = { endTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        endHour = now.get(Calendar.HOUR_OF_DAY)
                        endMinute = now.get(Calendar.MINUTE)
                        successMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Break / Almuerzo Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FreeBreakfast,
                    contentDescription = "Descanso",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Descanso / Almuerzo:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val breakOptions = listOf(0 to "0m", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h")
                breakOptions.forEach { (mins, label) ->
                    val isSelected = breakMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { breakMinutes = mins },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Duration and Earnings Preview Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tiempo neto trabajado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total a cobrar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val formattedEarnings = String.format(Locale.getDefault(), "%,.2f", estimatedEarnings)
                            Text(
                                text = "$currencySymbol$formattedEarnings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentEmerald
                            )
                        }
                    }

                    // Overtime details if applicable
                    if (overtimeHours > 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Horas extra",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Incluye ${String.format(Locale.US, "%.1f", overtimeHours)}h extra (+${((overtimeMultiplier - 1.0) * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Note Field
            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    successMessage = null
                },
                label = { Text("Nota o detalle (opcional)") },
                placeholder = { Text("Ej: Turno completo, horas extra...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Success feedback message
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = AccentEmerald,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Shift Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveShift(
                        startCal.timeInMillis,
                        endCal.timeInMillis,
                        hourlyRate,
                        breakMinutes,
                        if (enableOvertime) standardDailyHours else 0.0,
                        if (enableOvertime) overtimeMultiplier else 1.0,
                        noteText
                    )
                    successMessage = "✓ ¡Turno de ${hours}h ${minutes}m guardado con éxito!"
                    noteText = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GUARDAR TURNO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimeSelectorBox(
    title: String,
    timeText: String,
    icon: ImageVector,
    iconTint: Color,
    onClickTime: () -> Unit,
    onSetNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Ahora",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onSetNow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(onClick = onClickTime)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
