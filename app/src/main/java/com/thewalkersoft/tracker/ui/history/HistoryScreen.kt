package com.thewalkersoft.tracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thewalkersoft.tracker.ui.history.components.CycleHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val cycles by viewModel.cycleRecords.collectAsState()
    var selectedLogIdForDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cycle History & Gaps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (cycles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.EventNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Cycle History Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your logged periods and cycle gaps will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                item {
                    // Summary Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val completedCycles = cycles.drop(1)
                            val avgDays = if (completedCycles.isNotEmpty()) {
                                completedCycles.map { it.cycleLengthDays }.average().toInt()
                            } else 0

                            val minDays = if (completedCycles.isNotEmpty()) {
                                completedCycles.minOf { it.cycleLengthDays }
                            } else 0

                            val maxDays = if (completedCycles.isNotEmpty()) {
                                completedCycles.maxOf { it.cycleLengthDays }
                            } else 0

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Logs", style = MaterialTheme.typography.labelSmall)
                                Text("${cycles.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Length", style = MaterialTheme.typography.labelSmall)
                                Text(if (avgDays > 0) "$avgDays d" else "--", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Range (Min-Max)", style = MaterialTheme.typography.labelSmall)
                                Text(if (minDays > 0) "$minDays - $maxDays d" else "--", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(cycles, key = { it.id }) { record ->
                    CycleHistoryItem(
                        record = record,
                        onDeleteClick = { id -> selectedLogIdForDelete = id }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        selectedLogIdForDelete?.let { id ->
            AlertDialog(
                onDismissRequest = { selectedLogIdForDelete = null },
                title = { Text("Delete Period Record?") },
                text = { Text("Are you sure you want to delete this recorded cycle entry? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteLog(id)
                            selectedLogIdForDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLogIdForDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
