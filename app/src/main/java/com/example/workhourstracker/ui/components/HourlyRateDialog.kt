package com.example.workhourstracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun HourlyRateDialog(
    currentRate: Double,
    currentSymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (newRate: Double, newSymbol: String) -> Unit
) {
    var rateInput by remember { mutableStateOf(if (currentRate % 1.0 == 0.0) currentRate.toInt().toString() else currentRate.toString()) }
    var symbolInput by remember { mutableStateOf(currentSymbol) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configurar Pago por Hora",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Indica cuánto ganas por cada hora trabajada para calcular automáticamente tus ingresos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        isError = it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) < 0
                    },
                    label = { Text("Valor por hora") },
                    placeholder = { Text("Ej: 2500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Ingresa un número válido")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = symbolInput,
                    onValueChange = { symbolInput = it },
                    label = { Text("Símbolo de moneda") },
                    placeholder = { Text("Ej: $, USD, €") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rate = rateInput.toDoubleOrNull()
                    if (rate != null && rate >= 0) {
                        val sym = if (symbolInput.isBlank()) "$" else symbolInput.trim()
                        onConfirm(rate, sym)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
