package com.thewalkersoft.tracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import com.thewalkersoft.tracker.ui.security.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentCycleRecord: CycleRecord? = null,
    val prediction: PredictionResult? = null,
    val recentCycles: List<CycleRecord> = emptyList(),
    val todaySymptom: SymptomLogEntity? = null,
    val isBiometricEnabled: Boolean = false
)

class DashboardViewModel(
    private val repository: CycleRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllCycleRecords(),
        repository.getPrediction(),
        repository.getSymptomForDate(today),
        securityPreferences.isBiometricEnabled
    ) { cycleRecords, prediction, todaySymptom, isBiometricEnabled ->
        DashboardUiState(
            isLoading = false,
            currentCycleRecord = cycleRecords.firstOrNull(),
            prediction = prediction,
            recentCycles = cycleRecords.take(5),
            todaySymptom = todaySymptom,
            isBiometricEnabled = isBiometricEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun toggleBiometric(enabled: Boolean) {
        securityPreferences.setBiometricEnabled(enabled)
    }

    fun savePeriodAndSymptoms(
        startDate: LocalDate,
        endDate: LocalDate?,
        flowIntensity: String?,
        notes: String?,
        isPostpartumReset: Boolean,
        bbt: Float?,
        cramps: Int?,
        mood: String?,
        ovulation: String?
    ) {
        viewModelScope.launch {
            // Save period log
            repository.insertPeriodLog(
                PeriodLogEntity(
                    startDate = startDate,
                    endDate = endDate,
                    flowIntensity = flowIntensity,
                    notes = notes,
                    isPostpartumBaselineReset = isPostpartumReset
                )
            )

            // Save symptom log if any symptom is entered
            if (bbt != null || cramps != null || mood != null || ovulation != null) {
                val existingSymptom = repository.getLatestPeriodLog() // or query by date
                repository.insertSymptom(
                    SymptomLogEntity(
                        logDate = startDate,
                        basalBodyTemp = bbt,
                        crampsSeverity = cramps,
                        mood = mood,
                        ovulationTestResult = ovulation
                    )
                )
            }
        }
    }

    fun deletePeriod(logId: Long) {
        viewModelScope.launch {
            repository.deletePeriodLogById(logId)
        }
    }
}
