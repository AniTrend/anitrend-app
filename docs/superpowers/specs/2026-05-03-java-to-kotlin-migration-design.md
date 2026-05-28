# Java to Kotlin Migration Closure Design

Date: 2026-05-03
Project: AniTrend Android app
Scope: Verification and cleanup after Java-to-Kotlin migration, including full removal of ButterKnife and Data Binding, and standardization on View Binding.

## 1. Goals and Non-Goals

### Goals
- Verify Java source migration closure across the repo.
- Remove ButterKnife and Data Binding usage completely from source, build setup, and docs.
- Standardize layout-backed UI access on View Binding.
- Refactor high-impact shared surfaces (especially activity search/toolbar paths) safely.
- Preserve behavior while changing view access mechanics.

### Non-Goals
- No source set path move from `src/*/java` to `src/*/kotlin` in this effort.
- No broad architecture rewrite of presenters/viewmodels.
- No unrelated feature changes.

## 2. Current State Summary

- `*.java` files: none found.
- Build configuration already enforces `dataBinding = false` and `viewBinding = true` in `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/AndroidComponents.kt`.
- No active `DataBindingUtil`, `ViewDataBinding`, or `androidx.databinding` source references found.
- No active ButterKnife symbol usage found; only stale references in comments/docs were detected.
- Shared activity surface `app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBase.kt` has high blast radius and search/toolbar behavior that multiple inheritors rely on.

## 3. Migration Strategy (Approved)

Staged migration, foundation-first.

Rationale:
- Shared bases (`ActivityBase`, `FragmentBase`, `BottomSheetBase`) fan out into many screens and are the safest first place to normalize behavior.
- This limits per-screen hacks and isolates regressions early.
- Small staged batches improve rollback and review safety.

## 4. Phased Design

### Phase 0: Baseline Verification

Run and capture baseline for:
- No Java source files.
- No ButterKnife usage symbols (`ButterKnife`, `@BindView`, `@OnClick`, `Unbinder`).
- No Data Binding usage symbols (`DataBindingUtil`, `ViewDataBinding`, `androidx.databinding`, `BR.`).
- Existing assemble/test baseline for app flavor.

Deliverable:
- Baseline verification report in PR description or work log.

### Phase 1: Shared Foundation Refactor

Primary files:
- `app/src/main/java/com/mxt/anitrend/base/custom/activity/ActivityBase.kt`
- `app/src/main/java/com/mxt/anitrend/base/custom/fragment/FragmentBase.kt`
- `app/src/main/java/com/mxt/anitrend/base/custom/sheet/BottomSheetBase.kt`

Design intent:
- Normalize binding-safe integration points for shared toolbar/search and root view handling.
- Keep backwards-compatible behavior for inheritors while reducing nullability and lifecycle edge-case risks.
- Keep search callback behavior centralized in `ActivityBase` with safer ownership assumptions.

Key constraints:
- No behavior drift in home/back handling, search submit/change handling, and state restoration.
- Avoid introducing per-feature conditional logic in base classes.

### Phase 2: Shared Consumer Migration

Priority order:
1. `ActivityBase` inheritors that assign `mSearchView`.
2. Shared bottom sheets and fragments with manual view lookups where binding classes exist.
3. Remaining layout-backed screens still using manual view lookup patterns that can be replaced safely.

Rules:
- Prefer generated binding references when a binding class exists.
- Keep component lifecycle ownership explicit (especially nullable binding in fragment-like lifecycles).
- Replace only view-access mechanics; keep presentation and request logic unchanged.

### Phase 3: Cleanup and Hardening

- Remove stale ButterKnife/Data Binding references from comments/docs/build notes.
- Keep build-feature state enforced (`dataBinding = false`, `viewBinding = true`).
- Add lightweight regression guards (search checks in CI-friendly script or documented local verification command set).

## 5. Error Handling and Rollback

- Use small commits per migration batch for isolation.
- If regressions appear after base refactor, patch base compatibility first, then continue migration.
- Validate all `ActivityBase` inheritors for search/toolbar behavior before broad cleanup lands.

## 6. Testing and Verification Plan

Per phase verification gates:
- Static checks:
  - zero `*.java`
  - zero ButterKnife references
  - zero Data Binding references
- Build checks:
  - `./gradlew :app:assembleAppDebug`
  - `./gradlew :app:testAppDebugUnitTest`
- Behavior checks (targeted manual smoke):
  - search open/close/query submit
  - toolbar navigation/home behavior
  - bottom sheet open/dismiss where shared base behavior is involved

## 7. Definition of Done

- Java source migration closure confirmed (`*.java` absent).
- ButterKnife and Data Binding fully removed from source/build/docs for active implementation paths.
- View Binding is the only active binding mechanism for layout-backed components.
- Shared surfaces (`ActivityBase` and adjacent bases) remain behaviorally stable.
- Assemble and targeted unit tests pass for migrated scope.

## 8. Risks and Mitigations

### Risk: Shared search view regressions
Mitigation: Migrate/verify all `ActivityBase` inheritors early, with explicit smoke checks.

### Risk: Lifecycle leaks in fragment/sheet bindings
Mitigation: enforce clear binding ownership and lifecycle clearing patterns in base + consumers.

### Risk: Hidden stale references cause future confusion
Mitigation: final grep sweep and documentation cleanup in Phase 3.

## 9. Execution Readiness

This design is implementation-ready for a staged, foundation-first rollout.
Next step is to produce a concrete implementation plan (task sequence + file-level checkpoints), then execute phase by phase with verification after each batch.
