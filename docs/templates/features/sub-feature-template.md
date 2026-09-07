# Sub-Feature: <Parent feature name> - <Human-readable sub-feature name>

<!--
Authoring guidance:
- Explain one concrete screen workflow, user interaction slice, or algorithmic subsystem within the parent feature.
- Do not repeat the parent README's complete overview or architecture taxonomy; link back to it for context.
- Keep the Overview to one or two short paragraphs. Lead with the user, problem, current app experience, and outcome.
- Describe only evidence-backed behavior present in the code.
- Keep Current Implementation, Known Gaps, and FUTURE behavior separate.
- Replace every placeholder. Write "Not applicable — <reason>" rather than leaving an empty or generic row.
-->

## Human Orientation

### Overview

<In one or two short paragraphs, explain what this sub-feature does within its parent capability, who uses it, the specific interaction or calculation it addresses, what exists today in the Android app, and the resulting user outcome. Link back to the parent README for capability-wide context.>

### Sub-Feature Snapshot

| Field | Summary |
| --- | --- |
| Parent feature | [<Parent feature name>](../README.md) |
| Primary actors | <User, postpartum mother, healthcare provider> |
| User value | <The concrete outcome or problem solved by this sub-feature> |
| Current state | <`COMPLETED`, `MVP`, `IN_PROGRESS`, `FUTURE`, or `IDEA`, followed by a short evidence-based explanation> |
| Main trigger or entry point | <The user tap, Compose navigation event, modal bottom sheet opening, or calculation trigger> |
| Result | <The observable UI state, chart update, or persisted Room record> |
| Key boundaries | <Local isolation, privacy boundaries, or algorithmic invariants> |

### Current User Experience

<Describe what the user can see, interact with, or receive today. Focus on observable UI components (e.g., slider, date picker, toggle button, chart) rather than internal classes.>

- <Current observable UI interaction or behavior>
- <Immediate user feedback, animation, or visual confirmation>

### Scope and Boundaries

#### In Scope

- <Behavior owned directly by this sub-feature>

#### Out of Scope

- <Parent-owned behavior, adjacent screen workflows, or deferred features>

### User Journey

<Describe the normal user flow from its trigger to its outcome in natural business language.>

1. <The user triggers this workflow (e.g., taps a specific button or card).>
2. <The UI displays the input dialog, sheet, or details view.>
3. <The user provides input or views calculated data.>
4. <The sub-feature validates inputs and updates the local Room database or StateFlow.>
5. <The user sees immediate confirmation and returns to the primary screen.>

### Current Status and Direction

#### Current Implementation

- <What is implemented and verifiable now, with links to source code and tests>

#### Known Gaps or Limitations

- <Known limitation, unresolved UI edge case, or "None currently documented">

#### FUTURE or Deferred

- `FUTURE`: <Planned behavior that is explicitly not current, or "None currently documented">

---

## Engineering Reference

### Business Rules

- <Rule specific to this sub-feature (e.g., temperature valid range 35.0°C to 39.0°C, slider scale 0-10)>
- <Calculation policy, validation rule, or default fallback>
- <Link to the parent README for capability-wide rules instead of duplicating them>

### Failure and Edge-Case Behavior

| Scenario | Expected behavior | Owning component |
| --- | --- | --- |
| <Invalid input entered> | <Inline validation error message; action disabled> | <Compose UI / ViewModel> |
| <No historical data exists> | <Displays empty-state illustration or fallback prompt> | <Composable / Card> |
| <Activity recreation (screen rotation)> | <Maintains user input state without loss> | <ViewModel / rememberSaveable> |

### Code Touch Map

<List the primary Android paths needed to understand or modify this sub-feature. Use repository-relative paths.>

#### Presentation Layer (UI & State)

| Area | Path | Symbol | Role / Description |
| :--- | :--- | :--- | :--- |
| Composable Screen / Card | `app/src/main/java/com/thewalkersoft/tracker/ui/...` | `<ComposableName>` | <Renders UI controls and captures events> |
| ViewModel | `app/src/main/java/com/thewalkersoft/tracker/ui/...` | `<ViewModelName>` | <Manages UiState and dispatches intents> |

#### Domain & Data Layer

| Area | Path | Symbol | Role / Description |
| :--- | :--- | :--- | :--- |
| Domain Logic / Model | `app/src/main/java/com/thewalkersoft/tracker/domain/...` | `<EngineOrModel>` | <Pure domain calculation or data model> |
| Room DAO / Entity | `app/src/main/java/com/thewalkersoft/tracker/data/...` | `<DaoOrEntity>` | <Database read/write operations> |

### Android Invariants Checklist

- [ ] **Zero-Telemetry**: No network calls made; no telemetry or crash SDKs touched.
- [ ] **Dispatcher Correctness**: No Room or heavy calculations executed on the Main thread.
- [ ] **State Flow Hygiene**: ViewModels use `SharingStarted.WhileSubscribed(5000)`.
- [ ] **Compose Performance**: State hoisted properly; no object allocation inside Canvas `DrawScope`.

### Tests and Acceptance Evidence

| Scenario or acceptance signal | Expected result | Test command or evidence |
| :--- | :--- | :--- |
| <Sub-feature unit test> | <Asserts correct calculation or state mutation> | `./gradlew testDebugUnitTest --tests "<TestClass>"` |
| <Composable Preview> | <Renders interactive preview cleanly> | `@Preview in <ComposableFile>` |

### Related Docs

- [Parent Feature README](../README.md)
- [Parent Code Touch Map](../code-touch-map.md)
- [Parent Data Flow](../data-flow.md)
