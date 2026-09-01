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

data class DayTimelineItem(
    val date: LocalDate,
    val isToday: Boolean,
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val cycleDay: Int?,
    val phaseColor: androidx.compose.ui.graphics.Color?,
    val phaseName: String?
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentCycleRecord: CycleRecord? = null,
    val prediction: PredictionResult? = null,
    val recentCycles: List<CycleRecord> = emptyList(),
    val todaySymptom: SymptomLogEntity? = null,
    val isBiometricEnabled: Boolean = false,
    val timelineDays: List<DayTimelineItem> = emptyList()
)

class DashboardViewModel(
    private val repository: CycleRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllCycleRecords(),
        repository.getPrediction(),
        repository.getSymptomForDate(LocalDate.now()),
        securityPreferences.isBiometricEnabled
    ) { cycleRecords, prediction, todaySymptom, isBiometricEnabled ->
        val currentRecord = cycleRecords.firstOrNull()
        val timeline = buildTimeline(currentRecord, prediction)

        DashboardUiState(
            isLoading = false,
            currentCycleRecord = currentRecord,
            prediction = prediction,
            recentCycles = cycleRecords.take(5),
            todaySymptom = todaySymptom,
            isBiometricEnabled = isBiometricEnabled,
            timelineDays = timeline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun toggleBiometric(enabled: Boolean) {
        securityPreferences.setBiometricEnabled(enabled)
    }

    fun quickLogMood(mood: String?) {
        viewModelScope.launch {
            val date = today
            val existing = repository.getSymptomByDate(date)
            if (existing != null) {
                repository.updateSymptom(
                    existing.copy(mood = mood)
                )
            } else {
                repository.insertSymptom(
                    SymptomLogEntity(
                        logDate = date,
                        mood = mood
                    )
                )
            }
        }
    }

    fun quickLogCramps(crampsSeverity: Int?) {
        viewModelScope.launch {
            val date = today
            val existing = repository.getSymptomByDate(date)
            if (existing != null) {
                repository.updateSymptom(
                    existing.copy(crampsSeverity = crampsSeverity)
                )
            } else {
                repository.insertSymptom(
                    SymptomLogEntity(
                        logDate = date,
                        crampsSeverity = crampsSeverity
                    )
                )
            }
        }
    }

    fun quickLogPeriodStartedToday() {
        viewModelScope.launch {
            val date = today
            repository.insertPeriodLog(
                PeriodLogEntity(
                    startDate = date,
                    flowIntensity = "MEDIUM"
                )
            )
        }
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
                val existingSymptom = repository.getSymptomByDate(startDate)
                if (existingSymptom != null) {
                    repository.updateSymptom(
                        existingSymptom.copy(
                            basalBodyTemp = bbt ?: existingSymptom.basalBodyTemp,
                            crampsSeverity = cramps ?: existingSymptom.crampsSeverity,
                            mood = mood ?: existingSymptom.mood,
                            ovulationTestResult = ovulation ?: existingSymptom.ovulationTestResult
                        )
                    )
                } else {
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
    }

    fun deletePeriod(logId: Long) {
        viewModelScope.launch {
            repository.deletePeriodLogById(logId)
        }
    }

    private fun buildTimeline(
        currentRecord: CycleRecord?,
        prediction: PredictionResult?
    ): List<DayTimelineItem> {
        val now = LocalDate.now()
        val dtf = java.time.format.DateTimeFormatter.ofPattern("EEE")

        return (-3..3).map { offset ->
            val date = now.plusDays(offset.toLong())
            val isToday = offset == 0

            var cycleDay: Int? = null
            var phaseColor: androidx.compose.ui.graphics.Color? = null
            var phaseName: String? = null

            if (currentRecord != null && prediction != null) {
                val lastDate = prediction.lastPeriodDate
                val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastDate, date).toInt() + 1
                if (daysDiff >= 1) {
                    cycleDay = daysDiff
                    val targetDays = prediction.targetPeakDate.toEpochDay() - prediction.lastPeriodDate.toEpochDay()
                    val pPeak = targetDays.toInt()
                    val pMin = (prediction.earliestLikelyDate.toEpochDay() - prediction.lastPeriodDate.toEpochDay()).toInt()
                    val pMax = (prediction.latestLikelyDate.toEpochDay() - prediction.lastPeriodDate.toEpochDay()).toInt()

                    when {
                        daysDiff in 1..5 -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.Rose40
                            phaseName = "Period"
                        }
                        daysDiff in (pPeak - 18)..(pPeak - 12) -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.OvulationColor
                            phaseName = "Fertile"
                        }
                        daysDiff in pMin..pMax -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.WindowActiveColor
                            phaseName = "Due"
                        }
                        daysDiff > pMax -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.OverdueColor
                            phaseName = "Overdue"
                        }
                        daysDiff > (pPeak - 12) -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.LutealColor
                            phaseName = "Luteal"
                        }
                        else -> {
                            phaseColor = com.thewalkersoft.tracker.ui.theme.FollicularColor
                            phaseName = "Follicular"
                        }
                    }
                }
            }

            DayTimelineItem(
                date = date,
                isToday = isToday,
                dayOfWeek = date.format(dtf),
                dayOfMonth = date.dayOfMonth,
                cycleDay = cycleDay,
                phaseColor = phaseColor,
                phaseName = phaseName
            )
        }
    }
}

