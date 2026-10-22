package com.example.workhourstracker.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.workhourstracker.data.WorkShift
import com.example.workhourstracker.ui.components.AnimatedMoneyText
import com.example.workhourstracker.ui.components.EditShiftDialog
import com.example.workhourstracker.ui.components.ManualShiftDialog
import com.example.workhourstracker.ui.theme.AccentCoral
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.AccentPurple
import com.example.workhourstracker.ui.viewmodel.DaySettlement
import com.example.workhourstracker.ui.viewmodel.WeeklySettlement
import com.example.workhourstracker.ui.viewmodel.WorkHoursViewModel
import java.util.Calendar
import java.util.Locale

enum class HistoryViewMode {
    BY_WEEKS,
    BY_SHIFTS
}

enum class HistoryFilter {
    ALL,
    THIS_WEEK,
    THIS_MONTH,
    FIRST_FORTNIGHT,
    SECOND_FORTNIGHT,
    PREVIOUS_MONTH
}

@Composable
fun HistoryScreen(
    viewModel: WorkHoursViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val shifts by viewModel.allShifts.collectAsState()
    val weeklySettlements by viewModel.weeklySettlements.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val hourlyRate by viewModel.hourlyRate.collectAsState()
    val startDayOfWeek by viewModel.startDayOfWeek.collectAsState()

    var currentViewMode by remember { mutableStateOf(HistoryViewMode.BY_WEEKS) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showManualShiftDialog by remember { mutableStateOf(false) }
    var shiftToEdit by remember { mutableStateOf<WorkShift?>(null) }
    var shiftToDelete by remember { mutableStateOf<WorkShift?>(null) }
    var showShareMenu by remember { mutableStateOf(false) }

    val onEditShift: (WorkShift) -> Unit = remember { { shift -> shiftToEdit = shift } }
    val onDeleteShift: (WorkShift) -> Unit = remember { { shift -> shiftToDelete = shift } }

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

    // Filter shifts based on selection (for By-Shifts mode)
    val filteredShifts = remember(shifts, selectedFilter, startDayOfWeek) {
        when (selectedFilter) {
            HistoryFilter.ALL -> shifts
            HistoryFilter.THIS_WEEK -> {
                val weekCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    val currentDay = get(Calendar.DAY_OF_WEEK)
                    val diff = (currentDay - startDayOfWeek + 7) % 7
                    add(Calendar.DAY_OF_MONTH, -diff)
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
            HistoryFilter.FIRST_FORTNIGHT -> {
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 15)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                shifts.filter { it.startTimeMillis in startCal.timeInMillis..endCal.timeInMillis }
            }
            HistoryFilter.SECOND_FORTNIGHT -> {
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 16)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val nextMonthCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                shifts.filter { it.startTimeMillis in startCal.timeInMillis until nextMonthCal.timeInMillis }
            }
            HistoryFilter.PREVIOUS_MONTH -> {
                val startPrevCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startCurrCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                shifts.filter { it.startTimeMillis in startPrevCal.timeInMillis until startCurrCal.timeInMillis }
            }
        }
    }

    val totalHours = remember(filteredShifts) { filteredShifts.sumOf { it.durationHours } }
    val totalEarnings = remember(filteredShifts) { filteredShifts.sumOf { it.totalEarnings } }
    val filterLabel = remember(selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> "Historial Completo"
            HistoryFilter.THIS_WEEK -> "Esta Semana"
            HistoryFilter.THIS_MONTH -> "Este Mes"
            HistoryFilter.FIRST_FORTNIGHT -> "1ª Quincena (1-15)"
            HistoryFilter.SECOND_FORTNIGHT -> "2ª Quincena (16-fin)"
            HistoryFilter.PREVIOUS_MONTH -> "Mes Anterior"
        }
    }

    val allWeeksHours = remember(weeklySettlements) { weeklySettlements.sumOf { it.totalHours } }
    val allWeeksEarnings = remember(weeklySettlements) { weeklySettlements.sumOf { it.totalEarnings } }

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
                        text = "Historial",
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
                                text = { Text("Exportar a PDF (Documento oficial)") },
                                onClick = {
                                    showShareMenu = false
                                    val result = com.example.workhourstracker.util.PdfReportGenerator.generateReportPdf(
                                        context = context,
                                        shifts = filteredShifts,
                                        periodTitle = filterLabel,
                                        currencySymbol = currencySymbol
                                    )
                                    result.onSuccess { pdfFile ->
                                        sharePdf(context, pdfFile, filterLabel)
                                    }.onFailure { err ->
                                        android.widget.Toast.makeText(context, "Error al generar PDF: ${err.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
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

                // Mode Selector Toggle: "Por Semanas" vs "Por Turnos"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentViewMode == HistoryViewMode.BY_WEEKS) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentViewMode = HistoryViewMode.BY_WEEKS
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ViewWeek,
                                contentDescription = null,
                                tint = if (currentViewMode == HistoryViewMode.BY_WEEKS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Por Semanas",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentViewMode == HistoryViewMode.BY_WEEKS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentViewMode == HistoryViewMode.BY_SHIFTS) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentViewMode = HistoryViewMode.BY_SHIFTS
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (currentViewMode == HistoryViewMode.BY_SHIFTS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Por Turnos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentViewMode == HistoryViewMode.BY_SHIFTS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // If in BY_SHIFTS mode, show filter chips
                if (currentViewMode == HistoryViewMode.BY_SHIFTS) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.ALL,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.ALL
                            },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.THIS_WEEK,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.THIS_WEEK
                            },
                            label = { Text("Esta Semana") }
                        )
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.THIS_MONTH,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.THIS_MONTH
                            },
                            label = { Text("Este Mes") }
                        )
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.FIRST_FORTNIGHT,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.FIRST_FORTNIGHT
                            },
                            label = { Text("1ª Quincena (1-15)") }
                        )
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.SECOND_FORTNIGHT,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.SECOND_FORTNIGHT
                            },
                            label = { Text("2ª Quincena (16-fin)") }
                        )
                        FilterChip(
                            selected = selectedFilter == HistoryFilter.PREVIOUS_MONTH,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedFilter = HistoryFilter.PREVIOUS_MONTH
                            },
                            label = { Text("Mes Pasado") }
                        )
                    }
                }
            }

            // Totals Banner
            item {
                val bannerHours = if (currentViewMode == HistoryViewMode.BY_WEEKS) allWeeksHours else totalHours
                val bannerEarnings = if (currentViewMode == HistoryViewMode.BY_WEEKS) allWeeksEarnings else totalEarnings

                val totalMinutes = (bannerHours * 60).toInt()
                val h = totalMinutes / 60
                val m = totalMinutes % 60

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
                                text = if (currentViewMode == HistoryViewMode.BY_WEEKS) "SEMANAS REGISTRADAS: ${weeklySettlements.size}" else "HORAS ACUMULADAS",
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
                            AnimatedMoneyText(
                                amount = bannerEarnings,
                                currencySymbol = currencySymbol,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = AccentEmerald,
                                decimals = 2
                            )
                        }
                    }
                }
            }

            // List of content depending on view mode
            if (currentViewMode == HistoryViewMode.BY_WEEKS) {
                if (weeklySettlements.isEmpty()) {
                    item {
                        EmptyHistoryPlaceholder(onAddShift = { showManualShiftDialog = true })
                    }
                } else {
                    items(weeklySettlements, key = { it.weekId }) { settlement ->
                        WeeklySettlementCard(
                            settlement = settlement,
                            currencySymbol = currencySymbol,
                            onEditShift = onEditShift,
                            onDeleteShift = onDeleteShift,
                            onExportPdf = {
                                val result = com.example.workhourstracker.util.PdfReportGenerator.generateReportPdf(
                                    context = context,
                                    shifts = settlement.shifts,
                                    periodTitle = "Semana: ${settlement.weekRangeLabel}",
                                    currencySymbol = currencySymbol
                                )
                                result.onSuccess { pdfFile ->
                                    sharePdf(context, pdfFile, settlement.weekRangeLabel)
                                }.onFailure { err ->
                                    android.widget.Toast.makeText(context, "Error al generar PDF: ${err.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShareWhatsapp = {
                                shareReportText(
                                    context = context,
                                    report = viewModel.generateShareableReport(settlement.shifts, "Semana: ${settlement.weekRangeLabel}")
                                )
                            }
                        )
                    }
                }
            } else {
                if (filteredShifts.isEmpty()) {
                    item {
                        EmptyHistoryPlaceholder(onAddShift = { showManualShiftDialog = true })
                    }
                } else {
                    items(filteredShifts, key = { it.id }) { shift ->
                        ShiftHistoryCard(
                            shift = shift,
                            currencySymbol = currencySymbol,
                            onEdit = onEditShift,
                            onDelete = onDeleteShift
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

@Composable
fun WeeklySettlementCard(
    settlement: WeeklySettlement,
    currencySymbol: String,
    onEditShift: (WorkShift) -> Unit,
    onDeleteShift: (WorkShift) -> Unit,
    onExportPdf: () -> Unit,
    onShareWhatsapp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(settlement.isCurrentWeek) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "accordionChevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (settlement.isCurrentWeek) AccentEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (settlement.isCurrentWeek) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header / Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (settlement.isCurrentWeek) AccentEmerald.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (settlement.isCurrentWeek) Icons.Default.DateRange else Icons.Default.ViewWeek,
                            contentDescription = null,
                            tint = if (settlement.isCurrentWeek) AccentEmerald else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = settlement.weekRangeLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (settlement.isCurrentWeek) AccentEmerald.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (settlement.isCurrentWeek) "En curso" else "Cerrada",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settlement.isCurrentWeek) AccentEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            val totalMin = (settlement.totalHours * 60).toInt()
                            val h = totalMin / 60
                            val m = totalMin % 60
                            Text(
                                text = "${h}h ${m}m • ${settlement.daysCount} días",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (settlement.overtimeHours > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${String.format(Locale.US, "%.1f", settlement.overtimeHours)}h",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedMoneyText(
                            amount = settlement.totalEarnings,
                            currencySymbol = currencySymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = AccentEmerald,
                            decimals = 2
                        )
                        Text(
                            text = "${settlement.shiftsCount} turnos",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Contraer" else "Expandir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            }

            // Expanded content (Accordion body)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick actions for this week: PDF & WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportPdf,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PDF Semanal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onShareWhatsapp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AccentEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title of breakdown
                    Text(
                        text = "DESGLOSE DÍA POR DÍA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Daily breakdown items
                    if (settlement.daysWithActivity.isEmpty()) {
                        Text(
                            text = "Aún no hay turnos registrados en esta semana.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        settlement.daysWithActivity.forEach { daySettlement ->
                            DaySettlementSection(
                                daySettlement = daySettlement,
                                currencySymbol = currencySymbol,
                                onEditShift = onEditShift,
                                onDeleteShift = onDeleteShift
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaySettlementSection(
    daySettlement: DaySettlement,
    currencySymbol: String,
    onEditShift: (WorkShift) -> Unit,
    onDeleteShift: (WorkShift) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = daySettlement.dayTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val formattedEarnings = String.format(Locale.getDefault(), "%,.2f", daySettlement.totalEarnings)
                    Text(
                        text = "$currencySymbol$formattedEarnings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = AccentEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val dMin = (daySettlement.totalHours * 60).toInt()
                    val dh = dMin / 60
                    val dm = dMin % 60
                    Text(
                        text = "(${dh}h ${dm}m)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shifts for this day
            daySettlement.shifts.forEach { shift ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .clickable { onEditShift(shift) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shift.formattedTimeRange(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${shift.formattedDuration()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (shift.overtimeHours > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1fh", shift.regularHours)} norm + ${String.format(Locale.US, "%.1fh", shift.overtimeHours)} extra",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }

                        if (shift.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = shift.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onEditShift(shift) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteShift(shift) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar",
                                tint = AccentCoral.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder(onAddShift: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Weekend,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Todo tranquilo por acá",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No hay jornadas registradas en este período. ¡Disfrutá tu descanso o tocá abajo para registrar un turno!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = onAddShift,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Añadir turno manual", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShiftHistoryCard(
    shift: WorkShift,
    currencySymbol: String,
    onEdit: (WorkShift) -> Unit,
    onDelete: (WorkShift) -> Unit,
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
            .clickable { onEdit(shift) },
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
                        onClick = { onEdit(shift) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar turno",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(shift) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar turno",
                            tint = AccentCoral.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = shift.formattedTimeRange(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = shift.formattedDuration(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (shift.overtimeHours > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Horas extra: ${String.format(Locale.US, "%.1f", shift.overtimeHours)}h (${shift.overtimeMultiplier}x)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                    }

                    if (shift.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = shift.note,
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
    try {
        val fileName = "Reporte_Horas_${filterName.replace(" ", "_")}.csv"
        val file = java.io.File(context.cacheDir, fileName)
        file.writeText(csvContent)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar CSV")
        context.startActivity(shareIntent)
    } catch (_: Exception) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Reporte Horas - $filterName.csv")
            putExtra(Intent.EXTRA_TEXT, csvContent)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar CSV")
        context.startActivity(shareIntent)
    }
}

private fun sharePdf(context: Context, file: java.io.File, filterName: String) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar Reporte PDF ($filterName)")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No se pudo compartir el PDF: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}
