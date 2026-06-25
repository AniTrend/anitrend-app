# Java/Kotlin Migration Closure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete migration closure by removing stale ButterKnife/Data Binding usage, standardizing shared view wiring on View Binding, and adding regression verification guards.

**Architecture:** Keep behavior stable while refactoring high-fanout base surfaces (`ActivityBase`, `BottomSheetBase`) and then updating consumers in small batches. Use View Binding typed references for shared toolbar/search surfaces and remove stale migration residue in docs/comments. Add static migration guard checks so regressions are caught in CI/local verification.

**Tech Stack:** Kotlin, Android View Binding, Gradle (AGP), JUnit4, shell verification scripts.

---

### Task 1: Add Migration Guard Verification Script

**Files:**
- Create: `.github/scripts/verify-viewbinding-migration.sh`

- [ ] **Step 1: Create the failing verification script (TDD red)**

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo "Checking for Java sources and deprecated binding usage..."

JAVA_MATCHES=$(rg --files "$ROOT_DIR" -g "**/*.java" || true)
BUTTERKNIFE_MATCHES=$(rg -n "ButterKnife|@BindView|@OnClick|\bUnbinder\b" "$ROOT_DIR/app" "$ROOT_DIR/.github" || true)
DATABINDING_MATCHES=$(rg -n "DataBindingUtil|ViewDataBinding|androidx\.databinding|\bBR\." "$ROOT_DIR/app" "$ROOT_DIR/buildSrc" || true)

if [[ -n "$JAVA_MATCHES" || -n "$BUTTERKNIFE_MATCHES" || -n "$DATABINDING_MATCHES" ]]; then
  echo "Verification failed"
  [[ -n "$JAVA_MATCHES" ]] && echo "$JAVA_MATCHES"
  [[ -n "$BUTTERKNIFE_MATCHES" ]] && echo "$BUTTERKNIFE_MATCHES"
  [[ -n "$DATABINDING_MATCHES" ]] && echo "$DATABINDING_MATCHES"
  exit 1
fi

echo "Verification passed"
```

- [ ] **Step 2: Run script to verify it fails on known stale references**

Run: `bash .github/scripts/verify-viewbinding-migration.sh`
Expected: `Verification failed` with stale `ButterKnife` doc/comment hits.

- [ ] **Step 3: Make script executable**

Run: `chmod +x .github/scripts/verify-viewbinding-migration.sh`
Expected: no output.

- [ ] **Step 4: Re-run after cleanup tasks (later) to verify pass**

Run: `bash .github/scripts/verify-viewbinding-migration.sh`
Expected: `Verification passed`.

- [ ] **Step 5: Commit**

```bash
git add .github/scripts/verify-viewbinding-migration.sh
git commit -m "chore(migration): add view binding closure verification guard"
```

### Task 2: Refactor BottomSheetBase Shared Toolbar Wiring to View Binding

**Files:**
- Modify: `app/src/main/java/com/mxt/anitrend/base/custom/sheet/BottomSheetBase.kt`
- Modify: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetMessage.kt`
- Modify: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetSpoiler.kt`
- Modify: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomReviewReader.kt`

- [ ] **Step 1: Write failing compile-level expectation (red)**

Introduce new API in `BottomSheetBase.kt` first and immediately update one consumer callsite to the new signature before implementing all callsites so the module does not compile.

```kotlin
// In BottomSheetMessage.kt (temporary red step)
bindToolbarViews(requireNotNull(binding).customSheetToolbar)
```

Run: `./gradlew :app:compileAppDebugKotlin`
Expected: FAIL because `bindToolbarViews(CustomSheetToolbarBinding)` does not yet exist.

- [ ] **Step 2: Implement minimal shared toolbar binding API (green)**

```kotlin
// BottomSheetBase.kt
import com.mxt.anitrend.databinding.CustomSheetToolbarBinding

protected fun bindToolbarViews(toolbarBinding: CustomSheetToolbarBinding) {
    toolbarTitle = toolbarBinding.toolbarTitle
    toolbarState = toolbarBinding.toolbarState
    toolbarSearch = toolbarBinding.toolbarSearch
    searchView = toolbarBinding.searchView
}
```

- [ ] **Step 3: Migrate all bottom sheet callsites to typed binding**

```kotlin
// BottomSheetMessage.kt
bindToolbarViews(requireNotNull(binding).customSheetToolbar)

// BottomSheetSpoiler.kt
bindToolbarViews(requireNotNull(binding).customSheetToolbar)

// BottomReviewReader.kt
bindToolbarViews(requireNotNull(binding).customSheetToolbar)
```

- [ ] **Step 4: Run compile check for shared bottom sheets**

Run: `./gradlew :app:compileAppDebugKotlin`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mxt/anitrend/base/custom/sheet/BottomSheetBase.kt app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetMessage.kt app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetSpoiler.kt app/src/main/java/com/mxt/anitrend/view/sheet/BottomReviewReader.kt
git commit -m "refactor(ui): use view binding for shared bottom sheet toolbar wiring"
```

### Task 3: Refactor Shared Activity View Wiring for View Binding Consumer Consistency

**Files:**
- Modify: `app/src/main/java/com/mxt/anitrend/view/activity/base/AboutActivity.kt`
- Modify: `app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBase.kt`

- [ ] **Step 1: Write failing unit tests for ActivityBase search query normalization (red)**

**Test file:**
- Create: `app/src/test/java/com/mxt/anitrend/base/custom/activity/ActivityBaseSearchUtilTests.kt`

```kotlin
package com.mxt.anitrend.base.custom.activity

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.Locale

class ActivityBaseSearchUtilTests {
    @Test
    fun `normalizes null to empty query`() {
        assertThat(ActivityBaseSearchUtil.normalize(null, Locale.US), equalTo(""))
    }

    @Test
    fun `normalizes query to lowercase locale aware value`() {
        assertThat(ActivityBaseSearchUtil.normalize("AniTrend", Locale.US), equalTo("anitrend"))
    }
}
```

Run: `./gradlew :app:testAppDebugUnitTest --tests "*ActivityBaseSearchUtilTests"`
Expected: FAIL (type not found).

- [ ] **Step 2: Implement minimal utility to satisfy tests (green)**

**Create file:**
- `app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBaseSearchUtil.kt`

```kotlin
package com.mxt.anitrend.base.custom.activity

import java.util.Locale

internal object ActivityBaseSearchUtil {
    fun normalize(value: String?, locale: Locale): String = value?.lowercase(locale).orEmpty()
}
```

- [ ] **Step 3: Update ActivityBase to use shared normalization utility**

```kotlin
override fun onQueryTextChange(newText: String?): Boolean {
    presenterRef?.notifyAllListeners(
        ActivityBaseSearchUtil.normalize(newText, Locale.getDefault()),
        false
    )
    return false
}
```

- [ ] **Step 4: Convert AboutActivity from manual `findViewById` to View Binding**

```kotlin
private lateinit var binding: ActivityFrameGenericBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFrameGenericBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.customToolbar.toolbar)
}
```

- [ ] **Step 5: Run tests and compile checks**

Run:
- `./gradlew :app:testAppDebugUnitTest --tests "*ActivityBaseSearchUtilTests"`
- `./gradlew :app:compileAppDebugKotlin`

Expected: PASS for both.

- [ ] **Step 6: Commit**

```bash
git add app/src/test/java/com/mxt/anitrend/base/custom/activity/ActivityBaseSearchUtilTests.kt app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBaseSearchUtil.kt app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBase.kt app/src/main/java/com/mxt/anitrend/view/activity/base/AboutActivity.kt
git commit -m "refactor(activity): improve shared search handling and migrate about screen to view binding"
```

### Task 4: Remove Stale ButterKnife/Data Binding References

**Files:**
- Modify: `app/src/main/java/com/mxt/anitrend/adapter/recycler/index/MediaAdapter.kt`
- Modify: `AGENTS.md`

- [ ] **Step 1: Write failing verification check (red)**

Run: `bash .github/scripts/verify-viewbinding-migration.sh`
Expected: FAIL with stale references in `MediaAdapter.kt` and `AGENTS.md`.

- [ ] **Step 2: Remove stale ButterKnife comment references from adapter**

```kotlin
// Replace comments that mention ButterKnife with neutral wording:
// "Default constructor with view binding"
```

- [ ] **Step 3: Fix outdated copilot guideline line for binding approach**

```md
- **View Binding**: Use generated View Binding classes for view references; do not use ButterKnife/Data Binding.
```

- [ ] **Step 4: Re-run migration verification script (green)**

Run: `bash .github/scripts/verify-viewbinding-migration.sh`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mxt/anitrend/adapter/recycler/index/MediaAdapter.kt AGENTS.md
git commit -m "docs(refactor): remove stale butterknife references"
```

### Task 5: Full Verification and Wrap-up

**Files:**
- Modify (if needed): `docs/superpowers/specs/2026-05-03-java-to-kotlin-migration-design.md`

- [ ] **Step 1: Run static closure checks**

Run:
- `bash .github/scripts/verify-viewbinding-migration.sh`
- `rg --files -g "**/*.java"`

Expected: migration script PASS and zero Java files.

- [ ] **Step 2: Run build and test verification**

Run:
- `./gradlew :app:assembleAppDebug`
- `./gradlew :app:testAppDebugUnitTest`

Expected: PASS.

- [ ] **Step 3: Manual smoke checks for shared view surfaces**

Verify:
- Search open/close/query in at least one `ActivityBase` inheritor (`MainActivity` + one detail activity).
- Toolbar home/back behavior in `AboutActivity`.
- Bottom sheet toolbar collapse/close behavior in `BottomSheetMessage` and `BottomReviewReader`.

- [ ] **Step 4: Document verification results in PR description/changelog note**

```md
## Migration Closure Verification
- No Java/Data Binding/ButterKnife usage detected by script
- assembleAppDebug: PASS
- testAppDebugUnitTest: PASS
- Shared toolbar/search smoke checks: PASS
```

- [ ] **Step 5: Final commit (if additional verification docs changed)**

```bash
git add docs/superpowers/specs/2026-05-03-java-to-kotlin-migration-design.md
git commit -m "chore(migration): record verification outcomes"
```

## Plan Self-Review

- Spec coverage: all approved phases are represented (baseline guard, base refactor, shared consumers, cleanup, verification).
- Placeholder scan: no TODO/TBD placeholders.
- Type consistency: `ActivityBaseSearchUtil.normalize(...)` naming and usage are consistent across tasks.
