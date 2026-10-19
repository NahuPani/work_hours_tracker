package com.example.workhourstracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.data.WorkShift
import com.example.workhourstracker.ui.components.DirectShiftCard
import com.example.workhourstracker.ui.components.EarningsSummaryCard
import com.example.workhourstracker.ui.components.HourlyRateDialog
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.viewmodel.WorkHoursViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: WorkHoursViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hourlyRate by viewModel.hourlyRate.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val standardDailyHours by viewModel.standardDailyHours.collectAsState()
    val overtimeMultiplier by viewModel.overtimeMultiplier.collectAsState()
    val enableOvertime by viewModel.enableOvertime.collectAsState()
    val defaultBreakMinutes by viewModel.defaultBreakMinutes.collectAsState()
    val summary by viewModel.earningsSummary.collectAsState()
    val shifts by viewModel.allShifts.collectAsState()

    var showRateDialog by remember { mutableStateOf(false) }

    if (showRateDialog) {
        HourlyRateDialog(
            currentRate = hourlyRate,
            currentSymbol = currencySymbol,
            onDismiss = { showRateDialog = false },
            onConfirm = { newRate, newSymbol ->
                viewModel.updateHourlyRate(newRate, newSymbol)
                showRateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header: Date and Hourly Rate Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val dateFormatted = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
                        .format(Date())
                        .replaceFirstChar { it.uppercase() }

                    Text(
                        text = "Mi Jornada",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hourly Rate Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showRateDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Tarifa",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        val rateFormatted = if (hourlyRate % 1.0 == 0.0) hourlyRate.toInt().toString() else String.format(Locale.US, "%.2f", hourlyRate)
                        Text(
                            text = "$currencySymbol$rateFormatted/h",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Direct Shift Entry Card (Entrada / Salida directa)
        item {
            DirectShiftCard(
                hourlyRate = hourlyRate,
                currencySymbol = currencySymbol,
                standardDailyHours = standardDailyHours,
                overtimeMultiplier = overtimeMultiplier,
                enableOvertime = enableOvertime,
                defaultBreakMinutes = defaultBreakMinutes,
                onSaveShift = { startTime, endTime, rate, breakMin, stdHours, extraMultiplier, note ->
                    viewModel.addShift(
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        hourlyRate = rate,
                        breakMinutes = breakMin,
                        standardDailyHours = stdHours,
                        overtimeMultiplier = extraMultiplier,
                        note = note
                    )
                }
            )
        }

        // Summary Card
        item {
            EarningsSummaryCard(
                summary = summary,
                currencySymbol = currencySymbol
            )
        }

        // Recent Shifts Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Turnos Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (shifts.isNotEmpty()) {
                    TextButton(onClick = onNavigateToHistory) {
                        Text("Ver todos")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver historial",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (shifts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no has registrado ningún turno.\n¡Marca tu entrada arriba para comenzar!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(shifts.take(3), key = { it.id }) { shift ->
                RecentShiftItem(shift = shift, currencySymbol = currencySymbol)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RecentShiftItem(
    shift: WorkShift,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift.formattedShortDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = shift.formattedTimeRange(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (shift.note.isNotBlank()) {
                    Text(
                        text = shift.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val formattedAmount = String.format(Locale.getDefault(), "%,.2f", shift.totalEarnings)
                Text(
                    text = "$currencySymbol$formattedAmount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
                Text(
                    text = shift.formattedDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
