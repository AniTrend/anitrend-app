# Full Compose Rewrite + Apollo Kotlin Migration

## Context

AniTrend is a 402-file View-based Android app using AppCompat MD2 themes, Retrofit + custom GraphQL, ObjectBox, and ~30+ third-party libraries. Many libraries are outdated, abandoned, or redundant. The decision was made to modernize the entire stack: Apollo Kotlin for data, Room for local storage, Jetpack Compose + MD3 for UI, and Kotlin Coroutines/Flow for state management.

**Existing toolchain stays**: AGP 8.13.2, Kotlin 2.3.21, Gradle 9.5.1, Java 21, Koin 4.2.1.

**Feature freeze**: No new features during migration. All effort goes into the rewrite.

## Approach

**Hybrid Spiral** — Core data layer first, then a new Compose app shell, then feature-by-feature vertical slices (data → state → UI for each feature). Old and new app coexist until migration is complete.

## Module Structure

Old and new app coexist in separate Gradle modules sharing the same `buildSrc` and version catalog:

```
anitrend-app/
├── app/                          # Existing View-based app (unchanged until Phase 4)
├── app-compose/                  # New Compose module (built in Phases 1-3)
│   ├── src/main/java/com/mxt/anitrend/
│   │   ├── AniTrendApp.kt       # Application class
│   │   ├── theme/               # MD3 MaterialTheme
│   │   ├── navigation/          # NavHost + routes
│   │   ├── data/                # Apollo + Room repositories
│   │   ├── ui/                  # Compose screens per feature
│   │   └── di/                  # Koin modules
│   └── build.gradle.kts
├── buildSrc/                     # Shared build logic (updated in Phase 1)
├── gradle/libs.versions.toml     # Single version catalog
└── settings.gradle.kts           # Includes both :app and :app-compose
```

Both modules share the same flavor dimensions (`version` → `app`/`github`). In Phase 4, `app/` is deleted and `app-compose/` is renamed to `app/`.

## Architecture

| Layer | Before | After |
|---|---|---|
| **Networking** | Retrofit + custom `retrofit-graphql` | Apollo Kotlin (`apollo-runtime`) |
| **GraphQL** | Manual Gson-deserialized models | Apollo codegen (from `.graphql` queries) |
| **Serialization** | Gson + SimpleXML (Crunchyroll RSS) | Removed (Apollo handles; RSS via kotlinx-serialization) |
| **Local DB** | ObjectBox | Room (`room-runtime`, `room-ktx`, KSP) |
| **State** | LiveData + EventBus | StateFlow + SharedFlow |
| **DI** | Koin | Koin (kept) |
| **UI** | XML + AppCompat + MD2 Views | Jetpack Compose + MD3 `MaterialTheme` |
| **Navigation** | Manual Activity/Fragment transitions | Navigation Compose |


### Presentation Pattern

Every screen follows MVVM with unidirectional data flow:

```
Screen (Composable) ──observes──> ViewModel.uiState (StateFlow)
        │                                  │
        │  onEvent(event)                  │
        └────────────── ViewModel ──────────┘
                              │
                      Repository (Apollo / Room)
                              │
                    ApolloClient / Room DAO
```

- **ViewModel** exposes `StateFlow<UiState>` — no LiveData
- **UiState** is a sealed interface with `Loading`, `Success(data)`, `Error(message)` variants
- **Events** (one-shot) use `SharedFlow` or channel — replaces EventBus
- **Side effects** (navigation, snackbar) use `Channel<UiEffect>`

### Theme Design (MD3)

Compose `MaterialTheme` built per MD3 guidelines:

1. **Color** — Map AniTrend brand colors into proper MD3 color roles:
   - `primary` → blue accent (#54A5FA)
   - `secondary` → teal (#009688) or derived from palette
   - `tertiary` → complementary accent
   - Light scheme, Dark scheme, AMOLED Black scheme (third scheme via `MaterialTheme` wrapper)
2. **Typography** — Use MD3 type scale (`display`, `headline`, `title`, `body`, `label`) with `sans-serif-condensed` weight adapted
3. **Shape** — Follow MD3 shape categories (`small`, `medium`, `large`)

## Phases

### Phase 1: Core Data Layer

**Goal**: Apollo Kotlin client + Room database operational alongside the existing app.

**Libraries added**:
| Dependency | Purpose |
|---|---|
| `com.apollographql.apollo:apollo-runtime` | GraphQL client |
| `com.apollographql.apollo:apollo-normalized-cache-api` | In-memory cache |
| `com.apollographql.apollo:apollo-normalized-cache-sqlite` | Persistent cache (replaces ObjectBox for GraphQL data) |
| `com.apollographql.apollo:apollo-runtime-annotations` | Codegen annotations |
| `androidx.room:room-runtime` | Relational DB |
| `androidx.room:room-ktx` | Coroutine helpers |
| `androidx.room:room-compiler` (KSP) | Codegen |

**KSP migration (split across phases)**:
- **Phase 1**: Add KSP plugin. Move Glide compiler from `kapt` to `ksp` (Glide 5.0.5 supports KSP). Add Room compiler via `ksp`. Keep `kotlin-kapt` for ObjectBox.
- **Phase 4**: Remove `kotlin-kapt` entirely. ObjectBox is gone (replaced by Room, and GraphQL data is cached by Apollo).

**Cache strategy**:
- Apollo normalized SQLite cache for GraphQL query/response caching (offline support)
- Room for relational data that doesn't come from GraphQL (user preferences, local ratings, draft comments)
- Apollo `CacheFirst` fetch policy for list screens, `NetworkFirst` for detail screens

**Deliverable**: Apollo client configured, Room database with initial schema, existing app still runs unchanged.

### Phase 2: Compose App Shell

**Goal**: New Compose app that compiles and runs with navigation, theme, and placeholder screens.

**Setup**:
- Add Compose BOM, Compose compiler, MD3, Navigation Compose to `libs.versions.toml`
- Create MD3 `AniTrendTheme` composable (three color schemes)
- Create `NavHost` with route definitions for all major screens
- Wire Koin modules for the new Compose ViewModels
- Wire Apollo + Room into the new module

**Deliverable**: Runnable Compose app with MD3 theme, bottom navigation, and empty screen destinations.

### Phase 3: Feature-by-Feature Vertical Slices

**Goal**: Migrate each feature from old View system to new Compose screens, full stack (data → ViewModel → UI).

**Migration order** (low to high complexity):

| Order | Feature | Key files in old app |
|---|---|---|
| 1 | Splash / Login | `WelcomeActivity` (Onboarder), `LoginActivity` |
| 2 | Main Feed | `MainActivity`, media list adapters, pager |
| 3 | Media Detail | `MediaDetailActivity`, tab fragments, `MediaStatsFragment` → Vico Compose charts |
| 4 | Search | `SearchActivity`, `MaterialSearchView` → MD3 `SearchBar`/`SearchView` |
| 5 | Profile / Settings | `SettingsFragment`, `AboutFragment` |
| 6 | Comments & Social | `BottomSheetComposer`, comment fragment, emoji picker |
| 7 | Video Player | `VideoPlayerActivity` (JiaoZiVideoPlayer → Media3 ExoPlayer) |
| 8 | Markdown rendering | `RichMarkdownTextView` (embed via `AndroidView` in Compose — Markwon stays for its custom AniTrend plugin stack) |
| 9 | Onboarding / Alerter / TapTarget | Remaining utility screens |

**Per-feature pattern**:
1. Define `.graphql` queries if API-driven
2. Define/update Room entities if local data needed
3. Create ViewModel with `StateFlow`
4. Build Compose screen with MD3 components
5. Write Compose UI test
6. Verify against old screen for visual parity

### Phase 4: Cleanup

**Goal**: Delete old `app/` module and all unused dependencies.

**Removals**:
- Entire `app/src/main/java/` (all 402 files)
- All XML layouts (`app/src/main/res/layout/`, etc.)
- AppCompat theme resources (`style.xml`, `color.xml` old values)
- Custom `attr.xml` attributes (replaced by MD3 Compose theme)
- Legacy drawables (keep those still referenced by Compose)
- Version catalog entries for unused libs

**Libraries eliminated** (by migrating to Compose + Apollo + Room):

| Target | Replaced by |
|---|---|
| AppCompat | Compose MD3 |
| SmartTabLayout | Navigation Compose tabs |
| Material Dialogs (0.9.6.0) | MD3 `AlertDialog` |
| CircularProgressView | MD3 `CircularProgressIndicator` |
| PhotoView | Compose pinch-to-zoom `Modifier` |
| Onboarder | Custom Compose onboarding |
| JiaoZiVideoPlayer | Media3 ExoPlayer |
| Alerter | MD3 `Snackbar` |
| MaterialSearchView | MD3 `SearchBar` |
| Tap Target Prompt | Custom Compose overlay |
| MPAndroidChart | Vico Compose charts |
| EventBus | SharedFlow |
| Stream | Kotlin stdlib |
| Txtmark | Removed (Markwon handles all markdown) |
| BetterLinkMovementMethod | Removed (Markwon handles link movement) |
| PrettyTime | `android.icu.text` |
| Markwon (entire 12-artifact set) | **Kept** — embedded in Compose via `AndroidView { RichMarkdownTextView() }` |
| ObjectBox + processor | Room (KSP) |
| Retrofit + Gson | Apollo Kotlin |
| Firebase Core (already unused) | Removed |
| SimpleXML converter | kotlinx-serialization or manual parse |

## Non-goals

- Kotlin Multiplatform (Android-only)
- Screenshot testing (Compose UI tests instead)
- Compose Multiplatform / Desktop / iOS
- Animation overhauls (match existing behavior first; enhance later)
- Rewriting Markwon's custom AniTrend plugin stack (spoilers, mentions, YouTube) in pure Compose — embedded via `AndroidView` instead

## Success criteria

- `./gradlew :app-compose:assembleDebug` passes
- All 10+ features migrated and visually verified
- Old `app/` module deleted
- Zero compilation errors, zero lint errors
- Dependency count reduced from ~30+ to ~15
- Tests pass (Compose UI tests + unit tests)
