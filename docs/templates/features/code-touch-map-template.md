# Android Code Touch Map Template

Use this document to establish an authoritative code routing map across Android Clean Architecture layers for any feature or sub-feature.

---

## Authoring Contract

- Record concrete, implementation-backed file paths, symbols, functions, and composables that a human or AI agent must inspect first.
- For each entry, document caller/callee dependencies, state access patterns (Read, Write, Orchestrate), and invariants that must be preserved.
- Keep the map focused on change routing and architectural boundaries rather than exhaustive line-by-line inventories.
- Every section below must be addressed. If an architectural layer or component is not involved, write `Not applicable — <reason>`.
- Use repository-relative paths only (`app/src/main/java/com/thewalkersoft/tracker/...`).

---

## Code Touch Map

### 1. Presentation Layer — Jetpack Compose UI

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / UI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Screen / Route** | `app/src/main/java/com/thewalkersoft/tracker/ui/<feature>/...` | `<FeatureScreen>` | `NavigationHost.kt` / `Screen.<Feature>` | Reads `UiState` via `collectAsStateWithLifecycle()` | Must handle loading, error, and content states | Screen navigation and top-level layout |
| **Card / Component** | `app/src/main/java/com/thewalkersoft/tracker/ui/<feature>/components/...` | `<FeatureCard>` | `<FeatureScreen>` | Stateless / receives props & callbacks | Material 3 token compliance; stateless hoisting | Reusable widget styling and layout |
| **Custom Canvas Chart** | `app/src/main/java/com/thewalkersoft/tracker/ui/<feature>/components/...` | `<ChartComponent>` | `<FeatureScreen>` | Draws vector paths from domain data | **Zero object allocation in `DrawScope`**; memoize `Paint`/`Path` via `remember` | Canvas rendering, coordinate math, and drawing performance |
| **Modal / BottomSheet** | `app/src/main/java/com/thewalkersoft/tracker/ui/logging/...` | `<LogBottomSheet>` | Floating Action Button / Quick Action | Reads & writes local draft state | Input validation; dismiss animation on confirm | User data capture workflow |

### 2. State & Lifecycle Layer — ViewModels & Coroutines

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Concurrency Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ViewModel** | `app/src/main/java/com/thewalkersoft/tracker/ui/<feature>/...` | `<FeatureViewModel>` | Injected via `AppViewModelProvider.Factory` | Orchestrates repositories | Must expose immutable `StateFlow<UiState>` via `SharingStarted.WhileSubscribed(5000)` | State emission, background flow cancellation |
| **UiState Model** | `app/src/main/java/com/thewalkersoft/tracker/ui/<feature>/...` | `<FeatureUiState>` | `<FeatureViewModel>` & `<FeatureScreen>` | Read-only immutable data class | Must be stable and immutable for Compose optimization | UI state rendering and recomposition triggers |

### 3. Domain Layer — Pure Business Logic & Models

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Math Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Engine / Algorithm** | `app/src/main/java/com/thewalkersoft/tracker/domain/predictor/...` | `<PredictorEngine>` | Repository / ViewModel | Pure function / No DB state | **Pure Kotlin only** (no Android framework SDKs); deterministic math | Statistical forecasting, date calculations, clinical accuracy |
| **Domain Model** | `app/src/main/java/com/thewalkersoft/tracker/domain/model/...` | `<DomainRecord>` | Engine, Repository, ViewModel | Immutable domain representation | Business validation; decoupled from Room entities | Core business contracts |
| **Repository Contract**| `app/src/main/java/com/thewalkersoft/tracker/domain/repository/...` | `<CycleRepository>` | Implemented by Data layer; consumed by ViewModels | Contract interface | Returns observable `Flow<T>` or `suspend` methods | Decoupling boundary between Domain and Data |

### 4. Data Layer — Room SQLite & Local Storage

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Database Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Repository Impl** | `app/src/main/java/com/thewalkersoft/tracker/data/repository/...` | `<RepositoryImpl>` | Implements Domain interface; calls Room DAOs | Reads / writes via Room | Dispatches DB operations to `Dispatchers.IO` | Data transformation and query coordination |
| **Room DAO** | `app/src/main/java/com/thewalkersoft/tracker/data/local/dao/...` | `<EntityDao>` | Repository Impl | Executes SQLite queries | Emits reactive `Flow<List<Entity>>` or `suspend fun` mutations | Database queries, reactivity, and indexing |
| **Room Entity** | `app/src/main/java/com/thewalkersoft/tracker/data/local/entity/...` | `<EntityClass>` | Room DAO, Database | Database table schema | Explicit column types; primary key definitions; schema version bump required on change | SQLite schema, KSP generation, migrations |
| **Type Converters** | `app/src/main/java/com/thewalkersoft/tracker/data/local/converter/...` | `DateConverters.kt` | Room Database | Converts `LocalDate` <-> `Long` | Lossless epoch-day mapping | Date serialization across database |
| **Local Preferences**| `app/src/main/java/com/thewalkersoft/tracker/ui/security/...` | `SecurityPreferences.kt` | Security helpers / ViewModels | Reads / writes `SharedPreferences` | Encapsulated key-value flags (e.g. biometric lock enabled) | App-level local configurations |

### 5. Platform & Hardware Subsystems

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / Security Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Biometrics** | `app/src/main/java/com/thewalkersoft/tracker/ui/security/...` | `BiometricAuthHelper.kt` | `MainActivity.kt` | Interacts with `BiometricPrompt` | Hardware-backed gate; fallback to device credentials; never logs biometric state | App launch security barrier |
| **Vector PDF Export**| `app/src/main/java/com/thewalkersoft/tracker/ui/export/...` | `DoctorPdfGenerator.kt` | `ExportViewModel.kt` | Generates `PdfDocument` | A4 standard page bounds (595x842 pt); offloaded to `Dispatchers.IO`; zero network | Clinical report accuracy and printing |
| **File Provider** | `app/src/main/res/xml/file_paths.xml` | FileProvider config | `ExportViewModel.kt` | Scoped internal cache directory | Content URI only; restricted read permission to target intent | PDF sharing security boundary |

### 6. Dependency Injection

| Area | Path | Symbol / Entry Point | Caller / Dependency | State Access | Invariant / DI Rule | Change Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Container** | `app/src/main/java/com/thewalkersoft/tracker/di/...` | `AppContainer.kt` | `TrackerApplication.kt` | Singleton repository instances | Lazy, thread-safe manual dependency container; zero reflection | App-wide dependency provision |
| **ViewModel Factory**| `app/src/main/java/com/thewalkersoft/tracker/ui/...` | `AppViewModelProvider.kt` | Compose screen `viewModel()` calls | Creates ViewModel instances | Supplies container dependencies to ViewModels | ViewModel instantiation |

### 7. Tests & Acceptance Evidence

| Test Suite | Path | Test Class / Method | Verifies | Execution Command |
| :--- | :--- | :--- | :--- | :--- |
| **Domain Tests** | `app/src/test/java/.../domain/predictor/...` | `<EngineTest>` | Statistical calculations, MAE intervals, spotting suppression | `./gradlew testDebugUnitTest --tests "<EngineTest>"` |
| **ViewModel Tests**| `app/src/test/java/.../ui/...` | `<ViewModelTest>` | StateFlow emissions, coroutine transitions, fake repository updates | `./gradlew testDebugUnitTest --tests "<ViewModelTest>"` |
| **Compose Previews**| `app/src/main/java/.../ui/...` | `@Preview fun <PreviewName>` | UI rendering, dark/light theme, typography | Visual Android Studio Inspection |
