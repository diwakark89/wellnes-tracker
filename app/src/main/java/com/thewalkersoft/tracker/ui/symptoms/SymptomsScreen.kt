package com.thewalkersoft.tracker.ui.symptoms

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
import androidx.compose.ui.unit.dp
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SymptomsScreen(
    viewModel: SymptomsViewModel,
    modifier: Modifier = Modifier
) {
    val symptoms by viewModel.symptoms.collectAsState()
    var selectedSymptomIdForDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Symptoms & Biomarkers",
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
        if (symptoms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Symptoms Logged",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Track basal body temp, cramps, mood, and LH ovulation tests.",
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
                items(symptoms, key = { it.id }) { symptom ->
                    SymptomCard(
                        symptom = symptom,
                        onDeleteClick = { selectedSymptomIdForDelete = symptom.id }
                    )
                }
            }
        }

        selectedSymptomIdForDelete?.let { id ->
            AlertDialog(
                onDismissRequest = { selectedSymptomIdForDelete = null },
                title = { Text("Delete Symptom Log?") },
                text = { Text("Are you sure you want to delete this symptom record?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSymptom(id)
                            selectedSymptomIdForDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedSymptomIdForDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomCard(
    symptom: SymptomLogEntity,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symptom.logDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                symptom.mood?.let { mood ->
                    AssistChip(
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mood,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Mood: $mood") }
                    )
                }

                symptom.crampsSeverity?.let { cramps ->
                    AssistChip(
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        label = { Text("Cramps: $cramps / 5") }
                    )
                }

                symptom.basalBodyTemp?.let { bbt ->
                    AssistChip(
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        label = { Text("BBT: $bbt °C") }
                    )
                }

                symptom.ovulationTestResult?.let { ov ->
                    AssistChip(
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Biotech,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("LH Test: $ov") }
                    )
                }
            }
        }
    }
}
