package com.thewalkersoft.tracker.data.repository

import com.thewalkersoft.tracker.data.local.dao.PeriodLogDao
import com.thewalkersoft.tracker.data.local.dao.SymptomLogDao
import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngine
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CycleRepositoryImpl(
    private val periodLogDao: PeriodLogDao,
    private val symptomLogDao: SymptomLogDao,
    private val predictorEngine: CyclePredictorEngine
) : CycleRepository {

    override fun getAllPeriodLogs(): Flow<List<PeriodLogEntity>> = periodLogDao.getAllLogs()

    override fun getAllCycleRecords(): Flow<List<CycleRecord>> {
        return periodLogDao.getAllLogs().map { logs ->
            if (logs.isEmpty()) return@map emptyList()
            val sorted = logs.sortedBy { it.startDate }
            val records = mutableListOf<CycleRecord>()

            for (i in sorted.indices) {
                val current = sorted[i]
                val next = if (i + 1 < sorted.size) sorted[i + 1] else null
                val lengthDays = if (next != null) {
                    ChronoUnit.DAYS.between(current.startDate, next.startDate).toInt()
                } else {
                    ChronoUnit.DAYS.between(current.startDate, LocalDate.now()).toInt() + 1
                }

                records.add(
                    CycleRecord(
                        id = current.id,
                        startDate = current.startDate,
                        endDate = current.endDate,
                        cycleLengthDays = lengthDays,
                        flowIntensity = current.flowIntensity,
                        notes = current.notes,
                        isPostpartumBaselineReset = current.isPostpartumBaselineReset
                    )
                )
            }
            records.reversed() // Most recent first for UI
        }
    }

    override suspend fun getLatestPeriodLog(): PeriodLogEntity? = periodLogDao.getLatestLog()

    override suspend fun insertPeriodLog(log: PeriodLogEntity): Long = periodLogDao.insertLog(log)

    override suspend fun updatePeriodLog(log: PeriodLogEntity) {
        periodLogDao.updateLog(log)
    }

    override suspend fun deletePeriodLog(log: PeriodLogEntity) {
        periodLogDao.deleteLog(log)
    }

    override suspend fun deletePeriodLogById(id: Long) {
        periodLogDao.deleteLogById(id)
    }

    override fun getAllSymptoms(): Flow<List<SymptomLogEntity>> = symptomLogDao.getAllSymptoms()

    override fun getSymptomsBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<SymptomLogEntity>> = symptomLogDao.getSymptomsBetween(startDate, endDate)

    override fun getSymptomForDate(date: LocalDate): Flow<SymptomLogEntity?> =
        symptomLogDao.getSymptomForDateFlow(date)

    override suspend fun insertSymptom(symptom: SymptomLogEntity): Long =
        symptomLogDao.insertSymptom(symptom)

    override suspend fun updateSymptom(symptom: SymptomLogEntity) {
        symptomLogDao.updateSymptom(symptom)
    }

    override suspend fun deleteSymptomById(id: Long) {
        symptomLogDao.deleteSymptomById(id)
    }

    override fun getPrediction(): Flow<PredictionResult?> {
        return periodLogDao.getAllLogs().map { logs ->
            if (logs.isEmpty()) return@map null
            val sorted = logs.sortedBy { it.startDate }

            // Filter out logs before the latest postpartum baseline reset
            val latestResetIndex = sorted.indexOfLast { it.isPostpartumBaselineReset }
            val activeLogs = if (latestResetIndex != -1) {
                sorted.subList(latestResetIndex, sorted.size)
            } else {
                sorted
            }

            if (activeLogs.isEmpty()) return@map null

            val startDates = activeLogs.map { it.startDate }
            val intervals = predictorEngine.computeCycleIntervals(startDates)
            val lastPeriodDate = startDates.last()

            predictorEngine.predictNextCycle(
                lastPeriodDate = lastPeriodDate,
                cycleLengths = intervals,
                today = LocalDate.now()
            )
        }
    }
}
