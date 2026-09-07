# Feature: <Human-readable feature name>

<!--
Authoring guidance:
- Write for a human or AI coding agent who does not already know this feature, then provide architectural routing details.
- Keep the Overview to two or three short paragraphs. Explain the clinical/wellness capability, target user need, current user experience, and health outcome; do not merely restate the feature name.
- Describe only evidence-backed behavior present in the codebase. Never present planned behavior as implemented.
- Label current, limited, and FUTURE behavior separately.
- Adhere strictly to the application's 100% On-Device & Zero-Telemetry privacy non-negotiables.
- Replace every placeholder. For an irrelevant component or concern, write "Not applicable — <reason>" rather than leaving an empty or generic row.
-->

## Human Orientation

### Overview

<In two or three short paragraphs, explain what this feature is, who it serves, the problem it solves, what users can do in the Android app today, and the outcome it creates. Mention an important boundary or relationship to another feature only when it is necessary to understand the big picture. Do not use this section as a component inventory or commit history.>

### Feature Snapshot

| Field | Summary |
| --- | --- |
| Business / Health capability | <One-sentence description of the capability> |
| Primary actors | <Users tracking menstrual health, postpartum mothers, healthcare providers reviewing reports> |
| User value | <The practical outcome, clinical utility, or problem solved> |
| Current state | <Use project labels: `COMPLETED`, `MVP`, `IN_PROGRESS`, `FUTURE`, or `IDEA`, followed by a short evidence-based explanation> |
| Main entry points | <Primary Compose routes (e.g., `Screen.Dashboard`, `Screen.Symptoms`), modal bottom sheets, or quick-action buttons> |
| Primary components | <Key ViewModels, Composables, Engines, and Room DAOs owning the behavior> |
| Key boundaries | <Zero-telemetry boundaries, isolated calculation rules, protected invariants> |

### Actors and User Value

| Actor | Need or goal | Value provided |
| --- | --- | --- |
| <Actor> | <What the user needs to accomplish> | <How this feature helps> |

### Current User Experience

<Describe the observable experience that exists now. Focus on what the user can see, interact with, or configure on their Android device rather than classes or tables.>

- <Current observable UI behavior, e.g., interactive Compose card, dial, slider, or chart>
- <User input, feedback, or visual confirmation>

### Scope and Boundaries

#### In Scope

- <Current behavior owned by this feature>

#### Out of Scope

- <Behavior owned elsewhere, cloud sync (prohibited), or deferred capabilities>

### Capability Map

<Explain in one sentence how the sub-features combine to deliver the parent capability. If there are no sub-feature documents, say so and summarize the capability slices directly.>

| Sub-feature or capability slice | Document | Purpose | Current state |
| --- | --- | --- | --- |
| <Sub-feature> | [<Document title>](sub-features/<sub-feature>.md) | <Business purpose, not an implementation description> | <Current/FUTURE status> |

### End-to-End User Journey

<Describe the normal user journey on the Android device from initial trigger to resulting outcome. Keep the steps technology-neutral; place ViewModels, Coroutines, and Room tables in Technical Data Flow.>

1. <User triggers the workflow, e.g., navigates to Screen or taps Quick Log.>
2. <The UI displays current state or renders input controls.>
3. <The user inputs biomarker data or reviews evaluated health metrics.>
4. <The app validates input and updates local state.>
5. <The UI updates reactively, reflecting updated predictions, charts, or summaries.>

### Current Status and Direction

#### Current Implementation

- <What is implemented and verifiable now, with links to source code and tests>

#### Known Gaps or Limitations

- <Known limitation, unresolved decision, incomplete UI state, or "None currently documented">

#### FUTURE or Deferred

- `FUTURE`: <Planned capability that is explicitly not current behavior, or "None currently documented">

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Presentation (UI)** | Jetpack Compose / M3 | <Compose screen, card, custom Canvas chart, or modal bottom sheet; or Not applicable — reason> |
| **State Management** | ViewModel / StateFlow | <ViewModel holding immutable UiState, coroutineScope, WhileSubscribed; or Not applicable — reason> |
| **Domain Logic** | Pure Kotlin Engine / Model | <Business rules, statistical predictor engine, domain model; or Not applicable — reason> |
| **Data & Persistence** | Room SQLite / DAO | <Entity table, DAO observable Flow, TypeConverter; or Not applicable — reason> |
| **Platform / OS Services** | AndroidX / Native Graphics | <BiometricPrompt, Vector PdfDocument, FileProvider, SharedPreferences; or Not applicable — reason> |

### Technical Data Flow (Unidirectional Data Flow)

<Show the current technical ownership and data movement across Android Clean Architecture layers. Remove unused steps and link to data-flow.md when sequencing needs detail.>

```text
User gesture / Lifecycle trigger
  ↓
Composable Screen / Component (Jetpack Compose)
  ↓
ViewModel Intent / Action (Dispatchers.Main)
  ↓
Domain Use Case / Pure Engine (CyclePredictorEngine / Pure Kotlin)
  ↓
Repository Implementation (Dispatchers.IO / Dispatchers.Default)
  ↓
Room DAO / SQLite Database (Encrypted on-device storage)
  ↓
Observable Flow<List<Entity>> emission
  ↓
ViewModel UiState update (StateFlow)
  ↓
Recomposition of UI (Screen / Custom Canvas Charts)
```

### Shared Business Rules

- <Authoritative rule, medical/clinical formula, interval constraint, or statistical calculation policy>
- <Rule shared across multiple screens or domain components>

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected behavior | Owning component |
| --- | --- | --- |
| <Empty database state / initial install> | <Fallback defaults, e.g., heuristic 30-40 day window> | <Predictor / ViewModel> |
| <Breakthrough spotting (< 14 days apart)> | <Classified as spotting, excluded from cycle interval baselines> | <CyclePredictorEngine> |
| <Invalid biomarker input (out of range)> | <Inline validation error; submission disabled> | <Compose UI / ViewModel> |
| <Process death / Configuration change> | <ViewModel preserves StateFlow; UI restores seamlessly> | <ViewModel / SavedState> |

### Android Platform Invariants

- **Zero-Telemetry Verification**: Verified zero network calls; no `android.permission.INTERNET` declared; no external SDK trackers.
- **Dispatcher Concurrency**: Room transactions and heavy math dispatched to `Dispatchers.IO` / `Dispatchers.Default`; UI updates collected safely via `viewModelScope`.
- **Flow Lifecycle Hygiene**: StateFlows exposed via `SharingStarted.WhileSubscribed(5000)` to stop background flow processing when UI is stopped.
- **Canvas Allocations**: Any custom Canvas drawing (`Canvas`, `DrawScope`) avoids allocations inside `onDraw` blocks; `Paint` and `Path` memoized via `remember`.

### Code Touch Map Summary

<Keep this summary concise. Link to code-touch-map.md for detailed symbol tables.>

| Layer | Primary Path | Role / Symbol |
| :--- | :--- | :--- |
| UI | `app/src/main/java/com/thewalkersoft/tracker/ui/...` | `<Composable screen or component>` |
| ViewModel | `app/src/main/java/com/thewalkersoft/tracker/ui/...` | `<FeatureViewModel>` |
| Domain | `app/src/main/java/com/thewalkersoft/tracker/domain/...` | `<Engine or Model>` |
| Data | `app/src/main/java/com/thewalkersoft/tracker/data/...` | `<Entity, DAO, or RepositoryImpl>` |
| Platform | `app/src/main/java/com/thewalkersoft/tracker/...` | `<Biometric, PDF, or Preference helper>` |
| Tests | `app/src/test/java/com/thewalkersoft/tracker/...` | `<UnitTest class>` |

### Related Database Objects

<List Room SQLite tables, DAOs, indices, and type converters involved in this feature.>

| Table / Entity | DAO | Type Converters | Purpose |
| :--- | :--- | :--- | :--- |
| `<table_name>` (`<EntityClass>`) | `<DaoClass>` | `<DateConverters>` | <Business data or persistence responsibility> |

### Tests and Acceptance Evidence

| Scenario or acceptance signal | Expected result | Test command or evidence |
| :--- | :--- | :--- |
| <Happy path business calculation> | <Expected domain calculation output> | `./gradlew testDebugUnitTest --tests "<TestClass>"` |
| <Edge case (e.g. irregular cycles, reset)> | <Graceful fallback or suppression> | `<UnitTest method>` |
| <Compose UI Preview> | <Renders mock preview without crash> | `@Preview in <ComposableFile>` |

### Related Docs

- [Master Feature Index](../../README.md)
- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Algorithmic Specification](../../CYCLE_PREDICTION_ENGINE.md)
- [Developer Operating Manual](../../../AGENTS.md)

### AI Agent Notes

- Read the Overview, Snapshot, and Android Platform Invariants before making code changes.
- Start code exploration from the linked [Code Touch Map](code-touch-map.md).
- Preserve the distinction between verified current behavior, known gaps, and `FUTURE` enhancements.
- Update only sections impacted by an incremental change; do not mechanically rewrite the entire document.
- Ensure all Room queries and Canvas drawing rules comply with `AGENTS.md` and `.agents/skills/`.
