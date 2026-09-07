# Feature: Cycle History Analytics & Timeline

## Human Orientation

### Overview

Cycle History Analytics & Timeline provides users and clinicians with a clear, longitudinal view of reproductive health patterns over time. Menstrual cycles naturally vary across months due to stress, illness, travel, or hormonal shifts; this feature analyzes completed cycles to compute exact cycle lengths, gap intervals between periods, and historical averages.

To deliver immediate visual insights, the feature incorporates a custom Canvas bar chart (`CycleLengthChart`). The chart displays the user's completed cycles side-by-side, compares each cycle against their historical rolling average, and highlights normal fluctuations versus clinical anomalies (e.g., cycles under 21 days or over 45 days).

Furthermore, the feature provides full **inline CRUD editing**. Users can tap on any historical period card to modify start/end dates, adjust flow intensity, amend associated symptom logs, or remove erroneous entries. All edits instantly trigger reactive recalculations across both the history bar chart and the primary cycle prediction engine on the dashboard.

### Feature Snapshot

| Field | Summary |
| --- | --- |
| **Business / Health capability** | Longitudinal cycle interval analytics, historical length distribution charts, and inline entry editing |
| **Primary actors** | Users reviewing cycle trends, individuals auditing irregular cycles, healthcare providers |
| **User value** | Visual identification of cycle regularity trends and full control over historical health records |
| **Current state** | `COMPLETED` — History screen, Canvas bar chart, ViewModel CRUD actions, and Room updates are fully operational |
| **Main entry points** | `Screen.History` (`HistoryScreen.kt`), `CycleHistoryItem.kt`, `CycleLengthChart.kt` |
| **Primary components** | `HistoryViewModel`, `CycleLengthChart`, `CycleHistoryItem`, `PeriodLogDao`, `CycleRepositoryImpl` |
| **Key boundaries** | Strictly on-device storage; interval calculation requires $\ge 2$ recorded periods |

### Actors and User Value

| Actor | Need or Goal | Value Provided |
| --- | --- | --- |
| **User Monitoring Cycle Regularity** | Understand personal cycle variance over recent months | Bar chart visualizes cycle length deviations from the rolling average |
| **User Correcting Mistyped Dates** | Update previous period dates or flow levels retroactively | One-tap inline edit sheet amends period logs and recalculates predictions |
| **Clinician Diagnosing Irregularity** | Review historical cycle intervals and gap statistics | Clear chronological timeline with duration tags (e.g. "28 days", "32 days") |

### Current User Experience

- **Cycle History Screen**: Chronological timeline of all logged periods, showing start and end dates, flow intensity pills, and calculated cycle duration tags.
- **`CycleLengthChart` Custom Bar Chart**:
  - Side-by-side vertical rounded bars representing the last 8 completed cycle durations.
  - Horizontal dashed reference line indicating the user's historical average cycle length (or 28-day baseline).
  - Dynamic bar coloring: Highlights cycles adhering to standard clinical bounds vs atypical variations.
  - Duration labels (e.g., "29d") positioned above each bar and localized start date labels below.
- **Inline Period Editor**: Tapping an item opens `LogPeriodBottomSheet` pre-populated with that entry's dates, notes, flow, and symptoms for immediate editing or deletion.

### Scope and Boundaries

#### In Scope
- Calculation of cycle duration intervals: $\text{duration} = \text{startDate}_{i+1} - \text{startDate}_i$.
- Historical bar chart visualization for completed cycles.
- Updating period dates, notes, flow, and attached symptoms.
- Deleting individual period logs.

#### Out of Scope
- Automated diagnostic labeling of medical conditions (PCOS, endometriosis).
- Cloud backup or multi-device sync.

### End-to-End User Journey

1. **User opens History**: User taps the "History" tab on the bottom navigation bar.
2. **Reviewing Analytics**: User observes the `CycleLengthChart` comparing recent cycle lengths against their personal average.
3. **Editing an Entry**: User identifies a typo in a previous entry and taps the card.
4. **Updating Values**: The pre-filled bottom sheet opens; user changes the end date and updates cramp severity.
5. **Persistence & Recalculation**: `HistoryViewModel` writes the update to Room; both the history chart and dashboard forecast update reactively.

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Presentation (UI)** | Jetpack Compose / Canvas | `HistoryScreen.kt`, `CycleHistoryItem.kt`, `CycleLengthChart.kt` |
| **State Management** | ViewModel / StateFlow | `HistoryViewModel.kt` exposing `StateFlow<List<CycleRecord>>` and handling CRUD actions |
| **Domain Logic** | Domain Model / Engine | `CycleRecord.kt` computing duration; `CyclePredictorEngine` computing intervals |
| **Data & Persistence** | Room SQLite / DAO | `PeriodLogDao.kt`, `PeriodLogEntity.kt`, `SymptomLogDao.kt` |
| **Hardware / Canvas** | Native Graphics / Compose Canvas | Custom Canvas bar chart rendering with density-scaled typography |

### Shared Business Rules

1. **Completed Cycle Definition**: A cycle interval can only be calculated when a subsequent period start date exists. The most recent (active) cycle is marked as "Current" rather than completed.
2. **Cycle Length Calculation**:
   $$\text{Length in Days} = \text{ChronoUnit.DAYS.between}(\text{startDate}_i, \text{startDate}_{i+1})$$
3. **Spotting Threshold**: Intervals $< 14$ days are treated as mid-cycle spotting anomalies and filtered out from cycle bar chart distribution.

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected Behavior | Owning Component |
| :--- | :--- | :--- |
| **Fewer than 2 periods logged** | Bar chart hidden or displays informational fallback; list shows single entry | `CycleLengthChart.kt` / `HistoryScreen.kt` |
| **Deleting a period log** | Room removes row; remaining periods re-evaluate intervals immediately | `HistoryViewModel.kt` / `PeriodLogDao.kt` |
| **Atypical cycle (> 60 days)** | Chart caps display bounds gracefully to prevent layout distortion | `CycleLengthChart.kt` |

### Android Platform Invariants

- **Canvas Allocation Avoidance**: `CycleLengthChart` pre-computes bar dimensions and reuses density metrics; avoids object allocations in `onDraw`.
- **Database Threading**: All update and delete queries dispatched via `viewModelScope.launch(Dispatchers.IO)`.
- **StateFlow Hygiene**: History records stream uses `SharingStarted.WhileSubscribed(5000)`.

### Related Database Objects

| Table / Entity | DAO | Type Converters | Purpose |
| :--- | :--- | :--- | :--- |
| `period_logs` (`PeriodLogEntity`) | `PeriodLogDao` | `DateConverters` | Source of historical period entries |
| `symptom_logs` (`SymptomLogEntity`) | `SymptomLogDao` | `DateConverters` | Linked daily symptom records updated during inline edits |

### Tests and Acceptance Evidence

| Scenario or Acceptance Signal | Expected Result | Evidence / Test Command |
| :--- | :--- | :--- |
| Interval calculation tests | Correctly calculates durations across leap years and month boundaries | `CyclePredictorEngineTest.kt` (`./gradlew test`) |
| Bar Chart Preview | Renders bars and average reference lines cleanly | `@Preview in CycleLengthChart.kt` |
| Entry deletion & update | Room database successfully modifies rows | `./gradlew test` |

### Related Docs

- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Parent Feature Catalog](../README.md)
- [Developer Operating Manual](../../../AGENTS.md)
