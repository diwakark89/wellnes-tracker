---
name: adb-device-debugging
description: Comprehensive guide and runbooks for Android Debug Bridge (adb) operations, logcat filtering, crash analysis, database inspection, process lifecycle simulation, and device testing workflows.
---

# Android Debug Bridge (adb) Device Debugging Skill

This skill provides operational workflows, diagnostic recipes, and CLI commands for debugging Android applications on connected physical devices and emulators using `adb`.

---

## 1. Device Discovery & Connection

### Listing Devices
```bash
# List all connected devices and emulators with status
adb devices -l
```

### Targeting Specific Devices
When multiple devices/emulators are connected:
- **Target only emulator**: `adb -e <command>`
- **Target only USB physical device**: `adb -d <command>`
- **Target specific serial**: `adb -s <device_serial> <command>`

### Wireless Debugging (Android 11+)
```bash
# Pair device with pairing code
adb pair <ip_address>:<pairing_port> <pairing_code>

# Connect to paired device
adb connect <ip_address>:<port>
```

---

## 2. Advanced Logcat Diagnostics

### Filtering by Application Package (Auto-resolve PID)
```bash
# Filter logs exclusively for the target application (PowerShell)
$pkg = "com.thewalkersoft.tracker"
$pid = (adb shell pidof -s $pkg)
adb logcat --pid=$pid

# Alternative single-line filter (Bash/Linux/Mac)
adb logcat --pid=$(adb shell pidof -s com.thewalkersoft.tracker)
```

### Isolating Crashes and ANRs
```bash
# Display only fatal crash logs and unhandled exceptions
adb logcat -b crash -v time

# View recent system ANR traces
adb shell "cat /data/anr/traces.txt" 2>$null | tail -n 100
```

### Filtering by Tag & Log Level
Priority levels: `V` (Verbose), `D` (Debug), `I` (Info), `W` (Warning), `E` (Error), `F` (Fatal), `S` (Silent)
```bash
# Filter by specific tag with level (e.g., Room SQLite queries, AndroidRuntime)
adb logcat AndroidRuntime:E Room:D *:S

# Clear logcat buffer before reproducing an issue
adb logcat -c
```

---

## 3. App Lifecycle & Process Death Simulation

Testing state preservation (e.g., `rememberSaveable`, `SavedStateHandle`, ViewModel recreation) is critical for Android apps.

### Simulating Process Death (Background Kill)
1. Send app to background (press Home button on device/emulator):
   ```bash
   adb shell input keyevent KEYCODE_HOME
   ```
2. Kill the app process while preserving task state:
   ```bash
   adb shell am kill com.thewalkersoft.tracker
   ```
3. Reopen the app from the recent apps switcher to verify saved state restoration.

### Simulating Low Memory Trim
```bash
# Trigger memory trim levels: RUNNING_MODERATE, RUNNING_LOW, RUNNING_CRITICAL, COMPLETE
adb shell am send-trim-memory com.thewalkersoft.tracker RUNNING_CRITICAL
```

### Force Stopping & Starting Activities
```bash
# Force kill completely
adb shell am force-stop com.thewalkersoft.tracker

# Start the launcher activity
adb shell am start -n com.thewalkersoft.tracker/.MainActivity
```

---

## 4. Biometric & Hardware State Simulation

For apps utilizing AndroidX `BiometricPrompt`:

### Simulating Biometric Sensor (Emulator)
```bash
# Enroll fingerprint (touch sensor with finger ID 1)
adb -e emu finger touch 1

# Remove all enrolled fingerprints
adb -e emu finger remove 1
```

### Simulating Biometrics via Command Line (API 30+)
```bash
# Simulate successful biometric authentication
adb shell cmd biometric authenticate 1

# Check biometric sensor status
adb shell cmd biometric get-biometric-prompt-status
```

### Simulating Battery & Power States
```bash
# Set battery level percentage
adb shell dumpsys battery set level 15

# Simulate device unplugged from power
adb shell dumpsys battery unplug

# Reset battery state to real hardware
adb shell dumpsys battery reset
```

---

## 5. Local Database & Storage Inspection

### Inspecting Room / SQLite Database (Debug Builds)
For debuggable APKs, use `run-as` to access private app storage without rooting:

```bash
# Open interactive shell within app context
adb shell run-as com.thewalkersoft.tracker

# List local databases
adb shell "run-as com.thewalkersoft.tracker ls -la databases/"

# Copy database to local machine for inspection (e.g. SQLite Browser)
adb exec-out run-as com.thewalkersoft.tracker cat databases/wellness_database > local_wellness.db
adb exec-out run-as com.thewalkersoft.tracker cat databases/wellness_database-wal > local_wellness.db-wal
```

### Clearing App Data & Cache
```bash
# Clear all app data (preferences, Room DB, cache)
adb shell pm clear com.thewalkersoft.tracker
```

---

## 6. UI Inspection, Screenshots & Screen Recording

### Capturing Screenshots
```bash
# Capture screenshot directly to local machine
adb exec-out screencap -p > screenshot.png
```

### Recording Screen
```bash
# Start recording (max 3 minutes, Ctrl+C to stop)
adb shell screenrecord /sdcard/demo.mp4

# Pull recording to PC
adb pull /sdcard/demo.mp4 ./demo.mp4
adb shell rm /sdcard/demo.mp4
```

### Simulating Text Input & Taps
```bash
# Tap at specific (X, Y) coordinates
adb shell input tap 300 1200

# Input text into focused field
adb shell input text "SampleHealthNote"

# Key events
adb shell input keyevent KEYCODE_BACK
adb shell input keyevent KEYCODE_APP_SWITCH
```

---

## 7. Package & Permissions Verification

### Managing App Permissions
```bash
# Grant a runtime permission
adb shell pm grant com.thewalkersoft.tracker android.permission.POST_NOTIFICATIONS

# Revoke a runtime permission
adb shell pm revoke com.thewalkersoft.tracker android.permission.POST_NOTIFICATIONS

# Dump full package info and granted permissions
adb shell dumpsys package com.thewalkersoft.tracker
```

---

## 8. ADB Troubleshooting Runbook

| Issue | Cause | Fix |
| :--- | :--- | :--- |
| `device unauthorized` | Device USB debugging prompt not accepted | Check phone screen, allow USB debugging RSA key, or run `adb kill-server && adb start-server`. |
| `device offline` | Stale ADB connection or cable issue | Reconnect USB cable or run `adb reconnect`. |
| `run-as: Package is not debuggable` | Production/Release APK installed | Install debug build (`./gradlew assembleDebug` or `installDebug`). |
| `daemon not running / connection refused` | Port conflict with 5037 | Check for conflicting processes: `netstat -ano \| findstr 5037` and kill stuck process. |
