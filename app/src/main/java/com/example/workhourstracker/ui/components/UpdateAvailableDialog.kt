package com.example.workhourstracker.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.data.InAppUpdater
import com.example.workhourstracker.data.UpdateInfo
import com.example.workhourstracker.ui.theme.AccentCoral
import com.example.workhourstracker.ui.theme.AccentEmerald
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UpdateAvailableDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
    var needsPermission by remember { mutableStateOf(false) }

    fun startDownloadAndInstall() {
        val targetFile = File(context.cacheDir, "work_hours_tracker_update.apk")
        isDownloading = true
        downloadError = null
        needsPermission = false

        scope.launch {
            val result = InAppUpdater.downloadApk(updateInfo.downloadUrl, targetFile) { progress ->
                downloadProgress = progress
            }
            isDownloading = false
            result.onSuccess { file ->
                downloadedFile = file
                val launched = InAppUpdater.installApk(context, file)
                if (!launched) {
                    needsPermission = !InAppUpdater.canInstallApks(context)
                }
            }.onFailure { err ->
                downloadError = err.localizedMessage ?: "Error de descarga"
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                downloadedFile != null -> AccentEmerald.copy(alpha = 0.2f)
                                downloadError != null -> AccentCoral.copy(alpha = 0.2f)
                                else -> AccentEmerald.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            downloadedFile != null -> Icons.Default.CheckCircle
                            downloadError != null -> Icons.Default.Refresh
                            needsPermission -> Icons.Default.Security
                            else -> Icons.Default.SystemUpdate
                        },
                        contentDescription = null,
                        tint = if (downloadError != null) AccentCoral else AccentEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when {
                        isDownloading -> "Descargando..."
                        downloadedFile != null -> "¡Lista para instalar!"
                        downloadError != null -> "Error en la descarga"
                        else -> "¡Nueva versión disponible!"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Versión ${updateInfo.latestVersion}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentEmerald
                    )
                    Text(
                        text = "Actual: v${updateInfo.currentVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (updateInfo.releaseNotes.isNotBlank() && !isDownloading && downloadedFile == null) {
                    Text(
                        text = "Novedades:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = updateInfo.releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Progress Bar when downloading
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (downloadProgress >= 0f) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AccentEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(downloadProgress * 100).toInt()}% completado...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AccentEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Descargando instalador...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (downloadedFile != null) {
                    if (needsPermission) {
                        Text(
                            text = "Android requiere que permitas a esta app instalar actualizaciones. Tocá abajo para habilitar 'Permitir desde esta fuente' y continuar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "El instalador se abrirá automáticamente para actualizar la app sin perder ningún turno ni dato guardado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (downloadError != null) {
                    Text(
                        text = "Ocurrió un error al descargar: $downloadError. Podés reintentar directamente o descargar mediante el navegador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCoral
                    )
                } else {
                    Text(
                        text = "Se actualizará directamente dentro de la app con un solo toque, sin necesidad de abrir el navegador ni buscar archivos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (isDownloading) {
                // Disabled while downloading
            } else if (downloadedFile != null) {
                Button(
                    onClick = {
                        val file = downloadedFile ?: return@Button
                        val launched = InAppUpdater.installApk(context, file)
                        if (launched) {
                            onDismiss()
                        } else {
                            needsPermission = !InAppUpdater.canInstallApks(context)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(
                        imageVector = if (needsPermission) Icons.Default.Security else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (needsPermission) "Habilitar permiso e instalar" else "Instalar actualización",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (downloadError != null) {
                Button(
                    onClick = { startDownloadAndInstall() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reintentar descarga", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { startDownloadAndInstall() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actualizar ahora", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                Row {
                    if (downloadError != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                openBrowserFallback(context, updateInfo.downloadUrl)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Abrir navegador")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Más tarde")
                    }
                }
            }
        }
    )
}

private fun openBrowserFallback(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
