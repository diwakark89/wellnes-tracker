package com.thewalkersoft.tracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.ui.dashboard.components.CurrentCycleCard
import com.thewalkersoft.tracker.ui.dashboard.components.DailyCheckInCard
import com.thewalkersoft.tracker.ui.dashboard.components.PhaseInsightsCard
import com.thewalkersoft.tracker.ui.dashboard.components.PredictionWindowCard
import com.thewalkersoft.tracker.ui.theme.CycleTrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSymptoms: () -> Unit,
    onOpenLoggingSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onToggleBiometric = { viewModel.toggleBiometric(it) },
        onQuickLogMood = { viewModel.quickLogMood(it) },
        onQuickLogCramps = { viewModel.quickLogCramps(it) },
        onQuickLogPeriodStartedToday = { viewModel.quickLogPeriodStartedToday() },
        onNavigateToHistory = onNavigateToHistory,
        onOpenLoggingSheet = onOpenLoggingSheet,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onToggleBiometric: (Boolean) -> Unit,
    onQuickLogMood: (String?) -> Unit,
    onQuickLogCramps: (Int?) -> Unit,
    onQuickLogPeriodStartedToday: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenLoggingSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Adaptive Cycle Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    // Biometric Lock Toggle Icon
                    IconButton(
                        onClick = {
                            onToggleBiometric(!uiState.isBiometricEnabled)
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isBiometricEnabled) Icons.Default.Fingerprint else Icons.Default.LockOpen,
                            contentDescription = "Biometric Lock",
                            tint = if (uiState.isBiometricEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
        ) {
            // 1. Current Cycle Hero Card (Multi-phase Wheel + 7-Day Timeline)
            item {
                CurrentCycleCard(
                    currentRecord = uiState.currentCycleRecord,
                    prediction = uiState.prediction,
                    timelineDays = uiState.timelineDays,
                    onOpenLoggingSheet = onOpenLoggingSheet
                )
            }

            // 2. Prediction Range & Dynamic Confidence Card
            if (uiState.prediction != null) {
                item {
                    PredictionWindowCard(prediction = uiState.prediction)
                }
            }

            // 3. 1-Tap Daily Check-in & Symptoms Card
            item {
                DailyCheckInCard(
                    todaySymptom = uiState.todaySymptom,
                    currentRecord = uiState.currentCycleRecord,
                    onLogMood = { mood -> onQuickLogMood(mood) },
                    onLogCramps = { cramps -> onQuickLogCramps(cramps) },
                    onPeriodStartedToday = { onQuickLogPeriodStartedToday() },
                    onOpenLoggingSheet = onOpenLoggingSheet
                )
            }

            // 4. Phase-Specific Body Signals & Health Guidance Card
            if (uiState.prediction != null) {
                item {
                    PhaseInsightsCard(prediction = uiState.prediction)
                }
            }

            // 5. Quick Cycle Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val avgLength = if (uiState.recentCycles.size >= 2) {
                        val lengths = uiState.recentCycles.drop(1).map { it.cycleLengthDays }
                        if (lengths.isNotEmpty()) "${(lengths.average()).toInt()} days" else "--"
                    } else "--"

                    MetricCard(
                        title = "Avg Cycle",
                        value = avgLength,
                        subtitle = if (uiState.recentCycles.isNotEmpty()) "Last ${uiState.recentCycles.size} cycles" else "Awaiting logs",
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Recorded",
                        value = "${uiState.recentCycles.size} cycles",
                        subtitle = "Offline local database",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. Recent History Quick List
            if (uiState.recentCycles.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Cycles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToHistory) {
                            Text("See Full History")
                        }
                    }
                }

                items(uiState.recentCycles.take(3)) { record ->
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.startDate.format(dateFormatter),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Flow: ${record.flowIntensity ?: "Standard"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "${record.cycleLengthDays} days",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
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
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    CycleTrackerTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                currentCycleRecord = CycleRecord(
                    id = 1,
                    startDate = LocalDate.now().minusDays(10),
                    endDate = null,
                    cycleLengthDays = 28,
                    flowIntensity = "Medium"
                ),
                prediction = PredictionResult(
                    lastPeriodDate = LocalDate.now().minusDays(10),
                    currentCycleDay = 11,
                    earliestLikelyDate = LocalDate.now().plusDays(15),
                    targetPeakDate = LocalDate.now().plusDays(18),
                    latestLikelyDate = LocalDate.now().plusDays(21),
                    confidenceWindowDays = 6,
                    status = CycleStatus.FOLLICULAR
                ),
                recentCycles = listOf(
                    CycleRecord(
                        id = 2,
                        startDate = LocalDate.now().minusDays(38),
                        endDate = LocalDate.now().minusDays(10),
                        cycleLengthDays = 28,
                        flowIntensity = "Medium"
                    )
                ),
                todaySymptom = SymptomLogEntity(
                    id = 1,
                    logDate = LocalDate.now(),
                    mood = "HAPPY"
                ),
                isBiometricEnabled = true,
                timelineDays = listOf(
                    DayTimelineItem(
                        date = LocalDate.now(),
                        isToday = true,
                        dayOfWeek = "Mon",
                        dayOfMonth = LocalDate.now().dayOfMonth,
                        cycleDay = 11,
                        phaseColor = MaterialTheme.colorScheme.primary,
                        phaseName = "Follicular"
                    )
                )
            ),
            onToggleBiometric = {},
            onQuickLogMood = {},
            onQuickLogCramps = {},
            onQuickLogPeriodStartedToday = {},
            onNavigateToHistory = {},
            onOpenLoggingSheet = {}
        )
    }
}
