package com.thewalkersoft.tracker.ui.logging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogPeriodBottomSheet(
    onDismiss: () -> Unit,
    onSave: (
        startDate: LocalDate,
        endDate: LocalDate?,
        flow: String?,
        notes: String?,
        isPostpartumReset: Boolean,
        bbt: Float?,
        cramps: Int?,
        mood: String?,
        ovulation: String?
    ) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var flowIntensity by remember { mutableStateOf("MEDIUM") }
    var notes by remember { mutableStateOf("") }
    var isPostpartumReset by remember { mutableStateOf(false) }

    // Symptoms
    var bbtInput by remember { mutableStateOf("") }
    var crampsSeverity by remember { mutableStateOf<Int?>(null) }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var ovulationResult by remember { mutableStateOf<String?>(null) }

    // Date Picker Dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Period & Symptoms",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Period Start Date Picker
            Text(
                text = "Period Dates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedCard(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Start Date", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = startDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedCard(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("End Date (Optional)", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = endDate?.format(dateFormatter) ?: "Ongoing",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Flow Intensity
            Text(
                text = "Flow Intensity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val flows = listOf("SPOT" to "Spotting", "LIGHT" to "Light", "MEDIUM" to "Medium", "HEAVY" to "Heavy")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                flows.forEach { (key, label) ->
                    FilterChip(
                        selected = flowIntensity == key,
                        onClick = { flowIntensity = key },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Postpartum Reset Toggle Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Postpartum Baseline Reset",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Isolates calculation from older pre-pregnancy/birth cycles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPostpartumReset,
                        onCheckedChange = { isPostpartumReset = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Symptoms Section
            Text(
                text = "Daily Symptoms (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Mood selection
            Text("Mood", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            val moods = listOf("Happy", "Calm", "Tired", "Irritable", "Anxious", "Sad")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                moods.forEach { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = if (selectedMood == mood) null else mood },
                        label = { Text(mood) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cramps severity
            Text("Cramps Severity (1 to 5)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { rating ->
                    FilterChip(
                        selected = crampsSeverity == rating,
                        onClick = { crampsSeverity = if (crampsSeverity == rating) null else rating },
                        label = { Text("$rating ★") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Basal Body Temperature
            OutlinedTextField(
                value = bbtInput,
                onValueChange = { bbtInput = it },
                label = { Text("Basal Body Temperature (°C / °F)") },
                placeholder = { Text("e.g. 36.6") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Thermostat, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ovulation Test Result
            Text("Ovulation (LH) Test Result", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            val ovResults = listOf("NEGATIVE" to "Negative", "POSITIVE" to "Positive", "PEAK" to "Peak LH")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ovResults.forEach { (key, label) ->
                    FilterChip(
                        selected = ovulationResult == key,
                        onClick = { ovulationResult = if (ovulationResult == key) null else key },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Personal Notes") },
                placeholder = { Text("Add any extra observations...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val bbtVal = bbtInput.toFloatOrNull()
                    onSave(
                        startDate,
                        endDate,
                        flowIntensity,
                        notes.ifBlank { null },
                        isPostpartumReset,
                        bbtVal,
                        crampsSeverity,
                        selectedMood,
                        ovulationResult
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Log Entry", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (endDate ?: startDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        endDate = null
                        showEndDatePicker = false
                    }
                ) { Text("Clear / Ongoing") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
