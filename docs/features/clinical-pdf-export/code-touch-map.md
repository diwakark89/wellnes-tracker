# Code Touch Map: Doctor-Ready Clinical PDF Export

This document serves as the authoritative architectural routing map for the **Doctor-Ready Clinical PDF Export** feature.

---

## Code Touch Map

### 1. Presentation Layer — Jetpack Compose UI

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Screen** | `app/src/main/java/com/thewalkersoft/tracker/ui/export/ExportScreen.kt` | `ExportScreen` | `NavigationHost.kt` (`Screen.Export`) | Collects `exportState`, `cycles`, `symptoms` | Renders generation triggers, progress indicators, and share buttons | Export screen layout and user actions |

### 2. State & Lifecycle Layer — ViewModels & Coroutines

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ViewModel** | `app/src/main/java/com/thewalkersoft/tracker/ui/export/ExportViewModel.kt` | `ExportViewModel` | `AppViewModelProvider.Factory` | Combines cycle, symptom, and prediction state flows | Dispatches PDF generation on background coroutine; exposes `ExportState` | PDF generation orchestration and intent launches |
| **State Model** | `app/src/main/java/com/thewalkersoft/tracker/ui/export/ExportViewModel.kt` | `ExportState` | `ExportViewModel` & `ExportScreen` | Sealed class: `Idle`, `Generating`, `Success(file, uri)`, `Error(msg)` | Immutable state transitions | Screen state representation |

### 3. Platform & Graphics Layer — Vector PDF Engine

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Graphics Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PDF Generator** | `app/src/main/java/com/thewalkersoft/tracker/ui/export/DoctorPdfGenerator.kt` | `DoctorPdfGenerator` | `ExportViewModel.kt` | Reads `CycleRecord`s and `SymptomLogEntity`s | **Strict A4 page geometry (595x842 pt)**; vector Canvas rendering; safe fallback for missing values (`"--"`) | PDF formatting, typography, and page layout |
| **File Provider XML**| `app/src/main/res/xml/file_paths.xml` | `<paths>` | `FileProvider` | Maps internal cache directory `reports/` | Secure content URI creation; grants temporary read-only permissions | PDF file sharing and intent resolution |
| **Manifest Entry** | `app/src/main/AndroidManifest.xml` | `<provider>` | Android OS / System | Configures `FileProvider` authority | `com.thewalkersoft.tracker.fileprovider` | OS file sharing boundary |

### 4. Tests & Acceptance Evidence

| Test Suite | Path | Symbol | Verification Target | Command |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | `app/src/test/java/com/thewalkersoft/tracker/...` | `ExampleUnitTest` | Basic test verification | `./gradlew test` |
| **Build Check**| `app/build.gradle.kts` | `:app:assembleDebug` | Validates Android graphics and FileProvider compilation | `./gradlew assembleDebug` |
