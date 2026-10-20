package com.example.workhourstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentEmerald
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BackupPreviewData(
    val version: Int,
    val exportDateMillis: Long,
    val shiftCount: Int,
    val hourlyRate: Double,
    val currencySymbol: String,
    val startDayOfWeek: Int
)

@Composable
fun RestoreDialog(
    initialJson: String? = null,
    onDismiss: () -> Unit,
    onPickFileClick: () -> Unit,
    onConfirm: (json: String, replaceExisting: Boolean) -> Unit
) {
    var rawJson by remember { mutableStateOf(initialJson ?: "") }
    var replaceExisting by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewData by remember { mutableStateOf<BackupPreviewData?>(null) }

    fun parseJson(content: String): BackupPreviewData? {
        return try {
            val json = JSONObject(content.trim())
            val shiftsArray = json.optJSONArray("shifts")
            BackupPreviewData(
                version = json.optInt("version", 1),
                exportDateMillis = json.optLong("exportDate", 0L),
                shiftCount = shiftsArray?.length() ?: 0,
                hourlyRate = json.optDouble("hourlyRate", 0.0),
                currencySymbol = json.optString("currencySymbol", "$"),
                startDayOfWeek = json.optInt("startDayOfWeek", Calendar.WEDNESDAY)
            )
        } catch (_: Exception) {
            null
        }
    }

    LaunchedEffect(initialJson) {
        if (!initialJson.isNullOrBlank()) {
            rawJson = initialJson
            val parsed = parseJson(initialJson)
            if (parsed != null) {
                previewData = parsed
                errorMessage = null
            } else {
                errorMessage = "El archivo no contiene un formato de respaldo válido."
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (previewData != null) "Vista Previa de Respaldo" else "Restaurar Copia de Seguridad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (previewData == null) {
                    Text(
                        text = "Selecciona el archivo .json que guardaste previamente para restaurar tus turnos y tarifas:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Big native picker button
                    Button(
                        onClick = onPickFileClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elegir archivo .json", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { showManualInput = !showManualInput },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(if (showManualInput) "Ocultar texto manual" else "O pegar texto JSON manualmente")
                    }

                    AnimatedVisibility(visible = showManualInput) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = rawJson,
                                onValueChange = {
                                    rawJson = it
                                    errorMessage = null
                                    val parsed = parseJson(it)
                                    if (parsed != null) {
                                        previewData = parsed
                                    }
                                },
                                label = { Text("Contenido JSON") },
                                placeholder = { Text("{\"version\": 3, ...}") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Preview card
                    val data = previewData!!
                    val dateFormatted = if (data.exportDateMillis > 0) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        sdf.format(Date(data.exportDateMillis))
                    } else {
                        "Fecha desconocida"
                    }

                    val cycleName = when (data.startDayOfWeek) {
                        Calendar.MONDAY -> "Lun a Dom"
                        Calendar.TUESDAY -> "Mar a Lun"
                        Calendar.WEDNESDAY -> "Mié a Mar"
                        Calendar.THURSDAY -> "Jue a Mié"
                        Calendar.FRIDAY -> "Vie a Jue"
                        Calendar.SATURDAY -> "Sáb a Vie"
                        else -> "Dom a Sáb"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Archivo verificado correctamente", fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Turnos encontrados:", style = MaterialTheme.typography.bodyMedium)
                                Text("${data.shiftCount} jornadas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tarifa guardada:", style = MaterialTheme.typography.bodyMedium)
                                Text("${data.currencySymbol}${data.hourlyRate.toInt()} / h", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ciclo semanal:", style = MaterialTheme.typography.bodyMedium)
                                Text(cycleName, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fecha de creación:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateFormatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Merge vs Replace mode selection
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = replaceExisting,
                            onCheckedChange = { replaceExisting = it }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = if (replaceExisting) "Reemplazar todo" else "Fusionar (sin duplicados)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (replaceExisting) "Borrará los turnos actuales antes de restaurar." else "Mantiene tus turnos actuales y solo suma los faltantes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = {
                            previewData = null
                            rawJson = ""
                            errorMessage = null
                            onPickFileClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Elegir otro archivo")
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (previewData != null) {
                Button(
                    onClick = {
                        onConfirm(rawJson.trim(), replaceExisting)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text("✓ Confirmar y Restaurar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
