# Data Flow: Local Biometric Security Gate

This document details the reactive, end-to-end **Unidirectional Data Flow (UDF)** for local biometric authentication gating and preference toggling.

---

## 1. Flow Overview

```text
User Launches App (MainActivity.onCreate)
  ↓
MainActivity checks SecurityPreferences.isBiometricEnabled()
  ├─ If FALSE: Renders Compose NavigationHost immediately
  └─ If TRUE: Holds UI in gated state
       ↓
     BiometricAuthHelper.showBiometricPrompt()
       ↓ (Android OS Hardware Subsystem)
     BiometricPrompt displayed to user (Fingerprint / Face / Device PIN)
       ↓
     User authenticates via sensor or PIN
       ├─ On Success: onSuccess() callback triggers
       │    ↳ isAuthenticated state set to true
       │    ↳ Compose NavigationHost renders
       └─ On Failure: onError() callback triggers
            ↳ UI remains locked; user prompted to retry
```

---

## 2. Normal End-to-End Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Host | `MainActivity` | `Dispatchers.Main` | App launched from launcher | Reads `SecurityPreferences` | Decides whether to gate UI |
| **2** | Platform | `BiometricAuthHelper` | `Dispatchers.Main` | Calls `BiometricManager.canAuthenticate()` | Checks hardware availability | Builds `PromptInfo` |
| **3** | OS / Hardware | `BiometricPrompt` | System Service | Displays system biometric authentication dialog | Awaits user biometric input | System prompt overlay shown |
| **4** | OS / Hardware | Sensor Hardware | Hardware Layer | Scans fingerprint or face | Authenticates credential locally | System callback triggered |
| **5** | Platform | `BiometricPrompt.AuthenticationCallback` | Main Executor | `onAuthenticationSucceeded` fired | Invokes `onSuccess` lambda | Callback sent to `MainActivity` |
| **6** | Host / UI | `MainActivity` | `Dispatchers.Main` | Sets `isAuthenticated = true` | Triggers Compose rendering | NavigationHost renders Dashboard |

---

## 3. Preference Toggle Sequence

| Step | Layer | Entry Point | Dispatcher | Action / Handoff | State Mutation / Data Effect | UI Response |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Presentation | TopAppBar Action | `Dispatchers.Main` | User taps lock toggle icon | Reads current state | Toggles boolean value |
| **2** | Platform / Storage | `SecurityPreferences` | `Dispatchers.Main` | Calls `setBiometricEnabled(!current)` | Writes boolean to `SharedPreferences` | Data saved locally |
| **3** | Presentation | `MainActivity` | `Dispatchers.Main` | Recomposes lock icon | Icon updates (`Lock` vs `LockOpen`) | Visual feedback provided |

---

## 4. Failure and Edge Cases

| Scenario | System Behavior | Safeguard |
| :--- | :--- | :--- |
| **User cancels biometric prompt** | Callback receives `ERROR_USER_CANCELED`; app remains gated | Screen displays "Authentication required" with retry button |
| **Sensor failure / dirty sensor** | Callback receives `onAuthenticationFailed` | System prompt shakes; allows repeated attempts |
| **Device lacks biometric hardware** | Falls back to system PIN/Pattern | Supported via `Authenticators.DEVICE_CREDENTIAL` |

---

## 5. Privacy & Telemetry Audit

- **Hardware Isolation**: Raw biometric traits are processed strictly by the hardware secure element (TEE). No biometric data is readable by the app.
- **Zero-Telemetry**: No authentication timestamps or status signals are recorded in external logs or network payloads.
