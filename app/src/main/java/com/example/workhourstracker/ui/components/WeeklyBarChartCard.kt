package com.example.workhourstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.AccentPurple
import com.example.workhourstracker.ui.viewmodel.DayActivity
import com.example.workhourstracker.ui.viewmodel.SelectedWeekData
import java.util.Locale

@Composable
fun WeeklyBarChartCard(
    weekData: SelectedWeekData,
    currencySymbol: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weeklyActivity = weekData.days
    if (weeklyActivity.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    var selectedDayIndex by remember(weekData.weekOffset) { mutableStateOf<Int?>(null) }

    val totalWeekHours = weekData.totalHours
    val totalWeekEarnings = weekData.totalEarnings
    val maxDayHours = remember(weeklyActivity) {
        weeklyActivity.maxOfOrNull { it.durationHours }?.coerceAtLeast(8.0) ?: 8.0
    }
    val recordEarnings = remember(weeklyActivity) {
        weeklyActivity.maxOfOrNull { it.earnings }?.takeIf { it > 0 }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with title and total earned pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (weekData.isCurrentWeek) "Rendimiento Semanal" else "Semana Anterior",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val totalMinutes = (totalWeekHours * 60).toInt()
                        val h = totalMinutes / 60
                        val m = totalMinutes % 60
                        Text(
                            text = "${h}h ${m}m trabajadas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total earned pill
                val formattedWeekEarnings = String.format(Locale.getDefault(), "%,.0f", totalWeekEarnings)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$currencySymbol$formattedWeekEarnings",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Week Selector Navigator Row (◀ Semana ▶)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedDayIndex = null
                        onPreviousWeek()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Semana anterior",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weekData.weekRangeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!weekData.isCurrentWeek) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Hoy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentEmerald,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedDayIndex = null
                                    onResetWeek()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedDayIndex = null
                        onNextWeek()
                    },
                    enabled = weekData.weekOffset < 0,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Semana siguiente",
                        tint = if (weekData.weekOffset < 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars Chart Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyActivity.forEachIndexed { index, day ->
                    val isSelected = selectedDayIndex == index
                    val fraction = (day.durationHours / maxDayHours).toFloat().coerceIn(0f, 1f)
                    val barHeight = 8.dp + (82.dp * fraction)
                    val animatedHeight by animateDpAsState(
                        targetValue = barHeight,
                        animationSpec = tween(durationMillis = 500),
                        label = "barHeight"
                    )

                    val isRecord = day.earnings > 0 && day.earnings == recordEarnings

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Value on top of bar with record badge
                        if (day.durationHours > 0.0) {
                            if (isRecord) {
                                Text(
                                    text = "🔥",
                                    fontSize = 11.sp
                                )
                            }
                            val hoursFormatted = if (day.durationHours % 1.0 == 0.0) {
                                "${day.durationHours.toInt()}h"
                            } else {
                                String.format(Locale.US, "%.1fh", day.durationHours)
                            }
                            Text(
                                text = hoursFormatted,
                                fontSize = 10.sp,
                                fontWeight = if (isRecord || isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isRecord) AccentEmerald else if (day.overtimeHours > 0) AccentPurple else if (day.isToday) AccentEmerald else MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "",
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // The Bar
                        val barColorModifier = if (day.durationHours > 0.0) {
                            if (day.overtimeHours > 0) {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        colors = listOf(AccentPurple, Color(0xFFA855F7))
                                    )
                                )
                            } else if (day.isToday) {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        colors = listOf(AccentEmerald, Color(0xFF34D399))
                                    )
                                )
                            } else {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF38BDF8))
                                    )
                                )
                            }
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        }

                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 22.dp else 18.dp)
                                .height(animatedHeight)
                                .clip(RoundedCornerShape(9.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, Color.White, RoundedCornerShape(9.dp))
                                    } else Modifier
                                )
                                .then(barColorModifier)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label & date
                        Text(
                            text = day.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday || isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${day.dayOfMonth}",
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (day.isToday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Interactive Day Detail Pill (When user taps a bar)
            AnimatedVisibility(
                visible = selectedDayIndex != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDayIndex?.let { idx ->
                    if (idx in weeklyActivity.indices) {
                        val selDay = weeklyActivity[idx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${selDay.dayOfWeek} ${selDay.dayOfMonth}" + if (selDay.isToday) " (Hoy)" else "",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val formattedHours = if (selDay.durationHours > 0) {
                                        if (selDay.overtimeHours > 0) {
                                            String.format(Locale.US, "%.1fh (%.1fh norm + %.1fh extra)", selDay.durationHours, selDay.durationHours - selDay.overtimeHours, selDay.overtimeHours)
                                        } else {
                                            String.format(Locale.US, "%.1fh trabajadas", selDay.durationHours)
                                        }
                                    } else {
                                        "Sin turnos registrados"
                                    }
                                    Text(
                                        text = formattedHours,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (selDay.earnings > 0) {
                                    Text(
                                        text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", selDay.earnings)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = AccentEmerald
                                    )
                                } else {
                                    Text(
                                        text = "$currencySymbol 0",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
