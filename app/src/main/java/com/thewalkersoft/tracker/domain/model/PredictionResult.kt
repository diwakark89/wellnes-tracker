package com.thewalkersoft.tracker.domain.model

import java.time.LocalDate

data class PredictionResult(
    val lastPeriodDate: LocalDate,
    val currentCycleDay: Int,
    val earliestLikelyDate: LocalDate,
    val targetPeakDate: LocalDate,
    val latestLikelyDate: LocalDate,
    val confidenceWindowDays: Int,
    val status: CycleStatus,
    val meanAbsoluteError: Int = 0,
    val recentCycleCount: Int = 0
)
