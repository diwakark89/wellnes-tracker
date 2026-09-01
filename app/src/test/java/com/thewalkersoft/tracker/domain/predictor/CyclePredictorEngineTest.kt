package com.thewalkersoft.tracker.domain.predictor

import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CyclePredictorEngineTest {

    private lateinit var engine: CyclePredictorEngine

    @Before
    fun setUp() {
        engine = CyclePredictorEngine()
    }

    @Test
    fun `computeCycleIntervals filters out intervals under 14 days`() {
        val baseDate = LocalDate.of(2026, 1, 1)
        val dates = listOf(
            baseDate,
            baseDate.plusDays(5),  // Spotting entry (5 days) -> should be filtered
            baseDate.plusDays(32), // 32 - 5 = 27 days -> valid cycle
            baseDate.plusDays(60)  // 60 - 32 = 28 days -> valid cycle
        )

        val intervals = engine.computeCycleIntervals(dates)
        assertEquals(listOf(27, 28), intervals)
    }

    @Test
    fun `predictNextCycle uses fallback heuristic when fewer than 3 cycles exist`() {
        val lastPeriod = LocalDate.of(2026, 5, 1)
        val today = LocalDate.of(2026, 5, 10) // Day 10
        val cycles = listOf(30, 32) // Only 2 cycles

        val prediction = engine.predictNextCycle(lastPeriod, cycles, today)

        assertEquals(10, prediction.currentCycleDay)
        assertEquals(lastPeriod.plusDays(30), prediction.earliestLikelyDate)
        assertEquals(lastPeriod.plusDays(35), prediction.targetPeakDate)
        assertEquals(lastPeriod.plusDays(40), prediction.latestLikelyDate)
        assertEquals(10, prediction.confidenceWindowDays)
        assertEquals(CycleStatus.FOLLICULAR, prediction.status)
    }

    @Test
    fun `predictNextCycle correctly computes percentile and MAE expansion for fluctuating cycles`() {
        val lastPeriod = LocalDate.of(2026, 6, 1)
        val today = LocalDate.of(2026, 6, 15) // Day 15
        // Irregular fluctuating cycles: 30, 32, 36, 40, 34
        val cycles = listOf(30, 32, 36, 40, 34)

        val prediction = engine.predictNextCycle(lastPeriod, cycles, today)

        // Sorted recent: [30, 32, 34, 36, 40]
        // size = 5.
        // p25 index = (5 * 0.25).toInt() = 1 -> 32
        // median index = 5 / 2 = 2 -> 34
        // p75 index = (5 * 0.75).toInt() = 3 -> 36
        // errors from 34: |30-34|=4, |32-34|=2, |34-34|=0, |36-34|=2, |40-34|=6 -> total = 14
        // MAE = 14 / 5 = 2
        // expandedMin = (32 - (2/2)) = 31
        // expandedMax = 36 + (2/2) = 37
        assertEquals(34, prediction.targetPeakDate.toEpochDay() - lastPeriod.toEpochDay())
        assertEquals(31, prediction.earliestLikelyDate.toEpochDay() - lastPeriod.toEpochDay())
        assertEquals(37, prediction.latestLikelyDate.toEpochDay() - lastPeriod.toEpochDay())
        assertEquals(6, prediction.confidenceWindowDays)
    }

    @Test
    fun `predictNextCycle transitions to OVERDUE when current day exceeds pMax`() {
        val lastPeriod = LocalDate.of(2026, 1, 1)
        val cycles = listOf(28, 28, 28) // pMin=28, pPeak=28, pMax=28
        val today = LocalDate.of(2026, 2, 5) // Day 36 (> 28)

        val prediction = engine.predictNextCycle(lastPeriod, cycles, today)

        assertEquals(CycleStatus.OVERDUE, prediction.status)
        assertTrue(prediction.currentCycleDay > (prediction.latestLikelyDate.toEpochDay() - lastPeriod.toEpochDay()))
    }

    @Test
    fun `predictNextCycle detects PREDICTION_WINDOW_ACTIVE and OVULATION_WINDOW phases`() {
        val lastPeriod = LocalDate.of(2026, 3, 1)
        val cycles = listOf(28, 28, 28)

        // Ovulation window is roughly (pPeak - 18)..(pPeak - 12) -> Day 10 to 16
        val ovulationDay = LocalDate.of(2026, 3, 14) // Day 14
        val ovPrediction = engine.predictNextCycle(lastPeriod, cycles, ovulationDay)
        assertEquals(CycleStatus.OVULATION_WINDOW, ovPrediction.status)

        // Active prediction window is currentDay >= pMin
        val activeWindowDay = LocalDate.of(2026, 3, 28) // Day 28
        val activePrediction = engine.predictNextCycle(lastPeriod, cycles, activeWindowDay)
        assertEquals(CycleStatus.PREDICTION_WINDOW_ACTIVE, activePrediction.status)
    }

    @Test
    fun `isolatePostpartumRecords drops historical records prior to the latest reset`() {
        val baseDate = LocalDate.of(2025, 1, 1)
        val records = listOf(
            CycleRecord(1, baseDate, null, 60, isPostpartumBaselineReset = false),
            CycleRecord(2, baseDate.plusDays(60), null, 45, isPostpartumBaselineReset = false),
            // Postpartum reset baseline here!
            CycleRecord(3, baseDate.plusDays(105), null, 30, isPostpartumBaselineReset = true),
            CycleRecord(4, baseDate.plusDays(135), null, 32, isPostpartumBaselineReset = false)
        )

        val isolated = engine.isolatePostpartumRecords(records)

        assertEquals(2, isolated.size)
        assertEquals(3L, isolated[0].id)
        assertEquals(4L, isolated[1].id)
    }
}
