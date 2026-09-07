# Data Flow: Cycle History Analytics & Timeline

This document details the reactive, end-to-end **Unidirectional Data Flow (UDF)** for calculating historical cycle intervals, rendering the bar chart, and mutating past entries.

---

## 1. Flow Overview

```text
Period Logs in Database ('period_logs')
  ↓ (SQLite Thread Pool)
PeriodLogDao.getAllPeriodLogsFlow()
  ↓ (Dispatchers.Default)
CycleRepositoryImpl.getAllCycleRecords()
  ↳ Pairs adjacent period start dates chronologically
  ↳ Computes ChronoUnit.DAYS.between(currentStart, nextStart)
  ↳ Produces List<CycleRecord>
  ↓ (viewModelScope)
HistoryViewModel.cycleRecords (StateFlow)
  ↓ (Dispatchers.Main)
HistoryScreen collects StateFlow
  ↳ CycleLengthChart draws completed cycle bars on Canvas
  ↳ LazyColumn renders CycleHistoryItem cards
  ↳ User taps card → LogPeriodBottomSheet opens for inline edit
```

---

## 2. Inline Edit & Recalculation Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | `CycleHistoryItem` | `Dispatchers.Main` | User taps card to edit historical entry | Passes selected `CycleRecord` to state | Opens `LogPeriodBottomSheet` pre-populated |
| **2** | Presentation | `LogPeriodBottomSheet` | `Dispatchers.Main` | User modifies dates or notes and taps "Save" | Invokes ViewModel callback | Passes updated parameters to `HistoryViewModel` |
| **3** | State | `HistoryViewModel` | `viewModelScope` | `updatePeriodAndSymptoms()` executes | Launches coroutine | Updates both `PeriodLogEntity` and linked `SymptomLogEntity` |
| **4** | Data | `CycleRepositoryImpl` | `Dispatchers.IO` | Calls `periodLogDao.updatePeriodLog(entity)` | Updates row in SQLite | Triggers Room table invalidation |
| **5** | Persistence | `PeriodLogDao` | SQLite Pool | `getAllPeriodLogsFlow()` automatically re-queries | Emits new `List<PeriodLogEntity>` | Streams upstream to Repository |
| **6** | Domain / Data | `CycleRepositoryImpl` | `Dispatchers.Default` | Recalculates intervals between all entries | Produces revised `List<CycleRecord>` | Emits to `HistoryViewModel.cycleRecords` |
| **7** | Presentation | `HistoryScreen` | `Dispatchers.Main` | Collects updated state | Triggers recomposition | Bar chart and list cards re-render with updated duration |

---

## 3. Threading and Concurrency Boundaries

- **Background Interval Calculation**: Historical interval pairing and day arithmetic take place on `Dispatchers.Default` inside the repository flow operator.
- **Canvas Bar Scaling**: Bar coordinate mapping and text centering are computed synchronously within the `CycleLengthChart` Compose measure/layout pass, avoiding allocations during `onDraw`.

---

## 4. Failure and Edge Cases

| Scenario | System Behavior | Safeguard |
| :--- | :--- | :--- |
| **Single cycle recorded** | Bar chart shows empty placeholder; single card displays "Current" status | Guard check `cycles.size >= 2` prevents index errors |
| **User deletes period entry** | Entry is deleted; previous and next periods automatically merge interval | Room `@Delete` triggers complete interval re-pairing |
| **Date overlaps existing period** | Room maintains chronological ordering by `startDate DESC` | Sort invariant preserved |

---

## 5. Privacy & Telemetry Audit

- **Zero-Telemetry**: No network calls; historical duration data is strictly stored in local Room SQLite database.
- **Logcat Redaction**: Dates and notes are never printed to system logs in production.
