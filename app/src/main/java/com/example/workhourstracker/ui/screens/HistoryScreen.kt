package com.example.workhourstracker.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.data.WorkShift
import com.example.workhourstracker.ui.components.EditShiftDialog
import com.example.workhourstracker.ui.components.ManualShiftDialog
import com.example.workhourstracker.ui.theme.AccentCoral
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.AccentPurple
import com.example.workhourstracker.ui.viewmodel.WorkHoursViewModel
import java.util.Calendar
import java.util.Locale

enum class HistoryFilter {
    ALL,
    THIS_WEEK,
    THIS_MONTH
}

@Composable
fun HistoryScreen(
    viewModel: WorkHoursViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shifts by viewModel.allShifts.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val hourlyRate by viewModel.hourlyRate.collectAsState()

    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showManualShiftDialog by remember { mutableStateOf(false) }
    var shiftToEdit by remember { mutableStateOf<WorkShift?>(null) }
    var shiftToDelete by remember { mutableStateOf<WorkShift?>(null) }
    var showShareMenu by remember { mutableStateOf(false) }

    // Dialog for Manual Shift
    if (showManualShiftDialog) {
        ManualShiftDialog(
            defaultRate = hourlyRate,
            currencySymbol = currencySymbol,
            onDismiss = { showManualShiftDialog = false },
            onConfirm = { start, end, rate, note ->
                viewModel.addShift(startTimeMillis = start, endTimeMillis = end, hourlyRate = rate, note = note)
                showManualShiftDialog = false
            }
        )
    }

    // Dialog for Editing Shift
    shiftToEdit?.let { shift ->
        EditShiftDialog(
            shift = shift,
            currencySymbol = currencySymbol,
            onDismiss = { shiftToEdit = null },
            onConfirm = { updated ->
                viewModel.updateShift(updated)
                shiftToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    shiftToDelete?.let { shift ->
        AlertDialog(
            onDismissRequest = { shiftToDelete = null },
            title = { Text("Eliminar Registro") },
            text = { Text("¿Estás seguro de que deseas eliminar la jornada del ${shift.formattedShortDate()} (${shift.formattedDuration()})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteShift(shift)
                        shiftToDelete = null
                    }
                ) {
                    Text("Eliminar", color = AccentCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { shiftToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Filter shifts based on selection
    val filteredShifts = remember(shifts, selectedFilter) {
        val now = Calendar.getInstance()
        when (selectedFilter) {
            HistoryFilter.ALL -> shifts
            HistoryFilter.THIS_WEEK -> {
                val weekCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    if (firstDayOfWeek == Calendar.SUNDAY && get(Calendar.DAY_OF_WEEK) > now.get(Calendar.DAY_OF_WEEK)) {
                        add(Calendar.WEEK_OF_YEAR, -1)
                    }
                }
                val startOfWeek = weekCal.timeInMillis
                shifts.filter { it.startTimeMillis >= startOfWeek }
            }
            HistoryFilter.THIS_MONTH -> {
                val monthCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfMonth = monthCal.timeInMillis
                shifts.filter { it.startTimeMillis >= startOfMonth }
            }
        }
    }

    val totalHours = filteredShifts.sumOf { it.durationHours }
    val totalEarnings = filteredShifts.sumOf { it.totalEarnings }
    val filterLabel = when (selectedFilter) {
        HistoryFilter.ALL -> "Historial Completo"
        HistoryFilter.THIS_WEEK -> "Esta Semana"
        HistoryFilter.THIS_MONTH -> "Este Mes"
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showManualShiftDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir turno manual"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de Turnos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Share button with dropdown menu
                    Box {
                        IconButton(onClick = { showShareMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir reporte",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Compartir por WhatsApp / Texto") },
                                onClick = {
                                    showShareMenu = false
                                    shareReportText(
                                        context = context,
                                        report = viewModel.generateShareableReport(filteredShifts, filterLabel)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Exportar a CSV (Excel)") },
                                onClick = {
                                    showShareMenu = false
                                    val csvContent = viewModel.exportToCsv(filteredShifts)
                                    shareCsv(context = context, csvContent = csvContent, filterName = filterLabel)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ALL,
                        onClick = { selectedFilter = HistoryFilter.ALL },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.THIS_WEEK,
                        onClick = { selectedFilter = HistoryFilter.THIS_WEEK },
                        label = { Text("Esta Semana") }
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.THIS_MONTH,
                        onClick = { selectedFilter = HistoryFilter.THIS_MONTH },
                        label = { Text("Este Mes") }
                    )
                }
            }

            // Totals Banner for Current Filter
            item {
                val totalMinutes = (totalHours * 60).toInt()
                val h = totalMinutes / 60
                val m = totalMinutes % 60
                val formattedTotalEarnings = String.format(Locale.getDefault(), "%,.2f", totalEarnings)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HORAS ACUMULADAS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${h}h ${m}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL GENERADO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currencySymbol$formattedTotalEarnings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }

            // List of shifts
            if (filteredShifts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay jornadas registradas en este período.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredShifts, key = { it.id }) { shift ->
                    ShiftHistoryCard(
                        shift = shift,
                        currencySymbol = currencySymbol,
                        onEdit = { shiftToEdit = shift },
                        onDelete = { shiftToDelete = shift }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

@Composable
fun ShiftHistoryCard(
    shift: WorkShift,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (shift.overtimeHours > 0) AccentPurple else MaterialTheme.colorScheme.primary
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = shift.formattedDate(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Horario: ${shift.formattedTimeRange()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Duración: ${shift.formattedDuration()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (shift.overtimeHours > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Extras: ${String.format(Locale.US, "%.1f", shift.overtimeHours)}h (+${((shift.overtimeMultiplier - 1.0) * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (shift.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nota: ${shift.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val formattedEarnings = String.format(Locale.getDefault(), "%,.2f", shift.totalEarnings)
                    Text(
                        text = "$currencySymbol$formattedEarnings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentEmerald
                    )
                    val rateFormatted = if (shift.hourlyRate % 1.0 == 0.0) shift.hourlyRate.toInt().toString() else String.format(Locale.US, "%.2f", shift.hourlyRate)
                    Text(
                        text = "$currencySymbol$rateFormatted / hora",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun shareReportText(context: Context, report: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir reporte de horas")
    context.startActivity(shareIntent)
}

private fun shareCsv(context: Context, csvContent: String, filterName: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, "Reporte Horas - $filterName.csv")
        putExtra(Intent.EXTRA_TEXT, csvContent)
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Exportar CSV")
    context.startActivity(shareIntent)
}
