package com.thewalkersoft.tracker.domain.model

import java.time.LocalDate

data class CycleRecord(
    val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLengthDays: Int,
    val flowIntensity: String? = null,
    val notes: String? = null,
    val isPostpartumBaselineReset: Boolean = false
)
