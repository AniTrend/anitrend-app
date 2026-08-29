# Navigation refactor full wrap-up audit

**Status:** Accepted Phase 0 / Phase 1 policy record for the wrap-up gate on branch `refactor/navigation-inventory-foundation`. The route-policy decisions in Section 5.2 are user-approved policies recorded in this register; they are not open Phase 1 decisions.
**Date observed:** 17 August 2026
**Writer:** Phase 0 docs writer (static inspection only; no builds, unit tests, instrumentation tests, or emulator runs were executed for this document)
**Validation owner:** Orchestrator reviews this document, runs diff checks, and requests the Oracle gate (Phase 0 / Phase 1 review)

This audit is the single wrap-up artifact for the dirty navigation migration. It
reconciles the current working tree against the existing navigation-refactor
documents, registers every unresolved or under-verified item, and states what
must be true before the branch can be called wrapped up.

---

## 1. Purpose and scope

Purpose:

- Record the observed post-migration architecture from the working tree, not from
  memory or from the migration documents alone.
- Register every known risk, coverage gap, retained boundary, and doc truthfulness
  problem as an auditable item with evidence paths and verification methods.
- Provide dependency-ordered phases and a gate checklist so a later implementer or
  reviewer can close the branch without re-deriving the audit.
- Correct the record where the existing documents claim more than the tree proves.

Scope:

- In scope: `MainActivity` as the single Navigation 2 host, `nav_root.xml`
  destinations, ingress handling, shortcut and notification routes, retained
  activity boundaries, migrated fragment destinations, and the navigation-refactor
  document set.
- Out of scope (recorded, not executed): any source change, test run, build,
  emulator session, or destructive command. This document changes nothing except
  itself.
- Out of scope by design: Compose, Navigation 3, KSP migration, GraphQL codegen
  changes, and the state-synchronization refactor, except where navigation and
  state interact (NFR-007).

---

## 2. Branch and worktree baseline

Observed on 17 August 2026:

- Current branch: `refactor/navigation-inventory-foundation` (verified via `git
  branch --show-current`; no branch switch performed).
- Working tree: 202 dirty entries (uncommitted, counted via `git status
  --porcelain` at this docs-pass time; composition 63 modified, 77 deleted, 62
  untracked). The earlier 198-entry baseline recorded in the previous
  documentation pass predates the Phase 5A saved-state reproduction test and
  the Phase 5B designer-lane tests. The final-review documentation pass
  re-counted the tree at 203 entries (63 modified, 77 deleted, 63 untracked);
  the additional untracked entry is `.kotlin/` (a Gradle/Kotlin build cache
  directory, see Section 8). The re-counted composition is authoritative. This
  is a broad mixed change set, not a finished migration branch.
- Dirty set composition (observed via `git status --porcelain`):
  - Modified production sources: `MainActivity.kt`, `LoginActivity.kt`,
    `Modules.kt`, `NotificationUtil.kt`, `ShortcutUtil.kt`, `IntentBundleUtil.kt`
    is untouched but its consumers changed, list/detail fragments, widgets,
    layouts, menus, `strings.xml`, `shortcuts.xml`, and more.
  - Deleted Kotlin files in the activity tree: 21 files in the
    `view/activity/` tree, comprising 17 deleted production Activities
    (`AboutActivity`, `ChangelogActivity`, `LoggingActivity`, `SettingsActivity`,
    `SharedContentActivity`, `CharacterActivity`, `CommentActivity`,
    `FavouriteActivity`, `MediaActivity`, `MediaBrowseActivity`,
    `MediaListActivity`, `MessageActivity`, `NotificationActivity`,
    `ProfileActivity`, `StaffActivity`, `StudioActivity`, `SearchActivity`)
    and four deleted activity unit tests under
    `app/src/test/.../view/activity/` (`CharacterActivityTest`,
    `MediaActivityTest`, `ProfileActivityTest`, `StaffActivityTest`);
    17 deleted pager infrastructure files, comprising 16 adapters under
    `adapter/pager/` (7 detail, 9 index) plus
    `BaseStatePageAdapter` (`base/custom/pager/`); the
    `nav_settings_graph.xml`; and the pager layouts
    (`activity_pager_generic.xml`, `content_view_pager.xml`).
  - New production sources: `nav_root.xml`, `NavigationDestinations.kt`,
    `MediaFragment.kt`, `MediaSection.kt`, section controllers
    (`MediaOverviewSection` through `MediaReviewSection`), `ProfileFragment.kt`,
    `CharacterFragment.kt`, `StaffFragment.kt`, `StudioFragment.kt`,
    `MessageFragment.kt`, `SharedContentFragment.kt`, `LoggingFragment.kt`,
    `SearchFragment.kt`, `AnimeFragment.kt`, `MangaFragment.kt`, `FeedFragment.kt`,
    `AiringFragment.kt`, `TrendingFragment.kt`, `HubFragment.kt`,
    `FavouriteFragment.kt`, `DetailListSection.kt`, and adapters
    (`CharacterAdapters.kt`, `MediaCharacterAdapter.kt`, `MediaStaffRoleAdapter.kt`,
    `StaffAdapters.kt`, `RecyclerSectionAdapter.kt`).
  - Modified tests: `ArchitectureEnforcementTest.kt`,
    `KoinModuleVerificationTest.kt`, `NavigationArgsTest.kt`,
    `StudioActivityTest.kt`, `FragmentSearchFavouritesSheetsArgsTest.kt`,
    `UserFavouritesViewModelTest.kt`, `FragmentBundleRoundTripTest.kt`,
    `EntryPointFixtures.kt`, `EntryPointRenderAuthTest.kt`,
    `EntryPointRenderUnauthTest.kt`, `ToolbarHomeNavigationTest.kt`,
    `EventBusMutationStateGuardTest.kt`.
  - Deleted tests: `CharacterActivityTest.kt`, `MediaActivityTest.kt`,
    `ProfileActivityTest.kt`, `StaffActivityTest.kt`, `LoggingActivityTest.kt`,
    `FragmentMediaFamilyArgsTest.kt`.
  - New tests: `LoggingFragmentTest.kt` (androidTest),
    `MainActivityExternalIngressTest.kt` (androidTest), `MediaFragmentArgsTest.kt`
    (unit), `MediaFragmentSectionOrderTest.kt` (unit), `ShortcutUtilTest.kt`
    (unit), `DrawerMediaListNavigationTest.kt` (androidTest),
    `LoginShortcutIntentTest.kt` (androidTest),
    `SharedContentDismissalTest.kt` (androidTest),
    `MediaListSavedStateNavigationTest.kt` (androidTest, NFR-007
    two-direction saved-state regression), and an untracked unit test
    directory under `app/src/test/java/com/mxt/anitrend/view/fragment/detail/`
    containing `CharacterFragmentTest.kt`, `StaffFragmentTest.kt`, and
    `ProfileFragmentTest.kt` (NFR-015 section reuse guard).
  - Modified docs: `docs/app-journey-knowledge-graph.md`,
    `docs/architecture/README.md`, `docs/architecture/pr-checklist.md`.
  - New docs: `docs/architecture/navigation-refactor/` (seven files:
    `specification.md`, `migration-inventory.md`, `verification.md`,
    `destination-contracts.md`, `future-work.md`, `agent-task-template.md`,
    and this audit, which was added as the wrap-up record after the first
    six-file inventory).
  - Hygiene noise: modified `.idea/kotlinc.xml`; untracked
    `.artifacts/5dd0b8c5-c718-40a1-b7f4-9deb8321fa79/` (contains
    `implementation_plan.artifact.md` and `walkthrough.artifact.md`); an empty
    sibling directory `.artifacts/ab03d17d-d736-462d-bdac-0b0263f352f0/`
    (invisible to git); untracked `.gradle-home/` (a Gradle user home with
    caches, wrapper dists, daemon state, and jdks); and untracked `.kotlin/`
    (a Gradle/Kotlin build cache directory observed by the final-review
    pass). These paths are unknown/user-owned until the owner approves
    otherwise; see the worktree-admissibility phase (Phase 1).
- No commits on this branch were created by the audit writer.
- No staged entries: every dirty entry is an unstaged worktree modification,
  deletion, or untracked file; nothing is staged in the git index. Recorded as
  a scoped observation from the current index, not independently attributable
  to this session.

---

## 3. Evidence and confidence rules

Confidence taxonomy used throughout this document:

- **Observed:** directly read from the current working tree (code, XML, docs, git
  status). These statements carry file paths and line ranges.
- **Inferred:** strongly suggested by observed code paths but not demonstrated at
  runtime. Inferred items are labeled as such and require the listed verification.
- **Unknown:** cannot be settled from static evidence. Requires a device repro, a
  product decision, or an explicit test run.

Rules:

1. Static claims are preferred over document claims. When the existing
   navigation-refactor documents conflict with the tree, the tree wins and the
   conflict is registered as a doc-correctness item.
2. No claim of passing tests, builds, or device behavior appears in this audit.
   The existing `verification.md` claims compile and spotless passes for an
   earlier state of the tree; those are historical claims, not re-verified here.
3. A code path that "can" misbehave is not a proven bug. NFR-007 was marked
   unproven until its Phase 5A repro; the chip mapping symptom (NFR-008) is
   fixed in the tree and its device confirmation remains an acceptance
   criterion.
4. Every issue has a verification method so the Oracle gate can check closure
   without re-auditing.
5. Untracked paths are unknown by default. Before any source implementation,
   the worktree must pass an admissibility phase (Phase 1): inventory every
   untracked path, preserve unknown/user-owned artifacts by default, require
   explicit owner approval before deleting any of them or adding ignore rules,
    and stage only explicitly owned migration paths. The current untracked set
    (`.artifacts/5dd0b8c5-c718-40a1-b7f4-9deb8321fa79/`, the empty
    `.artifacts/ab03d17d-d736-462d-bdac-0b0263f352f0/`, `.gradle-home/`,
    `.kotlin/`) is recorded here as observed, not as approved for deletion or
    ignoring.

---

## 4. Observed current architecture

### 4.1 Host and graph

- `MainActivity` (app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt)
  is the single application window and hosts one `NavHostFragment` at
  `R.id.main_nav_host` (lines 115-116). It still extends `CommonActivity`
  (lines 93-96) and carries the drawer, toolbar, search bridge, and ingress
  routing.
- `nav_root.xml` (app/src/main/res/navigation/nav_root.xml) contains 28 fragment
  destinations (counted `<fragment` tags) as direct children of the root graph,
  with `app:startDestination="@id/animeFragment"` (line 5) and 25 global actions
  (lines 174-198). 28 fragment destinations is the controlling observed count;
  the earlier 27 count is retired. Companion docs must be checked in Phase 5
  for consistency (see NFR-016 and unresolved questions).
- Settings destinations are registered in the root graph (lines 135-172) because
  Navigation 2 resolves ids from the current graph; the former
  `nav_settings_graph.xml` was deleted.
- Deleted Kotlin files in the activity tree: 21 files, comprising 17 deleted
  production Activities and four deleted activity unit tests, plus 17 pager
  infrastructure files (16 adapters under `adapter/pager/` plus
  `BaseStatePageAdapter`). Remaining production `ViewPager2` use is
  onboarding-only (`WelcomeActivity`).

### 4.2 Destination inventory

28 destinations, grouped:

- Root index tabs: `animeFragment`, `mangaFragment`, `feedFragment`,
  `trendingFragment`, `airingFragment`, `hubFragment`, `reviewFragment`.
- List/browse: `mediaBrowseFragment`, `watchListFragment`, `mediaListFragment`,
  `searchFragment`.
- Detail: `mediaFragment`, `characterFragment`, `staffFragment`,
  `studioFragment`, `profileFragment`, `commentFragment`, `messageFragment`,
  `notificationFragment`, `favouriteFragment`.
- Utility: `aboutFragment`, `changelogFragment`, `loggingFragment`,
  `sharedContentFragment`.
- Settings: `settingsHubFragment`, `accountSettingsFragment`,
  `customizeSettingsFragment`, `settingsCategoryFragment`.

### 4.3 Navigation options

- Non-root routes use `destinationOptions()`:
  `launchSingleTop(true)` + `restoreState(true)`
  (NavigationDestinations.kt lines 211-214).
- Root tab routes use `rootDestinationOptions()`:
  `launchSingleTop(true)` + `restoreState(true)` + `popUpTo(animeFragment,
  inclusive = false, saveState = true)` (lines 216-219).
- `navigateToMediaList` (lines 102-118) now carries the route-origin contract:
  it selects `rootDestinationOptions()` when the caller passes
  `MediaListOrigin.ROOT` and `destinationOptions()` otherwise (line 116).
  `navigateToRootMediaList` (lines 120-124) funnels into the same body with
  `ROOT`; pushed flows (profile statistics, `ROUTE_MEDIA_LIST` ingress,
  `/user/<name>/animelist|mangalist` chains) call it with the default `PUSHED`
  origin (NFR-002).

### 4.4 Ingress

- Manifest (app/src/main/AndroidManifest.xml lines 47-98): `MainActivity` is
  `singleTop`, exported, with `ACTION_VIEW` filters for
  `/activity.*`, `/studio.*`, `/character.*`, `/staff.*`, `/actor.*`,
  `/anime.*`, `/manga.*`, `/user.*` on `anilist.co`, plus an `ACTION_SEND`
  text/plain filter.
- Seven Activity entries remain (lines 33-131): `SplashActivity`,
  `MainActivity`, `LoginActivity`, `WelcomeActivity`, `ImagePreviewActivity`,
  `GiphyPreviewActivity`, `VideoPlayerActivity`.
- `MainActivity.handleExternalRoute` (lines 654-822) switches on
  `EXTRA_ROUTE` string routes (`settings`, `notifications`, `messages`,
  `logging`, `comment`, `studio`, `character`, `staff`, `profile`,
  `media_list`, `favourites`, `search`, `media`, `feed`, `media_browse`, plus
  the new `airing` and `trending` constants at lines 1018-1019) and falls back
  to `handleExternalUriRoute` deep-link matching (lines 830-879).
- `handleExternalUriRoute` handles the `/user/<name>/animelist|mangalist`
  chain (lines 855-868): `resolveUserRoute` decides the landing, Profile is
  navigated first, and a typed media list is pushed on top with the media type
  from `arg_mediaType`; the dispatch count (1 or 2) drives `isExternalEntry`
  clearing (NFR-001, NFR-003).
- `IntentBundleUtil.injectIntentParams` (IntentBundleUtil.kt lines 40-113)
  decodes AniList URIs into legacy extras before the route switch runs.
- `SharedContentFragment` receives `ACTION_SEND` payloads through
  `MainActivity` (lines 656-660) with `SharedContentFragment.arguments(intent)`
  (SharedContentFragment.kt lines 216-227).

### 4.5 Top-level classification and back handling

- `isTopLevelDestination` (MainActivity.kt lines 465-478) treats 8
  destinations as top-level unconditionally and `mediaListFragment` as
  top-level only when the current entry carries `MediaListOrigin.ROOT`
  (lines 474-477, via `isRootOriginMediaList`, lines 486-488). Pushed media
  lists keep caller-back semantics (NFR-002).
- Top-level back shows the exit-confirm toast (lines 440-448); non-top-level
  back calls `navigateBackFromDestination` (lines 457-463), which either
  `finish()`es (when `isExternalEntry`) or `navigateUp()`s.
- `isExternalEntry` is set on cold start from `isTaskRoot` plus intent action
  and data (line 288), restored from saved state (line 290), recomputed on
  `onNewIntent` and then cleared (lines 307-318), and cleared after the
  initial ingress dispatch via `externalEntryAfterDispatch` (line 399). The
  warm `onNewIntent` path never arms external finish semantics (NFR-003).
- Toolbar: the destination listener applies `destination.label` for every
  destination, including root switches and restored back-stack entries, so a
  previous title cannot leak into the next root landing (MainActivity.kt lines
  189-195; see NFR-010).

### 4.6 Retained bridges

- Live search bridge: `applySearchToAllListFragments` (MainActivity.kt lines
  530-534) reaches `FragmentBaseList` instances through
  `supportFragmentManager.fragments`.
- KeyUtil request routing: `KeyUtil.*_REQ` style routing remains in
  `MediaFormatViewModel.kt` (line 91), `AiringListViewModel.kt` (line 128),
  `BrowseReviewViewModel.kt` (line 113), `MediaBrowseFragment.kt`, and others.
- Legacy fragment bases: `FragmentBaseList` is extended by `MediaListFragment`
  (line 49), `SearchFragment` (line 64), `MediaBrowseFragment`,
  `AiringListFragment`, `FeedListFragment`, `SuggestionListFragment`,
  `WatchListFragment`. `RecyclerViewAdapter` is still used by `SearchFragment`
  (import at line 27). `RecyclerSectionAdapter` is new.
- `MediaFragment` uses `childFragmentManager` only for the YouTube preview
  surface (MediaFragment.kt lines 421-428), documented as an embedded platform
  player, not navigation.

### 4.7 Fragment-level state patterns

- `MediaListFragment.onCreate` reads `mediaType` and `statusIn` from arguments
  (MediaListFragment.kt lines 128-131) and derives the screen title from
  `mediaType` (lines 300-307).
- `MediaFragment` builds section views in `sectionViewOrder` (lines 87-96,
  167-169) and selects them by `sectionViewOrder.indexOf` (line 320); creation
  and selection share one list, so the NFR-008 drift is no longer present in
  the tree.
- `ProfileFragment` re-renders on profile state success (line 228), but
  `renderSection` is guarded by `shouldRebuildSection`, so the active section
  is not re-inflated while the requested section is already rendered
  (ProfileFragment.kt lines 254-256, 299, 377). See NFR-015.
- `SharedContentFragment` dismisses through the NavController with a guarded
  `findNavController().popBackStack()` when the bottom sheet reaches
  `STATE_HIDDEN` (SharedContentFragment.kt lines 55, 161-166). See
  NFR-005/NFR-011.

---

## 5. Prioritized issue register

Legend: Severity C = critical, H = high, M = medium, L = low.
Confidence O = observed, I = inferred, U = unknown (needs repro or decision).
"Evidence" lists paths and line ranges in the current tree.

### 5.1 High-risk navigation state

NFR-007 was reproduced and fixed for root drawer switching; the
rotation/process-death follow-up and the PUSHED-origin saved-entry interaction
remain open. NFR-008 was fixed in the current tree with a unit assertion; see
its entry for the superseded evidence and the remaining execution gates.

#### NFR-007 Media list mediaType can be restored stale from Navigation 2 saved state

- Severity: C. Confidence: mechanism O, bug O (reproduced), fix O in the tree.
- Original evidence (superseded):
  - `navigateToMediaList` always writes `arg_mediaType` (when provided) plus
    `ARG_MEDIA_LIST_ORIGIN` and the legacy `ARG_UNIFIED_DESTINATION` flag
    (NavigationDestinations.kt lines 112-128).
  - Drawer My Anime/My Manga call `navigateToRootMediaList` with `ANIME` or
    `MANGA` (MainActivity.kt lines 547-560), which used
    `rootDestinationOptions()` with `popUpTo(... saveState = true)` +
    `restoreState(true)` (NavigationDestinations.kt lines 244-248).
  - `MediaListFragment.onCreate` reads `mediaType = args.getString(arg_mediaType)`
    (MediaListFragment.kt line 151) and loads and titles from it (lines 327,
    300-307).
- Repro (recorded, Phase 5A): on the Custom_API_36 emulator, real drawer flow,
  both directions: My Anime -> Feed -> My Manga restores the media-list
  destination with `mediaType=ANIME` instead of the newly selected `MANGA`,
  and My Manga -> Feed -> My Anime restores `mediaType=MANGA` instead of
  `ANIME`. Destination and `MediaListOrigin.ROOT` assertions pass at the
  failing step, proving the stale value comes from Navigation 2 saved-state
  restoration rather than an incorrect destination or origin. The new
  `MediaListSavedStateNavigationTest` (androidTest) failed 2/2 with the
  expected stale-argument assertions before the source fix. The first
  connected attempt hit a stale generated R jar and a disconnected emulator;
  `generateAppDebugRFile --rerun-tasks` and a cold AVD restart recovered the
  environment (infrastructure conditions, not source failures).
- Fix (applied in the current tree): a media-list-specific root `NavOptions`
  policy. ROOT media-list navigation keeps `launchSingleTop` and the existing
  `popUpTo(... saveState = true)` behavior but disables `restoreState`
  (`mediaListRootDestinationOptions`, NavigationDestinations.kt lines 258-262,
  selected at line 126), so every drawer My Anime/My Manga navigation applies
  the newly selected media type instead of resurrecting the saved entry's
  arguments. Other root destinations and PUSHED-origin routes retain their
  existing restoration policies.
- Status: **reproduced and fixed for root drawer switching**. The two-direction
  saved-state regression test passes on the recovered emulator. Remaining open
  by explicit distinction: rotation/process-death coverage for the media list
  (manual follow-up, part of AC-EMU-02 and G-013), and the PUSHED-origin
  saved-entry interaction, which is recorded as a separate risk to validate
  rather than silently changed.
- Infrastructure status: the ADB-inaccessibility incident that blocked the
  original repro (`docs/bugs/social-interaction-regression-evidence.md`
  lines 252-253) was resolved by a cold AVD restart; the repro and the
  regression test run were recorded on the recovered emulator. AC-EMU-01
  remains explicitly infrastructure-blocked (see its entry in Section 6.3);
  no other emulator acceptance criterion has recorded evidence.
- Approved policy (user-approved): reproduce-first. The device repro
  (AC-EMU-02) had to be recorded before any remediation was chosen; it is
  recorded, and the remediation above is the applied outcome. The media type
  decision remains argument-driven (MediaListFragment.kt line 151).
- Verification: the deterministic two-direction assertion is
  `MediaListSavedStateNavigationTest` (AC-INS-08); the remaining
  rotation/process-death and PUSHED-origin matrix is part of AC-EMU-02 and
  G-013 (FW-002 depends on a stable emulator or device test setup).

#### NFR-008 Media section chips can map to the wrong content

- Severity: H. Confidence: original mismatch O, **fixed in the current tree**.
- Original evidence (superseded):
  - `MediaSection.entries` order: OVERVIEW, RELATIONS, RECOMMENDATIONS, STATS,
    CHARACTERS, STAFF, FEED, REVIEWS (MediaSection.kt lines 6-15).
  - The first audit pass observed `MediaFragment.onCreateView` adding container
    children in a different order (overview, stats, relation, staff, character,
    recommendations, feed, review) and `selectSection` indexing children by
    `MediaSection.entries.indexOf`, which mismatched RELATIONS,
    RECOMMENDATIONS, STATS, and STAFF.
- Current tree (observed at docs-pass time): the mismatch is removed.
  `MediaFragment` now exposes one canonical `sectionViewOrder` list (lines
  87-96) that drives both child creation (`sectionViewOrder.forEach`, lines
  167-169) and selection (`sectionViewOrder.indexOf`, line 320); the order is
  identical to `MediaSection.entries`. The fix is asserted by the unit test
  `MediaFragmentSectionOrderTest` (`sectionViewOrder` equals `entries`, covers
  all eight sections once, and visibility sets preserve the order), which is
  the AC-STAT-05 assertion for this item.
- Status: **fixed in the tree and asserted by the unit test
  `MediaFragmentSectionOrderTest`; the focused unit execution (4 section-order
  tests and 5 argument tests, plus `spotlessCheck`) was recorded in the Phase 3
  validation**. The emulator chip matrix (AC-EMU-01) remains explicitly
  infrastructure-blocked (see AC-EMU-01 in Section 6.3), so the user-visible
  confirmation on device is still a recorded acceptance criterion rather than
  claimed evidence.
- Verification: run the unit suite (AC-STAT-03, AC-STAT-05), then the visual
  emulator check per chip (AC-EMU-01) once a stable emulator or device is
  available.

### 5.2 High-risk ingress

#### NFR-001 `/user/<name>/animelist` and `/mangalist` deep links lose the media type

- Severity: H. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - `IntentBundleUtil` writes `arg_mediaType` for `animelist`/`mangalist`
    subpaths (IntentBundleUtil.kt lines 62-65).
  - The first audit pass observed `MainActivity.handleExternalRoute` routing
    every `/user` path to `navigateToProfile(UserScreenParam(...))` and never
    reading `arg_mediaType`; `ProfileFragment` never read it either.
- Current tree (observed at docs-pass time): the route decision is implemented
  per the approved policy. `handleExternalUriRoute` resolves `/user` paths
  through `resolveUserRoute` (MainActivity.kt lines 855-868), lands on Profile,
  and pushes a typed media list carrying `arg_mediaType` with
  `MediaListOrigin.PUSHED`, preserving Profile beneath the list. Cold-start
  instrumentation tests assert the landing destination, the media type, and
  the pushed origin for `animelist`, `mangalist`, plain `/user`, and numeric
  user ids (`MainActivityExternalIngressTest`: `coldStartUserAnimeListLinkLandsOnProfileThenPushesTypedMediaList`,
  `coldStartUserMangaListLinkPushesTypedMediaList`,
  `coldStartPlainUserLinkLandsOnProfileOnly`,
  `coldStartNumericUserAnimeListLinkPreservesNumericUserId`,
  `coldStartNumericUserMangaListLinkPreservesNumericUserId`).
- Status: **implemented in the tree with focused instrumentation coverage;
  execution of the instrumentation suite is part of the connected-test gate
  (AC-INS-01) and has not been re-run by the docs pass**. The warm
  `/user/<name>/animelist` deep-link case remains an acceptance criterion
  (G-003, AC-INS-01) rather than claimed evidence.
- Approved policy (user-approved): `/user/<name>/animelist` and `/mangalist`
  land on the profile and push a typed media list, preserving Profile beneath
  the list (origin assignment in the NFR-002 producer table). The profile media
  tab is not the landing target; the pushed media list carries the media type
  from `arg_mediaType`.
- Verification: run the instrumentation suite (AC-INS-01, G-003); the
  cold-start assertions exist in the tree, and the warm deep-link case is
  covered by the same acceptance criterion.

#### NFR-002 `mediaListFragment` is treated as top-level even when pushed

- Severity: H. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed `isTopLevelDestination` including
    `mediaListFragment` unconditionally and `ARG_UNIFIED_DESTINATION` written
    for root and pushed routes alike.
- Current tree (observed at docs-pass time): the route-origin contract is
  implemented. `MediaListOrigin` (`ROOT`/`PUSHED`) and the stable wire key
  `ARG_MEDIA_LIST_ORIGIN` are defined in MediaListFragment.kt (lines 49-100);
  `NavigationArgs.resolveMediaListOrigin` defaults absent or unknown values to
  `PUSHED` (NavigationArgs.kt line 70); `navigateToMediaList` selects root or
  detail destination options from the origin (NavigationDestinations.kt lines
  102-124); and `isTopLevelDestination` gates `mediaListFragment` on
  `MediaListOrigin.ROOT` (MainActivity.kt lines 465-488). Drawer My Anime/My
  Manga are the only `ROOT` producers; profile stats, `ROUTE_MEDIA_LIST`
  ingress, shortcuts, and the deep-link chain push by default. Focused
  instrumentation tests assert the contract per producer:
  `DrawerMediaListNavigationTest` (root origin and exit-confirm back),
  `MainActivityExternalIngressTest.coldStartMediaListRouteEntryIsPushedAndBacksUpWithoutFinishing`,
  `LoginShortcutIntentTest` (post-login continuation), and the rewritten
  `ToolbarHomeNavigationTest` (toolbar up and system back from the pushed
  list).
- Status: **implemented in the tree with focused instrumentation coverage;
  execution is part of the connected-test gate (AC-INS-01, AC-INS-02) and the
  emulator back matrix (AC-EMU-03), which has no recorded device evidence (see
  the Section 6.3 infrastructure note)**. No claim of passing execution is made
  by this docs pass.
- Approved policy (user-approved): a distinct route-origin contract with these
  producer-to-origin assignments:

  | Producer | Approved origin |
  | --- | --- |
  | Drawer My Anime / My Manga | Root |
  | Profile stats | Pushed |
  | `/user/<name>/animelist` and `/mangalist` | Pushed, preserving Profile beneath the list |
  | Media-list shortcuts | Pushed/non-root by default; an explicit post-login continuation intentionally handled as a root destination is permitted only as a deliberate exception, and that post-login case is flagged for implementation verification rather than silently choosing a different policy |
  | `ROUTE_MEDIA_LIST` ingress | Pushed/non-root by default |
  | Post-login shortcut continuation | Explicitly checked during implementation against the chosen contract, not inferred |

  The existing `ARG_UNIFIED_DESTINATION` flag is not the origin contract: it is
  written for root and pushed routes alike and cannot distinguish them. Any
  producer whose policy is not settled at implementation time is an
  implementation verification gate, not silently resolved. The post-login
  continuation is now explicitly checked in the tree: `LoginShortcutIntentTest`
  asserts the produced shortcut intent carries `ROUTE_MEDIA_LIST` with
  `PUSHED` origin and caller-back semantics, matching the approved contract.
- Verification: run the instrumentation suite (AC-INS-01, AC-INS-02) and the
  emulator back-behavior matrix per entry path (AC-EMU-03) once a stable
  emulator or device is available.

#### NFR-003 `isExternalEntry` is sticky and can finish the task after internal navigation

- Severity: H. Confidence: original logic O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed `isExternalEntry` set once on cold start,
    restored from saved state, and never reset after the first back or after
    any internal navigation.
- Current tree (observed at docs-pass time): the approved policy is
  implemented. Warm `onNewIntent` delivery never arms external finish
  semantics and clears the flag after dispatch (MainActivity.kt lines 307-318);
  `handleExternalRoute` returns the dispatch count (1 for a direct landing, 2
  for an ingress followed by internal navigation), and
  `externalEntryAfterDispatch` clears the flag after a follow-up dispatch
  (line 399, helper at lines 1085+). Cold-start instrumentation tests assert
  both sides of the contract: direct external ingress finishes on back
  (`coldStartAnimeLinkBackFinishesExternalTask`,
  `coldStartActivityLinkBackFinishesExternalTask`) and the external
  user-list chain backs through Profile to the root without finishing
  (`externalUserListBackChainReturnsToProfileThenRootWithoutFinishingTask`).
- Status: **implemented in the tree with focused instrumentation coverage;
  execution is part of the connected-test gate (AC-INS-02) and the emulator
  repro (AC-EMU-05), which has no recorded device evidence (see the Section
  6.3 infrastructure note)**. No claim of passing execution is made by this
  docs pass.
- Approved policy (user-approved): once internal navigation occurs, external
  finish semantics are cleared; returning to the original ingress destination
  does not silently re-arm the flag. `isExternalEntry` is cleared on the first
  internal navigation, and arriving at the ingress destination again is not a
  re-arm trigger.
- Verification: run the instrumentation suite (AC-INS-02) and the emulator
  repro (deep link, navigate internally, back) once a stable emulator or
  device is available (AC-EMU-05, G-002).

#### NFR-004 Airing, Feed, and Trending dynamic shortcuts have no route producer

- Severity: H. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed `ShortcutUtil` building `SHORTCUT_AIRING`,
    `SHORTCUT_FEEDS`, and `SHORTCUT_TRENDING` with no `EXTRA_ROUTE` extra and
    no data URI to match.
- Current tree (observed at docs-pass time): the approved scope is implemented
  in `ShortcutUtil`, not in a new utility. `routeForShortcutType` maps all
  eight shortcut types to `MainActivity.EXTRA_ROUTE` values, including
  `ROUTE_AIRING`, `ROUTE_FEED`, and `ROUTE_TRENDING` (ShortcutUtil.kt lines
  35-48), and the producer writes the route extra (lines 147-158). The consumer
  branches exist in `MainActivity.handleExternalRoute` (`ROUTE_FEED` line 793,
  `ROUTE_AIRING` line 798, `ROUTE_TRENDING` line 803), with the new constants
  declared at lines 1017-1019. Unit coverage exists for the producer table,
  wire values, and budget-aware registration (`ShortcutUtilTest`), and
  `LoginShortcutIntentTest` covers the produced-intent contract cold launch for
  the media-list shortcuts.
- Status: **implemented in the tree with unit and focused instrumentation
  coverage; execution is part of the unit and connected-test gates
  (AC-STAT-05, AC-INS-01) and the emulator shortcut launches (AC-EMU-04),
  which has no recorded device evidence (see the Section 6.3 infrastructure
  note)**. No claim of passing execution is made by this docs pass.
- Approved scope (user-approved): extend `ShortcutUtil`, not create a new
  utility. Producer work: add `EXTRA_ROUTE` producers for Airing (`airing`),
  Feed (`feed`), and Trending (`trending`) on `SHORTCUT_AIRING`,
  `SHORTCUT_FEEDS`, and `SHORTCUT_TRENDING`. Consumer work: add the matching
  route constants and consumer branches in `MainActivity.handleExternalRoute`.
- Verification: run the unit suite (AC-STAT-05) and the instrumentation suite
  (AC-INS-01), then emulator dynamic shortcut launches (AC-EMU-04, G-004) once
  a stable emulator or device is available.

#### NFR-005 Shared content dismissal changed the activity-finish boundary and is untested

- Severity: H. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed dismissal via
    `parentFragmentManager.popBackStack()` on bottom-sheet `STATE_HIDDEN` with
    no coverage of the dismissal boundary.
- Current tree (observed at docs-pass time): `SharedContentFragment` now
  dismisses through the NavController with a guarded `findNavController()
  popBackStack()` (SharedContentFragment.kt line 165, `runCatching` at
  lines 161-166); the fragment-level pop is gone. `SharedContentDismissalTest`
  (androidTest) covers landing, hide dismissal via the Material `STATE_HIDDEN`
  transition, system-back dismissal, and the resulting back stack and task
  state (two tests, `warmShareIntentHideDismissalReturnsToPreviousDestination`
  and `systemBackDismissesSharedContentToPreviousDestination`).
- Status: **implemented in the tree with focused instrumentation coverage;
  execution is part of the connected-test gate (AC-INS-03) and the emulator
  share flow (AC-EMU-10), which has no recorded device evidence (see the
  Section 6.3 infrastructure note)**. No claim of passing execution is made by
  this docs pass.
- Verification: run the instrumentation suite (AC-INS-03), then the emulator
  share flow (dismiss via hide, back, toolbar close) once a stable emulator or
  device is available (AC-EMU-10).

#### NFR-011 Shared content pops the fragment manager, not the NavController

- Severity: H. Confidence: original mechanism O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed `parentFragmentManager.popBackStack()` at
    SharedContentFragment.kt line 55 while every other destination used
    NavController navigation.
- Current tree (observed at docs-pass time): dismissal goes through
  `findNavController().popBackStack()` (SharedContentFragment.kt lines
  161-166). The desynchronization hypothesis is addressed by construction, and
  `SharedContentDismissalTest` asserts the NavController back stack and
  FragmentManager back stack return to the start-destination shape after both
  hide and system-back dismissal (AC-INS-03).
- Status: **implemented in the tree with focused instrumentation coverage;
  execution is part of the connected-test gate (AC-INS-03)**. The restoration
  scenario (process death and `restoreState` while the sheet is open) remains
  an acceptance criterion (G-006, AC-INS-03) rather than claimed evidence.
- Verification: run the instrumentation suite (AC-INS-03), plus a restoration
  scenario once a stable emulator or device is available; if that repro
  confirms a desync, the dismissal policy is already NavController-based and
  only the repro evidence would need recording.

#### NFR-006 `ToolbarHomeNavigationTest` is stale

- Severity: M. Confidence: original defect O, **resolved in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed the test asserting toolbar home finishes the
    activity for the "MediaListFragment" entry point, which the deleted
    Activity boundary could no longer satisfy.
- Current tree (observed at docs-pass time): the test was rewritten against the
  NavController behavior. `ToolbarHomeNavigationTest` now drives the production
  toolbar navigation listener through `Toolbar.getNavButtonView` and asserts
  that toolbar up and system back from the pushed media list return to the
  caller beneath the list without finishing the task
  (`pushedMediaList_toolbarUpReturnsToPreviousDestinationWithoutFinishingTask`,
  `pushedMediaList_systemBackReturnsToPreviousDestinationWithoutFinishingTask`).
- Status: **resolved in the tree (rewritten, not deleted); execution is part of
  the connected-test gate (AC-INS-04)**. No claim of passing execution is made
  by this docs pass.

### 5.3 UX and behavior findings

#### NFR-009 Search has no actionable query input

- Severity: M. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - The first audit pass observed `SearchFragment` reading the query only from
    `arg_search` with no query-entry control in the fragment or its layout.
- Current tree (observed at docs-pass time): the fragment-local input decision
  was made. `SearchFragment` now owns a `TextInputEditText`
  (`R.id.search_query_input`, SearchFragment.kt lines 137-161) with a
  TextWatcher, IME search action and enter-key submission, and
  `submitQuery()`/`normalizeSubmittedQuery` (lines 301-302); the query is
  restorable from saved instance state and the legacy `arg_search` extra
  (lines 110-113). Unit coverage exists for the legacy query resolution and
  normalization in `FragmentSearchFavouritesSheetsArgsTest`.
- Status: **implemented in the tree; execution of the unit coverage is part of
  the static and unit gate (AC-STAT-03) and has not been re-run by the docs
  pass**. The emulator search flow (AC-EMU-07, G-009) remains an acceptance
  criterion with no recorded device evidence (see the Section 6.3
  infrastructure note).
- Verification: run the unit suite (AC-STAT-03), then the emulator search flow:
  submit from toolbar, shortcut, and route; query can be changed and re-run on
  the search destination (AC-EMU-07, G-009).

#### NFR-010 Root toolbar titles have no clear assignment

- Severity: M. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded):
  - The destination listener set the toolbar title only for non-top-level
    destinations; top-level destinations never assigned a title from the
    listener, so the graph labels (`drawer_title_*`) were available but unused
    for them.
  - `selectedTitle`/`KeyUtil.key_navigation_title` were saved and restored
    (MainActivity.kt lines 404-424) but never applied to the toolbar.
- Current tree (observed at docs-pass time): graph destination labels own the
  toolbar titles. The destination listener applies `destination.label` for
  every destination, including root switches and restored back-stack entries,
  so a previous title cannot leak into the next root landing (MainActivity.kt
  lines 189-195). `MediaListFragment` still titles itself via
  `activity?.setTitle` (MediaListFragment.kt line 327), which now agrees with
  the graph label for the same screen.
- Status: **implemented in the tree (Phase 5B designer lane); the emulator
  title matrix (AC-EMU-06, G-008) remains an acceptance criterion with no
  recorded device evidence**.
- Verification: emulator title matrix across all 8 top-level destinations,
  including switch-back cases (AC-EMU-06, G-008).

#### NFR-012 Error states lack inline message and retry on several screens

- Severity: M. Confidence: O for the named primary-load site; breadth U.
- Evidence:
  - `MediaFragment` primary-load error is rendered through
    `binding.mediaStateLayout.showError(...)` with an inline try-again action:
    the error branch shows the error drawable and message plus
    `actionText = getString(R.string.try_again)` and an action listener that
    returns to loading and calls `loadMedia()` again (MediaFragment.kt lines
    367-378). The primary-load surface is a `ProgressLayout`
    (`fragment_media.xml` line 2), and its `showError` contract accepts a
    drawable, message, action text, and action listener (ProgressLayout.kt
    lines 99-125).
  - Existing ProgressLayout and primary-load coverage: `ProgressLayoutStateTests`
    (unit) asserts the loading/error/content transition model;
    `ProgressLayoutInstrumentationTest` (androidTest) asserts the error overlay,
    the error action button visibility and text, and the action click
    (`progressStateErrorAction.performClick()`), plus error-to-content and
    repeated-transition restoration; `MediaFragmentArgsTest` asserts
    `primaryLoadPresentation` maps Loading, Success, and Error states.
  - What is not covered: a direct fragment-level UI assertion that
    `MediaFragment`'s error state renders the ProgressLayout error surface
    with the retry action wired to a reload. Recorded as a bounded follow-up
    under AC-EMU-08 and G-010, not as evidence that the primary-load error
    handling is absent.
  - Which other migrated destinations lack inline retry still needs a
    per-destination sweep; the base list fragments use `stateLayout` retry,
    and the unified detail sections render inline errors (for example
    `DetailListSection.renderError`, lines 109-119).
- Status: **primary-load error handling implemented in the tree and cited
  above; the missing direct fragment-level UI assertion is a bounded follow-up
  (AC-EMU-08, G-010), and the remaining per-destination sweep stays open**.
  The inventory-first bounded approach (AC-EMU-08) still applies: first
  inventory the recoverable error-state gaps per migrated destination, then
  fix that inventoried subset with recorded evidence; do not promise every
  destination upfront.
- Verification: per-destination error injection pass on emulator; document the
  retry surface per destination; add the fragment-level error-render assertion
  for MediaFragment as the bounded follow-up.

#### NFR-013 Profile message actions are not auth-gated

- Severity: M. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded): `action_message` showed the message composer
  to any user when the target was not the current user, without checking
  `settings.isAuthenticated`; only notification/settings items were gated.
- Current tree (observed at docs-pass time): the message action is now gated.
  `menu.findItem(R.id.action_message).isVisible = settings.isAuthenticated`
  (ProfileFragment.kt line 154) hides it when unauthenticated, and the handler
  returns early when unauthenticated (line 165). `onLongPressMedia` continues
  to gate on auth (lines 334-335).
- Status: **implemented in the tree (Phase 5B designer lane)**. The
  device-level confirmation remains AC-EMU-09 / G-011; AC-INS-06 records the
  final menu visibility gate execution.
- Verification: emulator unauth profile; the message action is hidden when
  unauthenticated (AC-EMU-09), matching the auth menu visibility acceptance
  criteria.

#### NFR-014 Chart, theme, tab, card, and accessibility labels diverge from the M3 design system

- Severity: M. Confidence: O for churn, I for mismatch.
- Evidence: `ProfileStatsWidget.kt`, `AboutPanelWidget.kt`,
  `StatusContentWidget.kt`, `AvatarIndicatorView.kt`, and their layouts are
  modified in this change set; `ProfileFragment` uses a `TabLayout`
  (ProfileFragment.kt lines 201-218), and `MediaFragment` builds chips by hand
  (lines 302-325) without design tokens.
- Note: `@DESIGN.md` is the app-wide M3 source of truth; it is updated in the
  same change only when the design pass produces an actual design decision
  change (new component pattern, revised token usage, updated spacing
  convention), per AC-EMU-11.
- Verification: manual design review against `@DESIGN.md` (colors, themes, tabs,
  cards, chips, accessibility labels), with a checklist recorded in the docs
  pass.

#### NFR-015 Profile rebuilds sections on resume

- Severity: M. Confidence: original defect O, **fixed in the current tree**.
- Original evidence (superseded): `onStart` called `renderSection()` when a
  model existed, and `renderSection` cleared and re-inflated the active section
  and restarted its observers; `observeProfile` also re-rendered on every state
  emission.
- Current tree (observed at docs-pass time): `renderSection` is guarded by
  `shouldRebuildSection(renderedSection?.name, section.name)` and returns early
  when the requested section is already rendered (ProfileFragment.kt lines
  254-256, 377); the unconditional `onStart` re-render is gone, and
  `renderedSection` tracks the last inflated section (line 299).
- Status: **implemented in the tree (Phase 5B designer lane) with unit coverage
  in `ProfileFragmentTest` (two tests asserting the reuse guard)**. The
  device-level duplicate-request check remains AC-EMU-09 / G-013 / FW-006.
- Verification: emulator with network logging (navigate away and back, observe
  request counts) per AC-EMU-09; the unit guard is `ProfileFragmentTest`.

#### NFR-016 Journey docs and migration documents overstate completion

- Severity: L. Confidence: O.
- Evidence:
  - `docs/app-journey-knowledge-graph.md` is modified in this change set but
    still references removed Activities and flows (journey graph stale).
  - `verification.md` phrases acceptance criteria as completed checks
    ("Opens from every known internal caller", lines 145-179) and claims
    compile/spotless evidence (lines 54-55) that this audit cannot re-confirm;
    `future-work.md` (lines 44-51) already concedes the targeted-check boundary.
  - `destination-contracts.md` (lines 85-87) claims "Cold-start fixtures now
    cover every AniList URI route, and the Android test suite includes a warm
    `singleTop` route assertion". The actual test evidence in the current tree:
    `MainActivityExternalIngressTest` contains the two warm assertions
    (`warmSingleTopIntentRoutesThroughTheExistingRootHost`, asserting
    `mediaFragment` after a warm `/anime/1` route, and
    `warmShareIntentRoutesThroughTheSharedContentDestination`, asserting
    `sharedContentFragment` after a warm `ACTION_SEND`, both invoked via the
    `onNewIntent` seam) plus nine newer focused tests that added cold-start
    coverage for `/user/<name>/animelist`, `/user/<name>/mangalist`, plain
    `/user`, numeric user ids, `/anime/1` and `/activity/1` back semantics,
    and the `ROUTE_MEDIA_LIST` entry. Cold-start coverage for the remaining
    manifest paths (`/studio`, `/character`, `/staff`, `/actor`, `/manga`,
    notification summary) is still an acceptance criterion (G-001, AC-INS-01),
    and no test in the tree covers the warm `/user/<name>/animelist` case
    (G-003). Any claim of complete route coverage must be compared against
    this evidence and restated as the observed assertions plus an acceptance
    criterion for the missing paths.
  - `migration-inventory.md` states "Status: Complete internal-destination
    migration inventory" (line 3) and marks per-wave slices "complete"
    (lines 14, 201, 230, 352, 638); those claims are part of the truthfulness
    pass, not evidence of verified behavior.
  - `verification.md` claims 24 Activity baseline and 7 current Activities
    (lines 14-16), which matches the manifest; the companion docs must be
    reconciled to the observed 28 fragment destinations, and the earlier 27
    count is retired.
- Status: **addressed by this documentation pass (Phase 5)**. The route-coverage
  claim in `destination-contracts.md` is restated against the observed test
  set; `verification.md`, `migration-inventory.md`, `specification.md`,
  `future-work.md`, and `agent-task-template.md` are reconciled; the journey
  graph is updated to Fragment destinations and MainActivity ingress. The
  compile/spotless claims remain historical ("recorded at the time", 16 August
  2026) and are not re-verified by this pass. A final-review documentation
  pass then reconciled the remaining stale register wording: NFR-007's
  reproduced-and-fixed drawer-switch status with rotation/process-death and
  PUSHED-origin kept as explicit follow-up, NFR-010 title ownership by graph
  labels, NFR-012 primary-load error evidence with the bounded follow-up for
  the direct UI assertion, NFR-013 auth gating, NFR-015 section reuse,
  AC-DOC-07 register closure, and the AC-EMU-01 infrastructure-blocked
  wording, plus the journey graph's auth table and device-walkthrough note.
- Verification: doc-correctness pass that rewrites claims as acceptance criteria
    across every file under `docs/architecture/navigation-refactor/`
    (`specification.md`, `migration-inventory.md`, `verification.md`,
    `destination-contracts.md`, `future-work.md`, `agent-task-template.md`),
    reconciles counts, updates the journey graph, and compares every
    route-coverage claim with the two warm assertions plus the newer focused
    tests in `MainActivityExternalIngressTest`; see Phase 5.

### 5.4 Coverage gaps

Status note (observed at docs-pass time): several gaps now have focused tests
in the tree (noted per row). Execution of those tests is part of the connected
or unit gates; the gap is closed only when the recorded evidence exists. Rows
without a note have no tests or evidence in the tree today.

| ID | Gap | Related issues |
| --- | --- | --- |
| G-001 | Cold-start destination assertions for every manifest deep-link path (partial: cold-start tests exist for `/user` variants, `/anime`, `/activity`, and the `ROUTE_MEDIA_LIST` entry; `/studio`, `/character`, `/staff`, `/actor`, `/manga`, and the notification summary remain uncovered) | NFR-001, NFR-003 |
| G-002 | External ingress nested back semantics (back after deep link plus internal navigation) | NFR-003 (test exists in the tree: `externalUserListBackChainReturnsToProfileThenRootWithoutFinishingTask`) |
| G-003 | Deep-link redirect assertions for `/user/<name>/animelist` and `/mangalist` | NFR-001 (cold-start assertions exist in the tree; the warm case is uncovered) |
| G-004 | Dynamic shortcut launches (all 8 shortcut types) | NFR-004 (producer table and produced-intent cold launch are unit/instrumentation covered; emulator launches remain) |
| G-005 | Notification summary routing (tap on summary notification opens notifications) | none |
| G-006 | Shared content dismissal and task behavior | NFR-005, NFR-011 (tests exist in the tree: `SharedContentDismissalTest`) |
| G-007 | Drawer route choice to destination mapping for all 9 drawer route choices (`nav_home_feed`, `nav_anime`, `nav_manga`, `nav_myanime`, `nav_mymanga`, `nav_airing`, `nav_hub`, `nav_reviews`, `nav_trending`; My Anime and My Manga are separate route contracts that share `mediaListFragment`) | NFR-002 (drawer My Anime/My Manga covered by `DrawerMediaListNavigationTest`) |
| G-008 | Root toolbar title matrix | NFR-010 |
| G-009 | Search query replacement flow | NFR-009 (input implemented in the tree; emulator flow remains) |
| G-010 | Error and retry state behavior per destination | NFR-012 |
| G-011 | Auth-dependent menu visibility on profile and detail screens | NFR-013 |
| G-012 | Theme and accessibility label review against `@DESIGN.md` | NFR-014 |
| G-013 | Rotation and background/foreground restore for migrated destinations | NFR-007, NFR-015 |
| G-014 | Media chip to section mapping | NFR-008 (unit assertion exists in the tree: `MediaFragmentSectionOrderTest`; device matrix remains) |

---

## 6. Acceptance criteria

Grouped by verification surface. Nothing below is claimed as passing; each item
is a gate that must produce recorded evidence.

### 6.1 Static and unit

- AC-STAT-01: `./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug` and
  `:app:assembleGithubDebug` pass on the wrapped-up tree.
- AC-STAT-02: `./gradlew :app:spotlessCheck` passes; changed Kotlin files are
  formatted.
- AC-STAT-03: `./gradlew test` passes (the unit test suite; flavor task name
  `:app:testAppDebugUnitTest` for the `app` flavor debug variant).
- AC-STAT-04: `ArchitectureEnforcementTest` covers the new surface: no pager
  infrastructure in migrated destinations, no `KeyUtil.*_REQ` additions,
  no adapter/widget repository access, no child-fragment pager emulation
  (except the documented YouTube preview), no new Activity destinations.
- AC-STAT-05: Unit tests exist and pass for: `resolve`/`resolveLegacy*` argument
  bridges, the media section index policy (NFR-008), the media list media type
  decision (NFR-007), the `/user/.../animelist` route decision (NFR-001), the
  shortcut route producer table (NFR-004), and `isExternalEntry` semantics
  (NFR-003).
- AC-STAT-06: `KoinModuleVerificationTest` passes with the final module graph.
- AC-STAT-07: No test file references a deleted Activity, pager adapter, or
  layout id (grep sweep across `app/src/test` and `app/src/androidTest`).

### 6.2 Instrumentation

Instrumentation evidence is produced by the `app` flavor connected test
configuration (AGP task name `:app:connectedAppDebugAndroidTest`); the exact
configuration is whatever the Android Gradle Plugin generates for the `app`
debug variant on the wrapped-up tree.

- AC-INS-01: `MainActivityExternalIngressTest` passes and is extended with
  cold-start deep-link destination assertions for every manifest path
  (G-001), including `/user/<name>/animelist` (G-003). The current file holds
  the two warm assertions plus nine newer focused tests (cold-start user-list
  chains, numeric user ids, external back semantics, and the `ROUTE_MEDIA_LIST`
  entry); the cold-start extension for the remaining manifest paths (`/studio`,
  `/character`, `/staff`, `/actor`, `/manga`, notification summary) is what
  the route-coverage claims in `destination-contracts.md` require.
- AC-INS-02: Back-stack assertions after warm ingress: internal navigation
  followed by back returns to the previous destination and does not finish the
  task (G-002, NFR-003).
- AC-INS-03: Shared content tests cover landing, dismissal via hide, dismissal
  via back, and resulting back stack (G-006, NFR-005, NFR-011).
- AC-INS-04: `ToolbarHomeNavigationTest` is either rewritten against
  NavController up behavior or deleted with coverage moved to AC-INS-02
  (NFR-006).
- AC-INS-05: Notification summary tap routes to `notificationFragment` (G-005).
- AC-INS-06: `EntryPointRenderAuthTest` and `EntryPointRenderUnauthTest` pass
  with the final menu visibility rules (G-011).
- AC-INS-07: `FragmentBundleRoundTripTest`, `NavigationArgsTest`, and the new
  fragment argument tests pass.
- AC-INS-08: Media list media type switching is asserted deterministically
  (my anime to my manga, restore, back) with the remediation chosen after the
  NFR-007 repro (G-013).

### 6.3 Emulator and manual UX

Infrastructure note (observed at docs-pass time, refined by the final-review
pass): the API 36 AVD used for device acceptance reached boot completion but
became ADB-inaccessible, retries failed, and a stale orphaned QEMU process
remained (`docs/bugs/social-interaction-regression-evidence.md` lines
252-253). A cold AVD restart later recovered the environment for the NFR-007
repro: the two-direction drawer-switch repro and the regression-test run were
recorded on the recovered emulator (see NFR-007). AC-EMU-01 remains explicitly
infrastructure-blocked: no media-chip matrix evidence has been recorded, and
none is claimed. No other emulator acceptance criterion (the AC-EMU-02
rotation/process-death remainder and AC-EMU-03 through AC-EMU-11) has
recorded evidence; device flows cannot be re-recorded until a stable emulator
or device is available, and FW-002 records the dependency on a stable test
setup.

- AC-EMU-01: Media section chip matrix: each chip shows its own content for both
  authenticated and unauthenticated visibility (NFR-008, G-014). The source
  mismatch is fixed in the tree and asserted by `MediaFragmentSectionOrderTest`
  (AC-STAT-05); this criterion records the device-level confirmation.
  **Explicitly infrastructure-blocked: no emulator chip results are claimed
  and no recorded evidence exists for this criterion.**
- AC-EMU-02: Media list repro matrix for NFR-007: My Anime, My Manga,
  profile-pushed list, and shortcut list, with rotation and background restore
  between switches. The drawer-switch portion is recorded: the repro was
  reproduced in both directions and the remediation (restoreState disabled
  for ROOT media-list navigation) is applied and asserted by
  `MediaListSavedStateNavigationTest` (AC-INS-08). The rotation/process-death
  and PUSHED-origin portions remain open (G-013) and are explicit follow-up,
  not evidence that the drawer-switch fix is incomplete.
- AC-EMU-03: Back matrix for every entry path of `mediaListFragment`: drawer,
  profile stats, shortcut, deep link; back returns to the caller where one
  exists (NFR-002).
- AC-EMU-04: Dynamic shortcut launches for all 8 types land on the expected
  destination (NFR-004, G-004).
- AC-EMU-05: External entry back behavior: deep link, internal navigation, then
  back does not finish the task (NFR-003).
- AC-EMU-06: Root toolbar title matrix across all 8 top-level destinations,
  including switch-back cases (NFR-010, G-008).
- AC-EMU-07: Search flow: submit from toolbar, shortcut, and route; query can be
  changed and re-run on the search destination (NFR-009, G-009).
- AC-EMU-08: Error injection pass: first inventory the recoverable error-state
  gaps per migrated destination (which destinations lack an inline message
  and a working retry where the state is recoverable), then fix that
  inventoried subset with recorded evidence; do not promise every destination
  upfront (NFR-012, G-010).
- AC-EMU-09: Profile: unauth message action hidden; return-from-detail does not
  rebuild sections or duplicate requests (NFR-013, NFR-015).
- AC-EMU-10: Share flow dismissal behaves like the old Activity boundary for
  the user (dismiss closes the sheet and returns to the previous screen)
  (NFR-005).
- AC-EMU-11: Design review against `@DESIGN.md`: charts, themes, tabs, cards,
  chips, colors, and accessibility labels; findings recorded and either fixed or
  backlogged with product sign-off (NFR-014, G-012). `@DESIGN.md` is updated
  only when the review produces an actual design decision change (a new
  component pattern, revised token usage, updated spacing convention), not as a
  blanket sync.

### 6.4 Documentation

- AC-DOC-01: Every file under `docs/architecture/navigation-refactor/`
  (`specification.md`, `migration-inventory.md`, `verification.md`,
  `destination-contracts.md`, `future-work.md`, `agent-task-template.md`) is
  rewritten so every completed-sounding claim is either an observed tree fact
  or an acceptance criterion; compile/spotless claims are restated as "recorded
  at the time", not as current truth. Status: addressed by this documentation
  pass; the orchestrator reviews the doc diff as the validation owner.
- AC-DOC-02: Route-coverage claims are explicitly compared with the actual test
  evidence: `MainActivityExternalIngressTest` contains two warm assertions and
  nine newer focused tests (see NFR-016), and no test covers the remaining
  manifest paths or the warm `/user/<name>/animelist` case. Any "every route"
  or "cold-start fixtures cover every route" claim must be restated as the
  observed assertions plus an acceptance criterion for the missing coverage.
  Status: addressed by this documentation pass in `destination-contracts.md`.
- AC-DOC-03: `specification.md` and `future-work.md` updated to reference this
  audit; FW-001, FW-002, FW-006 gain explicit links to NFR-001 through NFR-008.
  Status: addressed by this documentation pass.
- AC-DOC-04: `docs/app-journey-knowledge-graph.md` reconciled with the deleted
  Activities and new destinations (NFR-016). Status: addressed by this
  documentation pass.
- AC-DOC-05: All companion docs use the verified destination count of 28
  fragment destinations, including `migration-inventory.md` and
  `destination-contracts.md` claims re-checked against the same observed count.
  Status: addressed by this documentation pass.
- AC-DOC-06: Retained boundaries section (Section 9 of this audit) confirmed in
  the inventory with explicit keep status per Activity. Status: addressed by
  this documentation pass.
- AC-DOC-07: This audit's issue register is closed: every NFR and G item has a
  recorded resolution (fixed, tested, or explicitly deferred with owner), and
  the unresolved questions in Section 10 are answered. Status: **register
  closed at the documentation level by this documentation pass**. Every NFR
  item now carries a recorded tree implementation, a recorded test, or an
  explicit deferral with owner and criterion: NFR-001 through NFR-006, NFR-008
  through NFR-011, NFR-013, and NFR-015 are implemented in the tree with
  focused coverage; NFR-007 is reproduced in both drawer-switch directions and
  fixed for ROOT media-list navigation, with rotation/process-death and
  PUSHED-origin deferred to AC-EMU-02 and G-013; NFR-010 is implemented with
  graph labels owning the toolbar titles, with the device title matrix
  deferred to AC-EMU-06 and G-008; NFR-012 is implemented for the primary-load
  error surface, with the direct fragment-level UI assertion deferred to
  AC-EMU-08 and G-010; NFR-014 is deferred with owner and product sign-off;
  NFR-016 is addressed by the documentation passes. The G items remain open as
  recorded criteria. Closure of the execution gates in Sections 6.1 through
  6.3 still requires the recorded evidence, and AC-EMU-01 is explicitly
  infrastructure-blocked with no emulator chip results claimed.

---

## 7. Dependency-ordered implementation phases

Phases are ordered by dependency and blast radius. Each phase must land with its
own tests and a commit or commit group; no phase should be merged into the dirty
203-entry set as-is (see Section 8). Dependencies are stated per phase; work in
a phase does not wait on NFR-007's repro unless the phase entry says so, because
the NFR-007 repro is a runtime question, not a source prerequisite for unrelated
defects.

- **Phase 1: Safe worktree ownership, baseline inventory, and route-policy
  decisions.**
  - Worktree admissibility (Section 3 rule 5): inventory every untracked path
    (currently `.artifacts/5dd0b8c5-c718-40a1-b7f4-9deb8321fa79/`,
    the empty `.artifacts/ab03d17d-d736-462d-bdac-0b0263f352f0/`,
    `.gradle-home/`, `.kotlin/`), preserve unknown/user-owned artifacts by
    default, require
    explicit owner approval before deleting any of them or adding ignore rules,
    and stage only explicitly owned migration paths.
  - Baseline inventory: record the baseline dirty set composition (198 entries
    at Phase 1, re-counted at 203 by the final-review pass; see Section 2)
    as the starting point for the per-phase commit split.
  - Route-policy decisions are approved and recorded in Section 5.2: NFR-002
    origin contract (producer-to-origin table), NFR-003 `isExternalEntry`
    restoration rule, NFR-001 `/user` redirect target, NFR-004 ShortcutUtil
    extension scope, NFR-007 reproduce-first. NFR-009's search-input location
    is resolved in the tree (fragment-local input, see the NFR-009 entry);
    NFR-010's toolbar-title ownership is resolved in the tree (graph labels
    own the toolbar titles, see the NFR-010 entry) and was not a Phase 1 gate
    item.
- **Phase 2: Navigation identity and back semantics.**
  NFR-001 (route decision), NFR-002 (top-level classification per the Phase 1
  decision), NFR-003 (external entry semantics), NFR-004 (shortcut producers),
  NFR-007 (media list media type). The NFR-007 device repro (AC-EMU-02) may run
  in parallel with the other items in this phase; unrelated defects do not wait
  on it. The repro is recorded for both drawer-switch directions on the
  recovered emulator, and the deterministic remediation with tests (AC-INS-08,
  AC-STAT-05) landed after it: ROOT media-list navigation disables
  `restoreState` (`mediaListRootDestinationOptions`, NavigationDestinations.kt
  lines 258-262), and `MediaListSavedStateNavigationTest` asserts the selected
  media type in both directions. The rotation/process-death and PUSHED-origin
  portions of the matrix remain explicit follow-up under AC-EMU-02 and G-013
  (FW-002 depends on a stable emulator or device test setup). Tests:
  AC-STAT-05, AC-INS-01, AC-INS-02, AC-EMU-03, AC-EMU-04, AC-EMU-05, AC-EMU-02,
  AC-INS-08.
- **Phase 3: NFR-008 direct mapping defect and test.**
  The fix is present in the current tree: `MediaFragment` creates and selects
  section views through one `sectionViewOrder` identical to
  `MediaSection.entries`, with the unit assertion
  `MediaFragmentSectionOrderTest` (AC-STAT-05). The remaining work is running
  the assertion in the unit gate and the device-level chip matrix (AC-EMU-01)
  once a stable emulator or device is available. No further source change is
  implied.
- **Phase 4: Shared-content boundary plus ingress/back coverage.**
  Implemented in the current tree: dismissal goes through the NavController
  (SharedContentFragment.kt lines 161-166) with back-stack and task-state
  assertions in `SharedContentDismissalTest` (AC-INS-03, AC-EMU-10), and
  `ToolbarHomeNavigationTest` was rewritten against NavController up behavior
  (AC-INS-04). The remaining work is executing the instrumentation suite and
  the emulator share flow once a stable emulator or device is available.
- **Phase 5: Bounded UX triage, documentation truthfulness, full validation,
  and only then commit hygiene.**
  - UX triage: NFR-009 (search input; implemented in the tree, only the
    emulator flow remains), NFR-010 (title policy; implemented in the tree
    with graph labels owning the toolbar titles, only the device title matrix
    remains), NFR-012 (error/retry; the primary-load error surface is
    implemented in the tree with the existing ProgressLayout coverage, and the
    direct fragment-level UI assertion is a bounded follow-up under
    AC-EMU-08), NFR-013 (auth gating; implemented in the tree, only the device
    confirmation remains), NFR-014 (design review with `@DESIGN.md` updated
    only on an actual design decision change), NFR-015 (profile section
    lifecycle; implemented in the tree with unit coverage, only the device
    duplicate-request check remains). Tests: AC-EMU-06 through AC-EMU-11,
    AC-INS-06.
  - Documentation truthfulness: NFR-016, AC-DOC-01 through AC-DOC-07, covering
    every file under `docs/architecture/navigation-refactor/` and the journey
    graph, including the explicit comparison of route-coverage claims with the
    two warm assertions plus the nine newer focused tests in
    `MainActivityExternalIngressTest`.
  - Full validation: the complete static and unit gate (AC-STAT-01 through
    AC-STAT-07, including `./gradlew test`), instrumentation via the `app`
    flavor connected test configuration (AC-INS-01 through AC-INS-08), and the
    emulator evidence (AC-EMU-01 through AC-EMU-11).
  - Commit hygiene only after validation: split the dirty set (203 entries as
    re-observed, see Section 2) into the
    per-phase logical commits, with only explicitly owned migration paths
    staged (Phase 1 inventory), and produce the exit checklist evidence.

---

## 8. Artifact and commit hygiene risks

Observed risks in the current tree:

1. **Single 203-entry dirty set.** The branch mixes source, tests, docs, and
   noise (re-counted by the final-review pass; see Section 2). It cannot be
   reviewed or reverted surgically. Before wrap-up it must
   be split into logically scoped commits (per phase) or the branch must be
   rebased into the documented structure. The split happens only in Phase 5,
   after validation, using the Phase 1 baseline inventory.
2. **Untracked build/agent artifacts.** `.artifacts/5dd0b8c5-c718-40a1-b7f4-9deb8321fa79/`
   (agent artifact markdown), the empty `.artifacts/ab03d17d-d736-462d-bdac-0b0263f352f0/`,
   `.gradle-home/`, and `.kotlin/` are untracked and must not be committed.
   They are unknown/user-owned by default: do not delete them and do not add
   ignore rules for them as a default action. Deletion or ignore rules require
   explicit owner approval, recorded in the Phase 1 inventory; until then they
   are preserved untouched.
3. **IDE noise.** `.idea/kotlinc.xml` is modified and should not be part of the
   migration commit unless the repo convention tracks IDE files.
4. **Deletion set is large but coherent.** 21 Kotlin files in the `view/activity/`
   tree (17 Activities plus 4 activity tests), 17 pager infrastructure files
   (16 pager adapters plus `BaseStatePageAdapter`), and the pager layouts are
   deleted; the enforcement tests must prove zero remaining references
   (AC-STAT-07). A reference scan must be part of the wrap-up gate.
5. **Test churn.** Deleted Activity tests, new fragment tests, and modified
   fixtures must be reviewed as one unit so the final test surface matches the
   final architecture (NFR-006 is the known stale case; others may exist).
6. **Secrets.** No secrets were observed in the dirty set. `setup-config.sh`
   provisioning for `app/.config/secrets.properties` is local/CI concern and
   must stay out of the commits.
7. **Known automation mismatch (out of scope, flagged).** `app/.meta/version.json`
   reports minSdk 21 while the build uses 23. Not touched by this branch; do not
   alter build values here.

---

## 9. Retained boundaries (explicitly out of scope, with required status)

These are intentional or transitional and must each carry an explicit status
(keep, or tracked follow-up) in the wrap-up docs, but they are not defects of
this migration:

1. **`CommonActivity`**: compatibility shell still extended by `MainActivity`,
   `LoginActivity`, `SplashActivity` (MainActivity.kt lines 93-96). Keep;
   cleanup tracked as FW-005.
2. **Legacy fragment and list bases**: `FragmentBaseList` (extended by
   `MediaListFragment`, `SearchFragment`, `MediaBrowseFragment`,
   `AiringListFragment`, `FeedListFragment`, `SuggestionListFragment`,
   `WatchListFragment`) and `RecyclerViewAdapter` (used by `SearchFragment`).
   Keep for now; the generic `RecyclerViewAdapter` removal is tracked
   infrastructure debt per the state-synchronization notes.
3. **Live search bridge**: `applySearchToAllListFragments`
   (MainActivity.kt lines 530-534). Keep; it is the legacy live-filter bridge
   and needs an explicit replacement decision before removal.
4. **`KeyUtil.*_REQ` request routing**: still used by `MediaFormatViewModel.kt`
   (line 91), `AiringListViewModel.kt` (line 128), `BrowseReviewViewModel.kt`
   (line 113), `MediaBrowseFragment.kt`, and others. Keep; do not add new uses;
   migrate when touched.
5. **`GiphyPreviewActivity`**, plus `ImagePreviewActivity`,
   `VideoPlayerActivity`, `WelcomeActivity`, `LoginActivity`, `SplashActivity`:
   retained Activity boundaries with manifest entries (AndroidManifest.xml
   lines 33-131). Keep with documented roles; FW-003 and FW-004 track their
   evaluation.
6. **`MediaFragment` childFragmentManager**: used only for the embedded
   `YouTubeEmbedFragment` player surface (MediaFragment.kt lines 421-428).
   Keep; documented as a platform-backed preview, not navigation.
7. **`WelcomeActivity` ViewPager2**: onboarding-only (verification.md
   lines 82-84, 192-194). Keep; FW-004.

Each item must be listed with its status in `migration-inventory.md` (AC-DOC-06)
so future work does not mistake a boundary for a leftover.

---

## 10. Unresolved questions

Items 1 through 5 are recorded here for completeness; their policies are
approved and recorded in Section 5.2 and are not open Phase 1 decisions.

1. NFR-007: Does Navigation 2 `restoreState` actually reproduce the stale media
   type on the current library version? Resolved for root drawer switching.
   The repro was reproduced in both drawer-switch directions on the recovered
   emulator (My Anime -> Feed -> My Manga restores `mediaType=ANIME` and the
   reverse before the fix), and the remediation disables `restoreState` for
   ROOT media-list navigation (`mediaListRootDestinationOptions`,
   NavigationDestinations.kt lines 258-262), asserted by
   `MediaListSavedStateNavigationTest` (AC-INS-08). Rotation/process-death
   coverage and the PUSHED-origin saved-entry interaction remain explicit
   follow-up under AC-EMU-02 and G-013; they are recorded as open matrix
   items, not as evidence that the drawer-switch fix is incomplete. Policy
   remains reproduce-first, and the recorded repro satisfies it for the
   drawer-switch case.
2. NFR-008: Is the chip-to-content mismatch user-visible on device, and which
   mapping is intended for the unauthenticated visibility set? The source
   mismatch is fixed in the current tree: `MediaFragment` creates and selects
   section views through one `sectionViewOrder` identical to
   `MediaSection.entries`, asserted by `MediaFragmentSectionOrderTest`. The
   unauthenticated visibility set is implemented and asserted as all sections
   except FEED and REVIEWS (MediaSection.kt line 18). The device-level
   confirmation (AC-EMU-01) remains an emulator acceptance criterion with no
   recorded device evidence (see the Section 6.3 infrastructure note); it is
   no longer a source-blocking question.
3. NFR-003: Resolved by user approval. Approved policy: once internal
   navigation occurs, external finish semantics are cleared, and returning to
   the original ingress destination does not re-arm the flag. See NFR-003 in
   Section 5.2.
4. NFR-002: Resolved by user approval. Approved policy: distinct route-origin
   contract with the producer-to-origin table in NFR-002 (Section 5.2); the
   existing `ARG_UNIFIED_DESTINATION` flag is not the contract. Post-login
   shortcut continuation is an implementation verification gate against that
   contract; the gate is now checked in the tree by `LoginShortcutIntentTest`.
5. NFR-001: Resolved by user approval. Approved policy:
   `/user/<name>/animelist` and `/mangalist` land on the profile and push a
   typed media list, preserving Profile beneath the list. See NFR-001 in
   Section 5.2.
6. NFR-009: Resolved in the current tree by the fragment-local input decision:
   `SearchFragment` owns a `TextInputEditText` with IME submission and
   normalized query handling (see the NFR-009 entry). The emulator search flow
   (AC-EMU-07, G-009) remains an acceptance criterion with no recorded device
   evidence (see the Section 6.3 infrastructure note). No further policy
   decision is open.
7. NFR-010: Resolved in the current tree. Graph destination labels own the
   toolbar titles: the MainActivity destination listener applies
   `destination.label` for every destination, including root switches and
   restored back-stack entries (MainActivity.kt lines 189-195), so no previous
   title can leak into the next root landing. `MediaListFragment`'s own title
   assignment now agrees with the graph label. The device-level title matrix
   (AC-EMU-06, G-008) remains an emulator acceptance criterion with no
   recorded device evidence.
8. NFR-006: Resolved in the current tree. `ToolbarHomeNavigationTest` was
   rewritten against the NavController behavior (toolbar up and system back
   from the pushed media list return to the caller without finishing the task)
   instead of being deleted. Execution is part of AC-INS-04.
9. Destination count: `nav_root.xml` has 28 fragment destinations, and the
   earlier 27 count from the reconciled findings is retired. Confirmed during
   this Phase 5 documentation pass: `migration-inventory.md` and
   `destination-contracts.md` now use the observed 28 count consistently
   (AC-DOC-05).
10. G-005: What is the expected behavior when the notification summary tap
    arrives while the app is already foregrounded at another destination?
11. Shortcut login handoff: `LoginActivity` still builds
    `SHORTCUT_MY_ANIME_BUNDLE` with `arg_mediaType` (LoginActivity.kt line 162).
    The post-login redirect path is an implementation verification gate against
    the approved media-list origin contract: it must be explicitly checked
    during implementation, not inferred, and any intentional root-destination
    handling must be a deliberate exception per the NFR-002 producer table.
    The gate is now checked in the tree: `LoginShortcutIntentTest` launches the
    produced post-login intent cold and asserts the pushed typed list with
    `PUSHED` origin (see NFR-002).

---

## 11. Exit checklist

The branch may be called wrapped up only when all of the following hold,
grouped by the five phases in Section 7:

Phase 1 (worktree ownership, baseline, decisions):

- [ ] All untracked paths are inventoried (currently
      `.artifacts/5dd0b8c5-c718-40a1-b7f4-9deb8321fa79/`,
      the empty `.artifacts/ab03d17d-d736-462d-bdac-0b0263f352f0/`,
      `.gradle-home/`, `.kotlin/`); unknown/user-owned artifacts are preserved
      by default; no deletion or ignore rule exists without explicit owner
      approval; the staged set contains only explicitly owned migration paths.
- [ ] Route-policy decisions recorded in the register: NFR-002 origin contract
      (producer-to-origin table), NFR-003 `isExternalEntry` restoration rule,
      NFR-001 `/user` redirect target, NFR-004 ShortcutUtil extension scope,
      NFR-007 reproduce-first. NFR-009's search-input location is resolved in
      the tree; NFR-010's title ownership is resolved in the tree (graph
      labels own the toolbar titles) and is not a Phase 1 gate item.

Phase 2 (navigation identity and back semantics):

- [ ] NFR-001, NFR-002, NFR-003, NFR-004 are fixed and covered by
      instrumentation (AC-INS-01, AC-INS-02) and emulator checks
      (AC-EMU-03 through AC-EMU-05). The implementations and focused tests
      are present in the current tree (see the NFR entries in Section 5);
      the recorded execution evidence is still required. This independent
      work may be granted a **conditional Phase 2 approval** once the
      evidence is recorded, with the NFR-007 remaining matrix carried
      forward as an explicit blocker for full wrap-up.
- [ ] NFR-007 has a recorded device repro and, only after the repro, a
      deterministic remediation with tests (AC-INS-08, AC-EMU-02). The repro
      is recorded for both drawer-switch directions on the recovered emulator,
      and the remediation (restoreState disabled for ROOT media-list
      navigation) plus the two-direction regression test are applied in the
      tree. The rotation/process-death and PUSHED-origin portions of the
      matrix remain explicit follow-up under AC-EMU-02 and G-013; they are
      open matrix items, not evidence that the drawer-switch fix is
      incomplete. Phase 2 is not fully closed until the remaining matrix items
      have recorded evidence or an explicit deferral.

Phase 3 (NFR-008):

- [ ] NFR-008 is fixed and asserted (AC-STAT-05, AC-EMU-01). The fix and the
      unit assertion are present in the current tree
      (`sectionViewOrder` plus `MediaFragmentSectionOrderTest`); the unit gate
      execution (AC-STAT-05) and the device chip matrix (AC-EMU-01) still need
      recorded evidence.

Phase 4 (shared-content boundary):

- [ ] NFR-005 and NFR-011 dismissal behavior follows a tested dismissal policy
      (AC-INS-03, AC-EMU-10). The NavController-based dismissal and
      `SharedContentDismissalTest` are present in the current tree; the
      instrumentation and emulator evidence still needs recording.
- [ ] NFR-006 test is repaired or removed with coverage relocated (AC-INS-04).
      The test was rewritten in the current tree against NavController up
      behavior; the instrumentation evidence still needs recording.

Phase 5 (UX triage, truthfulness, validation, hygiene):

- [ ] NFR-009 through NFR-015 are resolved or explicitly deferred with owner
      and product sign-off; NFR-010, NFR-013, and NFR-015 are implemented in
      the tree with unit or focused coverage and their device confirmation
      deferred to the emulator criteria (AC-EMU-06, AC-EMU-09); NFR-012's
      primary-load error surface is implemented in the tree and its missing
      direct fragment-level UI assertion is a bounded follow-up (AC-EMU-08,
      G-010); `@DESIGN.md` updated only for actual design decision changes
      (AC-EMU-11).
- [ ] Coverage gaps G-001 through G-014 have recorded evidence or an explicit
      deferral (AC-INS-05, AC-INS-07, AC-INS-08).
- [ ] Retained boundaries in Section 9 carry explicit status in
      `migration-inventory.md` (AC-DOC-06).
- [ ] Documentation truthfulness pass done for every file under
      `docs/architecture/navigation-refactor/` (`specification.md`,
      `migration-inventory.md`, `verification.md`, `destination-contracts.md`,
      `future-work.md`, `agent-task-template.md`) plus the journey graph, with
      route-coverage claims explicitly compared against the two warm assertions
      plus the nine newer focused tests in `MainActivityExternalIngressTest`
      (AC-DOC-01 through AC-DOC-07).
- [ ] Static and unit gate green: compile, assemble both flavors, spotless,
      `./gradlew test` (unit suite), architecture enforcement
      (AC-STAT-01 through AC-STAT-07); instrumentation evidence produced via
      the `app` flavor connected test configuration (AC-INS-01 through
      AC-INS-08).
- [ ] Hygiene: no `.artifacts/`, `.gradle-home/`, or IDE noise in commits; the
      change set is split into the per-phase logical commits with only
      explicitly owned paths staged (Section 8).
- [ ] This audit's register is closed with recorded resolution per item.

---

## 12. Summary: what must be fixed before this branch can be called wrapped up

The migration structure is real and mostly matches the documents, but the branch
is not wrap-up-ready. Four things block closure:

1. **State correctness is proven and fixed for the drawer-switch case.** The
   media list reuses one destination with `launchSingleTop`, `restoreState`,
   and `popUpTo saveState` while reading `mediaType` from arguments (NFR-007).
   The stale-media-type repro was recorded in both drawer-switch directions on
   the recovered emulator, and the remediation disables `restoreState` for
   ROOT media-list navigation (`mediaListRootDestinationOptions`,
   NavigationDestinations.kt lines 258-262), asserted by the two-direction
   `MediaListSavedStateNavigationTest` (AC-INS-08). The rotation/process-death
   and PUSHED-origin saved-entry interaction remain explicit follow-up
   (AC-EMU-02, G-013), recorded as open matrix items rather than as proof the
   fix is incomplete.
2. **Chip to content mapping was mismatched by construction and is fixed in
   the tree.** `MediaFragment` now creates and selects section views through
   one `sectionViewOrder` identical to `MediaSection.entries`, and
   `MediaFragmentSectionOrderTest` asserts the invariant (NFR-008). The unit
   gate execution (AC-STAT-05) and the device chip matrix (AC-EMU-01) still
   need recorded evidence; the device matrix is infrastructure-blocked.
3. **Ingress behavior regressed in four places; all four are implemented in
   the tree with focused tests.** `/user/<name>/animelist` and `/mangalist`
   land on Profile and push a typed media list preserving Profile beneath the
   list (NFR-001), all eight dynamic shortcuts carry `EXTRA_ROUTE` producers
   with matching consumer branches (NFR-004), `isExternalEntry` is cleared
   after internal navigation and on warm delivery (NFR-003), and
   `mediaListFragment` is top-level only for the root drawer producer via the
   explicit `MediaListOrigin` contract (NFR-002). The shared-content dismissal
   boundary is NavController-based with back-stack and task-state tests
   (NFR-005/NFR-011), and the toolbar home test was rewritten against
    NavController behavior (NFR-006). The implementations and tests are
    observed in the tree; the connected-test and emulator evidence still needs
    recording, and the emulator portion has no recorded device evidence (see
    the Section 6.3 infrastructure note).
4. **The branch and its docs are not honest yet.** The 203-entry dirty set mixes
   noise with the migration; untracked artifacts (`.artifacts/`, `.gradle-home/`,
   `.kotlin/`)
   are preserved by default pending explicit owner approval, and only explicitly
   owned migration paths may be staged. The navigation-refactor documents
   phrased unverified acceptance criteria as completed evidence, including a
   claim of "cold-start fixtures cover every AniList URI route" that exceeds
   the actual evidence: two warm assertions plus nine newer focused tests in
   `MainActivityExternalIngressTest` cover only a subset of manifest paths,
   with `/studio`, `/character`, `/staff`, `/actor`, `/manga`, and the
   notification summary still uncovered (G-001, G-003). The Phase 5
   documentation pass restated those claims as observed facts plus acceptance
   criteria across every file in `docs/architecture/navigation-refactor/` plus
   the journey graph, and the final-review pass reconciled the remaining stale
   register wording (NFR-007 status, NFR-010, NFR-012, NFR-013, NFR-015,
   AC-DOC-07, AC-EMU-01); a commit hygiene pass is still required, with the
   coverage gaps G-001 through G-014 filled or explicitly deferred.

The rest (UX findings, design-system drift, retained boundaries, and journey
docs) are important but can be sequenced after the four blocking areas, provided
each is either fixed or explicitly deferred with an owner in this register.
Work proceeds in the five phases of Section 7; unrelated defects do not wait on
the NFR-007 repro.
