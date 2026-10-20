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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
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
import com.example.workhourstracker.ui.theme.AccentAmber
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
    startHour: Int = 8,
    startMinute: Int = 0,
    endHour: Int = 16,
    endMinute: Int = 0,
    dateMillis: Long = System.currentTimeMillis(),
    noteText: String = "",
    onStartTimeChange: (Int, Int) -> Unit = { _, _ -> },
    onEndTimeChange: (Int, Int) -> Unit = { _, _ -> },
    onDateChange: (Long) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
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

    // Current date state from draft
    val selectedCalendar = remember(dateMillis) {
        Calendar.getInstance().apply { timeInMillis = dateMillis }
    }

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
                onDateChange(newCal.timeInMillis)
                successMessage = null
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
                onStartTimeChange(hourOfDay, minute)
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
                onEndTimeChange(hourOfDay, minute)
                successMessage = null
            },
            endHour,
            endMinute,
            is24Hour
        )
    }

    // Calculate Milliseconds and Duration
    val startCal = remember(selectedCalendar, startHour, startMinute) {
        (selectedCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val endCal = remember(selectedCalendar, startCal, endHour, endMinute) {
        (selectedCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= startCal.timeInMillis) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    val grossDurationMillis = remember(startCal, endCal) {
        (endCal.timeInMillis - startCal.timeInMillis).coerceAtLeast(0)
    }
    val durationHours = remember(grossDurationMillis) { grossDurationMillis / 3600000.0 }
    val totalMinutes = remember(grossDurationMillis) { (grossDurationMillis / 60000).toInt() }
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    // Overtime calculation
    val isOvertimeActive = enableOvertime && standardDailyHours > 0.0 && overtimeMultiplier > 1.0
    val regularHours = remember(isOvertimeActive, durationHours, standardDailyHours) {
        if (isOvertimeActive) minOf(durationHours, standardDailyHours) else durationHours
    }
    val overtimeHours = remember(isOvertimeActive, durationHours, standardDailyHours) {
        if (isOvertimeActive) maxOf(0.0, durationHours - standardDailyHours) else 0.0
    }
    val estimatedEarnings = remember(regularHours, overtimeHours, hourlyRate, overtimeMultiplier) {
        (regularHours * hourlyRate) + (overtimeHours * (hourlyRate * overtimeMultiplier))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header: Compact Title & Date Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrar Turno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Date Chip
                val isToday = isSameDay(selectedCalendar, Calendar.getInstance())
                val dateLabel = if (isToday) {
                    "Hoy, " + SimpleDateFormat("d MMM", Locale("es", "ES")).format(selectedCalendar.time)
                } else {
                    SimpleDateFormat("d MMM", Locale("es", "ES")).format(selectedCalendar.time)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Connected Time Selector (Entrada -> Salida)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactTimeBox(
                    modifier = Modifier.weight(1f),
                    title = "ENTRADA",
                    timeText = String.format(Locale.US, "%02d:%02d", startHour, startMinute),
                    icon = Icons.AutoMirrored.Filled.Login,
                    accentColor = PrimaryBlue,
                    onClickTime = { startTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        onStartTimeChange(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
                        successMessage = null
                    }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )

                CompactTimeBox(
                    modifier = Modifier.weight(1f),
                    title = "SALIDA",
                    timeText = String.format(Locale.US, "%02d:%02d", endHour, endMinute),
                    icon = Icons.AutoMirrored.Filled.Logout,
                    accentColor = AccentEmerald,
                    onClickTime = { endTimePickerDialog.show() },
                    onSetNow = {
                        val now = Calendar.getInstance()
                        onEndTimeChange(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
                        successMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Compact Integrated Summary Bar (Duración + Ganancia proyectada)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration & Overtime
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (overtimeHours > 0.0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentAmber.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${String.format(Locale.US, "%.1f", overtimeHours)}h",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber
                            )
                        }
                    }
                }

                // Projected Total
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "A cobrar: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedMoneyText(
                        amount = estimatedEarnings,
                        currencySymbol = currencySymbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AccentEmerald,
                        decimals = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Compact Note Field
            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    onNoteChange(it)
                    successMessage = null
                },
                label = { Text("Nota (opcional)") },
                placeholder = { Text("Ej: Guardia, Turno tarde...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Success feedback animation
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = AccentEmerald,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Action Button: Guardar Jornada
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
                    android.widget.Toast.makeText(context, "✓ ¡Jornada guardada con éxito en el Historial!", android.widget.Toast.LENGTH_SHORT).show()
                    successMessage = "✓ ¡Turno de ${hours}h ${minutes}m guardado!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GUARDAR JORNADA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun CompactTimeBox(
    modifier: Modifier = Modifier,
    title: String,
    timeText: String,
    icon: ImageVector,
    accentColor: Color,
    onClickTime: () -> Unit,
    onSetNow: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClickTime),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            // Mini Header: Icon + Title + "Ahora"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                // Quick "Ahora" chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .clickable(onClick = onSetNow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ahora",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time Display (Big & Centered)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeText,
                    fontSize = 24.sp,
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
