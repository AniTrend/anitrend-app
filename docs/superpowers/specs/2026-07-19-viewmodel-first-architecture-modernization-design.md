# ViewModel-First Architecture Modernization Design

Date: 2026-07-19
Project: AniTrend Android app
Scope: Define architectural standards for new and refactored UI work in this single-module `:app` project. Unify direction without forcing an immediate full rewrite or module split.

## 1. Goals and Non-Goals

### Goals
- Establish a default ownership model: `ViewModel -> Activity/Fragment/Custom View`.
- Clarify when legacy patterns (`ActivityBase`, `ViewModelBase<T>`, `setViewModel()`) may still be used and when they must not.
- Record the long-term direction toward Nav3 + Compose with a single activity so new refactors do not reinforce the old multi-activity presenter architecture.
- Define two approved feature shapes (local vs. domain) so teams don't over-layer simple screens or under-layer API-backed ones.
- Document state, coroutine dispatcher, and testability conventions for new ViewModels.
- Provide a concrete first refactor target (`LoggingActivity`) that exercises the new standards.

### Non-Goals
- No immediate large-scale refactor of existing screens.
- No module split or introduction of Compose in this phase.
- No immediate removal of `BasePresenter` or `CommonPresenter` from every legacy screen in this phase.
- No change to Koin DI wiring or the existing DI module layout.

## 2. Current State Summary

### Legacy architecture
- Most activities extend `ActivityBase<V, P : CommonPresenter>` and acquire presenters through the activity superclass or Koin injection.
- `ActivityBase.setViewModel()` creates a `ViewModelBase<T>` and wires a `MutableLiveData<T?>` request/response path. This is the legacy pattern.
- `CommonPresenter` and concrete presenter subclasses (e.g., `BasePresenter`, `WidgetPresenter`) handle data loading, parsing, and mutation in many existing screens.

### Available modern precedent
- `app/src/main/java/com/mxt/anitrend/viewmodel/LoginAuthViewModel.kt` is an existing `androidx.lifecycle.ViewModel` that does NOT extend `ViewModelBase<T>`. It is acquired directly with `ViewModelProvider` in `LoginActivity`. This is the approved pattern for new work.
- `app/src/main/java/com/mxt/anitrend/view/activity/index/LoginActivity.kt` still extends `ActivityBase<Void, BasePresenter>` for shell behavior but does not call `setViewModel()`.

### LoggingActivity current state
- `app/src/main/java/com/mxt/anitrend/view/activity/base/LoggingActivity.kt` extends `ActivityBase<Void, BasePresenter>` and currently owns file loading, parsing with `LogParser`, filter state, clear/share/save actions, and UI rendering directly in the activity.
- The activity imposes a `MAX_DISPLAY_LINES = 5_000` cap on displayed log entries.
- The app logger (`TimberInitializer`) uses `fr.bipi.treessence.file.FileLoggerTree` configured at `app/src/main/java/com/mxt/anitrend/initializer/logger/TimberInitializer.kt` with `FILE_SIZE_LIMIT = 800 * 1024` (800 KB) and `FILE_CREATION_LIMIT = 1` (single file, appended). This is the confirmed file-level bound.

## 3. Approved Architectural Shapes

### 3.1 Local feature (app-local data, no API/domain layer)
```
UI (Activity/Fragment) -> ViewModel -> local collaborators
```
- ViewModel owns all loading, parsing, filtering, and state.
- Local collaborators include utilities, file helpers, in-memory state, and local-format parsers.
- Examples: `LoggingActivity`, settings/preference screens, about screen, local diagnostic viewers.
- Do not force a domain or repository layer when the data is entirely local.

### 3.2 Domain feature (API-backed, needs business logic)
```
UI -> ViewModel -> interactor/use case -> repository/data source
```
- Only required when a real domain boundary exists (API calls, cache strategies, multi-source sync).
- Existing domain-backed screens (media, user, search, notifications) would follow this shape when refactored.
- This repo is still single-module; interactors and repositories live in the same `:app` module under appropriate packages.

## 4. Coexistence with Legacy Patterns

### 4.1 ActivityBase
- New activities MAY still extend `ActivityBase<Void, BasePresenter>` for shared shell behavior while the migration is in progress.
- New activities MUST NOT call `setViewModel()` (this creates a legacy `ViewModelBase<T>`).
- New activities MUST NOT depend on `BasePresenter` or any `CommonPresenter` subclass for data loading, parsing, or domain orchestration.
- Activities are a legacy shell. The target architecture is Nav3 + Compose with a single activity.

### 4.2 Presenters
- Existing presenters in legacy screens remain as-is.
- New work MUST NOT add presenter usage.
- Refactors SHOULD remove presenter usage instead of carrying it forward.
- Presenters MUST NOT own data loading, parsing, filtering, or new orchestration in ViewModel-first screens.

### 4.3 ViewModelBase<T> and setViewModel()
- These are legacy patterns. New ViewModels do not extend `ViewModelBase<T>` and new screens do not call `setViewModel()`.
- Legacy screens that use these patterns continue unchanged until explicitly refactored.

### 4.4 Custom views
- Custom views remain view-only. They do not own a ViewModel or initiate data loading.
- Activities/Fragments observe ViewModel state and push data into custom views as needed.

## 5. State, Dispatcher, and Testing Conventions

### State surfaces
- Default for new ViewModels: `StateFlow` or `SharedFlow`.
- `LiveData` is acceptable when integrating with legacy screens already using it.

### Coroutine dispatchers
- Inject dispatchers into new ViewModels for testability (e.g., constructor parameter defaulting to `Dispatchers.Default` or `Dispatchers.IO`, overridden in tests with `UnconfinedTestDispatcher`).
- Keep `lifecycleScope` usage in activities limited to UI-bound launches and observation glue (e.g., `lifecycleScope.launch { viewModel.state.collect { ... } }`).

### Testing
- Parsing, filtering, state reduction, and metadata assembly MUST be unit-testable outside the activity.
- ViewModel tests should use `kotlinx-coroutines-test` and injected test dispatchers.
- Activity-level instrumentation tests continue to use `ActivityScenario` / Espresso patterns already present in `app/src/androidTest/**`.

## 6. Phased Migration Strategy

### Phase 1 (current, completed)
- Standards documented in `AGENTS.md` and this design spec.
- No code refactors.

### Phase 2: LoggingActivity refactor target
- Move log loading, `LogParser` invocation, filter state, current-filter tracking, and share/export metadata assembly out of `LoggingActivity` into a dedicated `LoggingViewModel` extending `androidx.lifecycle.ViewModel` directly.
- `LoggingActivity` continues extending `ActivityBase<Void, BasePresenter>` for toolbar and progress layout behavior.
- `LoggingActivity` observes `LoggingViewModel` state:
  - `entries: StateFlow<List<LogEntry>>` (filtered)
  - `currentFilter: StateFlow<LogFilter>`
  - `isLoading: StateFlow<Boolean>`
- Replace the current header treatment (app icon + version text) with a Material 3 card summarizing app version, build info, and device metadata for support sharing.
- Remove the activity-level `MAX_DISPLAY_LINES` constant. The file logger already bounds file size at 800 KB and uses a single file. Keep the `LogParser` default of 5,000 lines as the single defensive safety net unless the ViewModel passes a different explicit cap with a documented reason.
- Design a share/export flow so shared log files carry useful build, app, and device context for bug reports. Decide at the start of Phase 2 whether metadata is prepended to the shared file or generated as a separate attachment.

### Phase 3+: incremental adoption
- New screens follow the local or domain shape from Section 3.
- Existing screens are refactored one at a time when their maintenance burden justifies it.
- Navigation and screen ownership should converge toward Nav3 + Compose on a single activity instead of reinforcing activity-per-screen patterns.
- Custom views that currently hold their own presenter references should have those references pushed up to owning activities/fragments during refactoring.

## 7. Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| `ActivityBase` lifecycle expectations conflict with ViewModel ownership | Keep `ActivityBase` for shell only; ViewModel owns all data/state. `onActivityReady()` calls `viewModel.load()` instead of `makeRequest()`. |
| Presenter injection in legacy screens is entangled with Koin module setup | Do not change existing Koin modules. New ViewModels use `ViewModelProvider` or Koin `viewModel()` factory. |
| Custom views assume they can call presenter methods directly | Narrow custom views to pure UI during refactoring. Owning activity/fragment observes ViewModel and forwards data/events. |
| Over-layering simple local screens | Default to the local-feature shape. Only add interactors/repositories when a real domain boundary exists. |
| Testability gap if dispatchers are hardcoded | Inject dispatchers into new ViewModels. Use `UnconfinedTestDispatcher` in unit tests. |

## 8. Execution Readiness

Phase 1 (standards and spec) is complete. Phase 2 (`LoggingActivity` refactor) is designed and ready for implementation in a follow-up cycle per the migration plan in Section 6.

Next step: approve Phase 2 scope, then implement the `LoggingViewModel` + `LoggingActivity` refactor against this spec.
