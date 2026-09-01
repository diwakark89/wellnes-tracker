package com.thewalkersoft.tracker.ui.symptoms.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun BbtTrendChart(
    symptoms: List<SymptomLogEntity>,
    modifier: Modifier = Modifier
) {
    val bbtEntries = symptoms.filter { it.basalBodyTemp != null }
        .sortedBy { it.logDate }
        .takeLast(10)

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
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Basal Body Temp (BBT) Curve",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (bbtEntries.isNotEmpty()) "${bbtEntries.size} readings logged" else "Ovulation shift tracker",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (bbtEntries.isNotEmpty()) {
                    val latestTemp = bbtEntries.last().basalBodyTemp
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Latest: $latestTemp°",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (bbtEntries.size < 2) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Log BBT temperature across multiple mornings to detect your biphasic ovulation thermal shift.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.tertiary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

                val temps = bbtEntries.mapNotNull { it.basalBodyTemp }
                val minTemp = (temps.minOrNull() ?: 36.0f) - 0.2f
                val maxTemp = (temps.maxOrNull() ?: 37.0f) + 0.2f
                val tempRange = max(0.4f, maxTemp - minTemp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)) {
                        val chartHeight = size.height - 35.dp.toPx()
                        val chartWidth = size.width
                        val stepX = chartWidth / (bbtEntries.size - 1).coerceAtLeast(1)

                        // 1. Calculate points
                        val points = bbtEntries.mapIndexed { index, item ->
                            val temp = item.basalBodyTemp ?: minTemp
                            val x = index * stepX
                            val normalizedY = (temp - minTemp) / tempRange
                            val y = chartHeight - (normalizedY * chartHeight)
                            Offset(x, y)
                        }

                        // 2. Draw fill path under the curve
                        val fillPath = Path().apply {
                            moveTo(points.first().x, chartHeight)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )

                        // 3. Draw smooth curve line
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 4. Draw data points and text labels
                        points.forEachIndexed { index, point ->
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
                            drawCircle(color = primaryColor, radius = 3.5.dp.toPx(), center = point)

                            val entry = bbtEntries[index]
                            val tempText = "${entry.basalBodyTemp}°"

                            // Top temperature label
                            drawContext.canvas.nativeCanvas.apply {
                                val textPaint = android.graphics.Paint().apply {
                                    color = onSurfaceColor.hashCode()
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                drawText(tempText, point.x, point.y - 8.dp.toPx(), textPaint)

                                val datePaint = android.graphics.Paint().apply {
                                    color = onSurfaceVariantColor.hashCode()
                                    textSize = 9.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                val dateStr = entry.logDate.format(dateFormatter)
                                drawText(dateStr, point.x, size.height - 2.dp.toPx(), datePaint)
                            }
                        }
                    }
                }
            }
        }
    }
}
