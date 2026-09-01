package com.thewalkersoft.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val logDate: LocalDate,
    val basalBodyTemp: Float? = null,
    val crampsSeverity: Int? = null, // 1 to 5
    val mood: String? = null, // HAPPY, CALM, TIRED, IRRITABLE, ANXIOUS, SAD
    val ovulationTestResult: String? = null // NEGATIVE, POSITIVE, PEAK
)
