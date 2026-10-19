package com.example.workhourstracker.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.data.WorkShift
import com.example.workhourstracker.ui.theme.AccentEmerald
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditShiftDialog(
    shift: WorkShift,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (updatedShift: WorkShift) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    val initialStartCal = Calendar.getInstance().apply { timeInMillis = shift.startTimeMillis }
    val initialEndCal = Calendar.getInstance().apply { timeInMillis = shift.endTimeMillis }

    var selectedCalendar by remember { mutableStateOf(initialStartCal) }
    var startHour by remember { mutableIntStateOf(initialStartCal.get(Calendar.HOUR_OF_DAY)) }
    var startMinute by remember { mutableIntStateOf(initialStartCal.get(Calendar.MINUTE)) }
    var endHour by remember { mutableIntStateOf(initialEndCal.get(Calendar.HOUR_OF_DAY)) }
    var endMinute by remember { mutableIntStateOf(initialEndCal.get(Calendar.MINUTE)) }
    var breakMinutes by remember { mutableIntStateOf(shift.breakMinutes) }

    var rateInput by remember {
        mutableStateOf(
            if (shift.hourlyRate % 1.0 == 0.0) shift.hourlyRate.toInt().toString() else shift.hourlyRate.toString()
        )
    }
    var noteInput by remember { mutableStateOf(shift.note) }

    // Date Picker
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

    val startTimePicker = remember(startHour, startMinute) {
        TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, is24Hour)
    }

    val endTimePicker = remember(endHour, endMinute) {
        TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, is24Hour)
    }

    // Calculations
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

    val grossMillis = (endCal.timeInMillis - startCal.timeInMillis).coerceAtLeast(0)
    val netMillis = (grossMillis - breakMinutes * 60000L).coerceAtLeast(0)
    val netHours = netMillis / 3600000.0
    val totalMins = (netMillis / 60000).toInt()
    val hours = totalMins / 60
    val minutes = totalMins % 60

    val currentRate = rateInput.toDoubleOrNull() ?: shift.hourlyRate
    val isOvertime = shift.standardDailyHours > 0.0 && shift.overtimeMultiplier > 1.0
    val regH = if (isOvertime) minOf(netHours, shift.standardDailyHours) else netHours
    val extraH = if (isOvertime) maxOf(0.0, netHours - shift.standardDailyHours) else 0.0
    val estimatedEarnings = (regH * currentRate) + (extraH * currentRate * shift.overtimeMultiplier)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Turno",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Date Chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fecha: ${SimpleDateFormat("EEEE, d MMM yyyy", Locale("es", "ES")).format(selectedCalendar.time)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Cambiar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeBox(
                        title = "ENTRADA",
                        timeText = String.format(Locale.US, "%02d:%02d", startHour, startMinute),
                        icon = Icons.Default.Login,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { startTimePicker.show() },
                        modifier = Modifier.weight(1f)
                    )

                    TimeBox(
                        title = "SALIDA",
                        timeText = String.format(Locale.US, "%02d:%02d", endHour, endMinute),
                        icon = Icons.Default.Logout,
                        iconTint = AccentEmerald,
                        onClick = { endTimePicker.show() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Break Options
                Text(
                    text = "Descanso / Almuerzo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "0m", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h").forEach { (mins, label) ->
                        FilterChip(
                            selected = breakMinutes == mins,
                            onClick = { breakMinutes = mins },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hourly Rate Input
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Tarifa por hora ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note Input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Total Calculation Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total neto:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("A cobrar:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", estimatedEarnings)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rate = rateInput.toDoubleOrNull() ?: shift.hourlyRate
                    val updated = shift.copy(
                        startTimeMillis = startCal.timeInMillis,
                        endTimeMillis = endCal.timeInMillis,
                        hourlyRate = rate,
                        breakMinutes = breakMinutes,
                        note = noteInput.trim()
                    )
                    onConfirm(updated)
                }
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun TimeBox(
    title: String,
    timeText: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
