# Data Flow: Biomarker & Daily Symptom Logging

This document details the reactive, end-to-end **Unidirectional Data Flow (UDF)** for recording, persisting, and rendering daily symptoms and Basal Body Temperature measurements.

---

## 1. Flow Overview

```text
User Logs BBT & Symptoms (LogPeriodBottomSheet)
  ↓ (Dispatchers.Main)
User taps "Save" with BBT, Cramps, Mood, and LH Surge status
  ↓ (viewModelScope / Suspend fun)
HistoryViewModel / DashboardViewModel invokes repository
  ↓ (Dispatchers.IO)
CycleRepositoryImpl.insertSymptom(symptomEntity)
  ↓ (SQLite Thread Pool)
SymptomLogDao.insertSymptom() → AppDatabase ('symptom_logs')
  ↓ (Room Invalidation Tracker)
SymptomLogDao.getAllSymptoms() [Reactive Flow Emission]
  ↓ (viewModelScope)
SymptomsViewModel.symptoms StateFlow receives updated List<SymptomLogEntity>
  ↓ (Dispatchers.Main)
SymptomsScreen collects symptoms StateFlow
  ↳ BbtTrendChart filters (basalBodyTemp != null) and renders cubic Bezier curve on Canvas
  ↳ Symptoms list renders historical cards with tags
```

---

## 2. Normal End-to-End Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | `LogPeriodBottomSheet` | `Dispatchers.Main` | User inputs BBT (e.g. 36.65), adjusts cramps slider, selects mood | Captures draft state in Compose `remember` | Validates float range [35.0, 39.0] |
| **2** | State | `HistoryViewModel` / `DashboardViewModel` | `viewModelScope` | Receives values from bottom sheet onSave callback | Dispatches coroutine | Invokes `repository.insertSymptom(entity)` |
| **3** | Data | `CycleRepositoryImpl` | `Dispatchers.IO` | Prepares `SymptomLogEntity` | Prepares SQLite statement | Calls `symptomLogDao.insertSymptom(entity)` |
| **4** | Persistence | `SymptomLogDao` | SQLite Pool | Executes SQLite insert with `OnConflictStrategy.REPLACE` | Writes row into `symptom_logs` | Triggers Room table invalidation |
| **5** | Persistence | `SymptomLogDao` | SQLite Pool | `getAllSymptoms()` queries updated table | Emits updated `List<SymptomLogEntity>` | Flow streams upstream |
| **6** | State | `SymptomsViewModel` | `viewModelScope` | StateFlow receives new list of symptoms | Emits new `StateFlow` state | Active UI collectors notified |
| **7** | Presentation | `SymptomsScreen` | `Dispatchers.Main` | `collectAsState()` receives new list | Triggers recomposition | Screen updates cards |
| **8** | Presentation | `BbtTrendChart` | `Dispatchers.Main` (Canvas) | Filters last 10 entries with non-null BBT | Computes normalized points & Bezier curve | Draws smoothed gradient path on Canvas |

---

## 3. Threading and Concurrency Boundaries

- **Main Thread Isolation**: Input parsing, validation, and bottom sheet state management occur on `Dispatchers.Main`. All database interactions run on `Dispatchers.IO`.
- **Canvas Rendering Hygiene**:
  - `BbtTrendChart` performs vector path calculations within `Canvas`.
  - In compliance with `.agents/skills/jetpack-compose-best-practices/`, zero `Paint`, `PathEffect`, or `Path` objects are created inside the `DrawScope` onDraw pass; they are memoized using Compose `remember`.

---

## 4. Failure and Edge Cases

| Scenario | System Behavior | Safeguard |
| :--- | :--- | :--- |
| **User enters invalid BBT string** | NumberFormatException caught; error state displayed | Input validation blocks submission |
| **Temperature entered out of human range** | Values `< 35.0` or `> 39.0` flagged | Range check warns user |
| **No temperature entries exist** | Chart replaced with guidance card | Empty-state check avoids Canvas division by zero |
| **Database write fails** | Coroutine catches exception; logs error safely | App prevents crash; notifies user |

---

## 5. Privacy & Telemetry Audit

- **100% Local Persistence**: All biomarker logs remain strictly within the private app SQLite database.
- **No Logcat Exposure**: BBT readings, mood states, and pain levels are strictly excluded from Logcat logs.
