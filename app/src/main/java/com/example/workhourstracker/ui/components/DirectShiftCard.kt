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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentAmber
import com.example.workhourstracker.ui.theme.AccentCyan
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.PrimaryBlue
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
    onSaveShift: (
        startTimeMillis: Long,
        endTimeMillis: Long,
        hourlyRate: Double,
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

    // Default start at 08:00 and end at 16:00
    var startHour by remember { mutableIntStateOf(8) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(16) }
    var endMinute by remember { mutableIntStateOf(0) }

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

    // Time Pickers
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

    // Calculate Milliseconds and Duration
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
        if (timeInMillis <= startCal.timeInMillis) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val grossDurationMillis = (endCal.timeInMillis - startCal.timeInMillis).coerceAtLeast(0)
    val durationHours = grossDurationMillis / 3600000.0
    val totalMinutes = (grossDurationMillis / 60000).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    // Overtime calculation
    val isOvertimeActive = enableOvertime && standardDailyHours > 0.0 && overtimeMultiplier > 1.0
    val regularHours = if (isOvertimeActive) minOf(durationHours, standardDailyHours) else durationHours
    val overtimeHours = if (isOvertimeActive) maxOf(0.0, durationHours - standardDailyHours) else 0.0
    val estimatedEarnings = (regularHours * hourlyRate) + (overtimeHours * (hourlyRate * overtimeMultiplier))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header: Title & Interactive Date Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Registrar Turno",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Selecciona tus horarios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date Chip
                val isToday = isSameDay(selectedCalendar, Calendar.getInstance())
                val dateLabel = if (isToday) {
                    "Hoy, " + SimpleDateFormat("d MMM", Locale("es", "ES")).format(selectedCalendar.time)
                } else {
                    SimpleDateFormat("d MMM yyyy", Locale("es", "ES")).format(selectedCalendar.time)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Selector Boxes (Entrada & Salida side-by-side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ModernTimeBox(
                    modifier = Modifier.weight(1f),
                    title = "ENTRADA",
                    timeText = String.format(Locale.US, "%02d:%02d", startHour, startMinute),
                    icon = Icons.Default.Login,
                    accentColor = PrimaryBlue,
                    onClickTime = { startTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        startHour = now.get(Calendar.HOUR_OF_DAY)
                        startMinute = now.get(Calendar.MINUTE)
                        successMessage = null
                    }
                )

                ModernTimeBox(
                    modifier = Modifier.weight(1f),
                    title = "SALIDA",
                    timeText = String.format(Locale.US, "%02d:%02d", endHour, endMinute),
                    icon = Icons.Default.Logout,
                    accentColor = AccentEmerald,
                    onClickTime = { endTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        endHour = now.get(Calendar.HOUR_OF_DAY)
                        endMinute = now.get(Calendar.MINUTE)
                        successMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hero Live Preview Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tiempo total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total a cobrar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val formattedEarnings = String.format(Locale.getDefault(), "%,.2f", estimatedEarnings)
                            Text(
                                text = "$currencySymbol$formattedEarnings",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = AccentEmerald
                            )
                        }
                    }

                    // Overtime badge if applicable
                    if (overtimeHours > 0.0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Horas extras",
                                tint = AccentAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Incluye ${String.format(Locale.US, "%.1f", overtimeHours)}h extra (+${((overtimeMultiplier - 1.0) * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean Note TextField
            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    successMessage = null
                },
                label = { Text("Nota o detalle (opcional)") },
                placeholder = { Text("Ej: Turno tarde, guardia...") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Success feedback
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Save Shift Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveShift(
                        startCal.timeInMillis,
                        endCal.timeInMillis,
                        hourlyRate,
                        if (enableOvertime) standardDailyHours else 0.0,
                        if (enableOvertime) overtimeMultiplier else 1.0,
                        noteText
                    )
                    successMessage = "✓ ¡Turno de ${hours}h ${minutes}m guardado con éxito!"
                    noteText = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GUARDAR JORNADA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ModernTimeBox(
    title: String,
    timeText: String,
    icon: ImageVector,
    accentColor: Color,
    onClickTime: () -> Unit,
    onSetNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClickTime),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick "Ahora" chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .clickable(onClick = onSetNow)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Ahora",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
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
