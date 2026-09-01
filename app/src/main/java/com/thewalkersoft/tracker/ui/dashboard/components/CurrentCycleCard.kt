package com.thewalkersoft.tracker.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun CurrentCycleCard(
    currentRecord: CycleRecord?,
    prediction: PredictionResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentRecord == null || prediction == null) {
                // Empty state when no period is logged yet
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to Adaptive Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Log your last period start date to activate adaptive prediction and cycle insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                val cycleDay = prediction.currentCycleDay
                val status = prediction.status
                val (statusColor, statusBgColor, statusIcon) = when (status) {
                    CycleStatus.FOLLICULAR -> Triple(FollicularColor, FollicularColor.copy(alpha = 0.12f), Icons.Default.Spa)
                    CycleStatus.OVULATION_WINDOW -> Triple(OvulationColor, OvulationColor.copy(alpha = 0.12f), Icons.Default.AutoAwesome)
                    CycleStatus.LUTEAL -> Triple(LutealColor, LutealColor.copy(alpha = 0.12f), Icons.Default.Bedtime)
                    CycleStatus.PREDICTION_WINDOW_ACTIVE -> Triple(WindowActiveColor, WindowActiveColor.copy(alpha = 0.12f), Icons.Default.NotificationsActive)
                    CycleStatus.OVERDUE -> Triple(OverdueColor, OverdueColor.copy(alpha = 0.12f), Icons.Default.Warning)
                }

                // Phase Status Pill Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBgColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status.displayName,
                            color = statusColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main Circular Hero Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(8.dp)
                ) {
                    val targetDays = prediction.targetPeakDate.toEpochDay() - prediction.lastPeriodDate.toEpochDay()
                    val progress = (cycleDay.toFloat() / targetDays.toFloat()).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "cycle_progress")

                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 10.dp,
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = statusColor,
                        strokeWidth = 10.dp,
                        trackColor = Color.Transparent
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CYCLE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$cycleDay",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "of ~$targetDays days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = status.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Started",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentRecord.startDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Flow",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentRecord.flowIntensity ?: "Standard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (currentRecord.isPostpartumBaselineReset) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Baseline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
