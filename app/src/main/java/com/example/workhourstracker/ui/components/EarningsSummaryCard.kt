package com.example.workhourstracker.ui.components

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workhourstracker.ui.theme.AccentEmerald
import com.example.workhourstracker.ui.theme.AccentPurple
import com.example.workhourstracker.ui.theme.PrimaryBlue
import com.example.workhourstracker.ui.viewmodel.EarningsSummary

@Composable
fun EarningsSummaryCard(
    summary: EarningsSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    // Luxury Fintech Black & Emerald gradient
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark Slate
            Color(0xFF064E3B), // Deep Emerald
            Color(0xFF022C22)  // Dark Forest
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AccentEmerald.copy(alpha = 0.35f),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Badge & Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "GANANCIAS ACUMULADAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Cycle pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentEmerald.copy(alpha = 0.15f))
                            .border(1.dp, AccentEmerald.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Mié - Mar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Number: Week Earnings
                Text(
                    text = "Total Esta Semana",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(2.dp))

                AnimatedMoneyText(
                    amount = summary.weekEarnings,
                    currencySymbol = currencySymbol,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    decimals = 0
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Glassmorphic pills row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FintechSummaryPill(
                        title = "Hoy",
                        icon = Icons.Default.Today,
                        earnings = summary.todayEarnings,
                        hours = summary.todayHours,
                        accentColor = PrimaryBlue,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

                    FintechSummaryPill(
                        title = "Semana",
                        icon = Icons.Default.DateRange,
                        earnings = summary.weekEarnings,
                        hours = summary.weekHours,
                        accentColor = AccentPurple,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

                    FintechSummaryPill(
                        title = "Mes",
                        icon = Icons.Default.CalendarMonth,
                        earnings = summary.monthEarnings,
                        hours = summary.monthHours,
                        accentColor = AccentEmerald,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FintechSummaryPill(
    title: String,
    icon: ImageVector,
    earnings: Double,
    hours: Double,
    accentColor: Color,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val totalMinutes = (hours * 60).toInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    val durationText = "${h}h ${m}m"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(2.dp))

            AnimatedMoneyText(
                amount = earnings,
                currencySymbol = currencySymbol,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                decimals = 0
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}
