package com.example.workhourstracker.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workhourstracker.ui.components.HourlyRateDialog
import com.example.workhourstracker.ui.components.OvertimeDialog
import com.example.workhourstracker.ui.components.RestoreDialog
import com.example.workhourstracker.ui.components.UpdateAvailableDialog
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.viewmodel.WorkHoursViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: WorkHoursViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hourlyRate by viewModel.hourlyRate.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val standardDailyHours by viewModel.standardDailyHours.collectAsState()
    val overtimeMultiplier by viewModel.overtimeMultiplier.collectAsState()
    val enableOvertime by viewModel.enableOvertime.collectAsState()

    val summary by viewModel.earningsSummary.collectAsState()
    val shifts by viewModel.allShifts.collectAsState()

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()

    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    val startDayOfWeek by viewModel.startDayOfWeek.collectAsState()

    var showRateDialog by remember { mutableStateOf(false) }
    var showOvertimeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDayOfWeekDialog by remember { mutableStateOf(false) }

    if (showDayOfWeekDialog) {
        val days = listOf(
            Calendar.WEDNESDAY to "Miércoles (Mié a Mar)",
            Calendar.MONDAY to "Lunes (Lun a Dom)",
            Calendar.SUNDAY to "Domingo (Dom a Sáb)",
            Calendar.THURSDAY to "Jueves (Jue a Mié)",
            Calendar.FRIDAY to "Viernes (Vie a Jue)",
            Calendar.SATURDAY to "Sábado (Sáb a Vie)",
            Calendar.TUESDAY to "Martes (Mar a Lun)"
        )
        AlertDialog(
            onDismissRequest = { showDayOfWeekDialog = false },
            title = { Text("Inicio de Semana Laboral") },
            text = {
                Column {
                    Text(
                        text = "Elige qué día comienza tu ciclo de trabajo/pago para calcular totales y gráficos:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    days.forEach { (calDay, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateStartDayOfWeek(calDay)
                                    showDayOfWeekDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = startDayOfWeek == calDay,
                                onClick = {
                                    viewModel.updateStartDayOfWeek(calDay)
                                    showDayOfWeekDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (startDayOfWeek == calDay) FontWeight.Bold else FontWeight.Normal,
                                color = if (startDayOfWeek == calDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDayOfWeekDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showUpdateDialog && updateInfo != null && updateInfo!!.isUpdateAvailable) {
        UpdateAvailableDialog(
            updateInfo = updateInfo!!,
            onDismiss = {
                showUpdateDialog = false
                viewModel.dismissUpdate()
            }
        )
    }

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

    if (showOvertimeDialog) {
        OvertimeDialog(
            currentStandardHours = standardDailyHours,
            currentMultiplier = overtimeMultiplier,
            currentEnabled = enableOvertime,
            onDismiss = { showOvertimeDialog = false },
            onConfirm = { stdHours, mult, enabled ->
                viewModel.updateOvertimeSettings(stdHours, mult, enabled)
                showOvertimeDialog = false
            }
        )
    }

    if (showRestoreDialog) {
        RestoreDialog(
            onDismiss = { showRestoreDialog = false },
            onConfirm = { json, replaceExisting ->
                coroutineScope.launch {
                    val result = viewModel.restoreBackupJson(json, replaceExisting)
                    showRestoreDialog = false
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        val msg = if (replaceExisting) {
                            "✓ ¡Copia restaurada! ($count turnos importados)"
                        } else {
                            "✓ ¡$count turnos nuevos importados sin duplicar!"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al restaurar: archivo inválido", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Section: Configuración de Ganancias y Horas
        item {
            Text(
                text = "Tarifas y Jornada",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    val rateFormatted = if (hourlyRate % 1.0 == 0.0) hourlyRate.toInt().toString() else String.format(Locale.US, "%.2f", hourlyRate)
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Pago por Hora",
                        subtitle = "$currencySymbol$rateFormatted por hora normal",
                        onClick = { showRateDialog = true }
                    )

                    SettingsItem(
                        icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.TrendingUp,
                        title = "Horas Extras",
                        subtitle = if (enableOvertime) {
                            "Jornada de ${standardDailyHours.toInt()}h (+${((overtimeMultiplier - 1.0) * 100).toInt()}% en extras)"
                        } else {
                            "Desactivadas"
                        },
                        onClick = { showOvertimeDialog = true }
                    )

                    val dayNameLabel = when (startDayOfWeek) {
                        Calendar.WEDNESDAY -> "Miércoles (Mié a Mar)"
                        Calendar.MONDAY -> "Lunes (Lun a Dom)"
                        Calendar.SUNDAY -> "Domingo (Dom a Sáb)"
                        Calendar.THURSDAY -> "Jueves (Jue a Mié)"
                        Calendar.FRIDAY -> "Viernes (Vie a Jue)"
                        Calendar.SATURDAY -> "Sábado (Sáb a Vie)"
                        Calendar.TUESDAY -> "Martes (Mar a Lun)"
                        else -> "Miércoles (Mié a Mar)"
                    }

                    SettingsItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Inicio de Semana Laboral",
                        subtitle = "Ciclo semanal: $dayNameLabel",
                        onClick = { showDayOfWeekDialog = true }
                    )
                }
            }
        }

        // Section: Resumen General Histórico
        item {
            Text(
                text = "Estadísticas Globales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val totalMinutes = (summary.totalHours * 60).toInt()
                    val totalH = totalMinutes / 60
                    val totalM = totalMinutes % 60
                    val formattedEarnings = String.format(Locale.getDefault(), "%,.2f", summary.totalEarnings)

                    val extraHours = shifts.sumOf { it.overtimeHours }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total de turnos:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${shifts.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Horas totales acumuladas:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${totalH}h ${totalM}m", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    if (extraHours > 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Horas extras incluidas:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format(Locale.US, "%.1f", extraHours)}h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ganancias históricas:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol$formattedEarnings", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = AccentEmerald)
                    }
                }
            }
        }

        // Section: Copia de Seguridad y Restauración
        item {
            Text(
                text = "Copia de Seguridad y Datos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "Exportar Copia de Seguridad",
                        subtitle = "Guarda o envía tus registros en archivo JSON",
                        onClick = {
                            val backupJson = viewModel.exportBackupJson()
                            shareBackupText(context, backupJson)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Restore,
                        title = "Restaurar Copia de Seguridad",
                        subtitle = "Importa registros desde un respaldo previo",
                        onClick = { showRestoreDialog = true }
                    )
                }
            }
        }

        // Section: Privacidad y Almacenamiento
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacidad",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "Base de datos 100% local y privada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tus registros se guardan únicamente en tu teléfono (Room SQLite), sin publicidad, sin servidores externos y con total privacidad.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Actualizaciones
        item {
            Text(
                text = "Actualizaciones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                SettingsItem(
                    icon = Icons.Default.SystemUpdate,
                    title = "Buscar Actualizaciones",
                    subtitle = if (isCheckingUpdate) "Consultando GitHub..." else "Versión instalada: v$appVersion • Tocar para comprobar",
                    onClick = {
                        viewModel.checkForUpdates { info ->
                            if (info == null) {
                                Toast.makeText(context, "No se pudo conectar con GitHub. Verificá tu conexión.", Toast.LENGTH_SHORT).show()
                            } else if (info.isUpdateAvailable) {
                                showUpdateDialog = true
                            } else {
                                Toast.makeText(context, "✓ ¡Tu aplicación ya está actualizada a la última versión (v${info.currentVersion})!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }

        // Section: Info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Mi Turno v$appVersion • Jetpack Compose & Material 3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ver más",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareBackupText(context: Context, backupJson: String) {
    try {
        val fileName = "Mi_Turno_Backup.json"
        val file = java.io.File(context.cacheDir, fileName)
        file.writeText(backupJson)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/json"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Guardar o compartir copia de seguridad")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Fallback to text sharing if FileProvider is not configured
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Mi Turno.json")
            putExtra(Intent.EXTRA_TEXT, backupJson)
            type = "application/json"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Guardar o compartir copia de seguridad")
        context.startActivity(shareIntent)
    }
}
