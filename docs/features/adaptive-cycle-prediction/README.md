# Feature: Adaptive Cycle Prediction & Forecasting

## Human Orientation

### Overview

Adaptive Cycle Prediction & Forecasting is the core algorithmic capability of Wellness Tracker. Unlike conventional menstrual cycle trackers that assume a static 28-day model with ovulation rigidly fixed on day 14, this feature dynamically calculates cycle arrival targets, fertile windows, and active cycle phases using rolling percentiles (p25, median, p75) and dynamic Mean Absolute Error (MAE) confidence window expansion.

The feature specifically accommodates users with irregular cycles, PCOS, or those recovering postpartum. A dedicated **Postpartum Baseline Reset** mechanism allows users returning from extended postpartum amenorrhea (e.g., 200+ days) to isolate prior historical gaps from skewing current cycle predictions, restoring mathematical precision immediately upon cycle resumption.

All calculations, cycle interval analyses, and predictions occur **100% on-device** in pure Kotlin. Zero user data is transmitted over networks, ensuring total privacy over reproductive health records.

### Feature Snapshot

| Field | Summary |
| --- | --- |
| **Business / Health capability** | Dynamic cycle forecasting and phase tracking adapted for irregular and postpartum cycles |
| **Primary actors** | Menstrual tracking users, postpartum mothers, individuals with irregular cycles / PCOS |
| **User value** | Actionable, medically sound forecast windows without rigid 28-day assumptions or cloud exposure |
| **Current state** | `COMPLETED` — Algorithmic engine, Room integration, and Dashboard UI are fully operational with 100% passing tests |
| **Main entry points** | `Screen.Dashboard` (`DashboardScreen.kt`), `CurrentCycleCard.kt`, `PredictionWindowCard.kt` |
| **Primary components** | `CyclePredictorEngine`, `DashboardViewModel`, `CycleRepositoryImpl`, `PeriodLogDao` |
| **Key boundaries** | Strict zero-telemetry; spotting suppression (< 14 days); postpartum baseline isolation |

### Actors and User Value

| Actor | Need or Goal | Value Provided |
| --- | --- | --- |
| **Individual with Irregular Cycles** | Anticipate cycle arrival without false alerts based on rigid 28-day baselines | Dynamic MAE expansion scales the target window wider or narrower based on historical variance |
| **Postpartum Mother** | Resume cycle tracking after months of amenorrhea without distorted forecasts | One-tap postpartum reset excludes extended pre-pregnancy and postpartum gaps |
| **Healthcare Provider** | Review clinical cycle statistics, phase lengths, and regularity | Clear mathematical distribution of cycle lengths exportable via doctor-ready reports |

### Current User Experience

- **Dashboard Circular Cycle Dial**: Visualizes current cycle day (e.g., "Day 14") with a color-coded circular progress ring matching the active biological phase (`FOLLICULAR`, `OVULATION_WINDOW`, `LUTEAL`, `PREDICTION_WINDOW_ACTIVE`, or `OVERDUE`).
- **Phase Insights Card**: Provides evidence-based clinical guidance explaining the physiological shifts occurring during the active cycle phase.
- **Prediction Window Card**: Displays the statistically predicted target arrival date along with earliest and latest likely dates, accompanied by confidence indicators.
- **Cycle Status Badges**: Contextual status pills indicating whether the cycle is progressing normally, in the fertile window, in the active arrival window, or overdue.

### Scope and Boundaries

#### In Scope
- Statistical rolling percentile calculations (p25, median/p50, p75) on the last 6 cycles.
- Mean Absolute Error (MAE) dynamic expansion: $\text{Earliest} = \max(21, p25 - \lfloor MAE / 2 \rfloor)$, $\text{Latest} = p75 + \lfloor MAE / 2 \rfloor$.
- Breakthrough bleeding / spotting noise suppression (< 14 days apart).
- Postpartum baseline isolation via `isPostpartumBaselineReset`.
- Five-phase biological cycle state machine evaluation.

#### Out of Scope
- Cloud synchronization or backup (strictly prohibited).
- Direct medical diagnosis or pregnancy confirmation tests.
- Hormonal contraceptive tracking algorithms (future consideration).

### End-to-End User Journey

1. **User logs period start date**: User taps "+ Quick Log" and records a period start date (optionally tagging `isPostpartumBaselineReset = true`).
2. **Room Database update**: `PeriodLogDao` writes the record to local SQLite and triggers an observable `Flow` emission.
3. **Repository interval evaluation**: `CycleRepositoryImpl` retrieves historical logs, filters out spotting anomalies, applies postpartum isolation, and invokes `CyclePredictorEngine`.
4. **Statistical calculation**: The engine computes rolling percentiles, MAE variance, target dates, and assigns the current `CycleStatus`.
5. **UI Recomposition**: `DashboardViewModel` receives the updated `PredictionResult` and emits an updated `DashboardUiState`, immediately refreshing the dashboard dial, phase insights, and prediction cards.

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Presentation (UI)** | Jetpack Compose / M3 | `DashboardScreen.kt`, `CurrentCycleCard.kt`, `PredictionWindowCard.kt`, `PhaseInsightsCard.kt` |
| **State Management** | ViewModel / StateFlow | `DashboardViewModel.kt` exposing immutable `DashboardUiState` via `WhileSubscribed(5000)` |
| **Domain Logic** | Pure Kotlin Engine | `CyclePredictorEngine.kt` computing mathematical percentiles, MAE, and `CycleStatus` |
| **Data & Persistence** | Room SQLite / DAO | `PeriodLogEntity.kt`, `PeriodLogDao.kt`, and `DateConverters.kt` |
| **Dependency Injection** | Manual DI (`AppContainer`)| Injects `CycleRepository` into `DashboardViewModel` via `AppViewModelProvider.Factory` |

### Shared Business Rules

1. **Spotting Noise Suppression**: Any bleeding episode logged fewer than `14 days` after the preceding start date is treated as mid-cycle spotting or breakthrough bleeding. It is merged or suppressed from baseline interval calculations.
2. **Postpartum Baseline Reset**: If any historical log has `isPostpartumBaselineReset = true`, all records chronologically prior to that start date are discarded from cycle length calculations.
3. **Heuristic Fallback**: When fewer than 3 valid cycle intervals exist, the engine provides a standard heuristic range: Day 30 to 40, targeting Day 35.
4. **Phase Allocation**:
   - `FOLLICULAR`: Days 1 through Ovulation start ($pPeak - 18$).
   - `OVULATION_WINDOW`: Days $[pPeak - 18, pPeak - 12]$.
   - `LUTEAL`: Days between Ovulation window end and prediction window start.
   - `PREDICTION_WINDOW_ACTIVE`: When $\text{currentDay} \in [\text{earliestLikelyDay}, \text{latestLikelyDay}]$.
   - `OVERDUE`: When $\text{currentDay} > \text{latestLikelyDay}$.

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected Behavior | Owning Component |
| :--- | :--- | :--- |
| **No periods logged (First app launch)** | Dial displays "Cycle not started"; cards invite logging initial period | `DashboardViewModel` |
| **Single period logged (0 intervals)** | Displays Day count from logged date; prediction uses 30-40 day fallback | `CyclePredictorEngine` |
| **Cycle exceeds 60 days without new log** | Marked as `CycleStatus.OVERDUE`; prompt to log next period | `CyclePredictorEngine` |
| **App killed in background by OS** | StateFlow re-evaluates cleanly upon app restart from local Room DB | `DashboardViewModel` |

### Android Platform Invariants

- **Zero-Telemetry**: No network permissions (`android.permission.INTERNET`) exist in `AndroidManifest.xml`.
- **Threading**: Statistical prediction runs synchronously within Kotlin Coroutines off the main thread; Room database operations dispatched via `Dispatchers.IO`.
- **StateFlow Hygiene**: ViewModels utilize `SharingStarted.WhileSubscribed(5000)` to stop active Flow observation when the app transitions to the background.

### Related Database Objects

| Table / Entity | DAO | Type Converters | Purpose |
| :--- | :--- | :--- | :--- |
| `period_logs` (`PeriodLogEntity`) | `PeriodLogDao` | `DateConverters` (`LocalDate` <-> `Long`) | Stores user period start/end dates, flow intensity, and postpartum reset flags |

### Tests and Acceptance Evidence

| Scenario or Acceptance Signal | Expected Result | Evidence / Test Command |
| :--- | :--- | :--- |
| Regular 28-day cycle test | Target Day 28; earliest 26, latest 30 | `CyclePredictorEngineTest.kt` (`./gradlew test`) |
| Irregular fluctuating cycle test | MAE expands confidence window proportionally | `CyclePredictorEngineTest.kt` |
| Postpartum baseline reset test | Discards amenorrhea gap; predicts from resuming cycle | `CyclePredictorEngineTest.kt` |
| Breakthrough spotting test (< 14 days) | Suppresses spotting from cycle intervals | `CyclePredictorEngineTest.kt` |
| ViewModel state transition | Emits populated `DashboardUiState` upon repository emission | `DashboardViewModelTest.kt` |

### Related Docs

- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Algorithmic Technical Specification](../../CYCLE_PREDICTION_ENGINE.md)
- [Developer Operating Manual](../../../AGENTS.md)
