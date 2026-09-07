# Android Cross-Layer Flow Template

Use this template to document an end-to-end **Unidirectional Data Flow (UDF)** sequence that crosses Android architectural layers: from Jetpack Compose UI down to Room persistence and back up through reactive `StateFlow` streams.

---

## Authoring Contract

- Describe implementation-backed behavior present in the codebase.
- Name the exact layer, thread dispatcher (`Dispatchers.Main`, `Dispatchers.IO`, `Dispatchers.Default`), and entry symbol for every handoff.
- Document both the **forward action** (write or query) and the **reactive return path** (`Flow` emission and Compose recomposition).
- Address every section. If a platform service or persistence layer is not involved, mark `Not applicable — <reason>`.
- Use repository-relative paths and relative Markdown links.

---

## Flow Name

`<Short, business-recognizable Android flow name, e.g., "Log Period & Recalculate Prediction Window">`

---

## 1. Status and Purpose

| Field | Value |
| :--- | :--- |
| **Status** | `<COMPLETED / MVP / IN_PROGRESS / FUTURE>` with implementation evidence |
| **Purpose** | `<End-to-end clinical or tracking behavior this flow delivers>` |
| **Scope** | `<Start trigger (e.g., tap "Save Period") to end outcome (e.g., dial updates with new predicted dates)>` |
| **Explicit Exclusions** | `<Adjacent flows, background jobs, or prohibited network operations>` |
| **Source-of-truth Links** | `[Feature README](README.md)` \| `[Code Touch Map](code-touch-map.md)` |

---

## 2. Trigger, Actor, and Context

| Concern | Record |
| :--- | :--- |
| **Trigger** | `<Compose tap event, screen navigation, bottom sheet submission, or app resume>` |
| **Actor** | `<Primary user, postpartum user, or system lifecycle>` |
| **UI Context** | `<Current Screen route, Composable component, or active modal sheet>` |
| **Concurrency / Idempotency** | `<Single coroutine launch, Debounce policy, or Primary Key collision strategy>` |
| **Observable Outcome** | `<UI card updates, state recomposition, dialog dismisses, or snackbar shows>` |

---

## 3. Architecture Layer Ownership

| Layer | Responsibility & Owned State | Primary Entry Point | Layer Boundary |
| :--- | :--- | :--- | :--- |
| **Presentation (UI)** | Renders UI; captures gestures; collects `UiState` | `<ComposableScreen / Component>` | Must not invoke database directly |
| **State (ViewModel)** | Manages `UiState`; launches coroutines | `<FeatureViewModel>` | Bridges UI events to Domain/Data |
| **Domain (Logic)** | Pure math, statistical models, validation | `<CyclePredictorEngine / Model>` | Pure Kotlin; zero Android framework SDKs |
| **Data (Persistence)**| Room SQLite queries; emits reactive `Flow` | `<PeriodLogDao / RepositoryImpl>` | Executes queries on `Dispatchers.IO` |
| **Platform Services** | Biometrics, Canvas PDF, FileProvider | `<Helper / System Service>` | Scoped native capabilities |

---

## 4. Normal End-to-End Sequence

Use one row per material handoff across layers. Include dispatcher transitions and reactive flow returns.

| Step | Layer / Caller | Target Entry Point | Thread / Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response / Next Step |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | `<ComposableComponent>` | `Dispatchers.Main` | User triggers action (e.g., taps Save) | Captures draft input values | Passes data to ViewModel lambda |
| **2** | State | `<FeatureViewModel>` | `Dispatchers.Main` -> `viewModelScope` | Validates input; calls repository suspend fun | Updates internal loading/mutation state | UI displays loading indicator if needed |
| **3** | Data | `<CycleRepositoryImpl>` | `Dispatchers.IO` (`withContext`) | Transforms model; calls Room DAO | Dispatches SQLite query | Awaits SQLite transaction |
| **4** | Persistence | `<EntityDao>` | SQLite Thread Pool | Executes `@Insert` / `@Update` / `@Query` | Writes row into Room database | Triggers Room invalidation tracker |
| **5** | Persistence | `<EntityDao>` | SQLite Thread Pool | Emits updated `List<Entity>` via `Flow` | SQLite table snapshot emitted | Pushed to Repository flow pipeline |
| **6** | Domain | `<PredictorEngine>` | `Dispatchers.Default` | Calculates intervals, MAE, target dates | Computes new `PredictionResult` | Evaluated data passed to ViewModel |
| **7** | State | `<FeatureViewModel>` | `viewModelScope` | Updates `MutableStateFlow<UiState>` | Emits new immutable `UiState` | Pushes to active UI collectors |
| **8** | Presentation | `<ComposableScreen>` | `Dispatchers.Main` | `collectAsStateWithLifecycle()` receives state | Triggers recomposition | UI re-renders dial, charts, and cards |

---

## 5. Threading & Concurrency Safeguards

- **Dispatcher Hygiene**:
  - `Dispatchers.Main`: Reserved exclusively for UI rendering and ViewModel state exposure.
  - `Dispatchers.IO`: Dedicated to Room SQLite reads/writes and PDF generation.
  - `Dispatchers.Default`: Used for CPU-intensive statistical cycle predictions and Canvas coordinate math.
- **Structured Concurrency**: All asynchronous tasks launched within `viewModelScope`. Cancelled automatically when the host ViewModel is cleared.
- **Lifecycle Flow Collection**: UI layers collect via `collectAsStateWithLifecycle()` with `SharingStarted.WhileSubscribed(5000)` to stop upstream queries during app backgrounding.

---

## 6. Failure, Retry, and Recovery

| Failure Scenario | Expected App Behavior | Owning Component | Recovery Path |
| :--- | :--- | :--- | :--- |
| **Validation Failure** (e.g. end date before start date) | Inline error displayed; database write blocked | Compose UI / ViewModel | User corrects date; action re-enabled |
| **Empty Database State** | Predictor provides heuristic fallback (30-40 days) | `CyclePredictorEngine` | Replaces `--` placeholders with friendly guidance |
| **Process Death / Low Memory** | Activity recreates; ViewModel restores state from Room | Android OS / ViewModel | Room emits latest database snapshot on subscribe |

---

## 7. Observability & Privacy Audit

- **Zero-Telemetry Assurance**: Verified zero external network packets during this flow.
- **Logcat PHI Redaction**: Personal health information (cycle dates, symptom severity, BBT values) is **never** logged to production Logcat.
- **Local Isolation**: All mutations remain strictly confined to the app sandbox database (`wellness_tracker_db`).

---

## 8. Authoritative Code Touch Map and Evidence

- [Feature Code Touch Map](code-touch-map.md)
- [Parent Feature Specification](README.md)
- Unit Test Evidence: `./gradlew testDebugUnitTest --tests "<RelatedTestClass>"`
