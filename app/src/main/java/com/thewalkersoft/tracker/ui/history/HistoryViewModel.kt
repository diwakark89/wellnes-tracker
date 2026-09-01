package com.thewalkersoft.tracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(
    private val repository: CycleRepository
) : ViewModel() {

    val cycleRecords: StateFlow<List<CycleRecord>> = repository.getAllCycleRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getSymptomForDate(date: LocalDate): SymptomLogEntity? {
        return repository.getSymptomByDate(date)
    }

    fun updatePeriodAndSymptoms(
        periodId: Long,
        originalStartDate: LocalDate,
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
            repository.updatePeriodLog(
                PeriodLogEntity(
                    id = periodId,
                    startDate = startDate,
                    endDate = endDate,
                    flowIntensity = flowIntensity,
                    notes = notes,
                    isPostpartumBaselineReset = isPostpartumReset
                )
            )

            val existingOldSymptom = repository.getSymptomByDate(originalStartDate)
            val hasAnySymptom = bbt != null || cramps != null || mood != null || ovulation != null

            if (hasAnySymptom) {
                if (originalStartDate != startDate) {
                    if (existingOldSymptom != null) {
                        repository.deleteSymptomById(existingOldSymptom.id)
                    }
                    val existingNewSymptom = repository.getSymptomByDate(startDate)
                    if (existingNewSymptom != null) {
                        repository.updateSymptom(
                            existingNewSymptom.copy(
                                basalBodyTemp = bbt,
                                crampsSeverity = cramps,
                                mood = mood,
                                ovulationTestResult = ovulation
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
                } else {
                    if (existingOldSymptom != null) {
                        repository.updateSymptom(
                            existingOldSymptom.copy(
                                basalBodyTemp = bbt,
                                crampsSeverity = cramps,
                                mood = mood,
                                ovulationTestResult = ovulation
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
            } else {
                if (existingOldSymptom != null) {
                    repository.deleteSymptomById(existingOldSymptom.id)
                }
            }
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deletePeriodLogById(id)
        }
    }
}
