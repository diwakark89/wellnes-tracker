package com.thewalkersoft.tracker.domain.repository

import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.PredictionResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CycleRepository {
    fun getAllPeriodLogs(): Flow<List<PeriodLogEntity>>
    fun getAllCycleRecords(): Flow<List<CycleRecord>>
    suspend fun getLatestPeriodLog(): PeriodLogEntity?
    suspend fun insertPeriodLog(log: PeriodLogEntity): Long
    suspend fun updatePeriodLog(log: PeriodLogEntity)
    suspend fun deletePeriodLog(log: PeriodLogEntity)
    suspend fun deletePeriodLogById(id: Long)

    fun getAllSymptoms(): Flow<List<SymptomLogEntity>>
    fun getSymptomsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SymptomLogEntity>>
    fun getSymptomForDate(date: LocalDate): Flow<SymptomLogEntity?>
    suspend fun insertSymptom(symptom: SymptomLogEntity): Long
    suspend fun updateSymptom(symptom: SymptomLogEntity)
    suspend fun deleteSymptomById(id: Long)

    fun getPrediction(): Flow<PredictionResult?>
}
