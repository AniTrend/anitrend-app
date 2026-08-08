# AniTrend Agent Notes

## Architecture and module boundaries
- Real Gradle project is single-module `:app` (`settings.gradle.kts`); `app-compose/` exists in repo but is a stale, excluded build-output directory, not a module. Do not include it in the build.
- App build logic is centralized in `buildSrc` via plugin `com.mxt.anitrend.plugin` (`app/build.gradle.kts`); prefer changing `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/*` over adding ad-hoc module config.
- Product flavors are `app` and `github` (`app/src/app/`, `app/src/github/`); the `github` flavor adds a `-github` version suffix and has its own manifest/service surface. Preserve flavor behavior when editing build logic.
- Current toolchain: Gradle wrapper 9.6.1, AGP 9.3.1, Kotlin 2.4.10 (do not conflate these versions), compileSdk/targetSdk 37, minSdk 23, Java 21 (`.java-version` = `21.0.11`), Kotlin JVM 21.
- View Binding is enabled and Data Binding is disabled. Legacy KAPT remains in use alongside ObjectBox, GraphQL code generation, Kotlinx serialization, parcelize, and Spotless; do not opportunistically migrate to KSP.
- `app/.config/configuration.properties` is committed configuration. Secrets and signing properties are local or CI-provisioned and must never be committed.

## GraphQL migration state
- GraphQL is codegen-first: schema + operations live in `app/src/main/graphql/**`.
- Generated operation API package is `com.mxt.anitrend.graphql.generated` (configured in `GraphQLComponents.kt`), using Kotlinx serialization.
- Use typed generated requests (`SomeOperation.request(...)` / `GraphQLRequest<...>`); do not reintroduce removed legacy patterns `@GraphQuery`, `QueryContainerBuilder`, or `RequestHandler` in new work.
- `KeyUtil.*_REQ` request routing still exists (for example in `StaffPageAdapter`, `CharacterPageAdapter`, and `MediaFormatViewModel`); do not add new `KeyUtil.*_REQ` routing and migrate existing routing when touched.
- GraphQL list input variables that are semantically absent must be passed as `null`, not an empty list. Empty lists are treated by the AniList backend as real filter/search input and can produce no results, as seen around `MediaBrowseFragment` and its ViewModel.
- Avoid nullable list item types for GraphQL filters unless the schema or business rule explicitly requires nullable elements. Prefer `List<T>?` over `List<T?>?` for values such as genres or tags, because lists like `["value", null, null]` are not meaningful backend input.
- The unit-test workflow checks out retrofit-graphql branch `0.12.1`, while the catalog pins the runtime/API at `0.13.0`. Current Gradle files do not prove that checkout is consumed, so treat this as an unverified or unused CI checkout/version mismatch, not as proof that CI builds against `0.12.1`.

## Build and verification commands
- Fast local verification after code changes:
  - `./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug --no-daemon`
  - Run `bash .github/scripts/setup-config.sh` first when `app/.config/secrets.properties` is absent.
- Flavor APK builds:
  - `./gradlew :app:assembleAppDebug`
  - `./gradlew :app:assembleGithubDebug`

## CI scope and release gotchas
- PR/develop unit CI (`android-unit-test.yaml`) runs on JDK 21 Temurin and executes `./gradlew :app:spotlessCheck --stacktrace` and `./gradlew test --stacktrace`. It does not separately assemble APKs.
- Tag release CI (`android-build.yaml`) builds release APKs through Fastlane via `bash .github/scripts/fastlane.sh` and uploads them to the GitHub release. Do not claim CI globally never assembles; scope any claim to the workflow in question.
- CI always runs `bash .github/scripts/validate-changelogs.sh`; any file in `fastlane/metadata/android/en-GB/changelogs/*.txt` must be `<= 500` chars.
- Fastlane lanes live in `fastlane/Fastfile`: `verify`, `build`, `submit_beta`, `submit_prod`, and `deploy`.
- Version metadata is automated by workflow `version-updater.yml` on branch `platform/update-version-meta-data`, targeting `develop`; check that branch/PR before manual version-file edits. Version data lives in `gradle/version.properties`.
- Generated `app/.meta/version.json` currently reports minSdk 21 while the Android build uses minSdk 23. Treat this as a known automation mismatch; do not alter build values without a dedicated change.

## Dependency and build-edit conventions
- Use version catalog aliases from `gradle/libs.versions.toml`; do not hardcode dependency coordinates in module build files.
- Keep current toolchain choices unless asked explicitly: AGP/Kotlin/JVM levels, KAPT (do not opportunistically migrate to KSP), ObjectBox/DataBinding compatibility settings.

## ViewModel-first architecture (new and refactored UI)

### Default direction
- `ViewModel` owns loading, parsing, filtering, and state. Activities, Fragments, and Custom Views observe and render.
- New ViewModels extend `androidx.lifecycle.ViewModel` directly. Do not
  reintroduce the deleted legacy base-class wiring.
- Compose and Navigation 3 are future direction only. Current production UI is the XML/View system with AndroidX Navigation 2.9.8. Do not claim Compose or Navigation 3 is active.

### Coexistence with legacy
- `CommonActivity` is the current Koin `activityScope` shell for activities. Existing presenter dependencies are compatibility debt that must not be expanded.
- Direct-ViewModel activities on `CommonActivity` are already proven: `LoggingActivity`, `StudioActivity`, `ProfileActivity`, `MediaActivity`, `CharacterActivity`, and `StaffActivity`. `CommonActivity` remains a legacy shell to thin out.
- Do not add new presenter usage. Presenters are legacy-only and should be removed during refactors instead of being carried forward.
- When presenter dependencies still exist during legacy cleanup, inject the required presenter explicitly at the usage site with `by inject<T>()` rather than relying on removed base-class presenter wiring.

### Feature shape
- **Local feature** (app-local data, no API/domain layer): `UI -> ViewModel -> local collaborators` (e.g. `LoggingActivity`, settings screens).
- **Domain feature** (API-backed, needs business logic): `UI -> ViewModel -> interactor/use case -> repository/data source`. Only required when a real domain boundary exists; do not force this for every screen.

### State and testing
- New ViewModel state surfaces use `StateFlow` by default. `SharedFlow` has no current main-source usage; adopt it only for explicit one-shot events. `LiveData` is acceptable for screens already using it.
- Inject dispatchers into new ViewModels for testability. Keep `lifecycleScope` usage in activities limited to UI-bound launches and observation glue.
- Parsing, filtering, state reduction, and metadata assembly must be unit-testable outside the activity.

### Custom views
- Custom views remain view-only. They must not own a ViewModel or initiate data loading. Activities/Fragments observe the ViewModel and push data into custom views.

### Reference
- See `docs/superpowers/specs/2026-07-19-viewmodel-first-architecture-modernization-design.md` for the full design spec and migration strategy; `LoggingActivity` is the existing local-feature ViewModel-first precedent, not a future refactor target.

## State synchronisation and mutation architecture

The full specification lives at `docs/architecture/state-synchronization-and-mutation-refactor.md`. It is a proposed specification: the rules below are mandatory for new and refactored code in migrated domains (feed, comments, likes, replies, media-list entries) only. Do not assume the entire repository is migrated or that the specification is complete.

### Canonical stores
- Each migrated domain has one canonical store as the exclusive mutable owner of committed entity state. Repositories commit successful mutation responses into stores; ViewModels observe store state.
- Do not introduce repository `SharedFlow` mutation events (`mutationEvents`); the pattern appears only in the proposed specification and has no current main-source symbol. Business state must live in observable store or ViewModel state, not transient event streams.
- Do not create one generic global store. Stores are domain-specific (`FeedStore`, `MediaListStore`).

### No repository or coordinator access from adapters or widgets
- Adapters and custom views must not resolve repositories or any Koin business dependency.
- Adapters render immutable item models and forward actions via callbacks. Widgets render immutable state and emit user actions only.
- Do not launch business coroutines from adapters, ViewHolders, or widgets.

### Immutable RecyclerView items
- Migrated adapters use `ListAdapter` or `AsyncListDiffer` with immutable item UI models. No submitted list or item may be mutated after submission.
- Adapters must not maintain a canonical list independent of the ViewModel. Fragments call `submitList(state.items)`.
- Stable IDs must use actual stable domain IDs, not `hashCode()` or object identity. For heterogeneous lists, namespace the ID by entity type (for example, `"media:123"` vs `"character:123"`) so different item types that share an ID do not collide.
- The generic `RecyclerViewAdapter` base class is legacy infrastructure that uses `MutableList`, `notifyDataSetChanged()`, and `hashCode()`-based stable IDs. It remains for non-migrated screens. Do not use it for new or migrated screens. Use `ListAdapter` with `DiffUtil` instead. Track its removal as follow-up infrastructure debt.

### Identity-only navigation
- Navigation destinations receive stable IDs and presentation-independent arguments, not complete mutable entities.
- Activity results must not return a complete application entity to synchronise another screen. Use identity-only or explicit user selections.

### Per-resource mutation serialisation
- Mutations affecting the same logical resource execute sequentially via `MutationExecutor` with `ResourceKey`. Unrelated resources may run concurrently.
- Server-authoritative mutations by default: update state only after server success. Optimistic mutations are prohibited until a revision and rollback protocol is implemented and tested.
- Stale responses must be rejected using revision ordering; an older response must not overwrite newer committed state.

### Reference
- See `docs/architecture/state-synchronization-and-mutation-refactor.md` for phase gates, prohibited patterns, and completion criteria.

## Kotlin-first domain model migration
- The migration is proposed, not complete. See `docs/adr/2026-08-01-kotlin-first-domain-model-and-navigation.md` and its companion inventory `docs/adr/2026-08-01-kotlin-first-domain-model-inventory.md`.
- The legacy `com.mxt.anitrend.model.entity` tree is a compatibility boundary. The checked-in inventory is a 95-class snapshot from its generation time; the current legacy tree has 87 Kotlin files after deletions, and migration work must reconcile the inventory.
- Do not treat every entity as a domain model. Update the checked-in inventory with every model migration PR; the assigned target roles are proposed, not completed work.
- New domain values should be pure Kotlin and immutable. Persistence, remote, UI, framework/container, and navigation roles remain distinct; domain models must not import Android, ObjectBox, serialization frameworks, or presentation inheritance, and domain APIs must not expose generated GraphQL types.

## Material 3 design system
- `@DESIGN.md` is the app-wide M3 design system. It defines color tokens, typography hierarchy, spacing scale, component specs (bottom sheets, dialogs, cards, buttons, text fields, switches, sliders, chips, lists, navigation, custom views, loading), motion and feedback rules, and do's and don'ts.
- Consult `@DESIGN.md` before any UI/UX work: new screens, layout changes, component selection, styling, spacing, color usage, typography choices, dialog or sheet design, custom view creation, or any visual refactor.
- The manage list editor (`BottomSheetSeriesManage`) is the reference implementation of this design language. Future design passes must carry the same philosophy across the whole app.
- When a design decision changes (new component pattern, revised token usage, updated spacing convention), update `@DESIGN.md` in the same PR so it stays the source of truth.

## Branch, commit, and PR conventions
- Primary integration branch is `develop`. Branch prefixes in use: `feat/`, `fix/`, `chore/`, `refactor/`, `platform/`, `renovate/`, `translation/`.
- Commits follow conventional commits; common scopes include `ui`, `deps`, `architecture`, `navigation`, `skill`, `startup`, `config`, `media-browse`. Automation/platform version bot commits are the exception.
- Architecture PRs must document the fields required by `docs/architecture/pr-checklist.md`, including invariants, compatibility paths, tests, commands, limitations, follow-ups, and rollback.

## Test stack
- Unit tests: JUnit 4, coroutines test, Mockito.
- MockK and Koin test are instrumentation-scoped.
- Turbine is declared for androidTest but currently unused.
- Instrumentation uses AndroidX test core/runner/rules/ActivityScenario as applicable; Espresso and fragment-testing dependencies are declared but currently unused.
