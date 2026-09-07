# Data Flow: Doctor-Ready Clinical PDF Export

This document details the reactive, end-to-end **Unidirectional Data Flow (UDF)** for compiling health data, rendering vector PDF pages, and sharing reports via Android's `FileProvider`.

---

## 1. Flow Overview

```text
User Taps "Generate Doctor Report" (ExportScreen)
  ↓ (Dispatchers.Main)
ExportViewModel.generatePdfReport()
  ↓ Sets _exportState = ExportState.Generating
  ↓ (Dispatchers.IO via viewModelScope.launch)
DoctorPdfGenerator.generateClinicalReport()
  ↳ Allocates android.graphics.pdf.PdfDocument
  ↳ Starts Page 1 (A4: 595 x 842 pt)
  ↳ Draws header, patient metadata, and cycle regularity summary
  ↳ Draws historical cycles table
  ↳ Draws symptom frequencies & BBT trend summary
  ↳ Writes file to context.cacheDir/reports/
  ↓
FileProvider.getUriForFile() produces secure content:// URI
  ↓ (Dispatchers.Main)
ExportViewModel sets _exportState = ExportState.Success(file, uri)
  ↓
ExportScreen renders "Share with Doctor" and "View Report" actions
  ↳ User taps "Share" → Intent(ACTION_SEND) with FLAG_GRANT_READ_URI_PERMISSION
```

---

## 2. Normal End-to-End Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | `ExportScreen` | `Dispatchers.Main` | User taps "Generate Doctor Report" | UI triggers ViewModel method | Displays circular loading spinner |
| **2** | State | `ExportViewModel` | `viewModelScope` | Sets `_exportState.value = ExportState.Generating` | Updates state stream | Loading card rendered |
| **3** | Platform / Graphics | `DoctorPdfGenerator` | `Dispatchers.IO` | Aggregates snapshot of `cycles`, `symptoms`, and `prediction` | Measures table coordinates; renders text via `nativeCanvas` | Generates vector PDF document |
| **4** | Platform / Disk | `DoctorPdfGenerator` | `Dispatchers.IO` | Creates `File(cacheDir, "reports/clinical_report_....pdf")` | Writes binary PDF stream to disk | Closes `PdfDocument` safely |
| **5** | Platform / Security | `FileProvider` | `Dispatchers.IO` | Resolves `content://` URI from local file | Generates scoped URI | Grants temporary read-only permissions |
| **6** | State | `ExportViewModel` | `Dispatchers.Main` | Updates `_exportState.value = ExportState.Success(file, uri)` | Emits new StateFlow | Notifies UI collectors |
| **7** | Presentation | `ExportScreen` | `Dispatchers.Main` | Screen receives `ExportState.Success` | Recomposes UI | Displays success card with Share & View buttons |
| **8** | Presentation | `ExportScreen` | `Dispatchers.Main` | User taps "Share with Doctor" | Fires `Intent.ACTION_SEND` | Android system share sheet appears |

---

## 3. Threading and Concurrency Boundaries

- **Strict Background Generation**: Vector graphics rendering and disk file writes are dispatched strictly on `Dispatchers.IO` to ensure zero UI frame drops or ANRs on `Dispatchers.Main`.
- **Canvas Cleanup**: `pdfDocument.close()` is called in a guaranteed `try-finally` block to prevent native graphics memory leaks.

---

## 4. Failure and Edge Cases

| Scenario | System Behavior | Safeguard |
| :--- | :--- | :--- |
| **Disk Write Exception** | Caught by `try-catch`; emits `ExportState.Error` | Prevents application crash; displays retry prompt |
| **No App for PDF View** | `ActivityNotFoundException` caught safely | Displays informative toast to install a PDF viewer |
| **Zero Recorded Data** | Generator renders informative "No cycle history recorded yet" section | Document still generates cleanly without NullPointerExceptions |

---

## 5. Privacy & Telemetry Audit

- **100% On-Device Rendering**: Generated completely via Android's local graphics engine.
- **Scoped Cache Storage**: Files reside strictly in `context.cacheDir/reports/` and are automatically reclaimable by the OS.
- **Zero Cloud Leakage**: Export uses Android's native `ACTION_SEND` intent; data leaves the app sandbox only when explicitly directed by the user.
