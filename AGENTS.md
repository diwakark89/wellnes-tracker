# AGENTS.md — Agent & Developer Operating Manual

Welcome to **Wellness Tracker** (`com.thewalkersoft.tracker`). This document serves as the primary operational manual, architectural specification, and engineering playbook for AI coding agents and software engineers working in this codebase.

---

## 1. Project Mission & Core Philosophy

**Wellness Tracker** is a privacy-first, on-device Android application engineered for menstrual cycle tracking, biomarker and symptom logging, fertility window forecasting, and clinical PDF report generation — with dedicated algorithmic support for irregular cycles and postpartum recovery.

### Core Non-Negotiables & Philosophy
1. **100% On-Device & Zero-Telemetry**: All health records, predictions, biomarkers, logs, and generated clinical PDF summaries exist **exclusively on the local device**. No network permissions (`android.permission.INTERNET` is intentionally omitted), analytics SDKs, crash trackers, or cloud sync backends may ever be introduced.
2. **Clinical Utility**: Features must provide clear, medically accurate data visualization and allow local generation of doctor-ready clinical PDF summaries.
3. **Adaptive Prediction**: Menstrual cycles vary naturally (especially postpartum or with conditions like PCOS). The system utilizes dynamic statistical models (rolling percentile spans with Mean Absolute Error expansion) rather than rigid 28-day assumptions.
4. **Local Biometric Security**: User health records can be gated with hardware-backed biometric authentication (fingerprint/face unlock via AndroidX `BiometricPrompt`).

---

## 2. Tech Stack & Dependencies

| Layer | Technology | Version / Coordinates | Details |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` | Target JVM 11 |
| **Build Tooling** | AGP / Gradle | `9.2.1` | Kotlin Compose compiler plugin |
| **Android SDK** | Android SDK | Min: `34`, Target: `36`, Compile: `36` | Android 14+ baseline |
| **UI Framework** | Jetpack Compose | BOM `2025.02.00` | Material 3 (`androidx.compose.material3`) |
| **Architecture** | Clean Architecture + MVVM | Unidirectional Data Flow (UDF) | `StateFlow` + `UiState` patterns |
| **Persistence** | AndroidX Room | `2.6.1` + KSP (`2.2.10-2.0.2`) | Kotlin Symbol Processing, Type Converters |
| **Async / Stream** | Kotlin Coroutines & Flow | `1.10.1` | `SharingStarted.WhileSubscribed(5000)` |
| **Security** | AndroidX Biometric | `1.2.0-alpha05` | `BiometricPrompt` & `BiometricManager` |
| **Document Export**| Android Native Graphics | `android.graphics.pdf.PdfDocument` | Custom vector Canvas clinical report |
| **Dependency Injection** | Manual Container | `AppContainer` | Zero-reflection lightweight DI |
| **Testing** | JUnit 4, Coroutines Test | `4.13.2`, `1.10.1` | `StandardTestDispatcher`, `runTest` |

---

## 3. Architecture & Directory Structure

The project follows standard Android Clean Architecture with strict layer separation:

```
wellness-tracker/
├── .agents/
│   └── skills/                                 # Repository-level specialized agent skills
│       ├── adb-device-debugging/SKILL.md       # Runbooks for ADB, logcat, DB extraction, lifecycle
│       ├── gradle-build-optimization/SKILL.md  # Build caching, KSP flags, Gradle performance
│       └── jetpack-compose-best-practices/SKILL.md # Recomposition tuning, Canvas drawing, M3
│
├── docs/
│   ├── SRS.md                                  # Formal IEEE 29148 / 830 Software Requirements Spec
│   ├── CYCLE_PREDICTION_ENGINE.md              # Mathematical & clinical prediction specification
│   ├── templates/                              # Feature templates & authoring manual
│   │   ├── README.md                           # Templates usage and platform invariants
│   │   └── features/
│   │       ├── feature-readme-template.md      # Master feature specification template
│   │       ├── sub-feature-template.md         # Screen workflow & sub-capability template
│   │       ├── code-touch-map-template.md      # Android Clean Architecture touch map template
│   │       └── cross-layer-flow-template.md    # Reactive UDF sequence template
│   └── features/                               # Master Feature Catalog
│       ├── README.md                           # Master feature catalog index
│       ├── adaptive-cycle-prediction/          # Adaptive cycle prediction specification
│       ├── biomarker-symptom-logging/          # BBT, LH, cramps, and mood logging
│       ├── cycle-history-analytics/            # History records and bar chart analytics
│       ├── clinical-pdf-export/                # Doctor-ready vector PDF clinical export
│       └── local-biometric-security/           # Biometric prompt authentication gate
│
├── app/
│   ├── build.gradle.kts                        # Module build configuration & KSP arguments
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml             # Zero-permission manifest (NO INTERNET)
│       │   ├── java/com/thewalkersoft/tracker/
│       │   │   ├── MainActivity.kt             # Single-activity host; biometric authentication gate
│       │   │   ├── TrackerApplication.kt       # Application entry point; initializes AppContainer
│       │   │   │
│       │   │   ├── data/                       # Data Layer (Room, Converters, DAOs, Repositories)
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt      # Room database definition (version 1)
│       │   │   │   │   ├── converter/
│       │   │   │   │   │   └── DateConverters.kt  # LocalDate <-> Long epoch-day converters
│       │   │   │   │   ├── dao/
│       │   │   │   │   │   ├── PeriodLogDao.kt    # CRUD queries & Flow emissions for period entries
│       │   │   │   │   │   └── SymptomLogDao.kt   # CRUD queries & Flow emissions for daily symptoms
│       │   │   │   │   └── entity/
│       │   │   │   │       ├── PeriodLogEntity.kt # Database table: 'period_logs'
│       │   │   │   │       └── SymptomLogEntity.kt# Database table: 'symptom_logs'
│       │   │   │   └── repository/
│       │   │   │       └── CycleRepositoryImpl.kt # Implements repository; bridges DB & Predictor
│       │   │   │
│       │   │   ├── di/                         # Dependency Injection Layer
│       │   │   │   └── AppContainer.kt         # AppContainer interface & DefaultAppContainer
│       │   │   │
│       │   │   ├── domain/                     # Domain Layer (Pure business logic, models, math)
│       │   │   │   ├── model/
│       │   │   │   │   ├── CycleRecord.kt      # Evaluated cycle interval record
│       │   │   │   │   ├── CycleStatus.kt      # Enum: FOLLICULAR, OVULATION_WINDOW, LUTEAL, etc.
│       │   │   │   │   ├── PredictionResult.kt # Statistical target dates, intervals, confidence
│       │   │   │   │   └── SymptomRecord.kt    # Domain model for daily symptom & biomarker data
│       │   │   │   ├── predictor/
│       │   │   │   │   └── CyclePredictorEngine.kt # Rolling percentile + MAE predictor engine
│       │   │   │   └── repository/
│       │   │   │       └── CycleRepository.kt  # Domain repository interface contract
│       │   │   │
│       │   │   └── ui/                         # Presentation Layer (Compose, ViewModels, Theme)
│       │   │       ├── ViewModelFactory.kt     # AppViewModelProvider for instantiating ViewModels
│       │   │       ├── dashboard/
│       │   │       │   ├── DashboardScreen.kt  # Main landing screen with dial and quick actions
│       │   │       │   ├── DashboardViewModel.kt # Dashboard state management & timeline builder
│       │   │       │   └── components/
│       │   │       │       ├── CurrentCycleCard.kt
│       │   │       │       ├── DailyCheckInCard.kt
│       │   │       │       ├── PhaseInsightsCard.kt
│       │   │       │       ├── PredictionWindowCard.kt
│       │   │       │       └── QuickLogButton.kt
│       │   │       ├── export/
│       │   │       │   ├── DoctorPdfGenerator.kt # Vector Canvas clinical PDF report builder
│       │   │       │   ├── ExportScreen.kt       # Report export trigger & sharing screen
│       │   │       │   └── ExportViewModel.kt    # Background PDF generation via Dispatchers.IO
│       │   │       ├── history/
│       │   │       │   ├── HistoryScreen.kt      # List of historical cycles with gap stats
│       │   │       │   ├── HistoryViewModel.kt   # History state & CRUD actions
│       │   │       │   └── components/
│       │   │       │       ├── CycleHistoryItem.kt   # History row card with inline edit trigger
│       │   │       │       └── CycleLengthChart.kt   # Custom Canvas historical cycle length bar chart
│       │   │       ├── logging/
│       │   │       │   └── LogPeriodBottomSheet.kt # Modal bottom sheet for logging periods & symptoms
│       │   │       ├── navigation/
│       │   │       │   ├── NavigationHost.kt   # NavHost setup with Bottom Navigation Bar
│       │   │       │   └── Screen.kt           # Sealed screen definitions and routes
│       │   │       ├── security/
│       │   │       │   ├── BiometricAuthHelper.kt # BiometricPrompt wrapper for authentication
│       │   │       │   └── SecurityPreferences.kt# SharedPreferences wrapper for biometric toggle
│       │   │       ├── symptoms/
│       │   │       │   ├── SymptomsScreen.kt   # Detailed biomarker & daily symptom logging
│       │   │       │   ├── SymptomsViewModel.kt# Symptoms state management
│       │   │       │   └── components/
│       │   │       │       └── BbtTrendChart.kt# Custom Canvas smoothed Bezier BBT trend chart
│       │   │       └── theme/
│       │   │           ├── Color.kt            # Rose/wellness palette & cycle phase semantic colors
│       │   │           ├── Theme.kt            # Material 3 light/dark dynamic theme configuration
│       │   │           └── Type.kt             # Material 3 typography styles
│       │   └── res/
│       │       └── xml/file_paths.xml          # FileProvider configuration for scoped PDF sharing
│       └── test/java/com/thewalkersoft/tracker/
│           ├── domain/predictor/CyclePredictorEngineTest.kt # Comprehensive prediction unit tests
│           └── ui/dashboard/DashboardViewModelTest.kt       # Dashboard ViewModel Coroutine unit tests
```

---

## 4. Repository Workspace Skills

This repository includes specialized workspace skills located in `.agents/skills/`. Agents should refer to these whenever performing specific tasks:

1. **`adb-device-debugging`** (`.agents/skills/adb-device-debugging/SKILL.md`):
   - **When to use**: Diagnosing runtime issues, inspecting the local Room database, triaging crashes via logcat, and simulating biometric or lifecycle state changes.
   - **Key Runbook**:
     ```bash
     # Stream application logs filtered to Wellness Tracker
     adb logcat -v time | grep "com.thewalkersoft.tracker"
     
     # Extract Room SQLite database to local workstation for inspection
     adb exec-out run-as com.thewalkersoft.tracker cat databases/wellness_tracker_db > local_db.sqlite
     ```
2. **`gradle-build-optimization`** (`.agents/skills/gradle-build-optimization/SKILL.md`):
   - **When to use**: Investigating slow builds, updating `libs.versions.toml`, tuning KSP arguments, and ensuring configuration cache compliance.
   - **Key Runbook**: Keep `org.gradle.configuration-cache=true` and `ksp.incremental=true` active in `gradle.properties`.
3. **`jetpack-compose-best-practices`** (`.agents/skills/jetpack-compose-best-practices/SKILL.md`):
   - **When to use**: Building or modifying Compose UI components, custom Canvas drawings (`BbtTrendChart`, `CycleLengthChart`), avoiding unnecessary recompositions, and enforcing Material 3 styling.

---

## 5. Key Domain Logic: `CyclePredictorEngine`

> [!NOTE]
> For the complete technical and clinical specification with LaTeX mathematical formulas, architecture diagrams, and clinical justifications, refer to:
> **[`docs/CYCLE_PREDICTION_ENGINE.md`](docs/CYCLE_PREDICTION_ENGINE.md)**

The predictive logic in `CyclePredictorEngine.kt` is purpose-built to handle irregular cycles and postpartum transitions cleanly:

### 1. Spotting & Noise Suppression
- Periods logged fewer than `14 days` apart are treated as breakthrough spotting or mid-cycle anomalies rather than new cycle baselines and filtered out during interval calculation (`computeCycleIntervals`).

### 2. Postpartum Baseline Reset
- When a user logs a period with `isPostpartumBaselineReset = true`, all previous historical cycle records prior to this reset date are ignored by `isolatePostpartumRecords`. This prevents long postpartum amenorrhea (e.g., 200+ days) from skewing future predictions.

### 3. Rolling Percentiles with Dynamic MAE Expansion
- When fewer than 3 cycle intervals exist, a heuristic fallback (30 to 40 days, peak at day 35) is used.
- When 3 or more cycles exist, the last 6 cycles are evaluated:
  - **Peak (Target Date)** = Median cycle length (`50th percentile`).
  - **Lower Bound (p25)** = `25th percentile`.
  - **Upper Bound (p75)** = `75th percentile`.
  - **Mean Absolute Error (MAE)** = $\frac{1}{N} \sum |cycle_i - median|$.
  - **Expanded Confidence Window**:
    $$\text{Earliest Likely Date} = \max(21, p25 - \lfloor MAE / 2 \rfloor)$$
    $$\text{Latest Likely Date} = p75 + \lfloor MAE / 2 \rfloor$$

### 4. Cycle Status Evaluation
The engine assigns one of five active statuses based on the current day:
- `CycleStatus.FOLLICULAR`: Days 1 through Ovulation start ($pPeak - 18$).
- `CycleStatus.OVULATION_WINDOW`: Days within $[pPeak - 18, pPeak - 12]$.
- `CycleStatus.LUTEAL`: Post-ovulation prior to the prediction window.
- `CycleStatus.PREDICTION_WINDOW_ACTIVE`: When `currentDay >= earliestLikelyDay` and `<= latestLikelyDay`.
- `CycleStatus.OVERDUE`: When `currentDay > latestLikelyDay`.

---

## 6. Development & Build Commands

Always run Gradle commands via `./gradlew` from the project root:

```bash
# Run all unit tests (CyclePredictorEngineTest, DashboardViewModelTest, etc.)
./gradlew test

# Run a specific unit test class
./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngineTest"
./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.ui.dashboard.DashboardViewModelTest"

# Trigger KSP code generation (Room DAOs, converters)
./gradlew kspDebugKotlin

# Build Debug APK
./gradlew assembleDebug

# Install on connected device or running emulator
./gradlew installDebug

# Clean project build cache
./gradlew clean
```

---

## 7. Full Lifecycle Agent Playbooks

### Playbook A: Adding or Updating a Compose Screen
1. **Create/Update ViewModel**:
   - Define an immutable `UiState` data class (e.g., `MyFeatureUiState`).
   - Expose state via `StateFlow` created using `.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ...)`.
2. **Register in `AppViewModelProvider`**:
   - Add an initializer to `AppViewModelProvider.Factory` in `ui/ViewModelFactory.kt` injecting dependencies from `trackerApplication().container`.
3. **Define Screen Route**:
   - Add a new object to `Screen.kt` implementing `Screen(route, title, icon)`.
4. **Implement Compose Screen**:
   - Keep Composables stateless where possible by hoisting state and events.
   - Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` rather than hardcoded styles.
5. **Attach to `NavigationHost.kt`**:
   - Add a `composable(MyScreen.route)` block inside `NavHost`.

---

### Playbook B: Modifying Room Database, Entities, and DAOs
1. **Modify Entity**:
   - Edit the entity class in `data/local/entity/` (e.g., `PeriodLogEntity.kt`).
   - When adding fields, specify default values if applicable.
2. **Update Database Version & Migration**:
   - In `AppDatabase.kt`, increment the `version` integer.
   - Provide an explicit `Migration` object in `AppDatabase` rather than wiping user data.
3. **Update DAO**:
   - Add required queries in `data/local/dao/` returning `Flow<T>` for observable data or `suspend fun` for single queries/mutations.
4. **Regenerate KSP & Verify**:
   - Run `./gradlew kspDebugKotlin` to verify Room code generation succeeds and schemas are exported to `app/schemas/`.
   - Update `CycleRepository` interface and `CycleRepositoryImpl`.

---

### Playbook C: Enhancing Prediction Heuristics
1. **Modify Engine in `domain/predictor/CyclePredictorEngine.kt`**:
   - Keep calculations pure Kotlin (no Android framework dependencies, pure Java `LocalDate` / math).
2. **Add Unit Tests**:
   - Open `app/src/test/java/.../CyclePredictorEngineTest.kt`.
   - Write parameterized tests covering:
     - Regular cycles (e.g., 28-day intervals).
     - Highly fluctuating intervals (e.g., 25, 45, 32, 50).
     - Edge cases (short intervals < 14 days, postpartum reset flag enabled, zero or single cycle history).
3. **Validate**:
   - Run `./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngineTest"` to ensure 100% test pass rate.

---

### Playbook D: Adding New Biomarkers & Updating Clinical PDF Export
1. **Add Fields to `SymptomLogEntity`**:
   - E.g., cervical mucus, resting heart rate, sleep quality.
2. **Update Logging UI**:
   - Add inputs to `LogPeriodBottomSheet.kt` and `SymptomsScreen.kt`.
3. **Update `DoctorPdfGenerator.kt`**:
   - Update canvas rendering calculations in `generateClinicalReport()`.
   - Ensure table heights (`y += ...`), margins (`margin = 40f`, `rightMargin = 555f`), and text alignments remain balanced within standard A4 page dimensions (595x842 pt).
4. **Verify PDF Generation**:
   - Check that date formats use `DateTimeFormatter` and fallback values display cleanly (e.g., `"--"` instead of null strings).
   - Ensure PDF file creation is dispatched on `Dispatchers.IO`.

---

### Playbook E: Writing Unit Tests for ViewModels
1. **Use Coroutine Test Dispatchers**:
   - Use `StandardTestDispatcher()` and set main dispatcher via `Dispatchers.setMain(testDispatcher)` in `@Before` and reset in `@After`.
2. **Use Fake Repositories**:
   - Implement `CycleRepository` fakes with `MutableStateFlow` backing properties (see `DashboardViewModelTest.kt` for reference pattern).
3. **Execute Async Tests**:
   - Wrap test logic in `runTest(testDispatcher) { ... }` and call `advanceUntilIdle()` after ViewModel action invocations.

---

### Playbook F: Custom Canvas Charting (`BbtTrendChart`, `CycleLengthChart`)
1. **Avoid Object Allocations in Draw Scope**:
   - Do **not** instantiate `Paint`, `PathEffect`, or `Path` objects directly within the `onDraw` block of `Canvas`. Precompute or memoize them using `remember`.
2. **Coordinate & Bounds Scaling**:
   - Map domain values to coordinates using normalized scales:
     $$y_{coord} = \text{height} - \left( \frac{value - minVal}{maxVal - minVal} \right) \times \text{height}$$
   - Apply horizontal padding to prevent outer points or labels from clipping at Canvas edges.
3. **Typography & Label Rendering**:
   - For text labels on `Canvas`, use `drawContext.canvas.nativeCanvas.drawText(...)` with an `android.graphics.Paint` initialized with proper density-scaled text size (`sp.toPx()`).
4. **Smooth Curves**:
   - When rendering line charts (such as `BbtTrendChart`), use cubic Bezier splines (`cubicTo`) between adjacent points for smooth organic curve transitions.

---

### Playbook G: ADB & Device Debugging Workflows
1. **Logcat Monitoring**:
   - Use tag-specific or package-filtered streaming:
     ```bash
     adb logcat -c && adb logcat -s "WellnessTracker:*" "*:E"
     ```
2. **Inspecting Local Room Database**:
   - The database is private to the app sandbox. Inspect without root using `run-as`:
     ```bash
     adb exec-out run-as com.thewalkersoft.tracker cat databases/wellness_tracker_db > tracker_debug.db
     sqlite3 tracker_debug.db ".schema"
     sqlite3 tracker_debug.db "SELECT * FROM period_logs ORDER BY startDate DESC;"
     ```
3. **Simulating Biometric Verification in Emulator**:
   ```bash
   # Enroll fingerprint
   adb emu finger touch 1
   ```
4. **Testing Process Lifecycle & State Restoration**:
   ```bash
   # Send app to background and simulate OS low-memory termination
   adb shell am kill com.thewalkersoft.tracker
   ```

---

### Playbook H: Gradle Build & Cache Hygiene
1. **Configuration Cache**:
   - Avoid accessing project state at task execution time. Test configuration cache compliance with:
     ```bash
     ./gradlew test --configuration-cache
     ```
2. **KSP Code Generation**:
   - When Room entity or DAO signatures change, run `./gradlew kspDebugKotlin` to regenerate DAO implementations before compiling UI code.
3. **Memory & Parallel Execution**:
   - Respect settings in `gradle.properties`:
     - `org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC`
     - `org.gradle.parallel=true`

---

### Playbook I: Authoring or Updating Feature Documentation with Templates
1. **Locate Canonical Templates**:
   - Templates reside in `docs/templates/features/` (`feature-readme-template.md`, `sub-feature-template.md`, `code-touch-map-template.md`, `cross-layer-flow-template.md`).
2. **Scaffold New Feature**:
   - Create `docs/features/<feature-slug>/` (e.g. `docs/features/bbt-tracking/`).
   - Copy `feature-readme-template.md` to `README.md`, `code-touch-map-template.md` to `code-touch-map.md`, and `cross-layer-flow-template.md` to `data-flow.md`.
3. **Map Android Clean Architecture**:
   - Map symbols explicitly across Presentation (Compose/Canvas), State (ViewModel/UiState), Domain (Engines/Models), Data (Room DAOs/Entities), and Platform (Biometrics/PDF).
4. **Enforce Android Platform Invariants**:
   - Verify zero network requests / no `INTERNET` permissions.
   - Verify non-blocking threading (`Dispatchers.IO` / `Dispatchers.Default`).
   - Ensure `SharingStarted.WhileSubscribed(5000)` and no object allocations inside Canvas `DrawScope`.
5. **Cross-Link and Reference**:
   - Reference `docs/features/adaptive-cycle-prediction/` as the canonical gold standard.
   - Add new feature documentation links to the root `README.md` and `docs/templates/README.md`.

---

## 8. Dependency Injection & Architecture Conventions

- **Current DI**: The app uses `AppContainer` (defined in `di/AppContainer.kt`) owned by `TrackerApplication`.
- **Accessing Dependencies**:
  ```kotlin
  val app = context.applicationContext as TrackerApplication
  val repository = app.container.cycleRepository
  ```
- **ViewModel Instantiation**: Always use `AppViewModelProvider.Factory` with `viewModel(factory = AppViewModelProvider.Factory)` to inject repositories into ViewModels.
- **Future Hilt Migration**: If migrating to Hilt:
  1. Add `@HiltAndroidApp` to `TrackerApplication`.
  2. Add `@AndroidEntryPoint` to `MainActivity`.
  3. Annotate ViewModels with `@HiltViewModel` and `@Inject constructor`.
  4. Replace `AppContainer` with Hilt modules (`@Module`, `@InstallIn(SingletonComponent::class)`).

---

## 9. Privacy & Security Guardrails for AI Agents

> [!CAUTION]
> **Strict Security Directives**:
> - **DO NOT** add `android.permission.INTERNET` or any network capabilities to `AndroidManifest.xml`.
> - **DO NOT** add third-party analytics, crash reporting, tracking SDKs, or cloud sync backends.
> - **DO NOT** log personal health data (cycle dates, symptoms, notes) to `Logcat` or system logs in production builds.
> - **DO NOT** write unencrypted or exported health reports to external public shared storage without user-mediated `FileProvider` URIs.

---

## 10. Mandatory Feature Documentation & SRS Synchronization Protocol

> [!IMPORTANT]
> **Strict Engineering & Agent Mandate**:
> Modifying application code without synchronizing its corresponding documentation constitutes an incomplete, non-compliant task. Every pull request or code modification must update all affected feature documents in `docs/features/<feature>/` and, where applicable, update the formal Software Requirements Specification in `docs/SRS.md`.

### 1. Feature Documentation Update Triggers (`docs/features/<feature>/`)
Whenever any change touches an application feature, the engineer or AI agent **must update all 3 canonical documents** for that feature:

| Code Modification Trigger | Required Document Update Action | Target Document(s) |
| :--- | :--- | :--- |
| **UI / Compose Screen / Component** | Update *Current User Experience* section to reflect observable changes; update *Presentation Layer* table in code touch map. | `README.md`<br>`code-touch-map.md` |
| **ViewModel / StateFlow / UiState** | Update *State & Lifecycle Layer* table with new/modified properties; update *UDF Sequence* with changed state emissions and thread dispatchers. | `code-touch-map.md`<br>`data-flow.md` |
| **Domain Engine / Model / Math** | Update *Shared Business Rules* with new formulas, intervals, or validation logic; update *Domain Layer* table in code touch map; update `docs/CYCLE_PREDICTION_ENGINE.md` if cycle math changed. | `README.md`<br>`code-touch-map.md`<br>`docs/CYCLE_PREDICTION_ENGINE.md` |
| **Room Entity / DAO / Migrations** | Update *Related Database Objects* schema table; update *Data Layer* table in code touch map; update *Normal Sequence* with persistence handoffs. | `README.md`<br>`code-touch-map.md`<br>`data-flow.md` |
| **Platform / OS Service (Biometrics, PDF)** | Update *Platform Subsystems* table, permissions, or scoped storage boundaries. | `README.md`<br>`code-touch-map.md`<br>`data-flow.md` |

### 2. Software Requirements Specification (SRS) Triggers (`docs/SRS.md`)
Whenever code changes introduce, modify, or deprecate functional capabilities, the formal SRS in `docs/SRS.md` **must be synchronized immediately**:

| Requirement Change Event | Mandatory SRS Update Action | Relevant SRS Section |
| :--- | :--- | :--- |
| **New Business / Health Capability** | Add a new formal functional requirement `REQ-F-XX` detailing description, inputs, processing steps, and outputs. | `docs/SRS.md` — Section 3.1 |
| **Modified Clinical Rule / Calculation** | Update the corresponding `REQ-F-XX` entry (e.g. interval formulas, spotting thresholds, scale ranges). | `docs/SRS.md` — Section 3.1 |
| **Altered Non-Functional Constraint** | Update relevant NFR entry (`NFR-PRI-XX`, `NFR-SEC-XX`, `NFR-PERF-XX`, `NFR-THRD-XX`, `NFR-REL-XX`). | `docs/SRS.md` — Section 3.3 |
| **Verification / Architecture Traceability** | **Mandatory**: Add or update the mapping in Section 4 (*Verification & Traceability Matrix*), linking the requirement ID to its implementation class, Room DAO, and test suite. | `docs/SRS.md` — Section 4 |
| **New Feature Directory Created** | Register the new feature in `docs/features/README.md` (*Master Feature Catalog*) with status badges and links to all 3 documents. | `docs/features/README.md`<br>`README.md` |

---

## 11. Agent Verification Checklist Before Concluding Work

Before completing any task or pull request, agents must verify:

- [ ] **Tests Pass**: `./gradlew test` runs cleanly with 0 failures.
- [ ] **Build Succeeds**: `./gradlew assembleDebug` completes without compiler or KSP warnings/errors.
- [ ] **Privacy Preserved**: No network permissions (`android.permission.INTERNET`) or telemetry dependencies added.
- [ ] **No Main-Thread Blocking**: All Room operations and PDF generation run asynchronously via Coroutines (`Dispatchers.IO` / `viewModelScope`).
- [ ] **State Flow Hygiene**: ViewModel states use `SharingStarted.WhileSubscribed(5000)` to prevent background flow leaks.
- [ ] **Compose Previews**: UI components have working `@Preview` annotations with mock data.
- [ ] **Canvas Performance**: Custom Canvas drawing (`BbtTrendChart`, `CycleLengthChart`) avoids object allocations in `DrawScope`.
- [ ] **Feature Documentation Synchronized**: Any modified feature has its `README.md`, `code-touch-map.md`, and `data-flow.md` updated to match current code.
- [ ] **SRS Synchronized**: Any added, modified, or re-scoped requirement is updated in `docs/SRS.md` with requirement IDs and mapped in the Section 4 Traceability Matrix.
- [ ] **Master Catalog Updated**: Any newly introduced feature directory is indexed in `docs/features/README.md`.
- [ ] **Documentation Integrity**: Algorithmic and architectural changes are mirrored across `README.md`, `AGENTS.md`, and `docs/CYCLE_PREDICTION_ENGINE.md`.
