package com.thewalkersoft.tracker.data.local.dao

import androidx.room.*
import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodLogDao {
    @Query("SELECT * FROM period_logs ORDER BY startDate ASC")
    fun getAllLogs(): Flow<List<PeriodLogEntity>>

    @Query("SELECT * FROM period_logs ORDER BY startDate DESC")
    fun getAllLogsDesc(): Flow<List<PeriodLogEntity>>

    @Query("SELECT * FROM period_logs ORDER BY startDate ASC")
    suspend fun getAllLogsList(): List<PeriodLogEntity>

    @Query("SELECT * FROM period_logs ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestLog(): PeriodLogEntity?

    @Query("SELECT * FROM period_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): PeriodLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PeriodLogEntity): Long

    @Update
    suspend fun updateLog(log: PeriodLogEntity): Int

    @Delete
    suspend fun deleteLog(log: PeriodLogEntity): Int

    @Query("DELETE FROM period_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long): Int
}
