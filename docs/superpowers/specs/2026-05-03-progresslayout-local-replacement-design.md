# ProgressLayout Local Replacement Design

Date: 2026-05-03
Project: AniTrend Android app
Scope: Replace external `com.github.nguyenhoanglam:ProgressLayout` dependency with an app-local `ProgressLayout` implementation to unblock builds while preserving current behavior.

## 1. Goals and Non-Goals

### Goals
- Remove the external `ProgressLayout` dependency that currently blocks Gradle resolution.
- Introduce app-local `ProgressLayout` under `com.mxt.anitrend.widget`.
- Preserve existing runtime behavior and callsite contracts used by screens and sheets.
- Migrate all XML/Kotlin references from external class path to app-local class path in one pass.
- Keep implementation minimal (only APIs currently used by this codebase).

### Non-Goals
- No UX redesign of loading/content/error visuals.
- No animation or state-transition enhancement beyond current behavior.
- No broad refactor of fragment/sheet/activity presentation logic.
- No new dependency/module split for the widget in this effort.

## 2. Current State Summary

Dependency and build:
- Dependency alias exists in `gradle/libs.versions.toml` and is consumed in `app/build.gradle.kts` via `implementation(libs.progresslayout)`.
- Build/test currently fail at dependency resolution for `com.github.nguyenhoanglam:ProgressLayout:master-SNAPSHOT`.

Usage footprint:
- ProgressLayout class path in code/layouts: `com.nguyenhoanglam.progresslayout.ProgressLayout`.
- Used across base fragments/sheets and screens including list, comment, stats, user/staff/character/series overview, bottom sheets, and logging content.
- Layout tag appears in 9 layout files:
  - `app/src/main/res/layout/fragment_series_stats.xml`
  - `app/src/main/res/layout/content_logging.xml`
  - `app/src/main/res/layout/fragment_staff_overview.xml`
  - `app/src/main/res/layout/fragment_character_overview.xml`
  - `app/src/main/res/layout/bottom_sheet_list.xml`
  - `app/src/main/res/layout/fragment_comment.xml`
  - `app/src/main/res/layout/fragment_user_about.xml`
  - `app/src/main/res/layout/fragment_list.xml`
  - `app/src/main/res/layout/fragment_series_overview.xml`

Observed API usage in Kotlin callsites:
- `showLoading()`
- `showContent()`
- `showError(drawable, message, getString(R.string.try_again), stateLayoutOnClick)`
- `showEmpty(drawable, getString(R.string.layout_empty_response))`

Note: `showEmpty(...)` is actively used and must be included for compatibility.

## 3. Chosen Approach (Approved)

Option A: app-local drop-in style replacement with compatibility-first API parity.

Design choice:
- Introduce `com.mxt.anitrend.widget.ProgressLayout` as a custom `FrameLayout` that manages three visible states: loading, content, and error/empty.
- Keep public methods required by current callsites:
  - `showLoading()`
  - `showContent()`
  - `showError(drawable: Drawable?, message: CharSequence?, actionText: CharSequence?, action: OnClickListener?)`
  - `showEmpty(drawable: Drawable?, message: CharSequence?)`
- Migrate imports and XML tag class names to app-local package.
- Remove external dependency and version catalog entry.

Why this approach:
- Unblocks builds immediately without behavioral redesign.
- Keeps migration scope finite and testable.
- Avoids temporary wrappers and future cleanup burden.

## 4. Component Design

### 4.1 New app-local widget

Create:
- `app/src/main/java/com/mxt/anitrend/widget/ProgressLayout.kt`

Behavior model:
- Content view(s): all child views declared in XML inside the `ProgressLayout` container.
- Loading overlay: simple in-widget loading container (indeterminate progress + optional text).
- Error/empty overlay: icon + message + optional action button.

State transitions:
- `showLoading()` -> loading visible, content hidden, error hidden.
- `showContent()` -> content visible, loading hidden, error hidden.
- `showError(...)` -> error visible, loading hidden, content hidden.
- `showEmpty(...)` -> error/empty visible with action hidden by default.

Safety rules:
- Null drawable/message/action text must be handled gracefully.
- If `action` is null, hide action button.
- Repeated same-state calls are idempotent and do not break visibility.

### 4.2 XML migration

For all affected layouts, replace tag:
- From: `<com.nguyenhoanglam.progresslayout.ProgressLayout ...>`
- To: `<com.mxt.anitrend.widget.ProgressLayout ...>`

Keep existing Android/app attributes that belong to surrounding containers unchanged.

### 4.3 Kotlin migration

Update imports and type references:
- From: `import com.nguyenhoanglam.progresslayout.ProgressLayout`
- To: `import com.mxt.anitrend.widget.ProgressLayout`

No callsite behavior changes beyond package/type migration.

### 4.4 Build cleanup

Remove external dependency wiring:
- Delete `implementation(libs.progresslayout)` from `app/build.gradle.kts`.
- Remove `progresslayout` alias entry from `gradle/libs.versions.toml`.

## 5. Error Handling and Rollback

Error handling in widget:
- Defend against null `Drawable` and null/blank message.
- Ensure default visible fallback text can be used if needed (or preserve empty message if current UX depends on it).
- Action button hidden when not applicable.

Rollback plan:
- Keep changes grouped in a dedicated commit so dependency removal + migration can be reverted atomically if regressions appear.
- If regressions occur, first patch `ProgressLayout` behavior compatibility (do not revert all migrated callsites unless necessary).

## 6. Testing and Verification Plan

Static migration checks:
- No references remain for `com.nguyenhoanglam.progresslayout.ProgressLayout` in `app/src/main`.
- No remaining `implementation(libs.progresslayout)` in `app/build.gradle.kts`.
- No remaining `progresslayout` alias in `gradle/libs.versions.toml`.

Build/test gates:
- `./gradlew :app:compileAppDebugKotlin --no-daemon -q`
- `./gradlew :app:testAppDebugUnitTest --tests "*ActivityBaseSearchUtilTests" --no-daemon -q`
- `./gradlew :app:testAppDebugUnitTest --no-daemon -q`

Runtime smoke checks:
- One list fragment path: loading -> content and loading -> error.
- One bottom sheet path using `stateLayout`.
- `LoggingActivity` path (`showLoading()` then `showContent()`).
- Error action button callback still triggers retry behavior where used.

## 7. Definition of Done

- External `ProgressLayout` dependency removed from build configuration.
- App-local `ProgressLayout` class added and used everywhere.
- All existing callsites compile without API shims/hacks.
- Gradle no longer fails due to missing `com.github.nguyenhoanglam:ProgressLayout`.
- Build/test gates execute successfully (or expose unrelated pre-existing failures only).

## 8. Risks and Mitigations

Risk: subtle UI behavior drift versus previous library implementation.
- Mitigation: compatibility-first API surface and targeted smoke checks on representative screens.

Risk: missing method parity at compile time.
- Mitigation: implement all currently observed methods (`showLoading`, `showContent`, `showError`, `showEmpty`) before migration completion.

Risk: XML/style expectations differ from old library internals.
- Mitigation: keep layout usage simple and avoid introducing strict custom attrs in this first pass.

## 9. Execution Readiness

This design is implementation-ready.
Next step: generate a task-by-task implementation plan, then execute in the active worktree with verification after dependency removal and migration.
