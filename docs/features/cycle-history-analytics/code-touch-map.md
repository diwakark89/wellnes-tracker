# Code Touch Map: Cycle History Analytics & Timeline

This document serves as the authoritative architectural routing map for the **Cycle History Analytics & Timeline** feature.

---

## Code Touch Map

### 1. Presentation Layer — Jetpack Compose UI & Canvas

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Screen** | `app/src/main/java/com/thewalkersoft/tracker/ui/history/HistoryScreen.kt` | `HistoryScreen` | `NavigationHost.kt` (`Screen.History`) | Collects `cycleRecords` from `HistoryViewModel` | Renders `CycleLengthChart` and lazy list of `CycleHistoryItem` | History navigation and top-level list |
| **Length Bar Chart** | `app/src/main/java/com/thewalkersoft/tracker/ui/history/components/CycleLengthChart.kt` | `CycleLengthChart` | `HistoryScreen.kt` | Reads completed `CycleRecord`s | **Zero object allocations in `DrawScope`**; draws rounded bars and baseline dashed line on Canvas | Bar chart rendering and Canvas performance |
| **History Row Card**| `app/src/main/java/com/thewalkersoft/tracker/ui/history/components/CycleHistoryItem.kt` | `CycleHistoryItem` | `HistoryScreen.kt` | Displays single `CycleRecord` | Displays duration badge, flow pill, date range; triggers edit on tap | Row visual structure and tap interactions |

### 2. State & Lifecycle Layer — ViewModels & Coroutines

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ViewModel** | `app/src/main/java/com/thewalkersoft/tracker/ui/history/HistoryViewModel.kt` | `HistoryViewModel` | `AppViewModelProvider.Factory` | Observes `CycleRepository.getAllCycleRecords()` | Exposes `StateFlow<List<CycleRecord>>`; orchestrates `updatePeriodAndSymptoms` and `deleteLog` | State coordination and entry mutation |

### 3. Domain Layer — Pure Business Logic & Contracts

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Math Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Cycle Record** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/CycleRecord.kt` | `CycleRecord` | History UI, Repository, Engine | Computed domain interval | Computes days between start dates: `ChronoUnit.DAYS.between(startDate, nextStartDate)` | Historical interval metrics |
| **Repository Contract**| `app/src/main/java/com/thewalkersoft/tracker/domain/repository/CycleRepository.kt` | `CycleRepository` | Injected into ViewModels; implemented by Data | Interface contract | Defines `getAllCycleRecords()`, `updatePeriodLog()`, `deletePeriodLogById()` | Core data contract |

### 4. Data Layer — Room SQLite & Persistence

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Database Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Repository Impl** | `app/src/main/java/com/thewalkersoft/tracker/data/repository/CycleRepositoryImpl.kt` | `CycleRepositoryImpl` | Implements `CycleRepository`; calls `PeriodLogDao` | Transforms raw `PeriodLogEntity` entries into `CycleRecord`s | Calculates intervals off the main thread; updates Room | Historical data evaluation |
| **DAO** | `app/src/main/java/com/thewalkersoft/tracker/data/local/dao/PeriodLogDao.kt` | `PeriodLogDao` | `CycleRepositoryImpl` | Queries `period_logs` table | `getAllPeriodLogsFlow()` sorted by `startDate DESC`; `@Update`, `@Delete` | Database read/write operations |
| **Entity** | `app/src/main/java/com/thewalkersoft/tracker/data/local/entity/PeriodLogEntity.kt` | `PeriodLogEntity` | `PeriodLogDao`, `AppDatabase` | SQLite table `period_logs` | Primary key `id: Long = 0`; columns for `startDate`, `endDate`, `flowIntensity`, `notes` | Schema representation |

### 5. Tests & Acceptance Evidence

| Test Suite | Path | Symbol | Verification Target | Command |
| :--- | :--- | :--- | :--- | :--- |
| **Domain Tests** | `app/src/test/java/com/thewalkersoft/tracker/domain/predictor/CyclePredictorEngineTest.kt` | `computeCycleIntervals` tests | Interval calculation, sorting, and spotting suppression | `./gradlew test` |
| **Compose Preview**| `app/src/main/java/com/thewalkersoft/tracker/ui/history/components/CycleLengthChart.kt` | `@Preview fun CycleLengthChartPreview` | Verifies bar heights, average indicator, and date labels | Android Studio Preview |
