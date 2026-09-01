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
import com.thewalkersoft.tracker.ui.theme.Rose40
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogPeriodBottomSheet(
    initialStartDate: LocalDate = LocalDate.now(),
    initialEndDate: LocalDate? = null,
    initialFlow: String? = "MEDIUM",
    initialNotes: String? = null,
    initialIsPostpartumReset: Boolean = false,
    initialBbt: Float? = null,
    initialCramps: Int? = null,
    initialMood: String? = null,
    initialOvulation: String? = null,
    isEditMode: Boolean = false,
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

    var startDate by remember(initialStartDate) { mutableStateOf(initialStartDate) }
    var endDate by remember(initialEndDate) { mutableStateOf(initialEndDate) }
    var flowIntensity by remember(initialFlow) { mutableStateOf(initialFlow ?: "MEDIUM") }
    var notes by remember(initialNotes) { mutableStateOf(initialNotes ?: "") }
    var isPostpartumReset by remember(initialIsPostpartumReset) { mutableStateOf(initialIsPostpartumReset) }

    // Symptoms
    var bbtInput by remember(initialBbt) { mutableStateOf(initialBbt?.toString() ?: "") }
    var crampsSeverity by remember(initialCramps) { mutableStateOf(initialCramps) }
    var selectedMood by remember(initialMood) { mutableStateOf(initialMood) }
    var ovulationResult by remember(initialOvulation) { mutableStateOf(initialOvulation) }

    // Date Picker Dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditMode) "Edit Cycle & Log" else "Log Period & Symptoms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% private and on-device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // 1. Period Details Section Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Period Dates & Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Start Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = startDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedCard(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("End Date (Optional)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = endDate?.format(dateFormatter) ?: "Ongoing...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (endDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text("Flow Intensity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    val flows = listOf("SPOT" to "Spotting", "LIGHT" to "Light", "MEDIUM" to "Medium", "HEAVY" to "Heavy")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        flows.forEach { (key, label) ->
                            FilterChip(
                                selected = flowIntensity == key,
                                onClick = { flowIntensity = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (flowIntensity == key) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Postpartum switch
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Postpartum Baseline Reset",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isolates from older pre-pregnancy baseline.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isPostpartumReset,
                            onCheckedChange = { isPostpartumReset = it }
                        )
                    }
                }
            }

            // 2. Physical & Pain Severity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cramps / Pain Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (crampsSeverity != null) {
                            Text(
                                text = "$crampsSeverity / 5",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isNone = crampsSeverity == null
                        FilterChip(
                            selected = isNone,
                            onClick = { crampsSeverity = null },
                            label = { Text("None", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        (1..5).forEach { level ->
                            FilterChip(
                                selected = crampsSeverity == level,
                                onClick = { crampsSeverity = if (crampsSeverity == level) null else level },
                                label = { Text("$level★", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.weight(0.9f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // 3. Mood & Energy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mood & Emotions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val moods = listOf(
                        "Happy" to "😊",
                        "Calm" to "😌",
                        "Tired" to "🥱",
                        "Irritable" to "😣",
                        "Anxious" to "😰",
                        "Sad" to "😢"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moods.forEach { (mood, emoji) ->
                            val isSelected = selectedMood == mood
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMood = if (isSelected) null else mood },
                                label = { Text("$emoji $mood", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // 4. Biomarkers & Fertility Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Biomarkers & Fertility",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = bbtInput,
                        onValueChange = { bbtInput = it },
                        label = { Text("Basal Body Temperature (°C / °F)") },
                        placeholder = { Text("e.g. 36.6") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )

                    Text("Ovulation (LH) Test Result", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    val ovResults = listOf("NEGATIVE" to "Negative", "POSITIVE" to "Positive", "PEAK" to "Peak LH")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ovResults.forEach { (key, label) ->
                            FilterChip(
                                selected = ovulationResult == key,
                                onClick = { ovulationResult = if (ovulationResult == key) null else key },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (ovulationResult == key) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // 5. Notes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personal Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add any extra observations or symptoms...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3
                    )
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val bbtVal = bbtInput.toFloatOrNull()
                    val validEndDate = if (endDate != null && endDate!!.isBefore(startDate)) startDate else endDate
                    onSave(
                        startDate,
                        validEndDate,
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Update Entry" else "Save Entry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
                            val newStart = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            startDate = newStart
                            if (endDate != null && endDate!!.isBefore(newStart)) {
                                endDate = newStart
                            }
                        }
                        showStartDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
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
                            val selectedEnd = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            endDate = if (selectedEnd.isBefore(startDate)) startDate else selectedEnd
                        }
                        showEndDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
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
