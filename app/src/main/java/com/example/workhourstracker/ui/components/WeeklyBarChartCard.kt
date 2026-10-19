package com.example.workhourstracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.AccentPurple
import com.example.workhourstracker.ui.viewmodel.DayActivity
import java.util.Locale

@Composable
fun WeeklyBarChartCard(
    weeklyActivity: List<DayActivity>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (weeklyActivity.isEmpty()) return

    val totalWeekHours = remember(weeklyActivity) { weeklyActivity.sumOf { it.durationHours } }
    val totalWeekEarnings = remember(weeklyActivity) { weeklyActivity.sumOf { it.earnings } }
    val maxDayHours = remember(weeklyActivity) {
        weeklyActivity.maxOfOrNull { it.durationHours }?.coerceAtLeast(8.0) ?: 8.0
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
            // Header
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
                            text = "Rendimiento Semanal",
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

            Spacer(modifier = Modifier.height(20.dp))

            // Bars Chart Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyActivity.forEach { day ->
                    val fraction = (day.durationHours / maxDayHours).toFloat().coerceIn(0f, 1f)
                    val barHeight = 8.dp + (82.dp * fraction)
                    val animatedHeight by animateDpAsState(
                        targetValue = barHeight,
                        animationSpec = tween(durationMillis = 600),
                        label = "barHeight"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Value on top of bar
                        if (day.durationHours > 0.0) {
                            val hoursFormatted = if (day.durationHours % 1.0 == 0.0) {
                                "${day.durationHours.toInt()}h"
                            } else {
                                String.format(Locale.US, "%.1fh", day.durationHours)
                            }
                            Text(
                                text = hoursFormatted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.overtimeHours > 0) AccentPurple else if (day.isToday) AccentEmerald else MaterialTheme.colorScheme.primary
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
                                .width(18.dp)
                                .height(animatedHeight)
                                .clip(RoundedCornerShape(9.dp))
                                .then(barColorModifier)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label & date
                        Text(
                            text = day.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday) FontWeight.Black else FontWeight.SemiBold,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${day.dayOfMonth}",
                            fontSize = 9.sp,
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
        }
    }
}
