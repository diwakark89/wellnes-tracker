# Feature: Doctor-Ready Clinical PDF Export

## Human Orientation

### Overview

Doctor-Ready Clinical PDF Export solves a fundamental limitation of mobile health apps: making personal health data useful during real-world medical consultations without exposing sensitive records to the cloud. When diagnosing conditions like polycystic ovary syndrome (PCOS), luteal phase defects, or postpartum irregular bleeding, physicians require clear, aggregated medical summaries rather than a patient scrolling through raw phone screens.

This feature compiles the user's historical cycle intervals, statistical regularity metrics, symptom frequencies, and Basal Body Temperature shifts into a clean, multi-page vector A4 PDF document (`DoctorPdfGenerator`). The PDF includes clinical summaries, average cycle lengths, variation ranges, and a chronological table of recent cycles and biomarkers.

Critically, the entire document generation process executes **100% on-device** using Android's native 2D vector graphics pipeline (`android.graphics.pdf.PdfDocument`). The resulting file is stored in private internal app cache and shared directly via Android's secure `FileProvider`. Zero third-party cloud conversion services or remote servers are ever contacted.

### Feature Snapshot

| Field | Summary |
| --- | --- |
| **Business / Health capability** | Local vector Canvas generation of clinical A4 PDF health reports with secure scoped file sharing |
| **Primary actors** | Users preparing for doctor visits, OB/GYN clinicians, fertility specialists |
| **User value** | Doctor-ready clinical documentation generated on-device with zero privacy or cloud leakage risk |
| **Current state** | `COMPLETED` — Native PDF canvas generator, background IO generation, and FileProvider sharing are fully functional |
| **Main entry points** | `Screen.Export` (`ExportScreen.kt`), `ExportViewModel.kt` |
| **Primary components** | `DoctorPdfGenerator`, `ExportViewModel`, `ExportScreen`, `FileProvider`, `file_paths.xml` |
| **Key boundaries** | Standard A4 dimensions (595 x 842 pt); scoped internal cache storage; zero network permissions |

### Actors and User Value

| Actor | Need or Goal | Value Provided |
| --- | --- | --- |
| **Patient Visiting OB/GYN** | Present clear menstrual history without handing phone to doctor | Generates printable, doctor-friendly PDF summary of cycle lengths and symptoms |
| **Fertility Specialist** | Evaluate biphasic thermal shifts and LH surge timing | Summary details BBT baselines and ovulation test distributions |
| **Privacy-Conscious User** | Share health records without cloud servers or third-party PDF converters | 100% on-device vector rendering via native Android OS graphics |

### Current User Experience

- **Doctor Report Screen (`ExportScreen`)**:
  - Summary preview card detailing the number of cycles and symptoms ready for export.
  - "Generate Clinical Report" primary action button with progress indicator during rendering.
  - Success state card showing generated file name and creation timestamp.
  - Action buttons: "Share with Doctor" (opens system share sheet) and "View Report" (opens default local PDF viewer).

### Scope and Boundaries

#### In Scope
- Vector Canvas rendering of clinical metrics (A4 layout: 595x842 pt).
- Cycle summary statistics: total cycles, average cycle length, shortest/longest cycles.
- Detailed chronological table of recent cycles and symptoms.
- Scoped file sharing via `androidx.core.content.FileProvider`.

#### Out of Scope
- Direct integration with Electronic Health Record (EHR) APIs or HL7/FHIR servers (strictly prohibited to prevent network exposure).
- Cloud storage or cloud printing backends.

### End-to-End User Journey

1. **User navigates to Doctor Report**: User taps the "Doctor Report" tab on the bottom navigation bar.
2. **Reviewing export readiness**: Screen shows active cycle counts and summary details.
3. **Generating PDF**: User taps "Generate Doctor Report"; `ExportViewModel` runs `DoctorPdfGenerator` on a background thread.
4. **Local File Creation**: PDF is rendered onto vector Canvas and saved to app cache.
5. **Sharing**: User taps "Share with Doctor"; Android system share sheet opens with a secure `content://` URI for printing or emailing.

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Presentation (UI)** | Jetpack Compose / M3 | `ExportScreen.kt` displaying export triggers and success states |
| **State Management** | ViewModel / StateFlow | `ExportViewModel.kt` managing `ExportState` (`Idle`, `Generating`, `Success`, `Error`) |
| **Domain Logic** | Domain Repositories | Aggregates `CycleRecord`s, `SymptomLogEntity`s, and `PredictionResult` |
| **Platform / Graphics** | `android.graphics.pdf.PdfDocument` | Native vector Canvas rendering of A4 pages, tables, headers, and text |
| **Platform / Storage** | Android `FileProvider` | Secure scoped URI provision (`file_paths.xml`) |

### Shared Business Rules

1. **A4 Dimensions**: Document pages strictly conform to standard A4 points: width = 595 pt, height = 842 pt.
2. **Clinical Fallbacks**: Any missing biomarker fields display clean medical fallbacks (e.g. `"--"`) rather than `null` or blank gaps.
3. **Scoped Content Sharing**: Files are saved to `context.cacheDir/reports/` and shared via `FLAG_GRANT_READ_URI_PERMISSION`. Files are never written to public external shared storage.

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected Behavior | Owning Component |
| :--- | :--- | :--- |
| **No PDF viewer installed on device** | Error handled gracefully; user advised to install a PDF reader | `ExportViewModel.kt` |
| **Generation interrupted** | Temporary Canvas recycled; `ExportState.Error` emitted | `ExportViewModel.kt` |
| **Zero cycles logged** | PDF generates with informative "No cycle history recorded" section | `DoctorPdfGenerator.kt` |

### Android Platform Invariants

- **IO Thread Offloading**: PDF Canvas generation involves disk I/O and vector drawing; strictly dispatched on `Dispatchers.IO`.
- **Zero-Telemetry**: Zero bytes leave the device over network; sharing is strictly initiated by user via system intents.

### Related Files & Manifest Objects

| Component | Path / Object | Purpose |
| :--- | :--- | :--- |
| Generator | `ui/export/DoctorPdfGenerator.kt` | Vector Canvas rendering engine |
| File Provider Config | `app/src/main/res/xml/file_paths.xml` | Declares `<cache-path name="reports" path="reports/"/>` |
| Manifest Provider | `AndroidManifest.xml` | Configures `androidx.core.content.FileProvider` |

### Tests and Acceptance Evidence

| Scenario or Acceptance Signal | Expected Result | Evidence / Test Command |
| :--- | :--- | :--- |
| PDF generation execution | Generates valid non-empty `.pdf` file in cache directory | `./gradlew test` |
| UI State progression | Transitions `Idle` -> `Generating` -> `Success` | `ExportViewModelTest` |
| FileProvider URI check | Produces valid `content://com.thewalkersoft.tracker.fileprovider/...` URI | Unit test / device verification |

### Related Docs

- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Parent Feature Catalog](../README.md)
- [Developer Operating Manual](../../../AGENTS.md)
