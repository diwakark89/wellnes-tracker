# Wellness Tracker 🌸

A privacy-first, 100% on-device Android application designed for menstrual cycle tracking, symptom and biomarker logging, fertility window forecasting, and clinical PDF report generation.

---

## 🌟 Key Features

- **🛡️ 100% On-Device & Zero-Telemetry**: Complete privacy. No network permissions (`android.permission.INTERNET` is intentionally omitted), no cloud analytics, and no third-party trackers.
- **📊 Adaptive Prediction Engine**: Predicts cycle arrivals using dynamic rolling percentiles and Mean Absolute Error (MAE) expansion rather than rigid 28-day assumptions. Ideal for irregular cycles and postpartum recovery.
- **👶 Postpartum Baseline Reset**: Isolates long postpartum amenorrhea periods from historical calculations to restore accurate predictions upon cycle resumption.
- **🌡️ Biomarker & Symptom Logging**: Track Basal Body Temperature (BBT), LH ovulation tests, cramps/pain severity with an interactive slider, mood, and personal observations.
- **📄 Clinical Doctor PDF Export**: Generate clean, doctor-ready vector PDF health reports with cycle statistics and symptom history directly on-device.
- **🔒 Biometric Security**: Protect sensitive health records locally with fingerprint and face unlock via AndroidX `BiometricPrompt`.

---

## 📚 Technical & Algorithmic Documentation

For detailed mathematical formulations, clinical justifications, state diagrams, and algorithm specifications:
👉 **[Cycle Prediction Engine Specification (`docs/CYCLE_PREDICTION_ENGINE.md`)](docs/CYCLE_PREDICTION_ENGINE.md)**

---

## 🏗️ Architecture & Tech Stack

The application adheres to Android Clean Architecture with Unidirectional Data Flow (UDF):

```
app/src/main/java/com/thewalkersoft/tracker/
├── data/           # Room Database, DAOs, Entities, Converters, Repository Impl
├── di/             # Lightweight Dependency Injection (AppContainer)
├── domain/         # Pure Business Logic, Models, CyclePredictorEngine
└── ui/             # Jetpack Compose Screens, ViewModels, Material 3 Theme, PDF Generator
```

| Component | Technology | Version / Details |
| :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` (Target JVM 11) |
| **Android Baseline** | Min SDK `34`, Target SDK `36`, Compile SDK `36` | Android 14+ |
| **UI Framework** | Jetpack Compose | BOM `2025.02.00` (Material 3) |
| **Persistence** | AndroidX Room | `2.6.1` with KSP (`2.2.10-2.0.2`) |
| **Asynchronous** | Kotlin Coroutines & Flow | `1.10.1` (`StateFlow`, `WhileSubscribed`) |
| **Security** | AndroidX Biometric | `1.2.0-alpha05` (`BiometricPrompt`) |
| **PDF Rendering** | Android Native Graphics | `android.graphics.pdf.PdfDocument` |

---

## 🚀 Build & Development

Run all Gradle tasks from the project root:

```bash
# Run unit tests
./gradlew test

# Trigger KSP code generation
./gradlew kspDebugKotlin

# Build Debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

---

## 🤝 Developer & Agent Guide

Refer to [`AGENTS.md`](AGENTS.md) for architectural conventions, verification checklists, and operational playbooks.