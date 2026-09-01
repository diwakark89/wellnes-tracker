package com.thewalkersoft.tracker.data.local.dao

import androidx.room.*
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SymptomLogDao {
    @Query("SELECT * FROM symptom_logs ORDER BY logDate DESC")
    fun getAllSymptoms(): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs ORDER BY logDate ASC")
    suspend fun getAllSymptomsList(): List<SymptomLogEntity>

    @Query("SELECT * FROM symptom_logs WHERE logDate = :date LIMIT 1")
    fun getSymptomForDateFlow(date: LocalDate): Flow<SymptomLogEntity?>

    @Query("SELECT * FROM symptom_logs WHERE logDate = :date LIMIT 1")
    suspend fun getSymptomForDate(date: LocalDate): SymptomLogEntity?

    @Query("SELECT * FROM symptom_logs WHERE logDate BETWEEN :startDate AND :endDate ORDER BY logDate ASC")
    fun getSymptomsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SymptomLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomLogEntity): Long

    @Update
    suspend fun updateSymptom(symptom: SymptomLogEntity): Int

    @Delete
    suspend fun deleteSymptom(symptom: SymptomLogEntity): Int

    @Query("DELETE FROM symptom_logs WHERE id = :id")
    suspend fun deleteSymptomById(id: Long): Int
}
