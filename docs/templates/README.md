# Feature Documentation Templates for Android

This directory contains the canonical feature documentation templates for **Wellness Tracker** (`com.thewalkersoft.tracker`). These templates are specifically structured around **Android Clean Architecture**, **Unidirectional Data Flow (UDF)**, and our strict **100% On-Device & Zero-Telemetry** privacy model.

---

## 📂 Template Suite Structure

The documentation suite provides four modular templates located in [`features/`](features/):

| Template | File | Purpose | When to Use |
| :--- | :--- | :--- | :--- |
| **Feature README** | [`feature-readme-template.md`](features/feature-readme-template.md) | Canonical business capability specification | Start here for any user-visible feature (e.g., Cycle Prediction, Symptom Tracking, Clinical PDF Export). |
| **Sub-Feature** | [`sub-feature-template.md`](features/sub-feature-template.md) | Single screen journey, modal workflow, or algorithmic slice | Use when a feature has multiple distinct UI flows or complex sub-capabilities (e.g., BBT chart logging vs LH test logging). |
| **Android Code Touch Map** | [`code-touch-map-template.md`](features/code-touch-map-template.md) | Authoritative code routing across Android layers | Mandatory technical routing map detailing symbols, caller dependencies, state access, and change impact. |
| **Cross-Layer Flow** | [`cross-layer-flow-template.md`](features/cross-layer-flow-template.md) | End-to-end UDF flow sequence & threading model | Use for tracing reactive data flows crossing Compose UI, ViewModel, Repository, Room SQLite, and OS services. |

---

## 🏗️ Android Clean Architecture Taxonomy

All templates map directly to the application's clean architectural layers:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Presentation Layer (Jetpack Compose & Material 3)        │
│    Screens, Cards, Dialogs, BottomSheets, Custom Canvas     │
├─────────────────────────────────────────────────────────────┤
│ 2. State & Lifecycle Layer (ViewModels & Coroutines)        │
│    Immutable UiState, StateFlow, WhileSubscribed(5000)      │
├─────────────────────────────────────────────────────────────┤
│ 3. Domain Layer (Pure Kotlin Business Logic)                │
│    Domain Models, CyclePredictorEngine, Repository Contracts │
├─────────────────────────────────────────────────────────────┤
│ 4. Data Layer (Room Persistence & Local Storage)            │
│    RepositoryImpl, Room DAOs, SQLite Entities, Converters   │
├─────────────────────────────────────────────────────────────┤
│ 5. Platform Subsystems & Hardware Services                  │
│    BiometricPrompt, Native Vector PdfDocument, FileProvider │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Core Android Platform Invariants

Every feature document generated from these templates must enforce and document the following four platform invariants:

### 1. 100% On-Device & Zero-Telemetry
- The app strictly omits `android.permission.INTERNET`.
- No remote network requests, analytics SDKs, crash trackers, or cloud sync backends may be introduced.
- Sensitive health data (PHI/PII) must **never** be logged to `Logcat` or system logs in production.

### 2. Threading & Coroutine Dispatchers
- **No blocking work on `Dispatchers.Main`**: All Room database operations, vector PDF generation, and statistical calculations must run on `Dispatchers.IO` or `Dispatchers.Default`.
- State collection must use `SharingStarted.WhileSubscribed(5000)` to stop upstream observation when the UI is in the background.

### 3. Lifecycle, Process Death & State Restoration
- ViewModels must safely handle activity recreation and configuration changes (e.g., screen rotation).
- Critical transient user input in modal bottom sheets must survive or gracefully handle process death.

### 4. Jetpack Compose & Canvas Performance
- Custom Canvas drawings (`Canvas`, `DrawScope`) must **never allocate objects** (`Paint`, `Path`, `PathEffect`) inside `onDraw`. Objects must be memoized using `remember`.
- Composable functions must adhere to stability guidelines to minimize unnecessary recompositions.

---

## 🚀 How to Create a New Feature Document

1. **Create the Feature Folder**:
   Create a folder under `docs/features/<feature-slug>/` (e.g., `docs/features/bbt-tracking/`).

2. **Copy the Templates**:
   - Copy `features/feature-readme-template.md` to `docs/features/<feature-slug>/README.md`.
   - Copy `features/code-touch-map-template.md` to `docs/features/<feature-slug>/code-touch-map.md`.
   - Copy `features/cross-layer-flow-template.md` to `docs/features/<feature-slug>/data-flow.md`.
   - If the feature has sub-capabilities, create a `sub-features/` directory and copy `features/sub-feature-template.md` for each slice.

3. **Complete the Placeholders**:
   - Replace every `<placeholder>` with evidence-backed descriptions.
   - For components or concerns that do not apply, explicitly write `Not applicable — <reason>` rather than deleting sections.
   - Separate verified existing behavior from `FUTURE` or planned features.

4. **Reference Implementation**:
   For a gold-standard reference of a fully instantiated feature document, see:
   👉 **[`docs/features/adaptive-cycle-prediction/`](../features/adaptive-cycle-prediction/)**
