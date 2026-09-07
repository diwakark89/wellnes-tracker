# Code Touch Map: Local Biometric Security Gate

This document serves as the authoritative architectural routing map for the **Local Biometric Security Gate** feature.

---

## Code Touch Map

### 1. Presentation & Host Layer

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Activity Host** | `app/src/main/java/com/thewalkersoft/tracker/MainActivity.kt` | `MainActivity` | Android OS Launcher Intent | Reads `SecurityPreferences.isBiometricEnabled()` | If enabled, blocks Compose UI rendering until biometric success | App cold start lifecycle |
| **Top App Bar** | `app/src/main/java/com/thewalkersoft/tracker/MainActivity.kt` | `TopAppBar` Actions | User tap | Reads & writes `SecurityPreferences.setBiometricEnabled()` | Updates lock icon (`Icons.Default.Lock` vs `LockOpen`) | Quick security toggle |

### 2. Platform & Hardware Security Layer

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Biometric Helper** | `app/src/main/java/com/thewalkersoft/tracker/ui/security/BiometricAuthHelper.kt` | `BiometricAuthHelper` | `MainActivity.kt` | Interacts with `BiometricPrompt` & `BiometricManager` | Enforces `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`; dispatches callbacks on main executor | Biometric prompt dialog and hardware response |
| **Preferences Helper**| `app/src/main/java/com/thewalkersoft/tracker/ui/security/SecurityPreferences.kt` | `SecurityPreferences` | `MainActivity.kt` | Reads / writes private `SharedPreferences` | Default value is `false` (opt-in); encapsulated key access | Persistent security toggle state |

### 3. Tests & Acceptance Evidence

| Test Suite | Path | Symbol | Verification Target | Command |
| :--- | :--- | :--- | :--- | :--- |
| **Build Check** | `app/build.gradle.kts` | `:app:assembleDebug` | Compiles `androidx.biometric:biometric` dependencies | `./gradlew assembleDebug` |
| **ADB Hardware Test** | Emulator / Device | Fingerprint sensor simulation | Enrolls fingerprint and triggers authentication | `adb emu finger touch 1` |
