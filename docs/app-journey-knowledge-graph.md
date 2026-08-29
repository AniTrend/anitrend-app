# AniTrend App Journey Knowledge Graph

## Purpose
This document maps every screen in the AniTrend Android app, how users navigate between them, and what entry points trigger each screen. It is designed as a reference for future UAT automation; each screen node has enough metadata to construct automated test flows.

## Taxonomy
- **Screen**: A distinct user-facing UI state with its own content, data fetching, and navigation affordances. A screen may be a destination Fragment in the MainActivity Navigation 2 host (e.g., `MediaFragment`), a retained boundary Activity (e.g., `LoginActivity`, `ImagePreviewActivity`), or a local section within a destination (e.g., the seasonal sections of `AnimeFragment`).
- **Activity**: Android component (one manifest entry, one Activity subclass). After the navigation migration, ordinary internal navigation uses Fragment destinations; only launcher, OAuth, onboarding, preview, and player boundaries remain as Activities.
- **Navigation edge**: A user action that transitions from one screen to another (tap, swipe, deep link, back press).

> Migration note (17 August 2026): this graph was reconciled with the
> navigation migration on branch `refactor/navigation-inventory-foundation`.
> Screens that were Activities (Character, Staff, Studio, Profile, Comment,
> Settings, About, Logging, Notifications, Messages, Favourites, Media List,
> Browse, Shared Content) are now Fragment destinations in the MainActivity
> root graph (`nav_root.xml`, 28 destinations). The device walkthrough in
> Section 5 is historical evidence from before the migration, not a current
> verification. See `docs/architecture/navigation-refactor/full-wrap-up-audit.md`
> for the wrap-up record.

---

## 1. Screen Inventory

### 1.1 SplashActivity
| Field | Value |
|-------|-------|
| **Activity** | `SplashActivity` |
| **Entry points** | LAUNCHER (main entry), shortcut |
| **Description** | App entry point. Checks auth state. Routes to WelcomeActivity (fresh install), LoginActivity (unauthenticated), or MainActivity (authenticated). |
| **Auth required** | No |
| **Next navigation** | → WelcomeActivity (fresh), → LoginActivity (no auth), → MainActivity (auth'd) |
| **Notes** | Also shows migration failure dialog if DB migration fails |

### 1.2 WelcomeActivity
| Field | Value |
|-------|-------|
| **Activity** | `WelcomeActivity` |
| **Entry points** | SplashActivity (fresh install) |
| **Description** | Onboarding flow with 4 cards introducing the app |
| **Auth required** | No |
| **Next navigation** | → MainActivity |

### 1.3 LoginActivity
| Field | Value |
|-------|-------|
| **Activity** | `LoginActivity` |
| **Entry points** | SplashActivity, MainActivity drawer "Sign In", deep link `intent://com.mxt.anitrend`, drawer header banner tap |
| **Deep link** | `intent://com.mxt.anitrend` (ACTION=VIEW, browsable) |
| **Description** | OAuth login via AniList. Opens web view for auth, receives callback via deep link. |
| **Auth required** | No (auth target) |
| **Next navigation** | → MainActivity (on success) |

### 1.4 MainActivity (Hub), 9 screens

The main hub with a navigation drawer and one primary Navigation 2 host. Each
drawer item maps to a root destination or a destination-owned local section.

| Drawer Item | Destination | Local Sections | Auth Req |
|-------------|--------|---------|------|:--------:|
| Home Feed | `FeedFragment` | Local PROGRESS section | Local STATUS section | Local PUBLIC_STATUS section | NO |
| Anime | `AnimeFragment` | Local WINTER section | Local SPRING section | Local SUMMER section | Local FALL section | NO |
| Manga | `MangaFragment` | Local MANGA_LIST section | Local RECENTLY_ADDED section | NO |
| Trending | `TrendingFragment` | Local ANIME section | Local MANGA section | Local RECENTLY_ADDED section | NO |
| Reviews | `ReviewBrowseFragment` | Local ANIME section | Local MANGA section | NO |
| Airing | `AiringFragment` | Local airing list | `WatchListFragment` for latest episodes | YES |
| Hub | `HubFragment` | Local suggestions list | `WatchListFragment` for most popular | YES |
| My Anime | `MediaListFragment` | Local status selector: CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING | YES |
| My Manga | `MediaListFragment` | Local status selector: CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING | YES |

**Entry points**: SplashActivity, LoginActivity (on success), back navigation
**App bar actions**: Search, Filter (season-dependent), Overflow menu
**Drawer actions**: Sign In, Sign Out, Check Update, Banner tap (Profile if auth'd, Login if not)

### 1.5 MediaFragment (Detail screen)

| Field | Value |
|-------|-------|
| **Destination** | `MediaFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/anime*`, `/manga*`), card tap from any browse/list screen, search result tap |
| **Deep link** | `https://anilist.co/anime*` and `https://anilist.co/manga*` enter MainActivity and navigate with `MediaScreenParam` |
| **Sections (unauthenticated)** | OVERVIEW, RELATIONS, RECOMMENDATIONS, STATS, CHARACTERS, STAFF |
| **Sections (authenticated)** | OVERVIEW, RELATIONS, RECOMMENDATIONS, STATS, CHARACTERS, STAFF, **FEED**, **REVIEWS** |
| **Auth dependency** | FEED and REVIEWS sections are hidden when unauthenticated (asserted in `MediaFragmentSectionOrderTest` and `MediaFragmentArgsTest`) |
| **Toolbar actions** | MyAnimeList, Favourite, Manage, Share |

### 1.6 CharacterFragment

| Field | Value |
|-------|-------|
| **Destination** | `CharacterFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/character*`), card tap from character lists/search, MediaFragment Characters section |
| **Deep link** | `https://anilist.co/character*` enters MainActivity and navigates with `CharacterScreenParam` |
| **Sections** | OVERVIEW, ANIME ROLES, MANGA ROLES, ACTORS |
| **Toolbar actions** | Share |

### 1.7 StaffFragment

| Field | Value |
|-------|-------|
| **Destination** | `StaffFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/staff*`, `/actor*`), card tap from staff lists/search, MediaFragment Staff section |
| **Deep link** | `https://anilist.co/staff*` and `https://anilist.co/actor*` enter MainActivity and navigate with `StaffScreenParam` |
| **Sections** | OVERVIEW, ANIME ROLES, MANGA ROLES, STAFF ROLES (some filtering disabled when not auth'd) |
| **Toolbar actions** | Share, Filter |

### 1.8 StudioFragment

| Field | Value |
|-------|-------|
| **Destination** | `StudioFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/studio*`), card tap from studio lists/search |
| **Deep link** | `https://anilist.co/studio*` enters MainActivity and navigates with `StudioScreenParam` |
| **Content** | Grid of anime/manga produced by the studio |
| **Toolbar actions** | Share, Filter |

### 1.9 ProfileFragment

| Field | Value |
|-------|-------|
| **Destination** | `ProfileFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/user*`), drawer header banner tap (if auth'd), search Users tab, shortcut |
| **Deep link** | `https://anilist.co/user*` enters MainActivity and navigates with `UserScreenParam`; `/user/<name>/animelist` and `/mangalist` land on Profile and push a typed media list (NFR-001) |
| **Sections** | OVERVIEW, MEDIA-LIST FEED, TEXT FEED |
| **Toolbar actions** | Messages (if auth'd), Share |

### 1.10 CommentFragment

| Field | Value |
|-------|-------|
| **Destination** | `CommentFragment` in the MainActivity Navigation 2 host |
| **Entry points** | MainActivity deep-link ingress (`/activity*`), feed post tap |
| **Deep link** | `https://anilist.co/activity*` enters MainActivity and navigates with `CommentScreenParam` |
| **Content** | Activity feed with comment composer |
| **Toolbar actions** | Share |
| **Composer** | Text field + media insertion buttons (image, webm, link, youtube, giphy) |

### 1.11 SearchFragment

| Field | Value |
|-------|-------|
| **Destination** | `SearchFragment` in the MainActivity Navigation 2 host |
| **Entry points** | App bar Search button, shortcut |
| **Sections (unauthenticated)** | ANIME, MANGA, STUDIO, STAFF, CHARACTERS |
| **Sections (authenticated)** | ANIME, MANGA, STUDIO, STAFF, CHARACTERS, **USERS** |
| **Auth dependency** | Users section appears only when authenticated |

### 1.12 Settings destinations

| Field | Value |
|-------|-------|
| **Destinations** | `settingsHubFragment`, `accountSettingsFragment`, `customizeSettingsFragment`, `settingsCategoryFragment` in the MainActivity Navigation 2 host |
| **Entry points** | App bar overflow → Settings |
| **Description** | App settings (theme, lists, notifications, etc.), hosted in the root graph since the former `SettingsActivity` and `nav_settings_graph.xml` were removed |

### 1.13 AboutFragment

| Field | Value |
|-------|-------|
| **Destination** | `AboutFragment` in the MainActivity Navigation 2 host |
| **Entry points** | App bar overflow → About |

### 1.14 LoggingFragment

| Field | Value |
|-------|-------|
| **Destination** | `LoggingFragment` in the MainActivity Navigation 2 host |
| **Entry points** | App bar overflow → Bug Report |

### 1.15 Other Screens

| Screen | Entry Point | Description |
|----------|-------------|-------------|
| `NotificationFragment` | Shortcut, avatar tap, MainActivity programmatic | Notifications list |
| `MessageFragment` | Profile message action | Messages/composer |
| `FavouriteFragment` | ProfileFragment (Favourites tap) | User favourites browser |
| `MediaListFragment` | Drawer My Anime/My Manga (root origin), ProfileFragment stats, shortcuts, deep-link chains (pushed origin) | User media list |
| `MediaBrowseFragment` | MainActivity app bar Filter | Advanced media browse/filter |
| `SharedContentFragment` | ACTION_SEND text/plain | External share to create feed post |
| `ImagePreviewActivity` | Programmatic (image tap) | Full-screen image viewer (retained boundary) |
| `GiphyPreviewActivity` | Programmatic (GIPHY button) | GIPHY sticker/gif picker (retained boundary) |
| `VideoPlayerActivity` | Programmatic | Video playback (retained boundary) |

---

## 2. Navigation Edges

### 2.1 Launch Flow
```
App Launch (LAUNCHER)
  → SplashActivity
      → [Fresh install] WelcomeActivity → MainActivity
      → [No auth] → MainActivity (unauthenticated)
      → [Has auth] → MainActivity (authenticated)
```

### 2.2 Auth Flow (Sign In / Sign Out)
```
[Sign In] Drawer/Header "Sign In"
  → LoginActivity (OAuth web view)
      → [Success] → MainActivity (authenticated)
      → [Failure] → back to previous

[Sign Out] Drawer "Sign Out"
  → BottomSheetMessage (confirmation dialog)
      → [Confirm] → SplashActivity (clears auth)
```

### 2.3 Unauthenticated Hub Navigation
```
MainActivity (unauthenticated)
  ├── Drawer "Discover Anime" → AnimeFragment (local seasonal sections)
  ├── Drawer "Discover Manga" → MangaFragment (local sections)
  ├── Drawer "What's Trending" → TrendingFragment (local sections)
  ├── Drawer "Series Reviews" → ReviewBrowseFragment (local sections)
  ├── App bar "Search" → SearchFragment (local sections)
  ├── App bar "Overflow" → Settings / About / Bug Report / Discord / Donate
  └── Drawer "Sign In" → LoginActivity
```

### 2.4 Content Detail Navigation
```
[Any card tap on browse/list/search]
  └── MediaFragment (anime/manga)
  └── CharacterFragment
  └── StaffFragment
  └── StudioFragment
  └── ProfileFragment (user)
  └── CommentFragment (activity feed post)

MediaFragment local sections:
  ├── Overview → inline content
  ├── Relations → list of related media → tap → MediaFragment
  ├── Recommendations → list → tap → MediaFragment
  ├── Stats [auth'd only] → inline charts
  ├── Characters [auth'd only] → list → tap → CharacterFragment
  ├── Staff [auth'd only] → list → tap → StaffFragment
  ├── Feed [auth'd only] → activity feed posts
  └── Reviews [auth'd only] → list → tap → review detail
```
All deep links in this section enter through MainActivity, which dispatches the
typed `ScreenParam` into the root navigation graph.

### 2.5 Authenticated Hub Navigation
```
MainActivity (authenticated, additional items visible)
  ├── Drawer "Home Feed" → FeedFragment (local sections)
  ├── Drawer "Airing" → AiringFragment, then WatchListFragment for latest episodes
  ├── Drawer "Hub" → HubFragment, then WatchListFragment for most popular
  ├── Drawer "My Anime" → MediaListFragment (root origin, local status selector)
  ├── Drawer "My Manga" → MediaListFragment (root origin, local status selector)
  └── Header banner → ProfileFragment (own profile)
```
Drawer My Anime/My Manga are the only root-origin media-list producers; every
other producer (profile stats, shortcuts, deep-link chains) pushes the media
list with caller-back semantics (NFR-002 in the wrap-up audit).

---

## 3. Entry Points Reference

### 3.1 Deep Links (for UAT automation)
Use `adb shell am start -a android.intent.action.VIEW -d "<url>" com.mxt.anitrend`

All links below enter MainActivity, which dispatches the typed ScreenParam into
the root navigation graph. Cold-start destination assertions exist in
`MainActivityExternalIngressTest` for `/anime`, `/activity`, `/user`, and the
`/user/<name>/animelist|mangalist` chains; cold-start coverage for `/manga`,
`/character`, `/staff`, `/actor`, and `/studio` is still an acceptance
criterion (G-001 in the wrap-up audit), not claimed evidence.

| Screen | Deep Link Pattern | Example |
|--------|------------------|---------|
| MediaFragment (anime) | `https://anilist.co/anime/<id>` | `/anime/1` (Cowboy Bebop) |
| MediaFragment (manga) | `https://anilist.co/manga/<id>` | `/manga/1` (Monster) |
| CharacterFragment | `https://anilist.co/character/<id>` | `/character/1` (Spike Spiegel) |
| StaffFragment | `https://anilist.co/staff/<id>` | `/staff/1` (Shinichirō Watanabe) |
| StaffFragment | `https://anilist.co/actor/<id>` | `/actor/1` |
| StudioFragment | `https://anilist.co/studio/<id>` | `/studio/1` (Madhouse) |
| ProfileFragment | `https://anilist.co/user/<name>` | `/user/Max` |
| ProfileFragment | `https://anilist.co/user/<id>` | `/user/1` |
| ProfileFragment then pushed MediaListFragment | `https://anilist.co/user/<name>/animelist` | `/user/Max/animelist` |
| ProfileFragment then pushed MediaListFragment | `https://anilist.co/user/<name>/mangalist` | `/user/Max/mangalist` |
| CommentFragment | `https://anilist.co/activity/<id>` | `/activity/<valid_id>` |
| LoginActivity | `intent://com.mxt.anitrend` | (OAuth callback) |

### 3.2 App Shortcuts
Shortcut types defined in `KeyUtil`; every dynamic shortcut now carries the
`EXTRA_ROUTE` wire value consumed by MainActivity (NFR-004 in the wrap-up
audit). Emulator launch verification for all shortcut types remains an
acceptance criterion (AC-EMU-04), not claimed evidence.

| Shortcut | ID | Action | Auth |
|----------|----|--------|:----:|
| Search | `SHORTCUT_SEARCH` | → MainActivity(search route) → SearchFragment | NO |
| Trending | `SHORTCUT_TRENDING` | → MainActivity(trending route) → TrendingFragment | NO |
| Notifications | `SHORTCUT_NOTIFICATION` | → MainActivity(notifications route) → NotificationFragment | YES |
| Airing | `SHORTCUT_AIRING` | → MainActivity(airing route) → AiringFragment | YES |
| My Anime | `SHORTCUT_MY_ANIME` | → MainActivity(media_list route) → pushed MediaListFragment | YES |
| My Manga | `SHORTCUT_MY_MANGA` | → MainActivity(media_list route) → pushed MediaListFragment | YES |
| Feeds | `SHORTCUT_FEEDS` | → MainActivity(feed route) → FeedFragment | YES |
| Profile | `SHORTCUT_PROFILE` | → MainActivity(profile route) → ProfileFragment (current user) | YES |

### 3.3 Intent-Based Entry
- **redirectShortcut**: Add `arg_redirect` (int) extra to Intent for MainActivity with a drawer menu resource ID to jump directly to a section.
- **ACTION_SEND**: `Intent.ACTION_SEND` with MIME `text/plain` opens SharedContentFragment through MainActivity; dismissal pops through the NavController and returns to the previous destination (NFR-005/NFR-011).

---

## 4. Auth Dependencies

| Screen / Tab | Auth Required | Visible When Unauthenticated? | Unlock Trigger |
|-------------|:------------:|:----------------------------:|:--------------:|
| Home Feed (drawer) | YES | Hidden | Sign in |
| Airing (drawer) | YES | Hidden | Sign in |
| Hub (drawer) | YES | Hidden | Sign in |
| My Anime (drawer) | YES | Hidden | Sign in |
| My Manga (drawer) | YES | Hidden | Sign in |
| Search > Users section | YES | Hidden | Sign in (appears while on Search) |
| MediaFragment > Feed section | YES | Hidden | Sign in |
| MediaFragment > Reviews section | YES | Hidden | Sign in |
| Notifications shortcut | YES | N/A (shortcut) | Sign in |
| Profile shortcut | YES | N/A (shortcut) | Sign in |
| My Anime shortcut | YES | N/A (shortcut) | Sign in |
| My Manga shortcut | YES | N/A (shortcut) | Sign in |
| Airing shortcut | YES | N/A (shortcut) | Sign in |
| Feeds shortcut | YES | N/A (shortcut) | Sign in |
| Message action on ProfileFragment | YES | Hidden when unauthenticated; menu visibility and the composer handler are auth-gated (NFR-013, fixed in the wrap-up audit) | Sign in |
| LoginActivity | No | Always | N/A (login page itself) |

---

## 5. Device Walkthrough Verification Results

Historical record: verified on an API 35 emulator (unauthenticated state) on
2026-06-25, before the navigation migration. It documents the pre-migration
behavior and is not a current verification. The navigation migration changed
the screens below from Activities to Fragment destinations; re-verification is
an open acceptance criterion (AC-EMU-01 through AC-EMU-11 in the wrap-up
audit). The only emulator evidence recorded since the migration is the NFR-007
drawer-switch repro and regression run, captured on the recovered API 36 AVD;
AC-EMU-01 remains explicitly infrastructure-blocked with no emulator chip
results claimed, and no other emulator acceptance criterion has recorded
evidence.

| Flow | Result (historical) | Notes |
|------|:-----:|-------|
| App launch → SplashActivity → MainActivity | ✅ | Defaults to Discover Anime tab (Spring) |
| Drawer: Anime tab (4 seasons) | ✅ | SPRING selected by default |
| Drawer: Manga tab (2 subtabs) | ✅ | MANGA LIST + RECENTLY ADDED |
| Drawer: Trending (3 subtabs) | ✅ | Not described but navigated |
| Drawer: Reviews (2 subtabs) | ✅ | Navigated to |
| Drawer: Sign In | ✅ | LoginActivity opens |
| Drawer: Header banner | ✅ | Tappable, avatar shown |
| Card tap → MediaFragment (manga) | ✅ | Overview/Relations/Recommendations sections |
| Deep link: /anime/1 | ✅ | Cowboy Bebop detail |
| Deep link: /manga/1 | ✅ | Monster detail |
| Deep link: /character/1 | ✅ | Spike Spiegel (Overview/Anime Roles/Manga Roles) |
| Deep link: /staff/1 | ✅ | Shinichirō Watanabe (not verified by describe) |
| Deep link: /studio/1 | ✅ | Studio Pierrot with production list |
| Deep link: /activity/1 | ✅ | CommentFragment (activity not found - expected) |
| Deep link: /user/Max | ✅ | ProfileFragment with stats/tabs |
| Toolbar: Search | ✅ | Search button visible in app bar |
| Toolbar: Filter | ✅ | Filter button visible (Anime hub only) |
| Overflow: Settings/About/Logging | ✅ | Menu structure verified from code |

## 6. UAT Automation Recommendations

### 6.1 Test Entity IDs
Use these known-valid entities for deep-link testing:
- **Anime**: `/anime/1` (Cowboy Bebop)
- **Manga**: `/manga/1` (Monster)
- **Character**: `/character/1` (Spike Spiegel)
- **Staff**: `/staff/1` (Shinichirō Watanabe)
- **Studio**: `/studio/1` (Madhouse)
- **User**: `/user/Max` (max, an active user)

### 6.2 Test Scenarios to Automate

**Smoke (unauthenticated):**
1. App launch → Verify Anime hub shown
2. Navigate all 4 unauthenticated drawer tabs
3. Tap a card → Verify MediaFragment opens
4. Search for anime → Verify results
5. Open a deep link of each type → Verify the correct destination Fragment opens

**Auth flow:**
1. Tap Sign In → Verify LoginActivity opens
2. Complete OAuth → Verify MainActivity shows auth'd sections
3. Verify Home Feed, Airing, Hub, My Anime/My Manga all visible

**Detail screens (authenticated):**
1. Open MediaFragment → Verify 8 local sections (including Feed, Reviews)
2. Open Search → Verify Users section visible
3. Open ProfileFragment → Verify messages button visible

**Navigation migration specific (see the wrap-up audit):**
1. Drawer My Anime/My Manga land as root-origin media list; first back shows the exit-confirm
2. Profile stats, shortcuts, and `/user/<name>/animelist|mangalist` chains land as pushed media list; back returns to the caller
3. Cold-start deep links for every manifest path (G-001) and the remaining
   media-list media type restore matrix (rotation/process-death and
   PUSHED-origin; NFR-007, AC-EMU-02). The drawer-switch direction of NFR-007
   is reproduced in both directions and fixed, with the regression assertion in
   `MediaListSavedStateNavigationTest`.

### 6.3 Known Issues / Flaky Areas
- **CommentFragment**: Shows "Not Found" for invalid activity IDs; needs a real valid activity ID for testing
- **Activity ID**: AniList activity IDs are sequential; the earliest IDs may not exist. Test with a recent known activity from the feed.
- **Emulator availability**: the API 36 AVD became ADB-inaccessible with an
  orphaned QEMU process, and a cold restart recovered it only for the NFR-007
  drawer-switch repro and regression run; AC-EMU-01 remains explicitly
  infrastructure-blocked, and device flows beyond that recorded repro cannot
  be re-recorded until a stable emulator or device is available
  (`docs/bugs/social-interaction-regression-evidence.md`).
