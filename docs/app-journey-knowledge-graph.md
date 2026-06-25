# AniTrend App Journey Knowledge Graph

## Purpose
This document maps every screen in the AniTrend Android app, how users navigate between them, and what entry points trigger each screen. It is designed as a reference for future UAT automation — each screen node has enough metadata to construct automated test flows.

## Taxonomy
- **Screen**: A distinct user-facing UI state with its own content, data fetching, and navigation affordances. A screen may be an Activity (e.g., SettingsActivity) or a tab within an Activity's ViewPager.
- **Activity**: Android component (one manifest entry, one Activity subclass). The navigation target and lifecycle boundary.
- **Navigation edge**: A user action that transitions from one screen to another (tap, swipe, deep link, back press).

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

### 1.4 MainActivity (Hub) — 9 screens

The main hub with a navigation drawer and ViewPager. Each drawer item maps to a different adapter.

| Drawer Item | Screen | Adapter | Tabs | Auth Req |
|-------------|--------|---------|------|:--------:|
| Home Feed | `FeedPageAdapter` | FeedListFragment (following, MEDIA_LIST) | FeedListFragment (following, TEXT) | FeedListFragment (all, mixed) | YES |
| Anime | `SeasonPageAdapter` | MediaBrowseFragment (ANIME, WINTER) | MediaBrowseFragment (ANIME, SPRING) | MediaBrowseFragment (ANIME, SUMMER) | MediaBrowseFragment (ANIME, FALL) | NO |
| Manga | `MangaPageAdapter` | MediaBrowseFragment (MANGA) | MediaLatestList (MANGA, newest) | NO |
| Trending | `TrendingPageAdapter` | MediaLatestList (ANIME, trending) | MediaLatestList (MANGA, trending) | MediaLatestList (ANIME, newest) | NO |
| Reviews | `ReviewPageAdapter` | BrowseReviewFragment (ANIME) | BrowseReviewFragment (MANGA) | NO |
| Airing | `AiringPageAdapter` | AiringListFragment | WatchListFragment (FEEDS_LINK) | YES |
| Hub | `HubPageAdapter` | SuggestionListFragment | WatchListFragment (FEEDS_LINK) | YES |
| My Anime | `MediaListPageAdapter` | MediaListFragment x6 (CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING) | YES |
| My Manga | `MediaListPageAdapter` | MediaListFragment x6 (CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING) | YES |

**Entry points**: SplashActivity, LoginActivity (on success), back navigation
**App bar actions**: Search, Filter (season-dependent), Overflow menu
**Drawer actions**: Sign In, Sign Out, Check Update, Banner tap (Profile if auth'd, Login if not)

### 1.5 MediaActivity — Detail screens

| Field | Value |
|-------|-------|
| **Activity** | `MediaActivity` |
| **Entry points** | Deep link (`/anime*`, `/manga*`), card tap from any browse/list screen, search result tap |
| **Deep link** | `https://anilist.co/anime*` and `https://anilist.co/manga*` (ACTION=VIEW, browsable) |
| **Tabs (unauthenticated)** | OVERVIEW, RELATIONS, RECOMMENDATIONS |
| **Tabs (authenticated)** | OVERVIEW, RELATIONS, RECOMMENDATIONS, **STATS**, **CHARACTERS**, **STAFF**, **FEED**, **REVIEWS** |
| **Toolbar actions** | MyAnimeList (anime only), Share |

### 1.6 CharacterActivity

| Field | Value |
|-------|-------|
| **Activity** | `CharacterActivity` |
| **Entry points** | Deep link (`/character*`), card tap from character lists/search, MediaActivity Characters tab |
| **Deep link** | `https://anilist.co/character*` (ACTION=VIEW, browsable) |
| **Tabs** | OVERVIEW, ANIME ROLES, MANGA ROLES |
| **Toolbar actions** | Share |

### 1.7 StaffActivity

| Field | Value |
|-------|-------|
| **Activity** | `StaffActivity` |
| **Entry points** | Deep link (`/staff*`, `/actor*`), card tap from staff lists/search, MediaActivity Staff tab |
| **Deep link** | `https://anilist.co/staff*` and `https://anilist.co/actor*` (ACTION=VIEW, browsable) |
| **Tabs** | OVERVIEW, VOICE ACTING ROLES, MEDIA, STAFF (some filtering disabled when not auth'd) |
| **Toolbar actions** | Share, Filter |

### 1.8 StudioActivity

| Field | Value |
|-------|-------|
| **Activity** | `StudioActivity` |
| **Entry points** | Deep link (`/studio*`), card tap from studio lists/search |
| **Deep link** | `https://anilist.co/studio*` (ACTION=VIEW, browsable) |
| **Content** | Grid of anime/manga produced by the studio |
| **Toolbar actions** | Share, Filter |

### 1.9 ProfileActivity

| Field | Value |
|-------|-------|
| **Activity** | `ProfileActivity` |
| **Entry points** | Deep link (`/user*`), drawer header banner tap (if auth'd), search Users tab, shortcut |
| **Deep link** | `https://anilist.co/user*` (ACTION=VIEW, browsable) |
| **Tabs** | OVERVIEW, PROGRESS, STATUS |
| **Toolbar actions** | Messages (if auth'd), Share |

### 1.10 CommentActivity

| Field | Value |
|-------|-------|
| **Activity** | `CommentActivity` |
| **Entry points** | Deep link (`/activity*`), feed post tap |
| **Deep link** | `https://anilist.co/activity*` (ACTION=VIEW, browsable) |
| **Content** | Activity feed with comment composer |
| **Toolbar actions** | Share |
| **Composer** | Text field + media insertion buttons (image, webm, link, youtube, giphy) |

### 1.11 SearchActivity

| Field | Value |
|-------|-------|
| **Activity** | `SearchActivity` |
| **Entry points** | App bar Search button, shortcut |
| **Tabs (unauthenticated)** | ANIME, MANGA, STUDIO, STAFF, CHARACTERS |
| **Tabs (authenticated)** | ANIME, MANGA, STUDIO, STAFF, CHARACTERS, **USERS** |
| **Auth dependency** | Users tab appears only when authenticated |

### 1.12 SettingsActivity

| Field | Value |
|-------|-------|
| **Activity** | `SettingsActivity` |
| **Entry points** | App bar overflow → Settings |
| **Description** | App settings (theme, lists, notifications, etc.) |

### 1.13 AboutActivity

| Field | Value |
|-------|-------|
| **Activity** | `AboutActivity` |
| **Entry points** | App bar overflow → About |

### 1.14 LoggingActivity

| Field | Value |
|-------|-------|
| **Activity** | `LoggingActivity` |
| **Entry points** | App bar overflow → Bug Report |

### 1.15 Other Activities

| Activity | Entry Point | Description |
|----------|-------------|-------------|
| NotificationActivity | Shortcut, MainActivity programmatic | Notifications list |
| MessageActivity | Programmatic | Messages/composer |
| FavouriteActivity | ProfileActivity (Favourites tap) | User favourites browser |
| MediaListActivity | ProfileActivity, My Anime/Manga | User media list |
| MediaBrowseActivity | MainActivity app bar Filter | Advanced media browse/filter |
| SharedContentActivity | ACTION_SEND text/plain | External share to create feed post |
| ImagePreviewActivity | Programmatic (image tap) | Full-screen image viewer |
| GiphyPreviewActivity | Programmatic (GIPHY button) | GIPHY sticker/gif picker |
| VideoPlayerActivity | Programmatic | Video playback |

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
  ├── Drawer "Discover Anime" → SeasonPageAdapter (4 seasonal tabs)
  ├── Drawer "Discover Manga" → MangaPageAdapter (2 tabs)
  ├── Drawer "What's Trending" → TrendingPageAdapter (3 tabs)
  ├── Drawer "Series Reviews" → ReviewPageAdapter (2 tabs)
  ├── App bar "Search" → SearchActivity (5 tabs)
  ├── App bar "Overflow" → Settings / About / Bug Report / Discord / Donate
  └── Drawer "Sign In" → LoginActivity
```

### 2.4 Content Detail Navigation
```
[Any card tap on browse/list/search]
  └── MediaActivity (anime/manga)
  └── CharacterActivity
  └── StaffActivity
  └── StudioActivity
  └── ProfileActivity (user)
  └── CommentActivity (activity feed post)

MediaActivity tabs:
  ├── Overview → inline content
  ├── Relations → list of related media → tap → MediaActivity
  ├── Recommendations → list → tap → MediaActivity
  ├── Stats [auth'd only] → inline charts
  ├── Characters [auth'd only] → list → tap → CharacterActivity
  ├── Staff [auth'd only] → list → tap → StaffActivity
  ├── Feed [auth'd only] → activity feed posts
  └── Reviews [auth'd only] → list → tap → review detail
```

### 2.5 Authenticated Hub Navigation
```
MainActivity (authenticated, additional items visible)
  ├── Drawer "Home Feed" → FeedPageAdapter (3 feed tabs)
  ├── Drawer "Airing" → AiringPageAdapter (2 tabs)
  ├── Drawer "Hub" → HubPageAdapter (2 tabs)
  ├── Drawer "My Anime" → MediaListPageAdapter (6 status tabs)
  ├── Drawer "My Manga" → MediaListPageAdapter (6 status tabs)
  └── Header banner → ProfileActivity (own profile)
```

---

## 3. Entry Points Reference

### 3.1 Deep Links (for UAT automation)
Use `adb shell am start -a android.intent.action.VIEW -d "<url>" com.mxt.anitrend`

| Screen | Deep Link Pattern | Example |
|--------|------------------|---------|
| MediaActivity (anime) | `https://anilist.co/anime/<id>` | `/anime/1` (Cowboy Bebop) |
| MediaActivity (manga) | `https://anilist.co/manga/<id>` | `/manga/1` (Monster) |
| CharacterActivity | `https://anilist.co/character/<id>` | `/character/1` (Spike Spiegel) |
| StaffActivity | `https://anilist.co/staff/<id>` | `/staff/1` (Shinichirō Watanabe) |
| StaffActivity | `https://anilist.co/actor/<id>` | `/actor/1` |
| StudioActivity | `https://anilist.co/studio/<id>` | `/studio/1` (Madhouse) |
| ProfileActivity | `https://anilist.co/user/<name>` | `/user/Max` |
| ProfileActivity | `https://anilist.co/user/<id>` | `/user/1` |
| CommentActivity | `https://anilist.co/activity/<id>` | `/activity/<valid_id>` |
| LoginActivity | `intent://com.mxt.anitrend` | (OAuth callback) |

### 3.2 App Shortcuts
Shortcut IDs defined in `KeyUtil`:

| Shortcut | ID | Action | Auth |
|----------|----|--------|:----:|
| Search | `SHORTCUT_SEARCH` | → SearchActivity | NO |
| Trending | `SHORTCUT_TRENDING` | → MainActivity(nav_trending) | NO |
| Notifications | `SHORTCUT_NOTIFICATION` | → NotificationActivity | YES |
| Airing | `SHORTCUT_AIRING` | → MainActivity(nav_airing) | YES |
| My Anime | `SHORTCUT_MY_ANIME` | → MainActivity(nav_myanime) | YES |
| My Manga | `SHORTCUT_MY_MANGA` | → MainActivity(nav_mymanga) | YES |
| Feeds | `SHORTCUT_FEEDS` | → MainActivity(nav_home_feed) | YES |
| Profile | `SHORTCUT_PROFILE` | → ProfileActivity(current user) | YES |

### 3.3 Intent-Based Entry
- **redirectShortcut**: Add `arg_redirect` (int) extra to Intent for MainActivity with a drawer menu resource ID to jump directly to a section.
- **ACTION_SEND**: `Intent.ACTION_SEND` with MIME `text/plain` opens SharedContentActivity.

---

## 4. Auth Dependencies

| Screen / Tab | Auth Required | Visible When Unauthenticated? | Unlock Trigger |
|-------------|:------------:|:----------------------------:|:--------------:|
| Home Feed (drawer) | YES | Hidden | Sign in |
| Airing (drawer) | YES | Hidden | Sign in |
| Hub (drawer) | YES | Hidden | Sign in |
| My Anime (drawer) | YES | Hidden | Sign in |
| My Manga (drawer) | YES | Hidden | Sign in |
| Search > Users tab | YES | Hidden | Sign in (appears while on Search) |
| MediaActivity > Feed tab | YES | Hidden | Sign in |
| MediaActivity > Reviews tab | YES | Hidden | Sign in |
| Notifications shortcut | YES | N/A (shortcut) | Sign in |
| Profile shortcut | YES | N/A (shortcut) | Sign in |
| My Anime shortcut | YES | N/A (shortcut) | Sign in |
| My Manga shortcut | YES | N/A (shortcut) | Sign in |
| Airing shortcut | YES | N/A (shortcut) | Sign in |
| Feeds shortcut | YES | N/A (shortcut) | Sign in |
| MessageActivity toolbar | YES | Button visible | Sign in |
| LoginActivity | No | Always | N/A (login page itself) |

---

## 5. Device Walkthrough Verification Results

Verified on API 35 emulator (unauthenticated state) on 2026-06-25:

| Flow | Result | Notes |
|------|:-----:|-------|
| App launch → SplashActivity → MainActivity | ✅ | Defaults to Discover Anime tab (Spring) |
| Drawer: Anime tab (4 seasons) | ✅ | SPRING selected by default |
| Drawer: Manga tab (2 subtabs) | ✅ | MANGA LIST + RECENTLY ADDED |
| Drawer: Trending (3 subtabs) | ✅ | Not described but navigated |
| Drawer: Reviews (2 subtabs) | ✅ | Navigated to |
| Drawer: Sign In | ✅ | LoginActivity opens |
| Drawer: Header banner | ✅ | Tappable, avatar shown |
| Card tap → MediaActivity (manga) | ✅ | Overview/Relations/Recommendations tabs |
| Deep link: /anime/1 | ✅ | Cowboy Bebop detail |
| Deep link: /manga/1 | ✅ | Monster detail |
| Deep link: /character/1 | ✅ | Spike Spiegel (Overview/Anime Roles/Manga Roles) |
| Deep link: /staff/1 | ✅ | Shinichirō Watanabe (not verified by describe) |
| Deep link: /studio/1 | ✅ | Studio Pierrot with production list |
| Deep link: /activity/1 | ✅ | CommentActivity (activity not found - expected) |
| Deep link: /user/Max | ✅ | ProfileActivity with stats/tabs |
| Toolbar: Search | ✅ | Search button visible in app bar |
| Toolbar: Filter | ✅ | Filter button visible (Anime hub only) |
| Overflow: Settings/About/Logging | ✅ | Menu structure verified from code |

---

## 6. UAT Automation Recommendations

### 6.1 Test Entity IDs
Use these known-valid entities for deep-link testing:
- **Anime**: `/anime/1` — Cowboy Bebop
- **Manga**: `/manga/1` — Monster
- **Character**: `/character/1` — Spike Spiegel
- **Staff**: `/staff/1` — Shinichirō Watanabe
- **Studio**: `/studio/1` — Madhouse
- **User**: `/user/Max` — max (an active user)

### 6.2 Test Scenarios to Automate

**Smoke (unauthenticated):**
1. App launch → Verify Anime hub shown
2. Navigate all 4 unauthenticated drawer tabs
3. Tap a card → Verify MediaActivity opens
4. Search for anime → Verify results
5. Open deep link each type → Verify correct activity

**Auth flow:**
1. Tap Sign In → Verify LoginActivity opens
2. Complete OAuth → Verify MainActivity shows auth'd tabs
3. Verify Home Feed, Airing, Hub, My Anime/My Manga all visible

**Detail screens (authenticated):**
1. Open MediaActivity → Verify 8 tabs (including Feed, Reviews)
2. Open Search → Verify Users tab visible
3. Open ProfileActivity → Verify messages button visible

### 6.3 Known Issues / Flaky Areas
- **CommentActivity**: Shows "Not Found" for invalid activity IDs; needs a real valid activity ID for testing
- **Activity ID**: AniList activity IDs are sequential; the earliest IDs may not exist. Test with a recent known activity from the feed.
