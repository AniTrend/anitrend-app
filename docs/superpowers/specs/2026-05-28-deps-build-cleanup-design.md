# Dependency Upgrades & Build System Cleanup

## Context

Java→Kotlin migration is complete. The build is green (57/57 tests). This spec covers mechanical cleanup and dependency maintenance before moving on to higher-risk work (UI testing, release prep).

## Scope

Strictly **build system and dependency housekeeping**. No source code behavioral changes, no UI modifications.

## Steps (in order)

### 1. Remove `constraintlayout-solver` from `libs.versions.toml`
- The library entry `androidx-constraintLayout-solver` is declared in the version catalog but never referenced in any `build.gradle.kts` or `ProjectDependencies.kt`.
- **Action:** Delete the TOML line. No other file changes needed.
- **Risk:** None (zero references).

### 2. Kotlin `2.3.10` → `2.3.21`
- Patch bump within the same minor line (`2.3.x`).
- **Action:** Update `jetbrains-kotlin` version ref in `libs.versions.toml`.
- **Validation:** `./gradlew :app:assembleAppDebug` + `./gradlew :app:testAppDebugUnitTest`.

### 3. Koin `4.1.1` → `4.2.1`
- Minor bump. Koin has maintained compatibility for the Android APIs used here.
- **Action:** Update `io-koin` version ref in `libs.versions.toml`.
- **Validation:** Full build + test.

### 4. Chucker `4.2.0` → `4.3.1`
- Chucker is a debug-only HTTP inspector. Minimal risk of runtime impact.
- **Action:** Update `chuncker` version ref in `libs.versions.toml`.
- **Validation:** Full build + test.

### 5. Evaluate `lifecycle-extensions`
- Currently declared in `libs.versions.toml` AND consumed in `ProjectDependencies.kt`.
- This library is deprecated since `androidx.lifecycle:lifecycle-extensions:2.2.0`.
- The project already uses the individual `-ktx` artifacts (`lifecycle-runtime-ktx`, `lifecycle-livedata-ktx`, `lifecycle-viewmodel-ktx`, `lifecycle-livedata-core-ktx`).
- **Action:** Check whether removing `lifecycle-extensions` breaks the build. If not, remove it from both TOML and `ProjectDependencies.kt`.
- **Validation:** Full build + test.

### 6. Enable configuration cache
- Add `org.gradle.configuration-cache=true` to `gradle.properties`.
- Configuration cache is stable in Gradle 9.x and significantly improves incremental build speed.
- **Action:** Single line addition.
- **Validation:** `./gradlew :app:assembleAppDebug --configuration-cache` (first run caches, second run should reuse).

### 7. Fix Gradle deprecation warnings
- Run `./gradlew build --warning-mode all`.
- Address any warnings flagged, focusing on those incompatible with Gradle 10.
- **Common areas:** property accessors, implicit `taskConfiguration` avoidance, deprecated API usage.
- **Validation:** Clean `--warning-mode all` output after fixes.

## Non-goals

- AGP upgrade (staying on 8.13.2)
- Gradle wrapper update (staying on 9.3.1)
- Dependency tree audit beyond the listed items
- Any source code changes
- Removing `multidex` (it's actively used)

## Success criteria

- All 7 steps complete and verified.
- `./gradlew :app:assembleAppDebug` passes.
- `./gradlew :app:testAppDebugUnitTest` passes (57/57).
- No new deprecation warnings introduced.
