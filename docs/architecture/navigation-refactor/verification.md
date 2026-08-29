# Navigation migration verification

**Status:** Navigation 2 internal-destination migration implemented in the working tree; validation gates recorded, not claimed as passed

The authoritative wrap-up record for this branch is
[`full-wrap-up-audit.md`](full-wrap-up-audit.md). It registers every open
verification item (NFR-001 through NFR-016), the coverage gaps G-001 through
G-014, and the acceptance criteria grouped by surface. This document records
observed tree facts and acceptance criteria; it does not claim that builds,
tests, or device behavior pass unless the evidence is recorded here.

## Phase 0 evidence

The following repository facts were observed on 16 August 2026 and re-checked
against the working tree during the Phase 5 documentation pass:

- The original baseline declared 24 activities. The current manifest declares
  seven Activity entries, all retained as launcher, OAuth, onboarding, preview,
  player, or shared shell boundaries.
- `MainActivity` owns the application window, drawer selection, and the primary
  Navigation 2 host. The former index pager bridge is removed.
- The former detail and index page adapter implementations have been removed
  from production source. Onboarding retains its platform-specific ViewPager2.
- Settings destinations and their local actions are registered directly in
  `nav_root.xml` and are hosted by `MainActivity`; the former
  `nav_settings_graph.xml` is deleted.
- `nav_root.xml` contains 28 fragment destinations (the verified count used by
  all navigation-refactor documents) with `animeFragment` as the start
  destination.
- `ScreenParam` is a typed `Parcelable` contract with explicit stable wire keys.
- `CharacterFragment`, `StudioFragment`, and `StaffFragment` use one logical
  destination each, with local section state and no child-fragment pager.
- Staff web ingress for `/staff` and `/actor` is routed through `MainActivity`.
- Navigation argument and round-trip tests exist under
  `app/src/test/java/com/mxt/anitrend/navigation` and
  `app/src/androidTest/java/com/mxt/anitrend/navigation`.
- The state synchronization specification prohibits complete entity payloads in
  activity results and requires canonical-store ownership for committed state.

## Phase 0 checks

Run from the repository root:

```sh
git status --short --branch
rg -n '<activity|class .*Activity' app/src/main/AndroidManifest.xml app/src/main/java
rg -n 'ViewPager|ViewPager2|PageAdapter|BaseStatePageAdapter' app/src/main/java app/src/main/res
rg -n 'NavHostFragment|ScreenParam|screenParamKey|putScreenParam' app/src/main/java app/src/main/res
find app/src/main/java/com/mxt/anitrend/adapter/pager -type f -name '*.kt' | sort
```

Phase 0 is complete when the five navigation-refactor documents plus this
verification record exist, the historical 24-activity baseline has an inventory
entry, every current exported ingress is listed, and the active implementation
has explicit enforcement tests for the migrated destinations and retained
boundaries. The documents exist in the tree; the enforcement-test execution is
part of the static and unit gate (AC-STAT-04, AC-STAT-07 in the audit).

## Required tests for every migrated destination

See the acceptance criteria in the audit: static and unit (AC-STAT-01 through
AC-STAT-07), instrumentation (AC-INS-01 through AC-INS-08), emulator and manual
UX (AC-EMU-01 through AC-EMU-11), and documentation (AC-DOC-01 through
AC-DOC-07). None of those criteria are claimed as passing by this document.

## Current implementation evidence

These facts are observed in the working tree. Build and formatting claims below
are historical: they were recorded at the time (16 August 2026) for an earlier
state of the tree and have not been re-run by this documentation pass.

- `:app:compileAppDebugKotlin` passed at the time after the root host, detail
  routes, utility routes, share ingress, and pager removals (recorded, not
  re-verified).
- `:app:spotlessApply` passed at the time and formatted the changed Kotlin
  sources (recorded, not re-verified).
- The removed low-risk Activities have no remaining production or Android test
  references, except for the historical inventory and compatibility notes; the
  reference sweep is part of AC-STAT-07.
- `MessageFragment` restores inbox/outbox selection through saved instance
  state and does not create child fragments or a replacement pager.
- Internal comment actions route through `CommentScreenParam` when hosted by
  `MainActivity`, which receives external `/activity` links and routes them
  through `CommentScreenParam` to `CommentFragment`.
- The Logging screen is now a root-host `LoggingFragment`; its existing
  ViewModel, filters, menu actions, and instrumentation coverage use the route
  instead of an Activity. `LoggingFragmentTest` covers launch, parsing,
  filters, chips, and the clear action.
- The Studio detail screen is now a single root-host `StudioFragment`; its
  former hosted media screen was consolidated into the same Fragment and its
  `/studio` links route through `MainActivity`.
- The Character detail screen is now a single root-host `CharacterFragment`;
  its overview, anime roles, manga roles, and actor roles are local sections,
  and its `/character` links route through `MainActivity`.
- The Staff detail screen is now a single root-host `StaffFragment`; its
  overview, anime roles, media roles, and staff roles are local sections, and
  its `/staff` and `/actor` links route through `MainActivity`.
- The Profile detail screen is now a single root-host `ProfileFragment`; its
  overview, media-list feed, and text feed are ordinary section controllers,
  and its `/user` links route through `MainActivity`. `/user/<name>/animelist`
  and `/mangalist` land on Profile and push a typed media list (NFR-001).
- The share ingress is now a root-host `SharedContentFragment`; `ACTION_SEND`
  text/plain intents route through `MainActivity`, preserve shared text and
  subject arguments, and no longer require a dedicated Activity boundary.
  Dismissal pops through the NavController, not the FragmentManager
  (NFR-005/NFR-011).
- Remaining Activities are explicitly classified as launcher, OAuth, onboarding,
  preview, or player boundaries. `WelcomeActivity` is the only retained
  production `ViewPager2` use, and it is not an application destination pager.
- The obsolete `MainActivity.onActivityResult` voice-search bridge was removed.
  `MaterialSearchView` has no voice action or voice control, so no result
  contract remains to migrate.
- Fragment route helpers now require the owning NavHost and delegate directly to
  its `NavController`; they no longer launch a fallback `MainActivity`.
- `AvatarIndicatorView` is now render-only. `MainActivity` supplies avatar and
  notification state and owns profile, notification, and login navigation.
- `StatusContentWidget` is now render-only. Image, video, and YouTube preview
  requests are emitted as callbacks for a Fragment or Activity owner.
- Profile callers use `UserScreenParam` route helpers. The old profile Activity,
  page adapter, and page fragments are removed from production source.
- The media-list Activity boundary is removed. Profile statistics and shortcuts
  route to one `MediaListFragment` with a toolbar status selector. The legacy
  index media-list pager is also removed; My Anime and My Manga use the same
  root-host destination with local status selection. The route-origin contract
  (`MediaListOrigin`, `ARG_MEDIA_LIST_ORIGIN`) distinguishes root drawer entry
  from pushed producers (NFR-002).
- The Favourite Activity boundary is removed. Profile and About UI emit a
  callback to the root host, which opens `FavouriteFragment` with a typed
  `UserScreenParam`. Anime, character, manga, staff, and studio favourites are
  local sections on one Fragment, and the old five-page adapter is deleted.
- The Search Activity boundary is removed. MainActivity toolbar submission and
  the application shortcut route to `SearchFragment` through Navigation 2.
  Anime, manga, studio, staff, character, and user modes are local section
  state on one Fragment, while media results navigate to `MediaFragment`. The
  old Search Activity and page adapter are deleted.
- `SharedContentActivity` is removed. MainActivity accepts `ACTION_SEND`
  `text/plain` and routes to `SharedContentFragment` in the root graph.
- The home feed pager is removed from MainActivity's production home route.
  `FeedFragment` uses the existing feed ViewModel, adapter, and canonical store
  projection while exposing progress, status, and public status as local section
  state. MainActivity no longer owns a legacy index pager bridge.
- The reviews pager is removed from MainActivity's production reviews route.
  `ReviewBrowseFragment` keeps the existing review ViewModel and adapter while
  exposing anime and manga as local section state. The old ReviewPageAdapter is
  no longer referenced by MainActivity.
- The trending pager is removed from MainActivity's production trending route.
  `TrendingFragment` keeps the existing latest-media ViewModel and adapter while
  exposing anime, manga, and recently added as local section state. The old
  TrendingPageAdapter is deleted.
- The seasonal anime and manga pagers are removed from MainActivity's production
  routes. `AnimeFragment` exposes four seasons as local section state and
  `MangaFragment` exposes manga list and recently added as local section state.
  The old index SeasonPageAdapter and MangaPageAdapter are deleted.
- The airing and hub pagers are removed from MainActivity's production routes.
  `AiringFragment` and `HubFragment` own their primary list content, while
  latest episodes and most popular are explicit `WatchListFragment` routes.
  The old AiringPageAdapter and HubPageAdapter are deleted.
- The root Navigation 2 graph starts at `AnimeFragment`; MainActivity no longer
  owns ViewPager2, TabLayoutMediator, or a compatibility index Fragment.
- AniList anime and manga links now enter through MainActivity and navigate to
  `MediaFragment` with `MediaScreenParam`.
- Media section extraction is complete with `MediaOverviewSection`,
  `MediaStatsSection`, `MediaRelationSection`, `MediaStaffSection`, and
  `MediaCharacterSection`, `MediaRecommendationsSection`, `MediaReviewSection`,
  and `MediaFeedSection`, ordinary view controllers consumed by `MediaFragment`.
  The typed `MediaSection` model preserves authenticated visibility rules and
  selected-section restoration. Section views are created and selected through
  one `sectionViewOrder` identical to `MediaSection.entries`, asserted by
  `MediaFragmentSectionOrderTest` (NFR-008).
- Staff unit tests, Android test source compilation, and production reference
  scans passed at the time. No Staff Activity, page adapter, or former Staff
  section Fragment remains in production source (recorded, not re-verified).

## Acceptance criteria per destination

The items below are acceptance criteria for each migrated destination. They are
not claimed as verified by this document; each requires the recorded evidence
listed in the audit's Section 6.

### Navigation

- Opens from every known internal caller (caller sweep per destination in
  `migration-inventory.md`).
- Delivers the declared `ScreenParam`.
- Up and system Back return to the declared destination (AC-INS-02, AC-EMU-03).
- Repeated root navigation does not create duplicate root entries.
- The media list route-origin contract (root drawer vs pushed) is preserved per
  the producer table in audit NFR-002.

### Lifecycle and restoration

- Rotation and view recreation preserve the destination (FW-002; no recorded
  device evidence, see the audit's Section 6.3 note).
- Local section selection is restored through the declared state owner.
- Loading and data state are not restarted unnecessarily (NFR-015 for Profile).
- Returning from another destination restores the documented screen state.
- The NFR-007 media type restore repro is recorded for both drawer-switch
  directions on the recovered emulator, and the remediation (restoreState
  disabled for ROOT media-list navigation) is applied and asserted by
  `MediaListSavedStateNavigationTest` (AC-INS-08). Rotation/process-death
  coverage and the PUSHED-origin saved-entry interaction remain explicit
  follow-up under AC-EMU-02 and G-013 (see the audit's NFR-007 entry).

### External ingress, when applicable

- Cold-start deep link (partially covered by tests; G-001 remains open for the
  remaining manifest paths).
- Warm-process deep link with existing `MainActivity` (two warm assertions
  exist; see the route-coverage evidence below).
- `singleTop` new-intent delivery (asserted via the `onNewIntent` seam; the
  platform dispatch itself is not exercised by the harness, see the test
  seam notes in `MainActivityExternalIngressTest`).
- Notification, shortcut, share, and OAuth routes where listed in the inventory
  (shortcut producers and consumers implemented, NFR-004; notification summary
  routing is G-005).

### Pager replacement, when applicable

- Every former page has an equivalent local section or destination.
- Authentication-dependent visibility is preserved (asserted for MediaSection
  visibility sets in unit tests).
- Section switching does not trigger duplicate requests (FW-006).
- Section-specific scroll and state behavior is documented and tested.
- No child FragmentManager is used to emulate the pager (the only
  `childFragmentManager` use is the embedded YouTube preview surface in
  `MediaFragment`).

### State architecture

- No complete domain entity crosses the navigation boundary.
- No activity-result synchronization remains in the migrated path.
- Adapters and custom views emit callbacks and do not navigate.
- ViewModels do not depend on `NavController`.

## Route-coverage evidence

Compared against the actual tests in the tree (AC-DOC-02):

- `MainActivityExternalIngressTest` contains exactly two warm assertions:
  `warmSingleTopIntentRoutesThroughTheExistingRootHost` (warm `/anime/1` lands
  on `mediaFragment`) and `warmShareIntentRoutesThroughTheSharedContentDestination`
  (warm `ACTION_SEND` lands on `sharedContentFragment`), both invoked through
  the `onNewIntent` seam because ActivityScenario cannot deliver a warm
  `singleTop` intent through the platform dispatch.
- The same file contains nine newer focused tests adding cold-start coverage
  for `/user/<name>/animelist`, `/user/<name>/mangalist`, plain `/user`,
  numeric user ids, `/anime/1` and `/activity/1` back semantics, and the
  `ROUTE_MEDIA_LIST` entry.
- Additional focused instrumentation: `SharedContentDismissalTest` (hide and
  system-back dismissal with back-stack and task assertions),
  `DrawerMediaListNavigationTest` (root drawer origin and exit-confirm back),
  `LoginShortcutIntentTest` (post-login shortcut continuation),
  `ToolbarHomeNavigationTest` (toolbar up and system back from the pushed
  media list), `LoggingFragmentTest` (launch, filters, chips, clear).
- Not covered by any test in the tree: cold-start assertions for the remaining
  manifest paths (`/studio`, `/character`, `/staff`, `/actor`, `/manga`,
  notification summary) and the warm `/user/<name>/animelist` case (G-001,
  G-003). These remain acceptance criteria (AC-INS-01), not claimed evidence.
- No claim of passing instrumentation execution is made by this document; the
  connected-test gate (AC-INS-01 through AC-INS-08) records that evidence.

## Final enforcement

The architecture test (`ArchitectureEnforcementTest`) is present in the tree
and rejects the legacy index pager bridge in MainActivity and `nav_root.xml`,
rejects pager infrastructure in migrated destination files, and asserts that
the Media Activity, detail adapters, and generic pager base are deleted. The
remaining ViewPager2 use is onboarding-specific. Its execution is part of
AC-STAT-04.

## Phase completion and retained boundaries

Phases 0 through 10 of `specification.md` are implemented for ordinary
application navigation in the working tree; each phase is a recorded
implementation state, not a claim of verified behavior (see the audit's
acceptance criteria). The current manifest intentionally retains only the
shell, launcher, OAuth, onboarding, preview, and player boundaries.
`WelcomeActivity` is the only remaining application `ViewPager2` consumer, and
it belongs to onboarding rather than destination navigation.

No migrated Fragment, bottom sheet, adapter, or custom view relaunches
`MainActivity`. Those components either call a destination-owned Navigation 2
extension or emit an explicit callback to their hosting Fragment (observed in
the tree; enforcement is part of AC-STAT-04).

## Future validation and boundary work

The current checks are targeted and do not claim exhaustive device coverage.
The following follow-up IDs remain planned or deferred in
[`future-work.md`](future-work.md), with the NFR links added during the Phase 5
documentation pass:

- FW-001, exhaustive cold and warm external ingress coverage (linked to
  NFR-001 through NFR-004).
- FW-002, lifecycle and state-restoration validation (linked to NFR-007 and
  the emulator acceptance criteria; the NFR-007 drawer-switch repro and
  regression run were recorded on the recovered emulator, AC-EMU-01 remains
  explicitly infrastructure-blocked, and no other emulator acceptance
  criterion has recorded evidence, see the audit's Section 6.3 note).
- FW-003, individual retained Activity boundary decisions.
- FW-004, onboarding pager modernization.
- FW-005, `CommonActivity` compatibility cleanup.
- FW-006, section parity and duplicate-request review (linked to NFR-008,
  NFR-015).
- FW-007, normal CI integration for architecture enforcement.
- FW-008, conditional graph modularization review.
- FW-009, eventual Compose and Navigation 3 planning.
