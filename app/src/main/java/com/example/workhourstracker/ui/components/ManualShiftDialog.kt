package com.example.workhourstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ManualShiftDialog(
    defaultRate: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (startTime: Long, endTime: Long, rate: Double, note: String) -> Unit
) {
    val today = Calendar.getInstance()
    var dateString by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today.time))
    }
    var startTimeString by remember { mutableStateOf("09:00") }
    var endTimeString by remember { mutableStateOf("17:00") }
    var rateInput by remember {
        mutableStateOf(if (defaultRate % 1.0 == 0.0) defaultRate.toInt().toString() else defaultRate.toString())
    }
    var noteInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Turno Manual",
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
                Text(
                    text = "Añade una jornada laboral que no hayas registrado en tiempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    placeholder = { Text("Ej: 18/10/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startTimeString,
                        onValueChange = { startTimeString = it },
                        label = { Text("Entrada (HH:mm)") },
                        placeholder = { Text("09:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endTimeString,
                        onValueChange = { endTimeString = it },
                        label = { Text("Salida (HH:mm)") },
                        placeholder = { Text("17:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Pago por hora ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Ej: Jornada completa, extras") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val dateParts = dateString.split("/")
                        if (dateParts.size != 3) {
                            errorMessage = "Formato de fecha inválido. Usa dd/MM/yyyy"
                            return@TextButton
                        }
                        val day = dateParts[0].toInt()
                        val month = dateParts[1].toInt() - 1
                        val year = dateParts[2].toInt()

                        val startParts = startTimeString.split(":")
                        val endParts = endTimeString.split(":")
                        if (startParts.size != 2 || endParts.size != 2) {
                            errorMessage = "Formato de hora inválido. Usa HH:mm"
                            return@TextButton
                        }

                        val calStart = Calendar.getInstance().apply {
                            set(year, month, day, startParts[0].toInt(), startParts[1].toInt(), 0)
                        }

                        val calEnd = Calendar.getInstance().apply {
                            set(year, month, day, endParts[0].toInt(), endParts[1].toInt(), 0)
                        }

                        if (calEnd.timeInMillis <= calStart.timeInMillis) {
                            errorMessage = "La hora de salida debe ser posterior a la de entrada"
                            return@TextButton
                        }

                        val rate = rateInput.toDoubleOrNull()
                        if (rate == null || rate < 0) {
                            errorMessage = "Ingresa un pago por hora válido"
                            return@TextButton
                        }

                        onConfirm(
                            calStart.timeInMillis,
                            calEnd.timeInMillis,
                            rate,
                            noteInput
                        )
                    } catch (e: Exception) {
                        errorMessage = "Error en los datos ingresados: ${e.message}"
                    }
                }
            ) {
                Text("Agregar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
