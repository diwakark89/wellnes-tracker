# Feature: Biomarker & Daily Symptom Logging

## Human Orientation

### Overview

Biomarker & Daily Symptom Logging empowers users to capture multi-dimensional physiological signals alongside their menstrual periods. While traditional apps focus only on bleeding start and end dates, Wellness Tracker captures clinical biomarkers including Basal Body Temperature (BBT), Luteinizing Hormone (LH) ovulation test results, cramps/pain intensity on a numeric visual scale, and emotional mood states.

To support natural family planning and medical diagnosis of ovulatory disorders, the feature provides a smoothed vector Bezier temperature trend chart (`BbtTrendChart`). This chart visualizes biphasic thermal shifts—the subtle post-ovulation temperature rise driven by progesterone—enabling users to visually confirm ovulation and identify luteal phase defects without relying on external algorithms.

All logged symptoms and biomarkers are stored in local Room SQLite tables on the device. Data logging is accessible both directly from the dedicated Symptoms screen and seamlessly via the comprehensive period logging bottom sheet.

### Feature Snapshot

| Field | Summary |
| --- | --- |
| **Business / Health capability** | Multi-biomarker symptom recording, LH surge tracking, and hardware-accelerated BBT trend visualization |
| **Primary actors** | Users tracking fertility windows, individuals diagnosing cycle symptoms, healthcare providers |
| **User value** | Evidence-backed symptom history and visual BBT biphasic shift confirmation without cloud privacy risks |
| **Current state** | `COMPLETED` — Room tables, ViewModel reactive streams, Compose UI, and smoothed Canvas charts are fully implemented |
| **Main entry points** | `Screen.Symptoms` (`SymptomsScreen.kt`), `BbtTrendChart.kt`, `LogPeriodBottomSheet.kt` |
| **Primary components** | `SymptomsViewModel`, `BbtTrendChart`, `SymptomLogDao`, `SymptomLogEntity`, `LogPeriodBottomSheet` |
| **Key boundaries** | Strictly on-device storage; temperature bounds [35.0°C, 39.0°C]; cramps severity scale [1 to 5] |

### Actors and User Value

| Actor | Need or Goal | Value Provided |
| --- | --- | --- |
| **Fertility / Conception Seeker** | Confirm natural ovulation timing via physiological biomarkers | Visualizes LH surge states and post-ovulatory BBT thermal shifts |
| **User Managing Pain / Endometriosis** | Monitor cramp severity trends throughout cycle phases | Quantifies pain on an intuitive 1-5 slider and visualizes correlations |
| **Healthcare Provider** | Review accurate biomarker logs during clinical consultation | Clinical export aggregates daily symptoms, mood frequencies, and temperature baselines |

### Current User Experience

- **Symptoms Overview Screen**: Shows the last 10 logged BBT measurements rendered on a custom Canvas curve, followed by chronological symptom history cards.
- **`BbtTrendChart` Interactive Canvas**:
  - Continuous smoothed cubic Bezier line with gradient fill below the curve.
  - Horizontal dashed reference lines marking the baseline ($36.5^\circ\text{C}$) and peak thresholds.
  - Formatted temperature indicators ($^\circ\text{C}$) and localized date labels.
- **Quick Logging Bottom Sheet (`LogPeriodBottomSheet`)**:
  - Basal Body Temperature input field with decimal precision.
  - Cramps severity interactive slider (scale 1 to 5).
  - Mood selector chips (`HAPPY`, `CALM`, `TIRED`, `IRRITABLE`, `ANXIOUS`, `SAD`).
  - LH Ovulation test result selector (`NEGATIVE`, `POSITIVE`, `PEAK`).
- **Symptom History List**: Displays previous daily logs with one-tap delete capabilities and visual tags for mood, cramps, and LH status.

### Scope and Boundaries

#### In Scope
- Logging, updating, and deleting daily symptom records.
- Basal Body Temperature tracking and Bezier curve rendering.
- Cramps severity, mood, and LH ovulation test recording.
- Reactive updates to the Dashboard daily check-in card.

#### Out of Scope
- Integration with proprietary Bluetooth thermometers (strictly prohibited to avoid Bluetooth/location permission footprint).
- Cloud synchronization.

### End-to-End User Journey

1. **User opens Logging**: User taps "+ Quick Log" or opens the Symptoms tab.
2. **User records biomarkers**: Enters morning BBT (e.g., $36.7^\circ\text{C}$), selects cramps severity, tags mood as `CALM`, and selects LH `PEAK`.
3. **Validation & Persistence**: App validates numerical formats and saves the `SymptomLogEntity` into Room SQLite via `CycleRepository.insertSymptom()`.
4. **Reactive Flow Update**: `SymptomLogDao.getAllSymptoms()` emits the updated list.
5. **Chart & UI Recomposition**: `SymptomsScreen` instantly re-renders the `BbtTrendChart` and inserts the entry at the top of the history list.

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Presentation (UI)** | Jetpack Compose / Canvas | `SymptomsScreen.kt`, `BbtTrendChart.kt`, `LogPeriodBottomSheet.kt` |
| **State Management** | ViewModel / StateFlow | `SymptomsViewModel.kt` exposing `StateFlow<List<SymptomLogEntity>>` |
| **Domain Logic** | Repository Interface | `CycleRepository.getAllSymptoms()`, `insertSymptom()`, `deleteSymptomById()` |
| **Data & Persistence** | Room SQLite / DAO | `SymptomLogEntity.kt`, `SymptomLogDao.kt`, `DateConverters.kt` |
| **Hardware / Canvas** | Native Graphics / Compose Canvas | Hardware-accelerated cubic Bezier path rendering on `Canvas` |

### Shared Business Rules

1. **BBT Valid Range**: Temperatures are mapped within realistic human ranges ($35.0^\circ\text{C}$ to $39.0^\circ\text{C}$). Values outside this range are rejected during input validation.
2. **One Entry Per Date**: Each calendar date maintains a single aggregated `SymptomLogEntity`. Logging updates on an existing date replaces or amends the day's record (`OnConflictStrategy.REPLACE`).
3. **Cramps Scale**: Cramp severity is modeled as an integer from 1 (mild) to 5 (severe), mapped smoothly to user sliders.
4. **Ovulation Test States**: Restricted to standardized clinical states: `NEGATIVE`, `POSITIVE`, and `PEAK`.

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected Behavior | Owning Component |
| :--- | :--- | :--- |
| **No BBT data recorded** | Chart displays "No temperature logs recorded yet" guidance banner | `BbtTrendChart.kt` |
| **Single BBT data point** | Renders a single point marker with horizontal baseline guide | `BbtTrendChart.kt` |
| **Non-numeric temperature entered** | Input error state displayed; save button disabled | `LogPeriodBottomSheet.kt` |
| **Rapid successive taps on delete** | Handled idempotently via Room `deleteSymptomById(id)` | `SymptomLogDao.kt` |

### Android Platform Invariants

- **Zero Allocations in DrawScope**: `BbtTrendChart` computes paths and avoids creating `Paint` or `Path` objects in the drawing loop.
- **Room Threading**: All CRUD operations dispatched via `Dispatchers.IO`.
- **StateFlow Lifecycle**: `SymptomsViewModel` state collected safely via `SharingStarted.WhileSubscribed(5000)`.

### Related Database Objects

| Table / Entity | DAO | Type Converters | Purpose |
| :--- | :--- | :--- | :--- |
| `symptom_logs` (`SymptomLogEntity`) | `SymptomLogDao` | `DateConverters` | Stores daily BBT, cramps severity, mood, and LH ovulation test results |

### Tests and Acceptance Evidence

| Scenario or Acceptance Signal | Expected Result | Evidence / Test Command |
| :--- | :--- | :--- |
| Room DAO insert & query | Accurately writes and queries daily symptom records | `./gradlew test` |
| BBT Chart Preview | Renders smooth curve with sample temperatures | `@Preview in BbtTrendChart.kt` |
| ViewModel state emission | `SymptomsViewModel` emits updated list upon repository changes | `SymptomsViewModelTest` / Unit tests |

### Related Docs

- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Parent Feature Catalog](../README.md)
- [Developer Operating Manual](../../../AGENTS.md)
