# AniTrend Agent Notes

## Architecture and module boundaries
- Real Gradle project is single-module `:app` (`settings.gradle.kts`); `app-compose/` exists in repo but is not included in the build.
- App build logic is centralized in `buildSrc` via plugin `com.mxt.anitrend.plugin` (`app/build.gradle.kts`); prefer changing `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/*` over adding ad-hoc module config.
- Product flavors are `app` and `github` (`app/src/app/`, `app/src/github/`); preserve flavor behavior when editing build logic.

## GraphQL migration state
- GraphQL is codegen-first now: schema + operations live in `app/src/main/graphql/**`.
- Generated operation API package is `com.mxt.anitrend.graphql.generated` (configured in `GraphQLComponents.kt`).
- Use typed generated requests (`SomeOperation.request(...)` / `GraphQLRequest<...>`); do not reintroduce legacy `@GraphQuery` / `QueryContainerBuilder` patterns.
- GraphQL list input variables that are semantically absent must be passed as `null`, not an empty list. Empty lists are treated by the AniList backend as real filter/search input and can produce no results, as seen around `MediaBrowseFragment` and its ViewModel.
- Avoid nullable list item types for GraphQL filters unless the schema or business rule explicitly requires nullable elements. Prefer `List<T>?` over `List<T?>?` for values such as genres or tags, because lists like `["value", null, null]` are not meaningful backend input.

## Build and verification commands
- Java target is 21 (`.java-version` = `21.0.11`); CI also uses JDK 21.
- Fast local verification after code changes:
  - `./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug --no-daemon`
- Flavor APK builds:
  - `./gradlew :app:assembleAppDebug`
  - `./gradlew :app:assembleGithubDebug`
- Unit tests used in CI:
  - `bash .github/scripts/setup-config.sh` (creates placeholder `app/.config/secrets.properties`)
  - `./gradlew test --stacktrace`

## CI and release-specific gotchas
- CI always runs `bash .github/scripts/validate-changelogs.sh`; any file in `fastlane/metadata/android/en-GB/changelogs/*.txt` must be `<= 500` chars.
- Version metadata is automated by workflow `version-updater.yml` on branch `platform/update-version-meta-data`; check that branch/PR before manual version-file edits.

## Dependency and build-edit conventions
- Use version catalog aliases from `gradle/libs.versions.toml`; do not hardcode dependency coordinates in module build files.
- Keep current toolchain choices unless asked explicitly: AGP/Kotlin/JVM levels, KAPT (do not opportunistically migrate to KSP), ObjectBox/DataBinding compatibility settings.
- Do not hardcode secrets; keep environment-specific values in `app/.config/*.properties` / local files and avoid committing them.

## ViewModel-first architecture (new and refactored UI)

### Default direction
- `ViewModel` owns loading, parsing, filtering, and state. Activities, Fragments, and Custom Views observe and render during the migration to a single-activity architecture.
- New ViewModels extend `androidx.lifecycle.ViewModel` directly. Do not extend `ViewModelBase<T>` for new work.
- New screens do not call `ActivityBase.setViewModel()`.
- Activities are a legacy shell only. The long-term direction is Nav3 + Compose with a single activity.

### Coexistence with legacy
- Activities may still extend `ActivityBase<Void, BasePresenter>` for shared shell behavior while the migration is in progress.
- Do not add new presenter usage. Presenters are legacy-only and should be removed during refactors instead of being carried forward.
- When presenter dependencies still exist during legacy cleanup, inject the required presenter explicitly at the usage site with `by inject<T>()` rather than relying on removed base-class presenter wiring.

### Feature shape
- **Local feature** (app-local data, no API/domain layer): `UI -> ViewModel -> local collaborators` (e.g. `LoggingActivity`, settings screens).
- **Domain feature** (API-backed, needs business logic): `UI -> ViewModel -> interactor/use case -> repository/data source`. Only required when a real domain boundary exists; do not force this for every screen.

### State and testing
- New ViewModel state surfaces use `StateFlow` or `SharedFlow` by default. `LiveData` is acceptable for screens already using it.
- Inject dispatchers into new ViewModels for testability. Keep `lifecycleScope` usage in activities limited to UI-bound launches and observation glue.
- Parsing, filtering, state reduction, and metadata assembly must be unit-testable outside the activity.

### Custom views
- Custom views remain view-only. They must not own a ViewModel or initiate data loading. Activities/Fragments observe the ViewModel and push data into custom views.

### Reference
- See `docs/superpowers/specs/2026-07-19-viewmodel-first-architecture-modernization-design.md` for the full design spec, migration strategy, and `LoggingActivity` refactor target.

## State synchronisation and mutation architecture

The full specification lives at `docs/architecture/state-synchronization-and-mutation-refactor.md`. The rules below are mandatory for all new and refactored code in migrated domains (feed, comments, likes, replies, media-list entries).

### Canonical stores
- Each migrated domain has one canonical store as the exclusive mutable owner of committed entity state. Repositories commit successful mutation responses into stores; ViewModels observe store state.
- Do not use repository `SharedFlow` mutation events (`mutationEvents`) as a business-state propagation mechanism. Business state must live in observable store or ViewModel state, not transient event streams.
- Do not create one generic global store. Stores are domain-specific (`FeedStore`, `MediaListStore`).

### No repository or coordinator access from adapters or widgets
- Adapters and custom views must not resolve repositories, `WidgetMutationCoordinator`, or any Koin business dependency.
- Adapters render immutable item models and forward actions via callbacks. Widgets render immutable state and emit user actions only.
- Do not launch business coroutines from adapters, ViewHolders, or widgets.

### Immutable RecyclerView items
- Migrated adapters use `ListAdapter` or `AsyncListDiffer` with immutable item UI models. No submitted list or item may be mutated after submission.
- Adapters must not maintain a canonical list independent of the ViewModel. Fragments call `submitList(state.items)`.
- Stable IDs must use actual stable domain IDs, not `hashCode()` or object identity.
- The generic `RecyclerViewAdapter` base class is legacy infrastructure that uses `MutableList`, `notifyDataSetChanged()`, and `hashCode()`-based stable IDs. It remains for non-migrated screens. Do not use it for new or migrated screens. Use `ListAdapter` with `DiffUtil` instead. Track its removal as follow-up infrastructure debt.

### Identity-only navigation
- Navigation destinations receive stable IDs and presentation-independent arguments, not complete mutable entities.
- Activity results must not return a complete application entity to synchronise another screen. Use identity-only or explicit user selections.

### Per-resource mutation serialisation
- Mutations affecting the same logical resource execute sequentially via `MutationExecutor` with `ResourceKey`. Unrelated resources may run concurrently.
- Server-authoritative mutations by default: update state only after server success. Optimistic mutations are prohibited until a revision and rollback protocol is implemented and tested.
- Stale responses must be rejected using revision ordering; an older response must not overwrite newer committed state.

### Reference
- See `docs/architecture/state-synchronization-and-mutation-refactor.md` for the full specification, phase gates, prohibited patterns, and completion criteria.

## Material 3 design system
- `@DESIGN.md` is the app-wide M3 design system. It defines color tokens, typography hierarchy, spacing scale, component specs (bottom sheets, dialogs, cards, buttons, text fields, switches, sliders, chips, lists, navigation, custom views, loading), motion and feedback rules, and do's and don'ts.
- Consult `@DESIGN.md` before any UI/UX work: new screens, layout changes, component selection, styling, spacing, color usage, typography choices, dialog or sheet design, custom view creation, or any visual refactor.
- The manage list editor (`BottomSheetSeriesManage`) is the reference implementation of this design language. Future design passes must carry the same philosophy across the whole app.
- When a design decision changes (new component pattern, revised token usage, updated spacing convention), update `@DESIGN.md` in the same PR so it stays the source of truth.

## First-render regression guardrails
- For async list-backed screens, verify first open separately from refresh. A refresh-only test can hide timing and measurement regressions.
- When content visibility depends on an async adapter commit, update loading/content state from the adapter commit callback rather than immediately after `submitList`.
- Do not combine nested `RecyclerView` `wrap_content` measurement with `setHasFixedSize(true)`.
- When debugging a blank first render, inspect the request result, ViewModel item count, adapter item count, measured bounds, and accessibility tree in that order.
- See `docs/retrospectives/2026-08-04-comment-reply-first-render.md` for the incident evidence and verification record.
