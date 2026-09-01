---
name: gradle-build-optimization
description: Best practices and runbooks for optimizing Gradle build speed, managing Version Catalogs (libs.versions.toml), configuring Gradle & Kotlin compiler caching, KSP performance tuning, and troubleshooting build issues.
---

# Gradle Build Optimization & Maintenance Skill

This skill provides configuration guidelines, diagnostic recipes, and performance optimization techniques for modern Android Gradle builds (Kotlin DSL, KSP, Compose).

---

## 1. Core Performance Settings (`gradle.properties`)

Ensure the following flags are enabled in `gradle.properties` (or `~/.gradle/gradle.properties` for machine-wide configuration):

```properties
# Enable parallel project execution
org.gradle.parallel=true

# Enable build cache (reuses outputs across builds)
org.gradle.caching=true

# Enable Configuration Cache (skips configuration phase when inputs haven't changed)
org.gradle.configuration-cache=true

# Tune Gradle Daemon memory allocation based on system RAM
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC -XX:MaxMetaspaceSize=1024m -XX:+HeapDumpOnOutOfMemoryError

# Tune Kotlin Compiler Daemon memory
kotlin.daemon.jvmargs=-Xmx2048m

# Enable non-transitive R classes for faster incremental builds in multi-module apps
android.nonTransitiveRClass=true

# Enable KSP incremental processing
ksp.incremental=true
```

---

## 2. Profiling & Measuring Build Speed

### Running a Build Scan
```bash
# Generate a comprehensive cloud build scan from Gradle
./gradlew assembleDebug --scan
```

### Profiling Locally
```bash
# Generate a local HTML report analyzing task durations
./gradlew assembleDebug --profile

# View report generated at:
# <project-root>/build/reports/profile/profile-<timestamp>.html
```

### Dry Run (Validating Task Graph without Execution)
```bash
# Inspect which tasks would execute
./gradlew assembleDebug --dry-run
```

---

## 3. Dependency & Version Catalog Management (`libs.versions.toml`)

Always manage plugins, libraries, and compiler versions through the Version Catalog (`gradle/libs.versions.toml`).

### Structure Standards
```toml
[versions]
kotlin = "2.2.10"
agp = "8.9.0"
composeBom = "2025.02.00"
room = "2.6.1"
ksp = "2.2.10-2.0.2"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### Checking for Dependency Updates
```bash
# Check dependency insight for a specific library
./gradlew dependencyInsight --dependency androidx.room:room-runtime --configuration debugCompileClasspath
```

---

## 4. Kotlin Symbol Processing (KSP) Optimization

### KSP Best Practices with Room
In `app/build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}
```

### Triggering Fast KSP Re-generation
```bash
# Generate Room DAOs and type converters without a full APK build
./gradlew kspDebugKotlin
```

---

## 5. Clean vs. Incremental Builds

> [!TIP]
> Avoid running `./gradlew clean` unnecessarily. Frequent clean builds destroy the Gradle build cache and Kotlin incremental compilation state, drastically increasing build times.

### When to Clean:
- After switching branches with major dependency version jumps.
- When Room schema changes cause stale DAO generation conflicts.
- After modifying `gradle.properties` JVM args or upgrading Android Studio / AGP.

### Clean Command:
```bash
./gradlew clean --no-build-cache
```

---

## 6. Common Build Errors & Troubleshooting

| Error | Root Cause | Solution |
| :--- | :--- | :--- |
| `OutOfMemoryError: Metaspace` or `Java heap space` | JVM memory insufficient for Gradle/Kotlin daemon | Increase `org.gradle.jvmargs` in `gradle.properties` to `-Xmx4096m -XX:MaxMetaspaceSize=1024m`. |
| `Configuration cache state could not be cached` | Incompatible third-party Gradle plugin accessing project at execution time | Temporarily disable config cache with `--no-configuration-cache` or update plugin to latest version. |
| `Duplicate class found` | Multiple dependencies pulling incompatible transitive versions of the same library | Run `./gradlew app:dependencies` and exclude the duplicate module or align versions via BOM. |
| `KSP daemon died unexpectedly` | Memory ceiling exceeded during annotation processing | Add `ksp.jvmargs=-Xmx2048m` in `gradle.properties`. |
