package com.thewalkersoft.tracker.ui.dashboard

import com.thewalkersoft.tracker.data.local.entity.PeriodLogEntity
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import com.thewalkersoft.tracker.ui.security.SecurityPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCycleRepository
    private lateinit var fakeSecurityPreferences: FakeSecurityPreferences
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCycleRepository()
        fakeSecurityPreferences = FakeSecurityPreferences()
        viewModel = DashboardViewModel(fakeRepository, fakeSecurityPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quickLogMood creates new symptom log when none exists for today`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        viewModel.quickLogMood("Happy")
        advanceUntilIdle()

        val symptom = fakeRepository.getSymptomByDate(today)
        assertNotNull(symptom)
        assertEquals("Happy", symptom?.mood)
    }

    @Test
    fun `quickLogMood updates existing symptom log while preserving other fields`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        fakeRepository.insertSymptom(
            SymptomLogEntity(
                id = 1,
                logDate = today,
                basalBodyTemp = 36.6f,
                crampsSeverity = 3,
                mood = "Tired"
            )
        )

        viewModel.quickLogMood("Calm")
        advanceUntilIdle()

        val symptom = fakeRepository.getSymptomByDate(today)
        assertNotNull(symptom)
        assertEquals("Calm", symptom?.mood)
        assertEquals(3, symptom?.crampsSeverity)
        assertEquals(36.6f, symptom?.basalBodyTemp)
    }

    @Test
    fun `quickLogCramps updates cramps severity for today`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        viewModel.quickLogCramps(4)
        advanceUntilIdle()

        val symptom = fakeRepository.getSymptomByDate(today)
        assertNotNull(symptom)
        assertEquals(4, symptom?.crampsSeverity)
    }

    @Test
    fun `quickLogPeriodStartedToday inserts a period log starting today`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        viewModel.quickLogPeriodStartedToday()
        advanceUntilIdle()

        val latest = fakeRepository.getLatestPeriodLog()
        assertNotNull(latest)
        assertEquals(today, latest?.startDate)
        assertEquals("MEDIUM", latest?.flowIntensity)
    }

    @Test
    fun `savePeriodAndSymptoms saves both period log and full symptom details`() = runTest(testDispatcher) {
        val startDate = LocalDate.of(2026, 9, 1)
        val endDate = LocalDate.of(2026, 9, 5)

        viewModel.savePeriodAndSymptoms(
            startDate = startDate,
            endDate = endDate,
            flowIntensity = "HEAVY",
            notes = "First postpartum period",
            isPostpartumReset = true,
            bbt = 36.8f,
            cramps = 2,
            mood = "Calm",
            ovulation = "POSITIVE"
        )
        advanceUntilIdle()

        val period = fakeRepository.getLatestPeriodLog()
        assertNotNull(period)
        assertEquals(startDate, period?.startDate)
        assertEquals(endDate, period?.endDate)
        assertEquals("HEAVY", period?.flowIntensity)
        assertTrue(period?.isPostpartumBaselineReset == true)

        val symptom = fakeRepository.getSymptomByDate(startDate)
        assertNotNull(symptom)
        assertEquals(36.8f, symptom?.basalBodyTemp)
        assertEquals(2, symptom?.crampsSeverity)
        assertEquals("Calm", symptom?.mood)
        assertEquals("POSITIVE", symptom?.ovulationTestResult)
    }

    // Fake implementations for testing
    private class FakeCycleRepository : CycleRepository {
        private val periodLogs = mutableListOf<PeriodLogEntity>()
        private val symptoms = mutableListOf<SymptomLogEntity>()

        private val periodLogsFlow = MutableStateFlow<List<PeriodLogEntity>>(emptyList())
        private val cycleRecordsFlow = MutableStateFlow<List<CycleRecord>>(emptyList())
        private val predictionFlow = MutableStateFlow<PredictionResult?>(null)

        override fun getAllPeriodLogs(): Flow<List<PeriodLogEntity>> = periodLogsFlow
        override fun getAllCycleRecords(): Flow<List<CycleRecord>> = cycleRecordsFlow
        override suspend fun getLatestPeriodLog(): PeriodLogEntity? = periodLogs.lastOrNull()
        override suspend fun getPeriodLogById(id: Long): PeriodLogEntity? = periodLogs.find { it.id == id }

        override suspend fun insertPeriodLog(log: PeriodLogEntity): Long {
            val nextId = (periodLogs.size + 1).toLong()
            val inserted = log.copy(id = nextId)
            periodLogs.add(inserted)
            periodLogsFlow.value = periodLogs.toList()
            return nextId
        }

        override suspend fun updatePeriodLog(log: PeriodLogEntity) {
            val index = periodLogs.indexOfFirst { it.id == log.id }
            if (index != -1) {
                periodLogs[index] = log
                periodLogsFlow.value = periodLogs.toList()
            }
        }

        override suspend fun deletePeriodLog(log: PeriodLogEntity) {
            periodLogs.removeAll { it.id == log.id }
            periodLogsFlow.value = periodLogs.toList()
        }

        override suspend fun deletePeriodLogById(id: Long) {
            periodLogs.removeAll { it.id == id }
            periodLogsFlow.value = periodLogs.toList()
        }

        override fun getAllSymptoms(): Flow<List<SymptomLogEntity>> = MutableStateFlow(symptoms.toList())
        override fun getSymptomsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SymptomLogEntity>> =
            MutableStateFlow(symptoms.filter { it.logDate in startDate..endDate })

        override fun getSymptomForDate(date: LocalDate): Flow<SymptomLogEntity?> =
            MutableStateFlow(symptoms.find { it.logDate == date })

        override suspend fun getSymptomByDate(date: LocalDate): SymptomLogEntity? =
            symptoms.find { it.logDate == date }

        override suspend fun insertSymptom(symptom: SymptomLogEntity): Long {
            val existingIndex = symptoms.indexOfFirst { it.logDate == symptom.logDate }
            val nextId = if (symptom.id != 0L) symptom.id else (symptoms.size + 1).toLong()
            val entry = symptom.copy(id = nextId)
            if (existingIndex != -1) {
                symptoms[existingIndex] = entry
            } else {
                symptoms.add(entry)
            }
            return nextId
        }

        override suspend fun updateSymptom(symptom: SymptomLogEntity) {
            val index = symptoms.indexOfFirst { it.logDate == symptom.logDate || it.id == symptom.id }
            if (index != -1) {
                symptoms[index] = symptom
            } else {
                symptoms.add(symptom)
            }
        }

        override suspend fun deleteSymptomById(id: Long) {
            symptoms.removeAll { it.id == id }
        }

        override fun getPrediction(): Flow<PredictionResult?> = predictionFlow
    }

    private class FakeSecurityPreferences : SecurityPreferences {
        private val _isBiometric = MutableStateFlow(false)
        override val isBiometricEnabled: StateFlow<Boolean> = _isBiometric

        override fun setBiometricEnabled(enabled: Boolean) {
            _isBiometric.value = enabled
        }
    }
}
