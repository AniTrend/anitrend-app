# Dependency Upgrades & Build System Cleanup — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bump 3 dependencies, remove 1 unused + 1 deprecated library, enable configuration cache, and fix Gradle deprecation warnings — one change at a time with build verification after each.

**Architecture:** Pure build system changes (version catalog, gradle.properties, buildSrc plugin). No source code touched. Each step produces a passing build + green tests before moving on.

**Tech Stack:** Gradle 9.3.1, AGP 8.13.2, Kotlin 2.3.x, libs.versions.toml, buildSrc convention plugins.

---

### Task 1: Remove `constraintlayout-solver` from version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Delete the solver entry from the TOML**

Remove this line from `gradle/libs.versions.toml`:
```
androidx-constraintLayout-solver = { module = "androidx.constraintlayout:constraintlayout-solver", version.ref = "androidx.constraintlayout" }
```

- [ ] **Step 2: Verify it builds and tests pass**

```bash
./gradlew :app:assembleAppDebug
./gradlew :app:testAppDebugUnitTest
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): remove unused constraintlayout-solver from version catalog"
```

---

### Task 2: Bump Kotlin `2.3.10` → `2.3.21`

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Update the Kotlin version ref**

In `gradle/libs.versions.toml`, change:
```
jetbrains-kotlin = "2.3.10"
```
to:
```
jetbrains-kotlin = "2.3.21"
```

- [ ] **Step 2: Build and run tests**

```bash
./gradlew :app:assembleAppDebug
./gradlew :app:testAppDebugUnitTest
```
Expected: BUILD SUCCESSFUL, 57/57 tests passing

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): bump kotlin from 2.3.10 to 2.3.21"
```

---

### Task 3: Bump Koin `4.1.1` → `4.2.1`

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Update the Koin version ref**

In `gradle/libs.versions.toml`, change:
```
io-koin = "4.1.1"
```
to:
```
io-koin = "4.2.1"
```

- [ ] **Step 2: Build and run tests**

```bash
./gradlew :app:assembleAppDebug
./gradlew :app:testAppDebugUnitTest
```
Expected: BUILD SUCCESSFUL, 57/57 tests passing

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): bump koin from 4.1.1 to 4.2.1"
```

---

### Task 4: Bump Chucker `4.2.0` → `4.3.1`

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Update the Chucker version ref**

In `gradle/libs.versions.toml`, change:
```
chuncker = "4.2.0"
```
to:
```
chuncker = "4.3.1"
```

- [ ] **Step 2: Build and run tests**

```bash
./gradlew :app:assembleAppDebug
./gradlew :app:testAppDebugUnitTest
```
Expected: BUILD SUCCESSFUL, 57/57 tests passing

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): bump chucker from 4.2.0 to 4.3.1"
```

---

### Task 5: Evaluate & remove deprecated `lifecycle-extensions`

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt`

- [ ] **Step 1: Check if `lifecycle-extensions` is still needed**

The project already uses individual `-ktx` artifacts:
- `lifecycle-runtime-ktx`
- `lifecycle-livedata-ktx`
- `lifecycle-viewmodel-ktx`
- `lifecycle-livedata-core-ktx`

`lifecycle-extensions:2.2.0` was the old umbrella that bundled all of these. Since the individual artifacts are already declared, removing the umbrella should be safe. Build to verify.

- [ ] **Step 2: Remove from version catalog**

In `gradle/libs.versions.toml`, remove:
```
androidx-lifecycle-extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
```

- [ ] **Step 3: Remove from buildSrc dependencies**

In `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt`, remove the line:
```kotlin
dependencies.implementation(libs.androidx.lifecycle.extensions)
```

- [ ] **Step 4: Build and run tests**

```bash
./gradlew :app:assembleAppDebug
./gradlew :app:testAppDebugUnitTest
```
Expected: BUILD SUCCESSFUL, 57/57 tests passing

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt
git commit -m "chore(deps): remove deprecated lifecycle-extensions umbrella library"
```

---

### Task 6: Enable configuration cache

**Files:**
- Modify: `gradle.properties`

- [ ] **Step 1: Add configuration cache property**

In `gradle.properties`, add a new line:
```
org.gradle.configuration-cache=true
```

- [ ] **Step 2: First run (cold cache)**

```bash
./gradlew :app:assembleAppDebug --configuration-cache
```
Expected: BUILD SUCCESSFUL. Configuration cache stores on first run.

- [ ] **Step 3: Second run (reuse cache)**

```bash
./gradlew :app:assembleAppDebug
```
Expected: BUILD SUCCESSFUL, noticeably faster. Configuration cache reused.

- [ ] **Step 4: Commit**

```bash
git add gradle.properties
git commit -m "chore(build): enable Gradle configuration cache"
```

---

### Task 7: Fix Gradle deprecation warnings

**Files:**
- Various (determined by `--warning-mode all` output)

- [ ] **Step 1: Run with full warnings**

```bash
./gradlew build --warning-mode all 2>&1 | tee /tmp/gradle-warnings.txt
```

- [ ] **Step 2: Inspect warnings and fix them**

Read `/tmp/gradle-warnings.txt` and address each warning. Common categories:
- Property accessor usage (getX() vs `X` in Kotlin DSL)
- Deprecated configuration avoidance APIs
- `compile` / `runtime` configurations (should be `implementation` / `runtimeOnly`)

Fix each in the appropriate file, build again, iterate until warnings are clear.

- [ ] **Step 3: Final clean build + tests**

```bash
./gradlew clean :app:assembleAppDebug :app:testAppDebugUnitTest --warning-mode all
```
Expected: BUILD SUCCESSFUL, zero warnings, 57/57 tests passing.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore(build): fix Gradle deprecation warnings"
```

---

## Rollback note

Each step is separately committed. If any step causes issues, `git revert <commit-hash>` cleanly undoes exactly that change without affecting others.
