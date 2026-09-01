package com.thewalkersoft.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "period_logs")
data class PeriodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val flowIntensity: String? = null, // SPOT, LIGHT, MEDIUM, HEAVY
    val notes: String? = null,
    val isPostpartumBaselineReset: Boolean = false
)
