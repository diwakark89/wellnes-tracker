# AGENTS.md — Agent & Developer Operating Manual

Welcome to **Wellness Tracker** (`com.thewalkersoft.tracker`). This guide provides comprehensive operational guidelines, architectural context, development playbooks, domain logic specifications, and verification checklists for AI coding agents and developers working in this repository.

---

## 1. Project Mission & Core Philosophy

**Wellness Tracker** is a privacy-first, on-device Android application designed to track menstrual cycles, symptoms, biomarkers (such as Basal Body Temperature and ovulation tests), and health insights with an emphasis on irregular or postpartum cycle prediction.

### Core Non-Negotiables & Philosophy
1. **100% On-Device & Zero-Telemetry**: All health data, predictions, logs, and generated clinical reports exist **exclusively on the local device**. No network permissions, analytics SDKs, cloud trackers, or external API dependencies may ever be added.
2. **Clinical Utility**: Features must provide clear, medically accurate data visualization and allow local generation of doctor-ready clinical PDF summaries.
3. **Adaptive Prediction**: Menstrual cycles vary naturally (especially postpartum or with conditions like PCOS). The system utilizes dynamic statistical models (percentile spans with Mean Absolute Error expansion) rather than rigid 28-day assumptions.
4. **Local Biometric Security**: User health logs can be protected with biometric authentication (fingerprint/face unlock via AndroidX `BiometricPrompt`).

---

## 2. Tech Stack & Dependencies

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.2.10 | Target JVM 11 |
| **Android SDK** | Min SDK: `34`, Target SDK: `36`, Compile SDK: `36` | Android 14+ baseline |
| **UI Framework** | Jetpack Compose (BOM `2025.02.00`) | Material 3 (`androidx.compose.material3`) |
| **Architecture** | Clean Architecture + MVVM | Unidirectional Data Flow (UDF), StateFlow |
| **Persistence** | AndroidX Room `2.6.1` + KSP (`2.2.10-2.0.2`) | Kotlin Symbol Processing, Type Converters |
| **Async / Stream** | Kotlin Coroutines `1.10.1` & Flow | `SharingStarted.WhileSubscribed(5000)` |
| **Security** | AndroidX Biometric `1.2.0-alpha05` | `BiometricPrompt` & `BiometricManager` |
| **Document Export** | Android Native `android.graphics.pdf.PdfDocument` | Custom vector/canvas clinical PDF report |
| **Dependency Injection**| Manual Container (`AppContainer`) | Lightweight zero-overhead DI |
| **Testing** | JUnit 4, Kotlinx Coroutines Test | `StandardTestDispatcher`, `runTest` |

---

## 3. Architecture & Directory Structure

The project follows standard Android Clean Architecture with explicit layer separation:

```
app/src/main/java/com/thewalkersoft/tracker/
├── TrackerApplication.kt          # Application entry point; initializes AppContainer
├── MainActivity.kt                # Single-activity host; handles Biometric prompt & theme
│
├── data/                          # Data Layer: Room DB, Entities, DAOs, Converters, Repositories
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database definition (version 1)
│   │   ├── converter/
│   │   │   └── DateConverters.kt  # LocalDate <-> Long epoch-day Room type converters
│   │   ├── dao/
│   │   │   ├── PeriodLogDao.kt    # CRUD queries & Flow emissions for period entries
│   │   │   └── SymptomLogDao.kt   # CRUD queries & Flow emissions for daily symptoms
│   │   └── entity/
│   │       ├── PeriodLogEntity.kt # Database table: 'period_logs'
│   │       └── SymptomLogEntity.kt# Database table: 'symptom_logs'
│   └── repository/
│       └── CycleRepositoryImpl.kt # Implements domain repository; joins DB & Predictor
│
├── di/                            # Dependency Injection Layer
│   └── AppContainer.kt            # AppContainer interface & DefaultAppContainer
│
├── domain/                        # Domain Layer: Pure business logic, models, and algorithms
│   ├── model/
│   │   ├── CycleRecord.kt         # Evaluated cycle record with calculated duration
│   │   ├── CycleStatus.kt         # Enum: FOLLICULAR, OVULATION_WINDOW, LUTEAL, etc.
│   │   ├── PredictionResult.kt    # Calculated target peak, min/max dates, confidence
│   │   └── SymptomRecord.kt       # Domain model for symptom & biomarker tracking
│   ├── predictor/
│   │   └── CyclePredictorEngine.kt# Rolling percentile + MAE cycle predictor engine
│   └── repository/
│       └── CycleRepository.kt     # Repository interface contract
│
└── ui/                            # Presentation Layer: Compose screens, ViewModels, Theme
    ├── ViewModelFactory.kt        # AppViewModelProvider for instantiating ViewModels
    ├── dashboard/
    │   ├── DashboardScreen.kt     # Main landing screen with timeline and quick actions
    │   ├── DashboardViewModel.kt  # Dashboard state management and timeline builder
    │   └── components/            # Reusable dashboard widgets
    │       ├── CurrentCycleCard.kt
    │       ├── DailyCheckInCard.kt
    │       ├── PhaseInsightsCard.kt
    │       ├── PredictionWindowCard.kt
    │       └── QuickLogButton.kt
    ├── export/
    │   ├── DoctorPdfGenerator.kt  # Canvas-based clinical PDF document builder
    │   ├── ExportScreen.kt        # Screen to trigger report export & PDF preview/share
    │   └── ExportViewModel.kt     # Export logic & background PDF creation
    ├── history/
    │   ├── HistoryScreen.kt       # List of historical cycles with gap stats and edits
    │   ├── HistoryViewModel.kt    # History state & CRUD actions
    │   └── components/
    │       └── CycleHistoryItem.kt# Item row for cycle history cards
    ├── logging/
    │   └── LogPeriodBottomSheet.kt# Modal bottom sheet for logging periods & symptoms
    ├── navigation/
    │   ├── NavigationHost.kt      # NavHost setup with Bottom Navigation Bar
    │   └── Screen.kt              # Sealed screen definitions and routes
    ├── security/
    │   ├── BiometricAuthHelper.kt # BiometricPrompt wrapper for authentication
    │   └── SecurityPreferences.kt # Encapsulates biometric lock preference
    ├── symptoms/
    │   ├── SymptomsScreen.kt      # Detailed daily biomarker & symptom logging
    │   └── SymptomsViewModel.kt   # Symptoms state management
    └── theme/
        ├── Color.kt               # Rose/wellness color palette & cycle phase colors
        ├── Theme.kt               # Material 3 light/dark dynamic theme configuration
        └── Type.kt                # Typography styles
```

---

## 4. Key Domain Logic: `CyclePredictorEngine`

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
- `CycleStatus.FOLLICULAR`: Days 1 through Ovulation start.
- `CycleStatus.OVULATION_WINDOW`: Days within $[pPeak - 18, pPeak - 12]$.
- `CycleStatus.LUTEAL`: Post-ovulation prior to the prediction window.
- `CycleStatus.PREDICTION_WINDOW_ACTIVE`: When `currentDay >= earliestLikelyDay` and `<= latestLikelyDay`.
- `CycleStatus.OVERDUE`: When `currentDay > latestLikelyDay`.

---

## 5. Development & Build Commands

Always run Gradle commands via `./gradlew` from the project root:

```bash
# Run all unit tests (CyclePredictorEngineTest, DashboardViewModelTest, etc.)
./gradlew test

# Run a specific unit test class
./gradlew testDebugUnitTest --tests "com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngineTest"

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

## 6. Full Lifecycle Agent Playbooks

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
   - If migrating schema, provide a `Migration` definition in `AppDatabase` or configure `.fallbackToDestructiveMigration()` during early prototyping.
3. **Update DAO**:
   - Add required queries in `data/local/dao/` returning `Flow<T>` for observable data or `suspend fun` for single queries/mutations.
4. **Regenerate KSP & Verify**:
   - Run `./gradlew kspDebugKotlin` to verify Room code generation succeeds.
   - Update `CycleRepository` interface and `CycleRepositoryImpl`.

---

### Playbook C: Enhancing Prediction Heuristics
1. **Modify Engine in `domain/predictor/CyclePredictorEngine.kt`**:
   - Keep calculations pure (no Android framework dependencies, pure Java `LocalDate` / math).
2. **Add Unit Tests**:
   - Open `app/src/test/java/.../CyclePredictorEngineTest.kt`.
   - Write parameterized tests covering:
     - Regular cycles (e.g., 28-day intervals).
     - Highly fluctuating intervals (e.g., 25, 45, 32, 50).
     - Edge cases (short intervals < 14 days, postpartum reset flag enabled, zero or single cycle history).
3. **Validate**:
   - Run `./gradlew test` to ensure all tests pass.

---

### Playbook D: Adding New Biomarkers & Updating Clinical PDF Export
1. **Add Fields to `SymptomLogEntity`**:
   - E.g., cervical mucus, sleep hours, resting heart rate.
2. **Update Logging UI**:
   - Add inputs to `LogPeriodBottomSheet.kt` and `SymptomsScreen.kt`.
3. **Update `DoctorPdfGenerator.kt`**:
   - Update the canvas rendering calculations in `generateClinicalReport()`.
   - Ensure table heights (`y += ...`), margins (`margin = 40f`, `rightMargin = 555f`), and text alignments remain balanced within the standard A4 page dimensions (595x842 pt).
4. **Verify PDF Generation**:
   - Check that date formats use `DateTimeFormatter` and fallback values display cleanly (e.g., `"--"` instead of null strings).

---

### Playbook E: Writing Unit Tests for ViewModels
1. **Use Coroutine Test Dispatchers**:
   - Use `StandardTestDispatcher()` and set main dispatcher via `Dispatchers.setMain(testDispatcher)` in `@Before` and reset in `@After`.
2. **Use Fake Repositories**:
   - Implement `CycleRepository` fakes with `MutableStateFlow` backing properties (see `DashboardViewModelTest.kt` for reference pattern).
3. **Execute Async Tests**:
   - Wrap test logic in `runTest(testDispatcher) { ... }` and call `advanceUntilIdle()` after ViewModel action invocations.

---

## 7. Dependency Injection & Architecture Conventions

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

## 8. Privacy & Security Guardrails for AI Agents

> [!CAUTION]
> **Strict Security Directives**:
> - **DO NOT** add `android.permission.INTERNET` or any network capabilities to `AndroidManifest.xml`.
> - **DO NOT** add third-party analytics, crash reporting, tracking SDKs, or cloud sync backends.
> - **DO NOT** log personal health data (cycle dates, symptoms, notes) to `Logcat` or system logs in production builds.
> - **DO NOT** write unencrypted or exported health reports to external public shared storage without user-mediated `FileProvider` URIs.

---

## 9. Agent Verification Checklist Before Concluding Work

Before completing any task or pull request, agents must verify:

- [ ] **Tests Pass**: `./gradlew test` runs cleanly with 0 failures.
- [ ] **Build Succeeds**: `./gradlew assembleDebug` completes without compiler or KSP warnings/errors.
- [ ] **Privacy Preserved**: No network permissions or telemetry added.
- [ ] **No Main-Thread Blocking**: All Room operations and PDF generation run asynchronously via Coroutines (`Dispatchers.IO` / `viewModelScope`).
- [ ] **State Flow Hygiene**: ViewModel states use `SharingStarted.WhileSubscribed(5000)` to prevent background flow leaks.
- [ ] **Compose Previews**: UI components have working `@Preview` annotations with mock data.
