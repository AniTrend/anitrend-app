# Navigation migration inventory

**Status:** Internal-destination migration inventory observed on the active branch; validation gates recorded in the wrap-up audit
**Observed:** 16 August 2026 on branch `refactor/navigation-inventory-foundation`

This inventory is the decision record for the strangler migration. It records
the historical 24-activity baseline from `app/src/main/AndroidManifest.xml`,
current pager ownership, known internal callers, exported ingress, argument contracts, and
the intended migration wave. A field marked `unknown` is an explicit audit
item, not permission for an implementation agent to infer a contract.

The controlling wrap-up record is [`full-wrap-up-audit.md`](full-wrap-up-audit.md).
"Implemented on this branch" below means the source state is observed in the
working tree; it is not a claim that tests, builds, or device behavior passed.
The audit's acceptance criteria (AC-STAT, AC-INS, AC-EMU, AC-DOC) define the
evidence still required.

## Current implementation status

The following slices have been implemented on the active migration branch.
The root graph `nav_root.xml` contains 28 fragment destinations (the verified
count used across the navigation-refactor documents), with `animeFragment` as
the start destination.

| Legacy component | Current production destination | Compatibility status |
| --- | --- | --- |
| `AboutActivity` | `AboutFragment` in the root utility graph | Activity and manifest entry removed |
| MainActivity root host | `MainActivity` with `nav_root.xml` and one primary `NavHostFragment` | Legacy index pager bridge and root tab include removed; drawer routes use Navigation 2 destinations |
| `ChangelogActivity` | `ChangelogFragment` in the root utility graph | Activity and manifest entry removed |
| `SettingsActivity` | Settings destinations registered directly in the root graph (`settingsHubFragment`, `accountSettingsFragment`, `customizeSettingsFragment`, `settingsCategoryFragment`) | Activity and layout removed; the former `nav_settings_graph.xml` is deleted |
| `NotificationActivity` | `NotificationFragment` in the root account graph | Activity and manifest entry removed |
| `MediaBrowseActivity` | `MediaBrowseFragment` in the root main graph | Activity and manifest entry removed |
| `MessageActivity` | `MessageFragment` in the root account graph | Activity and pager adapter removed; inbox/outbox are local menu state |
| `CommentActivity` | `CommentFragment` in the root detail graph for internal and `/activity` ingress | Activity and manifest entry removed; MainActivity handles the external route |
| `LoggingActivity` | `LoggingFragment` in the root utility graph | Activity and manifest entry removed; MainActivity owns the route shell |
| `StudioActivity` | `StudioFragment` in the root detail graph for internal and `/studio` ingress | Activity and hosted media fragment removed; MainActivity handles the external route |
| `CharacterActivity` | `CharacterFragment` in the root detail graph for internal and `/character` ingress | Activity, page adapter, and Character-only page fragments removed; MainActivity handles the external route |
| `StaffActivity` | `StaffFragment` in the root detail graph for internal and `/staff` or `/actor` ingress | Activity, page adapter, and former staff section fragments removed; MainActivity handles the external route |
| `MediaListActivity` | `MediaListFragment` in the root detail graph for profile stats, shortcuts, and the `/user/<name>/animelist|mangalist` chains | Activity and manifest entry removed; My Anime and My Manga use the unified root destination with local status selection, and the route-origin contract (`MediaListOrigin`, `ARG_MEDIA_LIST_ORIGIN`) distinguishes the root drawer entry from pushed producers (NFR-002) |
| `FavouriteActivity` | `FavouriteFragment` in the root detail graph for profile/about actions and test entry points | Activity, manifest entry, and `FavouritePageAdapter` removed; five categories are local section state on one Fragment |
| `SearchActivity` | `SearchFragment` in the root main graph for toolbar, shortcut, and test entry points | Activity, manifest entry, and `SearchPageAdapter` removed; six modes are local section state on one Fragment |
| MainActivity home pager | `FeedFragment` in the root main graph | `FeedPageAdapter` removed from the home route; progress, status, and public status are local section state |
| MainActivity reviews pager | `ReviewBrowseFragment` in the root main graph | `ReviewPageAdapter` removed; anime and manga are local section state |
| MainActivity trending pager | `TrendingFragment` in the root main graph | `TrendingPageAdapter` removed; anime, manga, and recently added are local section state |
| MainActivity seasonal anime pager | `AnimeFragment` in the root main graph | `SeasonPageAdapter` removed; winter, spring, summer, and fall are local section state |
| MainActivity manga pager | `MangaFragment` in the root main graph | `MangaPageAdapter` removed; manga list and recently added are local section state |
| MainActivity airing pager | `AiringFragment` and `WatchListFragment` in the root main graph | `AiringPageAdapter` removed; airing is a root destination and latest episodes is an independently navigable feed destination |
| MainActivity hub pager | `HubFragment` and `WatchListFragment` in the root main graph | `HubPageAdapter` removed; suggestions is a root destination and most popular is an independently navigable feed destination |

The baseline entries below remain as a historical decision record. Current
implementation state is determined by the table above, the retained-boundary
list, and the architecture enforcement tests. Do not infer completion from the
presence of a destination graph alone. Every scoped caller, external ingress,
and compatibility removal criterion must remain satisfied, and the audit gates
(AC-STAT, AC-INS, AC-EMU) must produce recorded evidence before the branch is
called wrapped up.

## Classification vocabulary

- **Internal screen:** ordinary application navigation that should become a
  Fragment destination.
- **Ingress boundary:** launcher, deep link, notification, shortcut, share,
  OAuth, or restored-intent entry that must be routed before its Activity can be
  removed.
- **Window boundary:** system or window behavior that may justify retaining an
  Activity after internal destinations are migrated.
- **Compatibility:** an existing Activity or legacy extra retained until all
  callers and ingress paths are migrated.

## Summary

| Legacy component | External or special contract | Pager or hosted content | Target classification | Wave | Risk |
| --- | --- | --- | --- | --- | --- |
| `MainActivity` | `singleTop`, application entry from Splash | Eight index page adapters and drawer | Root Navigation 2 host shell | 1 | high |
| `SettingsActivity` | Exported, explicit callers, parent MainActivity | Existing XML Navigation 2 graph | Settings graph in root host | 4 | medium |
| `SplashActivity` | Launcher and shortcut metadata | None | Retained launcher coordinator | 9 | medium |
| `LoginActivity` | `intent://com.mxt.anitrend` OAuth entry | None | Retained OAuth ingress boundary | 9 | high |
| `ProfileActivity` | AniList `/user...` link | Former `ProfilePageAdapter`, 3 sections | `ProfileFragment` | 6 | high |
| `MediaActivity` | Removed; AniList `/anime...` and `/manga...` links enter through MainActivity | `AnimePageAdapter` and `MangaPageAdapter` removed after extracting 8 section controllers | `MediaFragment` with local sections | 6 | high |
| `CharacterActivity` | AniList `/character...` link | Former `CharacterPageAdapter`, 4 entries | `CharacterFragment` with local sections | 6 | medium |
| `StaffActivity` | AniList `/staff...` and `/actor...` links | `StaffPageAdapter`, 4 entries | `StaffFragment` with local sections | 6 | high |
| `StudioActivity` | AniList `/studio...` link | One hosted `StudioMediaFragment` | `StudioFragment` | 6 | medium |
| `CommentActivity` | AniList `/activity...` link | One hosted `CommentFragment` | `CommentFragment` | 7 and 8 | high |
| `FavouriteActivity` | Explicit internal launches | `FavouritePageAdapter`, 5 entries | Favourites destination or local sections | 5 | medium |
| `MediaListActivity` | Explicit and shortcut launches | `MediaListFragment`, local status sections | Media list destination | 5 | medium |
| `MediaBrowseActivity` | Explicit utility launches | One hosted `MediaBrowseFragment` | Browse destination | 5 | medium |
| `SearchActivity` | Toolbar and shortcut launches now route through `MainActivity` | `SearchPageAdapter`, 6 entries | `SearchFragment` with local mode state | 5 | medium |
| `NotificationActivity` | Explicit notification/profile launches | One hosted `NotificationFragment` | Notification destination | 3 | low |
| `MessageActivity` | Explicit profile launches | `MessagePageAdapter`, inbox/outbox | Messages destination with local mode state | 3 | medium |
| `AboutActivity` | Main menu action | One hosted `AboutFragment` | Utility destination | 3 | low |
| `ChangelogActivity` | About flow | One hosted `ChangelogFragment` | Utility destination | 3 | low |
| `LoggingActivity` | Main menu action | Local ViewModel screen | Utility destination | 3 | low |
| `SharedContentActivity` | `ACTION_SEND` text/plain | Bottom-sheet composer | Removed, `SharedContentFragment` | 8 | high |
| `WelcomeActivity` | Splash flow | Onboarding `ViewPager2` | Retained onboarding window boundary | 9 | medium |
| `ImagePreviewActivity` | Widget and utility launches | Preview window | Retained preview window boundary | 9 | medium |
| `GiphyPreviewActivity` | Composer launch | Preview window | Retained preview window boundary | 9 | low |
| `VideoPlayerActivity` | Text/widget launches, orientation config | Player window | Retained player window boundary | 9 | medium |

## Detailed entries

### `MainActivity`

- **Current callers:** `SplashActivity`; launcher routing; `WelcomeActivity`;
  shortcut and notification helpers. It also owns the shell
  routes for Settings, About, Logging, Login, Search, Feed, and Profile.
- **External entry points:** exported `singleTop` Activity with AniList filters
  for activity, studio, character, staff, actor, and user links. Receives
  shortcut and restored redirect state.
- **Current parameter contract:** legacy `KeyUtil.arg_redirect`; selected menu
  and page state are saved in the Activity bundle.
- **Returns result:** legacy `onActivityResult` path; exact consumers require
  migration audit.
- **Page adapter:** removed. Airing and Hub now use root-host destinations;
  the watch-list pages are explicit Navigation 2 destinations rather than
  pager entries.
- **Target:** shell Activity with primary `NavHostFragment`.
- **Target graph:** `nav_root.xml` (the single root graph; the former split
  graphs were merged into it).
- **Section model:** top-level destinations, not pager pages.
- **Selector:** drawer for top-level destinations; destination-owned selectors
  for local modes.
- **Back behavior:** must be explicitly defined before drawer cutover.
- **State restoration:** redirect intent, current root destination, and each
  destination's declared state owner.
- **Special behavior:** toolbar, drawer, authentication chrome, search chrome,
  notification permission, and window/insets handling remain in the shell.
- **Migration wave:** 1.
- **Removal criteria:** no feature rendering, page adapter ownership,
  repository access, or feature-specific state remains in the Activity.

### `SettingsActivity`

- **Current callers:** `MainActivity`, `ProfileActivity`, and the profile menu.
- **External entry points:** exported with `MainActivity` as parent; no manifest
  deep-link filter.
- **Current parameter contract:** `SettingsCategoryScreenParam` exists; the XML
  graph currently carries the `categoryId` string argument.
- **Returns result:** no known result contract.
- **Page adapter:** no. It hosts `NavHostFragment` using
  `@navigation/nav_settings_graph`.
- **Target:** settings graph included by the root host.
- **Target graph:** `nav_root.xml` (Settings destinations are direct children
  of the root graph; the former `nav_settings_graph.xml` is deleted).
- **Section model:** graph destinations, not local sections.
- **Selector:** settings hub actions.
- **Back behavior:** graph Up, then root destination.
- **State restoration:** Navigation 2 back stack and category argument.
- **Special behavior:** separate toolbar currently owned by the Activity.
- **Migration wave:** 4.
- **Removal criteria:** root host renders the graph with toolbar parity and all
  explicit Activity callers use the root destination.

### `SplashActivity`

- **Current callers:** launcher starts it; it starts `MainActivity` or
  `WelcomeActivity` according to onboarding state.
- **External entry points:** `MAIN` and `LAUNCHER`; shortcut metadata is attached
  to the launcher Activity.
- **Current parameter contract:** restored launch intent and onboarding state.
- **Returns result:** no known result contract.
- **Page adapter:** no.
- **Target:** retained launcher coordinator. It owns startup checks and chooses
  between onboarding and the root host.
- **Target graph:** none. It is an ingress boundary, not an application
  destination.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** launcher flow specific; do not infer from ordinary screens.
- **State restoration:** startup and onboarding state.
- **Special behavior:** startup window and launch timing.
- **Migration wave:** 9.
- **Removal criteria:** only if startup checks and onboarding routing move into
  a dedicated launcher flow with equivalent cold-start behavior.

### `LoginActivity`

- **Current callers:** `MainActivity`, `AvatarIndicatorView`, and explicit
  account actions.
- **External entry points:** exported `singleTop` `intent://com.mxt.anitrend`
  browser/OAuth return.
- **Current parameter contract:** OAuth intent data and authentication state.
- **Returns result:** caller behavior needs an explicit audit.
- **Page adapter:** no.
- **Target:** retained OAuth ingress boundary. The browser return is an
  external callback, not an ordinary application destination.
- **Target graph:** none while the translucent browser-return window remains.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** cancel returns to the invoking account-aware destination.
- **State restoration:** OAuth data and authentication transition.
- **Special behavior:** translucent theme and browser return.
- **Migration wave:** 9.
- **Removal criteria:** OAuth cold and warm paths have an equivalent root-host
  callback route with no lost browser return intent.

### `ProfileActivity` (migrated)

- **Current callers:** migrated to `MainActivity` route helpers. Media-list links
  use the root host and a typed `UserScreenParam`.
- **External entry points:** `https://anilist.co/user...` now resolve through
  `MainActivity` and the root Navigation 2 host.
- **Current parameter contract:** `UserScreenParam`, with legacy `arg_id` and
  `arg_userName` compatibility extras.
- **Returns result:** no.
- **Page adapter:** removed. The former overview, media-list feed, and text-feed
  pages are ordinary section controllers owned by one `ProfileFragment`.
- **Target:** `ProfileFragment` with local section state.
- **Target graph:** `nav_root.xml`.
- **Section model:** local overview, media-list, and text-feed state.
- **Selector:** local TabLayout selector, not a child Fragment or pager.
- **Back behavior:** standard detail destination Back.
- **State restoration:** `UserScreenParam`, selected section, and ViewModel state.
- **Special behavior:** profile toolbar/menu and authentication-dependent actions.
- **Migration wave:** 6, implemented on this branch.
- **Removal criteria:** implemented. Internal callers and web links route to the
  Fragment, legacy profile fragments and the page adapter are deleted, and the
  manifest Activity entry is gone. The `/user/<name>/animelist|mangalist`
  chains land on Profile and push a typed media list (NFR-001); the profile
  toolbar/menu auth gating is implemented (NFR-013), and its device-level
  confirmation remains an acceptance criterion (AC-EMU-09).

### `MediaActivity` (migrated)

- **Current callers:** media favourite, browse, media list, feed, studio media,
  notification, browse review, comment, search, character/staff role,
  recommendations, relation, format, and overview flows.
- **External entry points:** `https://anilist.co/anime...` and
  `https://anilist.co/manga...`.
- **Current parameter contract:** `MediaScreenParam`, with legacy `arg_id` and
  `arg_mediaType` compatibility extras.
- **Returns result:** menu and page state are Activity-owned; result payload
  contract is not observed.
- **Page adapter:** removed. The eight former pages are ordinary section
  controllers owned by `MediaFragment`.
- **Target:** `MediaFragment` with local section state and extracted section
  renderers.
- **Target graph:** `nav_root.xml`.
- **Section model:** overview, relations, recommendations, stats, characters,
  staff, feed, reviews. Section views are created and selected through one
  `sectionViewOrder` identical to `MediaSection.entries`; the invariant is
  asserted by `MediaFragmentSectionOrderTest` (NFR-008).
- **Selector:** horizontally scrolling selector or menu, not a fixed cramped
  chip row.
- **Back behavior:** standard detail destination Back.
- **State restoration:** `MediaScreenParam`, selected section, per-section UI
  state, and authentication-dependent availability.
- **Special behavior:** toolbar actions, trailer/player launches, and menu state.
- **Migration wave:** 6, implemented on this branch.
- **Removal criteria:** implemented. All callers and AniList links route to
  `MediaFragment`; the Activity, manifest entry, detail adapters, and obsolete
  pager resources are deleted.

### `CharacterActivity` (migrated)

- **Current callers:** character favourites, character search, media character
  and actor flows, and related detail navigation.
- **External entry points:** `https://anilist.co/character...`.
- **Current parameter contract:** `CharacterScreenParam`, with legacy `arg_id`.
- **Returns result:** legacy menu/activity behavior; no state result observed.
- **Page adapter:** formerly `CharacterPageAdapter`; it exposed overview, anime
  roles, manga roles, and actors.
- **Target:** `CharacterFragment` with local `Section` state and ordinary
  overview/list section renderers.
- **Target graph:** `nav_root.xml`.
- **Section model:** overview, anime roles, manga roles, actors.
- **Selector:** compact selector or menu, subject to screen width review.
- **Back behavior:** standard detail destination Back.
- **State restoration:** `CharacterScreenParam` and selected section.
- **Special behavior:** toolbar and related-media launches.
- **Migration wave:** 6, implemented on this branch.
- **Removal criteria:** implemented. Internal callers use the canonical
  Character route, `/character` ingress is handled by MainActivity, and the
  Activity, page adapter, manifest entry, and Character-only page fragments are
  removed. Character sections use ordinary list and overview renderers rather
  than child fragments.

### `StaffActivity` (migrated)

- **Current callers:** staff favourites, staff search, character actors, media
  staff, and role navigation.
- **External entry points:** `https://anilist.co/staff...` and
  `https://anilist.co/actor...`.
- **Current parameter contract:** `StaffScreenParam`, with legacy `arg_id`.
- **Returns result:** legacy menu/activity behavior; no state result observed.
- **Page adapter:** former `StaffPageAdapter` exposed overview, anime roles,
  manga roles, and staff roles. It has no remaining production consumer.
- **Target:** `StaffFragment` with local section state and ordinary section
  renderers/controllers.
- **Target graph:** `nav_root.xml`.
- **Section model:** overview, anime roles, manga roles, staff roles.
- **Selector:** horizontally scrolling selector or menu.
- **Back behavior:** standard detail destination Back.
- **State restoration:** `StaffScreenParam`, selected section, and auth visibility.
- **Special behavior:** page reload after authentication changes.
- **Migration wave:** 6, implemented on this branch.
- **Removal criteria:** implemented. All listed callers and web ingress route
  to `StaffFragment`; the Activity, manifest entry, page adapter, and former
  staff section fragments are removed.

### `StudioActivity`

- **Current callers:** studio favourites, studio search, and media overview.
- **External entry points:** `https://anilist.co/studio...`, now received by
  `MainActivity` and converted to `StudioScreenParam`.
- **Current parameter contract:** `StudioScreenParam`, with legacy `arg_id`.
- **Returns result:** no known result contract.
- **Page adapter:** no. It hosts one `StudioMediaFragment` through the legacy
  FragmentItem helper.
- **Target:** `StudioFragment` as the single logical screen.
- **Target graph:** `nav_root.xml`.
- **Section model:** none currently; one logical screen.
- **Selector:** none.
- **Back behavior:** standard detail destination Back.
- **State restoration:** `StudioScreenParam` and ViewModel state.
- **Special behavior:** metadata, favourite state, media pagination, sorting,
  sharing, and menu actions are owned by the unified Fragment. No child
  FragmentManager remains.
- **Migration wave:** 6, implemented on this branch.
- **Removal criteria:** implemented. Internal callers and `/studio` ingress use
  the root destination, and the legacy Activity and hosted media Fragment are
  deleted.

### `CommentActivity`

- **Current callers:** `FeedListFragment`, `NotificationFragment`, and comment
  related UI.
- **External entry points:** `https://anilist.co/activity...`.
- **Current parameter contract:** legacy Fragment extras; `CommentScreenParam`
  is the target typed contract.
- **Returns result:** no. The former complete `FeedList` result path was removed;
  the destination now relies on canonical state observation.
- **Page adapter:** no. It hosts `CommentFragment`.
- **Target:** `CommentFragment` as a root-host destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** standard detail Back with no result payload.
- **State restoration:** `CommentScreenParam`; committed mutations are observed
  through the canonical store.
- **Special behavior:** external `/activity` links are received by
  `MainActivity`, converted to `CommentScreenParam`, and opened in the root
  detail graph.
- **Migration wave:** 7, external routing in 8, implemented on this branch.
- **Removal criteria:** implemented. Feed and notification callers use the
  destination, the complete `FeedList` result path is deleted, and the manifest
  Activity entry is gone.

### `FavouriteActivity` (migrated)

- **Current callers:** profile overview statistics, About panel compatibility
  UI, and Android entry-point fixtures.
- **External entry points:** none after the exported Activity entry was removed.
- **Current parameter contract:** typed `UserScreenParam` with legacy `arg_id`
  and `arg_userName` compatibility extras.
- **Returns result:** no result payload.
- **Page adapter:** removed. `FavouriteFragment` uses one RecyclerView and a
  type-erased section adapter while existing item adapters retain their binding
  implementations.
- **Target:** `FavouriteFragment` with local anime, character, manga, staff,
  and studio section state.
- **Target graph:** `nav_root.xml`.
- **Section model:** five mutually exclusive local sections.
- **Selector:** horizontally scrolling Material 3 chips, because five compact
  categories fit the selector without creating navigation destinations.
- **Back behavior:** standard detail destination Back with no result payload.
- **State restoration:** typed user identity, selected section, pagination, and
  current section adapter state.
- **Special behavior:** media long-press actions remain owned by the Fragment;
  item adapters only emit callbacks.
- **Migration wave:** 5, implemented on this branch.
- **Removal criteria:** implemented. Profile and About UI emit callbacks to the
  root host, the Activity and page adapter are deleted, and the manifest entry
  is gone.

### `MediaListActivity`

- **Current callers:** profile statistics, shortcuts, and MainActivity's My
  Anime/My Manga drawer entries route through `MainActivity`.
- **External entry points:** none after the exported Activity entry was removed.
- **Current parameter contract:** typed `UserScreenParam` plus optional media type;
  legacy user extras remain accepted by the compatibility route.
- **Returns result:** no.
- **Page adapter:** removed. The six status pages are now one
  `MediaListFragment` with local status selection.
- **Target:** `MediaListFragment` with local status selection.
- **Target graph:** `nav_root.xml`.
- **Section model:** media-list statuses.
- **Selector:** toolbar status menu, suitable for the six-status screen.
- **Back behavior:** route-origin contract: root for drawer My Anime/My Manga,
  pushed for profile stats, `ROUTE_MEDIA_LIST` ingress, shortcuts, and the
  `/user/<name>/animelist|mangalist` chains (NFR-002, producer table in the
  audit).
- **State restoration:** user identity, status, filters, and scroll state.
- **Special behavior:** canonical media-list state rules apply; the NFR-007
  stale media type restore was reproduced in both drawer-switch directions and
  fixed for ROOT media-list navigation by disabling `restoreState`
  (`mediaListRootDestinationOptions`, NavigationDestinations.kt lines
  258-262, asserted by `MediaListSavedStateNavigationTest`).
  Rotation/process-death and PUSHED-origin interaction remain explicit
  follow-up under AC-EMU-02 and G-013.
- **Migration wave:** 5, implemented on this branch.
- **Removal criteria:** implemented. The Activity, manifest entry, and index
  page adapter are deleted.

### `MediaBrowseActivity`

- **Current callers:** `MediaBrowseUtil`, media overview/statistics utility flows.
- **External entry points:** exported `singleTop`; no manifest deep-link filter.
- **Current parameter contract:** legacy browse bundle.
- **Returns result:** unknown; audit before removal.
- **Page adapter:** no. It hosts one `MediaBrowseFragment`.
- **Target:** browse destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none currently; browse filters are ViewModel state.
- **Selector:** filter chips and existing browse controls.
- **Back behavior:** standard browse Back.
- **State restoration:** browse filter state and semantically absent GraphQL lists.
- **Special behavior:** utility launch helpers.
- **Migration wave:** 5.
- **Removal criteria:** all utility launches use Navigation 2 and the Activity
  manifest entry is removed.

### `SearchActivity` (migrated)

- **Current callers:** MainActivity toolbar search and the application search
  shortcut route through `MainActivity.ROUTE_SEARCH`.
- **External entry points:** no direct Activity entry remains; shortcut ingress
  is handled by `MainActivity`.
- **Current parameter contract:** legacy `KeyUtil.arg_search` query scalar,
  which is presentation input rather than entity identity.
- **Returns result:** no result payload.
- **Page adapter:** removed. `SearchFragment` uses one RecyclerView and a
  type-erased section adapter for ordinary search modes plus a Paging adapter
  for media modes.
- **Target:** `SearchFragment` with local mode state.
- **Target graph:** `nav_root.xml`.
- **Section model:** media anime, media manga, studio, staff, character, user.
- **Selector:** horizontally scrolling Material 3 chips sized for six modes;
  users are hidden when unauthenticated.
- **Back behavior:** standard search Back and query restoration.
- **State restoration:** query, mode, filters, and loading state.
- **Special behavior:** MainActivity owns search chrome and the Fragment owns
  result rendering. Media result clicks navigate to `MediaFragment` through the
  root Navigation 2 host.
- **Migration wave:** 5, implemented on this branch.
- **Removal criteria:** implemented for the SearchActivity boundary. All search
  callers use Navigation 2, the old Activity and page adapter are deleted, and
  the manifest entry is gone. MainActivity's legacy live filtering remains part
  of the top-level search bridge and is tracked separately.

### `NotificationActivity`

- **Current callers:** profile actions, notification utility, shortcuts, and
  avatar/profile flows.
- **External entry points:** exported `singleTop`; no web filter.
- **Current parameter contract:** no typed ScreenParam observed; notification
  identity and restored intent require audit.
- **Returns result:** unknown.
- **Page adapter:** no. It hosts `NotificationFragment`.
- **Target:** notification destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** standard account Back.
- **State restoration:** notification route and unread state.
- **Special behavior:** notification intent delivery.
- **Migration wave:** 3.
- **Removal criteria:** notification and shortcut routes are verified cold and warm.

### `MessageActivity`

- **Current callers:** `ProfileActivity` and message actions.
- **External entry points:** exported; no manifest deep-link filter.
- **Current parameter contract:** legacy user/message extras; audit required.
- **Returns result:** unknown.
- **Page adapter:** yes. `MessagePageAdapter` exposes inbox and outbox modes.
- **Target:** messages destination with local mode state.
- **Target graph:** `nav_root.xml`.
- **Section model:** inbox, outbox.
- **Selector:** compact selector or segmented control.
- **Back behavior:** standard account Back.
- **State restoration:** selected mailbox and list state.
- **Special behavior:** authentication-sensitive data.
- **Migration wave:** 3.
- **Removal criteria:** profile and other callers use the destination.

### `AboutActivity`

- **Current callers:** `MainActivity` menu.
- **External entry points:** exported `singleTop`; no deep-link filter.
- **Current parameter contract:** none.
- **Returns result:** no known result.
- **Page adapter:** no. It hosts `AboutFragment`.
- **Target:** utility Fragment destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** standard Up and Back.
- **State restoration:** static content and child navigation as needed.
- **Special behavior:** external links remain ordinary actions.
- **Migration wave:** 3.
- **Removal criteria:** MainActivity menu routes to the utility destination.

### `ChangelogActivity`

- **Current callers:** `MainActivity` and `AboutFragment`.
- **External entry points:** not exported.
- **Current parameter contract:** none.
- **Returns result:** no known result.
- **Page adapter:** no. It hosts `ChangelogFragment`.
- **Target:** utility Fragment destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** standard utility Back.
- **State restoration:** static changelog scroll state.
- **Special behavior:** currently entered from About.
- **Migration wave:** 3.
- **Removal criteria:** About flow navigates inside the root host.

### `LoggingActivity`

- **Current callers:** `MainActivity` menu and the diagnostic route shim.
- **External entry points:** none. The former exported Activity entry was not a
  public ingress contract.
- **Current parameter contract:** none.
- **Returns result:** no known result.
- **Page adapter:** no. It is a local ViewModel-first screen.
- **Target:** `LoggingFragment` utility destination.
- **Target graph:** `nav_root.xml`.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** standard utility Back.
- **State restoration:** existing ViewModel state.
- **Special behavior:** preserve log collection lifecycle, menu actions, share
  file creation, and the legacy storage permission path inside the Fragment.
- **Migration wave:** 3, implemented on this branch.
- **Removal criteria:** implemented. MainActivity routes the menu action
  through `ROUTE_LOGGING`, and the legacy Activity and manifest entry are
  deleted.

### `SharedContentActivity`

- **Current callers:** Android `ACTION_SEND` ingress, now routed to MainActivity.
- **External entry points:** `MainActivity` handles exported `ACTION_SEND`
  `text/plain` with `singleTop` delivery.
- **Current parameter contract:** shared text and optional subject copied into
  `SharedContentFragment` arguments.
- **Returns result:** no result payload.
- **Page adapter:** no. It presents composer UI and a Giphy bottom sheet.
- **Target:** `SharedContentFragment` in the root utility graph.
- **Target graph:** `nav_root.xml`.
- **Section model:** composer state, not a pager.
- **Selector:** actions and bottom sheet.
- **Back behavior:** cancel returns to the previous destination.
- **State restoration:** shared text and composer `FeedComposerScreenParam`.
- **Special behavior:** external chooser and composer bottom sheet behavior are
  preserved inside the Fragment.
- **Migration wave:** 8, implemented on this branch.
- **Removal criteria:** implemented. Share intents route through MainActivity
  without losing text or subject, the legacy Activity is deleted, and dismissal
  pops through the NavController with back-stack and task assertions in
  `SharedContentDismissalTest` (NFR-005/NFR-011).

### `WelcomeActivity`

- **Current callers:** `SplashActivity`.
- **External entry points:** exported `singleTop`; no deep-link filter.
- **Current parameter contract:** onboarding state.
- **Returns result:** no known result.
- **Page adapter:** no page adapter class, but it owns an onboarding
  `ViewPager2`.
- **Target:** retained onboarding window boundary. Its `ViewPager2` is the
  onboarding interaction itself, not application navigation.
- **Target graph:** none.
- **Section model:** onboarding pages if retained.
- **Selector:** onboarding controls.
- **Back behavior:** onboarding-specific.
- **State restoration:** onboarding completion and page.
- **Special behavior:** startup and theme behavior.
- **Migration wave:** 9.
- **Removal criteria:** only after startup parity and onboarding window parity
  are proven by a separately approved migration.

### `ImagePreviewActivity`

- **Current callers:** `CompatUtil`, `StatusContentWidget`, and preview actions.
- **External entry points:** exported `singleTop`; no deep-link filter.
- **Current parameter contract:** `ImagePreviewScreenParam` with URL identity.
- **Returns result:** share action only; no application state result observed.
- **Page adapter:** no.
- **Target:** retained preview window boundary. Fullscreen behavior, download
  permissions, and large-image memory behavior are window-specific.
- **Target graph:** none while retained.
- **Section model:** none.
- **Selector:** actions.
- **Back behavior:** standard preview Back.
- **State restoration:** preview URL and zoom state.
- **Special behavior:** share and external viewer actions.
- **Migration wave:** 9.
- **Removal criteria:** a separately approved root-host preview design preserves
  window, permission, and memory behavior.

### `GiphyPreviewActivity`

- **Current callers:** `BottomSheetGiphy`.
- **External entry points:** exported `singleTop`; no deep-link filter.
- **Current parameter contract:** `GiphyPreviewScreenParam` with URL identity.
- **Returns result:** no known application state result.
- **Page adapter:** no.
- **Target:** retained preview window boundary.
- **Target graph:** none while retained.
- **Section model:** none.
- **Selector:** none.
- **Back behavior:** return to composer.
- **State restoration:** Giphy URL.
- **Special behavior:** preview window behavior.
- **Migration wave:** 9.
- **Removal criteria:** a separately approved root-host preview design preserves
  composer and preview lifecycle parity.

### `VideoPlayerActivity`

- **Current callers:** `StatusContentWidget` and `TextConfigurationPlugin`.
- **External entry points:** exported `singleTop`; no deep-link filter.
- **Current parameter contract:** `VideoPlayerScreenParam` with URL identity.
- **Returns result:** no known application state result.
- **Page adapter:** no.
- **Target:** retain as a genuine window boundary unless a separate player
  migration is approved.
- **Target graph:** none required while retained.
- **Section model:** none.
- **Selector:** player controls.
- **Back behavior:** player-specific.
- **State restoration:** player URL, playback, and orientation.
- **Special behavior:** explicit orientation/configuration handling in the
  manifest is a retention reason.
- **Migration wave:** 9.
- **Removal criteria:** a separately approved player/window design replaces the
  current behavior; activity count is not sufficient.

## Pager classification baseline

The current detail page adapters expose these entries. This table is a
classification proposal to be confirmed during the relevant migration wave,
not an implementation instruction to create child Fragments.

| Adapter | Current entries | Initial classification |
| --- | --- | --- |
| `AnimePageAdapter` | Overview, relations, recommendations, stats, characters, staff, feed, reviews | Local `MediaSection` state |
| `MangaPageAdapter` (index) | Manga list and recently added | `MangaFragment` local section state; adapter removed from the home route |
| Former `CharacterPageAdapter` | Overview, anime roles, manga roles, actors | Local `CharacterSection` state in `CharacterFragment` |
| Former `StaffPageAdapter` | Overview, anime roles, manga roles, staff roles | Local `StaffSection` state in `StaffFragment` |
| `ProfilePageAdapter` | Overview, media list feed, text feed | Local `ProfileSection` state |
| `FavouritePageAdapter` | Anime, characters, manga, staff, studios | Local category state unless product review assigns a destination identity |
| `MessagePageAdapter` | Inbox, outbox | Local mailbox state |
| `SearchPageAdapter` | Anime, manga, studio, staff, character, user | Local search mode state |
| `MediaListPageAdapter` | Media-list statuses | `MediaListFragment` local status state; adapter removed from the home route |
| `FeedPageAdapter` | Media list, text, mixed | `FeedFragment` local section state; adapter removed from the home route |
| `AiringPageAdapter` | Airing list, watch list | `AiringFragment` root destination plus `WatchListFragment` feed destination; adapter removed |
| `HubPageAdapter` | Suggestions, watch list | `HubFragment` root destination plus `WatchListFragment` feed destination; adapter removed |
| `ReviewPageAdapter` | Anime reviews, manga reviews | `ReviewBrowseFragment` local section state; adapter removed from the reviews route |
| `SeasonPageAdapter` | Winter, spring, summer, fall | `AnimeFragment` local section state; adapter removed from the home route |
| `TrendingPageAdapter` | Anime trending, manga trending, recently added | `TrendingFragment` local section state; adapter removed from the home route |

## Final audit and retained boundaries

- Media detail pager consolidation is implemented in the working tree (eight
  section controllers on one `MediaFragment` with a single `sectionViewOrder`).
  Device-level behavioral parity remains a runtime validation activity
  (AC-EMU-01, FW-002), not an unimplemented navigation phase.
- The obsolete `MainActivity.onActivityResult` voice-search bridge was removed;
  no result-based navigation or state synchronization contract remains.
- Authentication-dependent section availability is enforced by each migrated
  Fragment's local state (asserted for `MediaSection` in
  `MediaFragmentSectionOrderTest` and `MediaFragmentArgsTest`) and remains
  covered by the destination test matrix.
- The stale GitNexus index must not be treated as authoritative until it is
  regenerated in an environment that can write its registry.
- The verified destination count is 28 fragment destinations in `nav_root.xml`
  (AC-DOC-05). All navigation-refactor documents use this count.

The current source intentionally retains `CommonActivity` as a compatibility
base class, plus the manifest Activity boundaries `MainActivity`,
`SplashActivity`, `LoginActivity`, `WelcomeActivity`, `ImagePreviewActivity`,
`GiphyPreviewActivity`, and `VideoPlayerActivity`, as documented platform or
window-specific surfaces. Ordinary internal navigation does not use these
Activities.

### Retained boundaries with explicit status

Each boundary below is intentional or transitional and carries an explicit
status (keep, or tracked follow-up). They are not defects of this migration;
future work must not mistake a boundary for a leftover (AC-DOC-06, audit
Section 9).

| Boundary | Role | Status | Tracked as |
| --- | --- | --- | --- |
| `CommonActivity` | Compatibility shell extended by `MainActivity`, `LoginActivity`, `SplashActivity` | Keep | FW-005 |
| `FragmentBaseList` and `RecyclerViewAdapter` | Legacy fragment and list bases used by `MediaListFragment`, `SearchFragment`, `MediaBrowseFragment`, `AiringListFragment`, `FeedListFragment`, `SuggestionListFragment`, `WatchListFragment`; `RecyclerViewAdapter` used by `SearchFragment` | Keep for now; removal is infrastructure debt | state-synchronization notes |
| Live search bridge (`applySearchToAllListFragments`) | Legacy live-filter bridge reaching `FragmentBaseList` instances | Keep; requires an explicit replacement decision before removal | audit Section 9 item 3 |
| `KeyUtil.*_REQ` request routing | Used by `MediaFormatViewModel`, `AiringListViewModel`, `BrowseReviewViewModel`, `MediaBrowseFragment`, and others | Keep; no new uses, migrate when touched | audit Section 9 item 4 |
| Retained manifest Activities (`SplashActivity`, `LoginActivity`, `WelcomeActivity`, `ImagePreviewActivity`, `GiphyPreviewActivity`, `VideoPlayerActivity`) | Launcher, OAuth, onboarding, preview, player boundaries | Keep with documented roles | FW-003, FW-004 |
| `MediaFragment` `childFragmentManager` | Embedded `YouTubeEmbedFragment` preview surface only | Keep; platform-backed preview, not navigation | audit Section 9 item 6 |
| `WelcomeActivity` `ViewPager2` | Onboarding interaction, not application navigation | Keep | FW-004 |

The follow-up decisions and validation tasks for these retained boundaries are
tracked in [`future-work.md`](future-work.md), especially FW-001 through
FW-005. This inventory remains the source of truth for each Activity's current
contract; the backlog records the next decision or evidence required before a
boundary is changed.
