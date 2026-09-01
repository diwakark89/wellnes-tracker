package com.thewalkersoft.tracker.domain.model

import java.time.LocalDate

data class SymptomRecord(
    val id: Long = 0,
    val logDate: LocalDate,
    val basalBodyTemp: Float? = null,
    val crampsSeverity: Int? = null, // 1 to 5
    val mood: String? = null,
    val ovulationTestResult: String? = null
)
