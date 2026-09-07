# Feature: Local Biometric Security Gate

## Human Orientation

### Overview

Because menstrual records, biomarker levels, and fertility intentions constitute highly personal and sensitive health information, Wellness Tracker incorporates a hardware-backed local security barrier. In many environments, devices are shared among family members or left unlocked; this feature guarantees that personal health history cannot be accessed without explicit biometric verification (fingerprint or facial unlock) or device credential verification (PIN/pattern/password).

The security barrier is managed locally on-device using Android's modern `androidx.biometric.BiometricPrompt` framework. When enabled, the gate engages upon application cold start and when returning from background states. If a user's device lacks biometric hardware, the system seamlessly falls back to device lock credentials, ensuring security without excluding users.

Importantly, biometric authentication is optional and user-controlled. Users can toggle the biometric lock on or off at any time directly from the top application bar, with the security preference persisted locally and privately via `SecurityPreferences`.

### Feature Snapshot

| Field | Summary |
| --- | --- |
| **Business / Security capability** | Hardware-backed biometric authentication gate with device credential fallback and local toggle |
| **Primary actors** | Privacy-conscious users, individuals sharing devices |
| **User value** | Total physical and biometric privacy protection for sensitive health records |
| **Current state** | `COMPLETED` — `BiometricAuthHelper`, `SecurityPreferences`, and `MainActivity` gating are fully operational |
| **Main entry points** | `MainActivity.kt` (launch interceptor), Top App Bar Lock Action |
| **Primary components** | `BiometricAuthHelper`, `SecurityPreferences`, `BiometricPrompt`, `MainActivity` |
| **Key boundaries** | AndroidX `BiometricPrompt` API; hardware sensor boundary; local `SharedPreferences` |

### Actors and User Value

| Actor | Need or Goal | Value Provided |
| --- | --- | --- |
| **User Sharing Device** | Keep cycle logs and symptoms confidential from family or friends | App cannot be opened without fingerprint, face unlock, or device PIN |
| **User on Device without Biometrics** | Secure health records using standard phone lock | System falls back to system PIN, password, or pattern seamlessly |
| **User Preferring Quick Access** | Disable biometric verification for faster entry | One-tap toggle in the top app bar enables or disables the gate |

### Current User Experience

- **Application Launch Gate**: When biometric protection is enabled, app launch triggers the standard Android system `BiometricPrompt` overlay ("Unlock Cycle Tracker - Authenticate to view your private health data").
- **Authentication Feedback**: Instant hardware verification; on success, the prompt dismisses and the dashboard immediately appears. On cancellation or failure, entry is denied.
- **Top App Bar Lock Toggle**: Displays a lock icon indicating current protection status. Tapping toggles biometric protection on or off with an instant visual indicator.

### Scope and Boundaries

#### In Scope
- Hardware-backed biometric authentication (`BIOMETRIC_STRONG`).
- Device credential fallback (`DEVICE_CREDENTIAL`).
- Local preference persistence via `SecurityPreferences`.
- Dynamic toggle directly from top application bar.

#### Out of Scope
- Custom proprietary PIN/password storage (using the OS-level credential store avoids insecure local password storage).
- Remote or cloud authentication.

### End-to-End User Journey

1. **User launches app**: User taps the Wellness Tracker app icon.
2. **Preference check**: `MainActivity` reads `SecurityPreferences.isBiometricEnabled()`.
3. **Biometric prompt**: If enabled, `BiometricAuthHelper.showBiometricPrompt()` prompts for fingerprint or face unlock.
4. **Sensor verification**: User touches fingerprint sensor or looks at camera; Android OS verifies credentials.
5. **UI Unlock**: `onAuthenticationSucceeded` callback triggers; dashboard UI renders.

---

## Engineering Reference

### Clean Architecture Components Involved

| Component Layer | Technology | Current Role |
| :--- | :--- | :--- |
| **Host Activity** | `MainActivity.kt` | Enforces authentication check prior to rendering Compose navigation host |
| **Platform Helper** | `BiometricAuthHelper.kt` | Wraps `androidx.biometric.BiometricPrompt` and `BiometricManager` |
| **Local Storage** | `SecurityPreferences.kt` | Wraps `SharedPreferences` for `is_biometric_enabled` boolean state |
| **UI Control** | Jetpack Compose TopAppBar | Renders lock icon and handles user toggle events |

### Shared Business Rules

1. **Graceful Degradation**: If biometrics are not configured or available on the device, the app falls back to system PIN/pattern/password, or allows entry if no device lock is configured.
2. **No Biometric Data Stored**: The app never receives or stores raw biometric data (fingerprint or face images); it only receives binary success/failure signals from the Android OS.

### Failure, Edge-Case & Offline Behavior

| Scenario | Expected Behavior | Owning Component |
| :--- | :--- | :--- |
| **User cancels biometric prompt** | App remains gated; prompt offers retry | `BiometricAuthHelper.kt` |
| **Multiple failed attempts** | Android OS enforces temporary biometric lockout; falls back to PIN | Android OS Biometric Subsystem |
| **Running on emulator without fingerprint** | `isBiometricAvailable()` returns false; falls back cleanly | `BiometricAuthHelper.kt` |

### Android Platform Invariants

- **Hardware Security**: Relies on Android's Trusted Execution Environment (TEE) / Secure Element via `BiometricPrompt`.
- **Zero-Telemetry**: No authentication timestamps or status signals are logged remotely or stored insecurely.

### Related Files

| Component | Path | Purpose |
| :--- | :--- | :--- |
| Security Helper | `app/src/main/java/com/thewalkersoft/tracker/ui/security/BiometricAuthHelper.kt` | AndroidX BiometricPrompt wrapper |
| Preferences | `app/src/main/java/com/thewalkersoft/tracker/ui/security/SecurityPreferences.kt` | SharedPreferences wrapper |
| Host | `app/src/main/java/com/thewalkersoft/tracker/MainActivity.kt` | Authentication gating in `onCreate()` |

### Tests and Acceptance Evidence

| Scenario or Acceptance Signal | Expected Result | Evidence / Test Command |
| :--- | :--- | :--- |
| Hardware availability check | Accurately queries `BiometricManager.canAuthenticate()` | Unit test / Emulator verification |
| ADB Biometric simulation | `adb emu finger touch 1` successfully unlocks app | ADB Device Testing (`.agents/skills/adb-device-debugging`) |
| Toggle state persistence | Preferences correctly survive app restart | Instrumentation test |

### Related Docs

- [Code Touch Map](code-touch-map.md)
- [Data Flow](data-flow.md)
- [Parent Feature Catalog](../README.md)
- [Developer Operating Manual](../../../AGENTS.md)
