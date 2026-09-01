package com.thewalkersoft.tracker.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.ui.dashboard.DayTimelineItem
import com.thewalkersoft.tracker.ui.theme.*
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CurrentCycleCard(
    currentRecord: CycleRecord?,
    prediction: PredictionResult?,
    timelineDays: List<DayTimelineItem> = emptyList(),
    onOpenLoggingSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentRecord == null || prediction == null) {
                // Empty state onboarding card
                EmptyOnboardingView(onOpenLoggingSheet = onOpenLoggingSheet)
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
                    modifier = Modifier.padding(bottom = 12.dp)
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

                val targetDays = (prediction.targetPeakDate.toEpochDay() - prediction.lastPeriodDate.toEpochDay()).toInt().coerceAtLeast(20)
                val progress = (cycleDay.toFloat() / targetDays.toFloat()).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 900),
                    label = "cycle_progress"
                )

                // Multi-Phase Circular Wheel Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        val strokeWidth = 11.dp.toPx()
                        val diameter = size.minDimension
                        val arcSize = Size(diameter, diameter)
                        val arcOffset = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

                        // 1. Draw segmented background track representing phases
                        val pPeak = targetDays.toFloat()
                        val mDays = 5f
                        val ovStartDays = (pPeak - 18f).coerceAtLeast(mDays)
                        val ovEndDays = (pPeak - 12f).coerceAtLeast(ovStartDays)

                        val menstrualSweep = (mDays / pPeak) * 360f
                        val follicularSweep = ((ovStartDays - mDays) / pPeak) * 360f
                        val ovulationSweep = ((ovEndDays - ovStartDays) / pPeak) * 360f
                        val lutealSweep = 360f - (menstrualSweep + follicularSweep + ovulationSweep)

                        // Base track with subtle colors
                        var curAngle = -90f
                        drawArc(
                            color = Rose40.copy(alpha = 0.22f),
                            startAngle = curAngle,
                            sweepAngle = menstrualSweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        curAngle += menstrualSweep

                        drawArc(
                            color = FollicularColor.copy(alpha = 0.22f),
                            startAngle = curAngle,
                            sweepAngle = follicularSweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        curAngle += follicularSweep

                        drawArc(
                            color = OvulationColor.copy(alpha = 0.25f),
                            startAngle = curAngle,
                            sweepAngle = ovulationSweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        curAngle += ovulationSweep

                        drawArc(
                            color = LutealColor.copy(alpha = 0.22f),
                            startAngle = curAngle,
                            sweepAngle = lutealSweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Draw Active Progress Arc
                        drawArc(
                            color = statusColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 3. Draw head dot at current progress position
                        val currentAngleRad = Math.toRadians((-90f + (animatedProgress * 360f)).toDouble())
                        val radius = diameter / 2f
                        val centerX = arcOffset.x + radius
                        val centerY = arcOffset.y + radius
                        val headX = centerX + radius * cos(currentAngleRad).toFloat()
                        val headY = centerY + radius * sin(currentAngleRad).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(headX, headY)
                        )
                        drawCircle(
                            color = statusColor,
                            radius = 4.dp.toPx(),
                            center = Offset(headX, headY)
                        )
                    }

                    // Center Labels & Day Counter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CYCLE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "$cycleDay",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val daysRemaining = targetDays - cycleDay
                        val subText = when {
                            daysRemaining > 0 -> "Period in ~$daysRemaining days"
                            daysRemaining == 0 -> "Period expected today"
                            else -> "${-daysRemaining} days past target"
                        }
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (daysRemaining < 0) OverdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Legend Mini Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Rose40, label = "Period")
                    Spacer(modifier = Modifier.width(10.dp))
                    LegendItem(color = FollicularColor, label = "Follicular")
                    Spacer(modifier = Modifier.width(10.dp))
                    LegendItem(color = OvulationColor, label = "Fertile")
                    Spacer(modifier = Modifier.width(10.dp))
                    LegendItem(color = LutealColor, label = "Luteal")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7-Day Horizontal Calendar Timeline Strip
                if (timelineDays.isNotEmpty()) {
                    Text(
                        text = "Cycle Timeline",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timelineDays.forEach { item ->
                            DayTimelineCard(item = item, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Summary Row
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Started",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentRecord.startDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Flow",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentRecord.flowIntensity?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Medium",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
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
                                text = "Postpartum Reset",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DayTimelineCard(
    item: DayTimelineItem,
    modifier: Modifier = Modifier
) {
    val bg = if (item.isToday) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val borderModifier = if (item.isToday) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
            .then(borderModifier)
            .height(68.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.dayOfWeek.uppercase(),
                fontSize = 9.sp,
                fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${item.dayOfMonth}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            // Phase indicator dot
            val dotColor = item.phaseColor ?: Color.Transparent
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
private fun EmptyOnboardingView(
    onOpenLoggingSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Adaptive Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track your menstrual cycles with complete offline privacy. Get adaptive rolling predictions, fertility window forecasts, and symptom insights without sending any data to the cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onOpenLoggingSheet,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Your First Period", fontWeight = FontWeight.Bold)
        }
    }
}
