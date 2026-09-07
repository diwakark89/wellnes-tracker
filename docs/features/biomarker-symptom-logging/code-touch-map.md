# Code Touch Map: Biomarker & Daily Symptom Logging

This document serves as the authoritative architectural routing map for the **Biomarker & Daily Symptom Logging** feature.

---

## Code Touch Map

### 1. Presentation Layer — Jetpack Compose UI & Canvas

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Screen** | `app/src/main/java/com/thewalkersoft/tracker/ui/symptoms/SymptomsScreen.kt` | `SymptomsScreen` | `NavigationHost.kt` (`Screen.Symptoms`) | Collects `symptoms` from `SymptomsViewModel` | Renders `BbtTrendChart` when data exists; renders symptom log cards | Symptoms screen navigation and top-level layout |
| **BBT Chart** | `app/src/main/java/com/thewalkersoft/tracker/ui/symptoms/components/BbtTrendChart.kt` | `BbtTrendChart` | `SymptomsScreen.kt` | Reads filtered symptoms (`basalBodyTemp != null`) | **Zero object allocations in `DrawScope`**; cubic Bezier path smoothing (`cubicTo`) | Temperature visualization and canvas rendering performance |
| **Log Sheet** | `app/src/main/java/com/thewalkersoft/tracker/ui/logging/LogPeriodBottomSheet.kt` | `LogPeriodBottomSheet` | Dashboard FAB / Symptoms FAB | Manages draft inputs for BBT, cramps, mood, LH | Float conversion for BBT; integer mapping for cramps slider (1-5) | Biomarker data capture workflow |

### 2. State & Lifecycle Layer — ViewModels & Coroutines

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ViewModel** | `app/src/main/java/com/thewalkersoft/tracker/ui/symptoms/SymptomsViewModel.kt` | `SymptomsViewModel` | `AppViewModelProvider.Factory` | Observes `CycleRepository.getAllSymptoms()` | Exposes `StateFlow<List<SymptomLogEntity>>` via `WhileSubscribed(5000)` | State emission, symptom deletion actions |

### 3. Domain Layer — Pure Business Logic & Contracts

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Math Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Repository Contract** | `app/src/main/java/com/thewalkersoft/tracker/domain/repository/CycleRepository.kt` | `CycleRepository` | Injected into ViewModels; implemented by Data | Interface contract | Defines `getAllSymptoms()`, `insertSymptom()`, `deleteSymptomById()`, `getSymptomByDate()` | Domain repository boundary |
| **Symptom Record** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/SymptomRecord.kt` | `SymptomRecord` | Engine, Repository | Domain representation | Captures biomarker signals independently of Room SQLite entity | Domain modeling |

### 4. Data Layer — Room SQLite & Persistence

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Database Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Repository Impl** | `app/src/main/java/com/thewalkersoft/tracker/data/repository/CycleRepositoryImpl.kt` | `CycleRepositoryImpl` | Implements `CycleRepository`; calls `SymptomLogDao` | Dispatches Room DAO calls on `Dispatchers.IO` | Bridges Room entities with repository Flow streams | Data layer orchestration |
| **DAO** | `app/src/main/java/com/thewalkersoft/tracker/data/local/dao/SymptomLogDao.kt` | `SymptomLogDao` | `CycleRepositoryImpl` | Queries `symptom_logs` table | `getAllSymptoms(): Flow<List<SymptomLogEntity>>` sorted by `logDate DESC` | SQLite queries, reactivity, and indexing |
| **Entity** | `app/src/main/java/com/thewalkersoft/tracker/data/local/entity/SymptomLogEntity.kt` | `SymptomLogEntity` | `SymptomLogDao`, `AppDatabase` | SQLite table `symptom_logs` | Primary key `id: Long = 0`; columns for `logDate`, `basalBodyTemp`, `crampsSeverity`, `mood`, `ovulationTestResult` | Database schema structure |
| **Type Converters** | `app/src/main/java/com/thewalkersoft/tracker/data/local/converter/DateConverters.kt` | `DateConverters` | Room Database | Converts `LocalDate` <-> `Long` (epochDay) | Lossless integer epoch-day serialization | Room date persistence |

### 5. Tests & Acceptance Evidence

| Test Suite | Path | Symbol | Verification Target | Command |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | `app/src/test/java/com/thewalkersoft/tracker/...` | `ExampleUnitTest` | Basic test verification | `./gradlew test` |
| **Compose Preview** | `app/src/main/java/com/thewalkersoft/tracker/ui/symptoms/components/BbtTrendChart.kt` | `@Preview fun BbtTrendChartPreview` | Verifies Bezier curve, gradient fill, and temperature guides | Android Studio Compose Preview |
