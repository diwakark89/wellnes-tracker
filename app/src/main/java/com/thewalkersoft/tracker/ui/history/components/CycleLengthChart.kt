package com.thewalkersoft.tracker.ui.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.ui.theme.*
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun CycleLengthChart(
    cycles: List<CycleRecord>,
    modifier: Modifier = Modifier
) {
    val completedCycles = if (cycles.size >= 2) cycles.drop(1).take(8).reversed() else emptyList()
    val avgLength = if (completedCycles.isNotEmpty()) {
        completedCycles.map { it.cycleLengthDays }.average().toInt()
    } else 28

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cycle Length Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Last ${completedCycles.size} completed cycles",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (completedCycles.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Avg: $avgLength d",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (completedCycles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Log at least 2 period cycles to visualize historical duration trends and baseline variations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        val maxCycle = max(45, (completedCycles.maxOfOrNull { it.cycleLengthDays } ?: 35) + 5)
                        val minCycle = 0
                        val chartHeight = size.height - 35.dp.toPx()
                        val chartWidth = size.width
                        val barCount = completedCycles.size
                        val slotWidth = chartWidth / barCount
                        val barWidth = (slotWidth * 0.48f).coerceIn(16.dp.toPx(), 36.dp.toPx())

                        // 1. Draw average baseline dashed line
                        val avgY = chartHeight - ((avgLength.toFloat() / maxCycle.toFloat()) * chartHeight)
                        drawLine(
                            color = primaryColor.copy(alpha = 0.6f),
                            start = Offset(0f, avgY),
                            end = Offset(chartWidth, avgY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // 2. Draw Bars and Labels
                        completedCycles.forEachIndexed { index, record ->
                            val days = record.cycleLengthDays
                            val barHeight = ((days.toFloat() / maxCycle.toFloat()) * chartHeight).coerceAtLeast(10.dp.toPx())
                            val barX = (index * slotWidth) + (slotWidth - barWidth) / 2f
                            val barY = chartHeight - barHeight

                            val isCloseToAvg = kotlin.math.abs(days - avgLength) <= 2
                            val barColor = if (isCloseToAvg) primaryColor else primaryColor.copy(alpha = 0.6f)

                            // Draw rounded bar
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(barX, barY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Top day count text
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = onSurfaceColor.hashCode()
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                drawText("${days}d", barX + barWidth / 2f, barY - 4.dp.toPx(), paint)
                            }

                            // Bottom date text
                            drawContext.canvas.nativeCanvas.apply {
                                val datePaint = android.graphics.Paint().apply {
                                    color = onSurfaceVariantColor.hashCode()
                                    textSize = 9.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                val formattedDate = record.startDate.format(dateFormatter)
                                drawText(formattedDate, barX + barWidth / 2f, size.height - 2.dp.toPx(), datePaint)
                            }
                        }
                    }
                }
            }
        }
    }
}
