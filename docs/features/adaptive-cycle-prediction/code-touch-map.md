# Code Touch Map: Adaptive Cycle Prediction & Forecasting

This document serves as the authoritative architectural routing map for the **Adaptive Cycle Prediction & Forecasting** feature.

---

## Code Touch Map

### 1. Presentation Layer — Jetpack Compose UI

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Screen** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/DashboardScreen.kt` | `DashboardScreen` | `NavigationHost.kt` (`Screen.Dashboard`) | Collects `DashboardUiState` via `collectAsStateWithLifecycle()` | Displays loading indicator during initialization; renders cards when state is ready | Top-level dashboard layout and lifecycle |
| **Cycle Dial** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/components/CurrentCycleCard.kt` | `CurrentCycleCard` | `DashboardScreen.kt` | Receives current day, total days, and `CycleStatus` | Arc stroke color dynamically reflects phase; smooth animation transitions | Visual center of dashboard |
| **Prediction Card** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/components/PredictionWindowCard.kt` | `PredictionWindowCard` | `DashboardScreen.kt` | Receives `PredictionResult` | Displays formatted target date range; displays fallback text if insufficient data | Prediction window display |
| **Phase Insights** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/components/PhaseInsightsCard.kt` | `PhaseInsightsCard` | `DashboardScreen.kt` | Receives `CycleStatus` | Displays phase explanation and clinical guidance | Educational content display |
| **Log Sheet** | `app/src/main/java/com/thewalkersoft/tracker/ui/logging/LogPeriodBottomSheet.kt` | `LogPeriodBottomSheet` | Quick log action / History item edit | Reads draft date, flow, and reset toggle | Validates date ranges; triggers `onSave` callback | Input mechanism for periods |

### 2. State & Lifecycle Layer — ViewModels & Coroutines

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ViewModel** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/DashboardViewModel.kt` | `DashboardViewModel` | `AppViewModelProvider.Factory` | Observes `CycleRepository.observePredictionResult()` | Exposes `StateFlow<DashboardUiState>` via `SharingStarted.WhileSubscribed(5000)` | Primary state coordinator |
| **UiState** | `app/src/main/java/com/thewalkersoft/tracker/ui/dashboard/DashboardViewModel.kt` | `DashboardUiState` | `DashboardViewModel` & `DashboardScreen` | Immutable data class | Contains `predictionResult`, `currentCycleDay`, `cycleStatus`, and `timeline` | Recomposition trigger for Dashboard UI |

### 3. Domain Layer — Pure Business Logic & Models

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Math Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Engine** | `app/src/main/java/com/thewalkersoft/tracker/domain/predictor/CyclePredictorEngine.kt` | `CyclePredictorEngine` | `CycleRepositoryImpl` | Pure Kotlin object | Spotting suppression (<14 days); postpartum reset; percentile rolling window; dynamic MAE | Algorithmic prediction logic |
| **Prediction Model** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/PredictionResult.kt` | `PredictionResult` | Engine, Repository, ViewModel | Immutable domain model | Holds `predictedStartDate`, `earliestLikelyDate`, `latestLikelyDate`, `confidenceScore` | Domain representation of forecast |
| **Status Enum** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/CycleStatus.kt` | `CycleStatus` | Engine, UI Cards, ViewModel | Enum definition | `FOLLICULAR`, `OVULATION_WINDOW`, `LUTEAL`, `PREDICTION_WINDOW_ACTIVE`, `OVERDUE` | Semantic styling across UI |
| **Cycle Record** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/CycleRecord.kt` | `CycleRecord` | Engine, Repository | Computed interval record | Computes `cycleLengthDays = ChronoUnit.DAYS.between(start, nextStart)` | Historical cycle calculations |
| **Repository Contract** | `app/src/main/java/com/thewalkersoft/tracker/domain/repository/CycleRepository.kt` | `CycleRepository` | Injected into ViewModels; implemented by Data | Interface contract | Defines `observePredictionResult()`, `observeCurrentCycleDay()`, etc. | Core abstraction boundary |

### 4. Data Layer — Room SQLite & Persistence

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Database Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Repository Impl** | `app/src/main/java/com/thewalkersoft/tracker/data/repository/CycleRepositoryImpl.kt` | `CycleRepositoryImpl` | Implements `CycleRepository`; calls `PeriodLogDao` | Reads `PeriodLogEntity` flow; invokes `CyclePredictorEngine` | Maps Room entities to domain models; dispatches Room flows | Domain-database bridge |
| **DAO** | `app/src/main/java/com/thewalkersoft/tracker/data/local/dao/PeriodLogDao.kt` | `PeriodLogDao` | `CycleRepositoryImpl` | Queries `period_logs` table | `getAllPeriodLogsFlow(): Flow<List<PeriodLogEntity>>` sorted by `startDate DESC` | Observable database queries |
| **Entity** | `app/src/main/java/com/thewalkersoft/tracker/data/local/entity/PeriodLogEntity.kt` | `PeriodLogEntity` | `PeriodLogDao`, `AppDatabase` | SQLite table `period_logs` | Primary key `id: Long = 0`; columns for `startDate`, `endDate`, `isPostpartumBaselineReset` | SQLite schema structure |
| **Type Converters** | `app/src/main/java/com/thewalkersoft/tracker/data/local/converter/DateConverters.kt` | `DateConverters` | Room Database | Converts `LocalDate` <-> `Long` (epochDay) | Lossless integer epoch-day serialization | Room date handling |
| **Database** | `app/src/main/java/com/thewalkersoft/tracker/data/local/AppDatabase.kt` | `AppDatabase` | `DefaultAppContainer` | Database singleton | Room version 1; entities `PeriodLogEntity`, `SymptomLogEntity` | Local storage lifecycle |

### 5. Dependency Injection

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / DI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **AppContainer** | `app/src/main/java/com/thewalkersoft/tracker/di/AppContainer.kt` | `DefaultAppContainer` | `TrackerApplication.kt` | Instantiates `AppDatabase` and `CycleRepositoryImpl` | Lazy singleton creation; zero reflection | DI lifecycle |
| **ViewModel Factory** | `app/src/main/java/com/thewalkersoft/tracker/ui/ViewModelFactory.kt` | `AppViewModelProvider.Factory` | `DashboardScreen.kt` | Injects `cycleRepository` into `DashboardViewModel` | Factory pattern using AndroidX `ViewModelProvider` | ViewModel injection |

### 6. Tests & Acceptance Evidence

| Test Suite | Path | Symbol | Verification Target | Command |
| :--- | :--- | :--- | :--- | :--- |
| **Engine Unit Tests** | `app/src/test/java/com/thewalkersoft/tracker/domain/predictor/CyclePredictorEngineTest.kt` | `CyclePredictorEngineTest` | Percentile math, spotting suppression, postpartum reset, MAE intervals | `./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngineTest"` |
| **ViewModel Tests** | `app/src/test/java/com/thewalkersoft/tracker/ui/dashboard/DashboardViewModelTest.kt` | `DashboardViewModelTest` | StateFlow emissions, coroutine flow updates, fake repository integration | `./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.ui.dashboard.DashboardViewModelTest"` |
