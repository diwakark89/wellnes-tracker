package com.thewalkersoft.tracker.domain.predictor

import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class CyclePredictorEngine {

    /**
     * Filters historical period start dates based on the latest postpartum reset baseline.
     * Drops all dates prior to the most recent reset date.
     */
    fun isolatePostpartumRecords(records: List<CycleRecord>): List<CycleRecord> {
        val sorted = records.sortedBy { it.startDate }
        val latestResetIndex = sorted.indexOfLast { it.isPostpartumBaselineReset }
        return if (latestResetIndex != -1) {
            sorted.subList(latestResetIndex, sorted.size)
        } else {
            sorted
        }
    }

    /**
     * Computes valid cycle intervals (in days) between consecutive start dates.
     * Ignores intervals under 14 days to filter out breakthrough bleeding/spotting noise.
     */
    fun computeCycleIntervals(sortedStartDates: List<LocalDate>): List<Int> {
        if (sortedStartDates.size < 2) return emptyList()
        return sortedStartDates.zipWithNext { current, next ->
            ChronoUnit.DAYS.between(current, next).toInt()
        }.filter { it >= 14 }
    }

    /**
     * Predicts the next cycle window using rolling medians, percentiles, and MAE expansion.
     */
    fun predictNextCycle(
        lastPeriodDate: LocalDate,
        cycleLengths: List<Int>,
        today: LocalDate = LocalDate.now()
    ): PredictionResult {
        val currentDay = ChronoUnit.DAYS.between(lastPeriodDate, today).toInt() + 1

        // Fallback heuristic if historical cycle intervals are fewer than 3
        if (cycleLengths.size < 3) {
            val defaultMin = 30
            val defaultPeak = 35
            val defaultMax = 40
            return buildPredictionResult(
                lastPeriodDate = lastPeriodDate,
                currentDay = currentDay,
                pMin = defaultMin,
                pPeak = defaultPeak,
                pMax = defaultMax,
                mae = 2,
                recentCycleCount = cycleLengths.size
            )
        }

        // Focus on recent cycles (up to last 6 cycles) to adapt to recent baseline changes
        val recentCycles = cycleLengths.takeLast(6).sorted()

        val p25 = recentCycles[(recentCycles.size * 0.25).toInt()]
        val median = recentCycles[recentCycles.size / 2]
        val p75 = recentCycles[(recentCycles.size * 0.75).coerceAtMost(recentCycles.size - 1.0).toInt()]

        // Calculate dynamic Mean Absolute Error (MAE) on recent predictions
        val mae = calculateMeanAbsoluteError(recentCycles, median)
        val expandedMin = (p25 - (mae / 2)).coerceAtLeast(21)
        val expandedMax = p75 + (mae / 2)

        return buildPredictionResult(
            lastPeriodDate = lastPeriodDate,
            currentDay = currentDay,
            pMin = expandedMin,
            pPeak = median,
            pMax = expandedMax,
            mae = mae,
            recentCycleCount = recentCycles.size
        )
    }

    /**
     * Calculates Mean Absolute Error of recent cycles against the median baseline.
     */
    fun calculateMeanAbsoluteError(cycles: List<Int>, baseline: Int): Int {
        if (cycles.isEmpty()) return 2
        val totalError = cycles.sumOf { abs(it - baseline) }
        return (totalError / cycles.size).coerceAtLeast(1)
    }

    private fun buildPredictionResult(
        lastPeriodDate: LocalDate,
        currentDay: Int,
        pMin: Int,
        pPeak: Int,
        pMax: Int,
        mae: Int = 0,
        recentCycleCount: Int = 0
    ): PredictionResult {
        val earliest = lastPeriodDate.plusDays(pMin.toLong())
        val peak = lastPeriodDate.plusDays(pPeak.toLong())
        val latest = lastPeriodDate.plusDays(pMax.toLong())

        val status = when {
            currentDay > pMax -> CycleStatus.OVERDUE
            currentDay >= pMin -> CycleStatus.PREDICTION_WINDOW_ACTIVE
            currentDay in (pPeak - 18)..(pPeak - 12) -> CycleStatus.OVULATION_WINDOW
            currentDay > (pPeak - 12) -> CycleStatus.LUTEAL
            else -> CycleStatus.FOLLICULAR
        }

        return PredictionResult(
            lastPeriodDate = lastPeriodDate,
            currentCycleDay = currentDay,
            earliestLikelyDate = earliest,
            targetPeakDate = peak,
            latestLikelyDate = latest,
            confidenceWindowDays = pMax - pMin,
            status = status,
            meanAbsoluteError = mae,
            recentCycleCount = recentCycleCount
        )
    }
}
