# Data Flow: Adaptive Cycle Prediction & Forecasting

This document details the reactive, end-to-end **Unidirectional Data Flow (UDF)** for calculating and displaying adaptive cycle predictions.

---

## 1. Flow Overview

```text
User Logs Period / App Launches
  ↓ (Dispatchers.Main)
LogPeriodBottomSheet / DashboardScreen
  ↓ (viewModelScope / Suspend fun)
DashboardViewModel.savePeriod()
  ↓ (Dispatchers.IO)
CycleRepositoryImpl.insertPeriodLog()
  ↓ (SQLite Thread Pool)
PeriodLogDao.insert() → AppDatabase ('period_logs')
  ↓ (Room Invalidation Tracker)
PeriodLogDao.getAllPeriodLogsFlow() [Reactive Flow Emission]
  ↓ (Dispatchers.Default)
CycleRepositoryImpl.observePredictionResult()
  ↳ Calls CyclePredictorEngine.predictNextCycle()
  ↳ Evaluates intervals, MAE expansion, and CycleStatus
  ↓ (viewModelScope)
DashboardViewModel updates _uiState (MutableStateFlow)
  ↓ (Dispatchers.Main)
DashboardScreen collects DashboardUiState via collectAsStateWithLifecycle()
  ↳ CurrentCycleCard recomposes (arc animation & phase color)
  ↳ PredictionWindowCard recomposes (target dates & confidence)
  ↳ PhaseInsightsCard recomposes (clinical guidance text)
```

---

## 2. Normal End-to-End Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | `LogPeriodBottomSheet` | `Dispatchers.Main` | User selects start date, flow intensity, and taps "Save Period" | Validates non-null date | Dismisses bottom sheet; calls `viewModel.savePeriod()` |
| **2** | State | `DashboardViewModel` | `viewModelScope` | Receives period data; invokes repository | Launches coroutine | Passes data to repository suspend function |
| **3** | Data | `CycleRepositoryImpl` | `Dispatchers.IO` | Maps parameters to `PeriodLogEntity` | Prepares SQLite insert | Calls `periodLogDao.insertPeriodLog(entity)` |
| **4** | Persistence | `PeriodLogDao` | SQLite Pool | Inserts new row into `period_logs` table | SQLite row created | Room triggers table invalidation |
| **5** | Persistence | `PeriodLogDao` | SQLite Pool | `getAllPeriodLogsFlow()` automatically re-queries | Emits new `List<PeriodLogEntity>` | Flow pushed upstream to Repository |
| **6** | Domain | `CyclePredictorEngine` | `Dispatchers.Default` | `CycleRepositoryImpl` transforms entities to `CycleRecord`s and passes to engine | Computes percentiles, MAE, earliest/latest dates, and `CycleStatus` | Returns new `PredictionResult` |
| **7** | State | `DashboardViewModel` | `viewModelScope` | Observes new `PredictionResult` and updates `_uiState.value` | Emits updated `DashboardUiState` via `StateFlow` | Pushed to active UI collectors |
| **8** | Presentation | `DashboardScreen` | `Dispatchers.Main` | `collectAsStateWithLifecycle()` receives new state | Triggers recomposition of Compose hierarchy | Circular dial updates progress; prediction cards display new dates |

---

## 3. Threading and Concurrency Boundaries

- **UI Thread Safety**: No database operations or heavy math are executed on `Dispatchers.Main`.
- **Database Threading**: `periodLogDao.insertPeriodLog` runs on `Dispatchers.IO`. Room executes table queries on its internal background query executor.
- **Engine Execution**: `CyclePredictorEngine` is a pure function invoked inside the repository's Flow mapping pipeline on `Dispatchers.Default`.
- **Lifecycle Protection**: `DashboardViewModel` combines multiple repository flows (`observePredictionResult`, `observeCurrentCycleDay`, `observeSymptoms`) using:
  ```kotlin
  .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = DashboardUiState(isLoading = true)
  )
  ```
  This ensures that when the app is placed in the background or the screen turns off, flow subscription stops after 5 seconds, preserving battery and device resources.

---

## 4. Failure and Edge Cases

| Scenario | System Behavior | Safeguard |
| :--- | :--- | :--- |
| **Zero Historical Logs** | Predictor returns `null` or default fallback | Dashboard displays "Log your first period to begin predictions" |
| **Spotting Entry (< 14 days)** | Engine suppresses spotting interval from cycle lengths | Prediction window remains stable; no skew introduced |
| **Postpartum Reset Flag** | Engine isolates logs prior to the reset date | Postpartum amenorrhea gap is completely excluded from MAE calculation |
| **Process Death** | Android OS terminates app process in background | On restore, Room emits latest database state into the newly created ViewModel |

---

## 5. Privacy & Telemetry Audit

- **100% On-Device**: Verified that no HTTP client, WebSocket, or remote logging occurs during this data flow.
- **Sandboxed Persistence**: All records remain within `/data/data/com.thewalkersoft.tracker/databases/wellness_tracker_db`.
