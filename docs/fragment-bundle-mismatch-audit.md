# Fragment Bundle Mismatch Audit

> Date: 2026-07-27
> Scope: All `*PageAdapter` -> Fragment -> ViewModel bundle flows and Activity -> Fragment -> ViewModel bundle flows in `app/src/main/`

## Executive Summary

A code map of all 16 `*PageAdapter` classes (9 in `adapter/pager/index/`, 7 in `adapter/pager/detail/`) and 17 Activity -> Fragment relationships found **10 distinct bundle-key mismatches** where keys are set but never read, read but never forwarded to the ViewModel, or silently replaced by Settings values. The most severe is the seed case: `SeasonPageAdapter` sets `arg_season` (WINTER/SPRING/SUMMER/FALL) on all 4 tabs, but `MediaBrowseFragment` never reads it, so all season tabs show identical results.

A systemic design issue underlies most mismatches: `MediaBrowseFragment.makeRequest()` reads filter parameters from `Settings` preferences rather than from the bundle, so any adapter or activity passing filter keys via bundle has those keys silently ignored.

## How to Read This Report

Each finding is classified as:
- **Dead key**: SET in bundle but NEVER READ by the fragment or any base class.
- **Read but not forwarded**: READ by the fragment but NEVER passed to the ViewModel.
- **Silently replaced**: READ by the fragment (or expected to be read) but the fragment uses `Settings` values instead, so the bundle value is ignored.
- **Caller-contract dependency**: READ by the fragment but NOT SET by the adapter; relies on the caller pre-populating `params`.

Severity is assessed by user-visible impact:
- **HIGH**: User-facing behavior is broken or silently wrong.
- **MEDIUM**: Intent is lost but a fallback exists.
- **LOW**: Latent issue that only triggers under specific conditions.

---

## Findings

### 1. SeasonPageAdapter -> MediaBrowseFragment: `arg_season` silently dropped

**Severity: HIGH**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/adapter/pager/index/SeasonPageAdapter.kt:24-56` |
| **Target** | `app/src/main/java/com/mxt/anitrend/view/fragment/list/MediaBrowseFragment.kt` |
| **ViewModel** | `app/src/main/java/com/mxt/anitrend/viewmodel/MediaBrowseViewModel.kt` |
| **Repository** | `app/src/main/java/com/mxt/anitrend/repository/BrowseRepository.kt:88` |

`SeasonPageAdapter` creates 4 `MediaBrowseFragment` instances, one per season. Each bundle receives:
- `arg_mediaType` = `ANIME`
- `arg_season` = `WINTER` / `SPRING` / `SUMMER` / `FALL`
- `arg_page_limit` = `PAGING_LIMIT`

`MediaBrowseFragment` reads `arg_mediaType`, `arg_page_limit`, `arg_isAdult`, `arg_asHtml`, and `arg_media_util` from the bundle. It **never reads `arg_season`**. The `makeRequest()` method uses `settings.seasonYear` (a single integer year) for the year filter and does not pass a season parameter to the ViewModel.

`MediaBrowseViewModel.load()` accepts `seasonYear: Int?` but has no `season` parameter. However, `BrowseRepository.getMediaBrowse()` does accept `season: MediaSeason?` and passes it to the GraphQL request.

**Impact**: All 4 season tabs (Winter, Spring, Summer, Fall) return identical results. The season filter is completely non-functional. The repository layer supports season filtering but the fragment and ViewModel never forward the value.

**Classification**: Dead key (SET but NEVER READ).

---

### 2. MediaStatsFragment rank click -> MediaBrowseFragment: 5 filter keys silently dropped

**Severity: HIGH**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaStatsFragment.kt:185-221` |
| **Target** | `MediaBrowseFragment` via `MediaBrowseActivity` |

When a user clicks a media rank in `MediaStatsFragment`, it creates an intent for `MediaBrowseActivity` with a bundle containing:
- `arg_mediaType` - read by fragment, forwarded to VM
- `arg_format` - **silently replaced** by `settings.animeFormat`/`settings.mangaFormat`
- `arg_season` - **dead key** (never read from bundle or settings)
- `arg_seasonYear` - **silently replaced** by `settings.seasonYear`
- `arg_startDateLike` - **dead key** (never read from bundle; fragment computes it from `settings.seasonYear` for manga)
- `arg_sort` - **silently replaced** by `settings.mediaSort + settings.sortOrder`
- `arg_page_limit` - read by fragment, forwarded to VM
- `arg_isAdult` - read by fragment, forwarded to VM
- `arg_media_util` - read by fragment (used for UI config)
- `arg_activity_tag` - read by `MediaBrowseActivity` for toolbar title (not a fragment key)

The bundle also includes `MediaBrowseUtil().setFilterEnabled(false)`, which disables the entire filter block in `makeRequest()`. When `isFilterableEnabled` is false, `makeRequest()` skips all filter parameters (format, seasonYear, startDateLike, status, genres, tags, sort), so even the Settings fallbacks are not used.

**Impact**: When a user clicks a rank (e.g. "Most Rated Anime in Winter 2024"), the resulting browse screen shows unfiltered media. All 5 filter keys in the intent are ignored.

**Classification**: 2 dead keys (`arg_season`, `arg_startDateLike`) + 3 silently replaced (`arg_format`, `arg_seasonYear`, `arg_sort`).

---

### 3. MediaOverviewFragment genre click -> MediaBrowseFragment: `arg_genres` silently dropped

**Severity: HIGH**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaOverviewFragment.kt:183-200` |
| **Target** | `MediaBrowseFragment` via `MediaBrowseActivity` |

When a user clicks a genre in `MediaOverviewFragment`, it creates an intent with:
- `arg_mediaType` - read by fragment, forwarded to VM
- `arg_genres` - **silently replaced** by `settings.selectedGenres` via `GenreTagUtil.getMappedValues()`
- `arg_page_limit` - read by fragment, forwarded to VM
- `arg_isAdult` - read by fragment, forwarded to VM
- `arg_activity_tag` - read by `MediaBrowseActivity` for toolbar title
- `arg_media_util` - read by fragment (used for UI config)

**Impact**: When a user clicks a genre (e.g. "Action"), the resulting browse screen applies **no genre filter at all** (`genres = null`). The bundle value is ignored, and the Settings fallback is also blocked because `MediaBrowseUtil.setBasicFilter(true)` causes `makeRequest()` to skip the inner filter block (line 323: `if (mediaBrowseUtil?.isBasicFilter != true)`). Only `sort` from Settings is applied. The user sees unfiltered media, not media filtered by their Settings genres.

**Classification**: Dead key (SET but NEVER READ; Settings fallback also blocked by `isBasicFilter`).

---

### 4. MediaOverviewFragment tag click -> MediaBrowseFragment: `arg_tags` silently dropped

**Severity: HIGH**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaOverviewFragment.kt:226-246` |
| **Target** | `MediaBrowseFragment` via `MediaBrowseActivity` |

When a user clicks a tag in `MediaOverviewFragment`, it creates an intent with:
- `arg_mediaType` - read by fragment, forwarded to VM
- `arg_tags` - **silently replaced** by `settings.selectedTags` via `GenreTagUtil.getMappedValues()`
- `arg_page_limit` - read by fragment, forwarded to VM
- `arg_isAdult` - read by fragment, forwarded to VM
- `arg_activity_tag` - read by `MediaBrowseActivity` for toolbar title
- `arg_media_util` - read by fragment (used for UI config, `setBasicFilter(true)`)

**Impact**: When a user clicks a tag (e.g. "Time Travel"), the resulting browse screen applies **no tag filter at all** (`tags = null`). The bundle value is ignored, and the Settings fallback is also blocked because `MediaBrowseUtil.setBasicFilter(true)` causes `makeRequest()` to skip the inner filter block (line 323: `if (mediaBrowseUtil?.isBasicFilter != true)`). Only `sort` from Settings is applied. The user sees unfiltered media, not media filtered by their Settings tags.

**Classification**: Dead key (SET but NEVER READ; Settings fallback also blocked by `isBasicFilter`).

---

### 5. FeedPageAdapter -> FeedListFragment: `arg_asHtml` silently dropped

**Severity: MEDIUM**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/adapter/pager/index/FeedPageAdapter.kt:37,46` |
| **Target** | `app/src/main/java/com/mxt/anitrend/view/fragment/list/FeedListFragment.kt` |
| **ViewModel** | `app/src/main/java/com/mxt/anitrend/viewmodel/FeedListViewModel.kt` |

`FeedPageAdapter` sets `arg_asHtml = false` on positions 1 and 2. `FeedListFragment.makeRequest()` reads `arg_page_limit`, `arg_isFollowing`, `arg_type`, and `arg_isMixed` but **never reads `arg_asHtml`**. `FeedListViewModel.load()` has no `asHtml` parameter.

**Note**: `FeedListFragment.applyBaseFeedRequestArguments()` (lines 141-156) does read `arg_asHtml`, but this method is **dead code** and never called anywhere in the codebase.

**Impact**: The HTML rendering intent is lost. Feeds are always rendered in the same mode regardless of the `arg_asHtml` value. The current value is `false`, so the practical impact is low, but the contract is broken.

**Classification**: Dead key (SET but NEVER READ by any live code path).

---

### 6. StaffPageAdapter -> MediaAnimeRoleFragment: `arg_mediaType` and `arg_request_type` read but not forwarded

**Severity: MEDIUM**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/adapter/pager/detail/StaffPageAdapter.kt:32-33` |
| **Target** | `app/src/main/java/com/mxt/anitrend/view/fragment/group/MediaAnimeRoleFragment.kt` |
| **ViewModel** | `app/src/main/java/com/mxt/anitrend/viewmodel/MediaAnimeRoleViewModel.kt` |

`StaffPageAdapter` position 1 creates `MediaAnimeRoleFragment` with:
- `arg_mediaType` = `ANIME`
- `arg_request_type` = `STAFF_CHARACTERS_REQ`

`MediaAnimeRoleFragment.onCreate()` reads both keys into instance fields (`mediaType` at line 67, `requestType` at line 64). However, `makeRequest()` (lines 102-108) only calls `mediaAnimeRoleViewModel.load(id, onList, page)`. The `mediaType` and `requestType` fields are never used after assignment.

`MediaAnimeRoleViewModel.load()` signature is `fun load(id: Long, onList: Boolean?, page: Int)` and accepts neither parameter.

**Impact**: The media type and request type are read from the bundle but have no effect on the API request. The ViewModel may be producing correct results through other means (e.g. the request type is encoded in the repository call), but the bundle values are dead baggage that create a false impression of parameterization.

**Classification**: Read but not forwarded.

---

### 7. SuggestionListFragment.newInstance(): `arg_page_limit`, `arg_onList`, `arg_mediaType` set but ignored by makeRequest

**Severity: MEDIUM**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/view/fragment/list/SuggestionListFragment.kt:39-41` |
| **Target** | `SuggestionListFragment` (extends `MediaBrowseFragment`) |
| **ViewModel** | `app/src/main/java/com/mxt/anitrend/viewmodel/SuggestionListViewModel.kt` |

`SuggestionListFragment.newInstance()` sets:
- `arg_mediaType` = `ANIME`
- `arg_onList` = `false`
- `arg_page_limit` = `PAGING_LIMIT`

`SuggestionListFragment` extends `MediaBrowseFragment`. The parent's `onCreate()` reads `arg_page_limit` into `requestArgs`, but `SuggestionListFragment` overrides `makeRequest()` entirely (lines 71-81) and never reads any of these three keys from `requestArgs` or `arguments`.

`SuggestionListViewModel.load()` signature is `fun load(sort, page, tags, genres, isAdult)` and hardcodes `type = MediaType.ANIME`, `onList = false`, `perPage = KeyUtil.PAGING_LIMIT`.

**Impact**: The bundle keys create a false impression that these values are configurable. They are not. The ViewModel hardcodes the same values, so the behavior is currently correct, but if someone changed the bundle values expecting different behavior, nothing would change.

**Classification**: Dead keys (SET but NEVER READ by the subclass's `makeRequest()`).

---

### 8. AiringPageAdapter -> WatchListFragment: `arg_id`, `arg_mediaType` not set (latent)

**Severity: LOW**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/adapter/pager/index/AiringPageAdapter.kt:28` |
| **Target** | `app/src/main/java/com/mxt/anitrend/view/fragment/list/WatchListFragment.kt` |

`AiringPageAdapter` position 1 creates `WatchListFragment.newInstance(externalLinks, false)`, which sets `arg_list_model` and `arg_popular` but does NOT set `arg_id` or `arg_mediaType`.

`WatchListFragment.onCreate()` reads `arg_id` (line 75) and `arg_mediaType` (line 76), defaulting to `0L` and `null`. However, `makeRequest()` only uses these values in the `externalLinks == null` branch. Since the adapter always passes `externalLinks`, the missing keys do not trigger.

**Note**: `arg_list_model` and `arg_popular` are read by the base class `FragmentChannelBase.onCreate()` (lines 90-91), not by `WatchListFragment` directly. This is a hidden base-class consumption path.

**Impact**: No current impact. If `externalLinks` ever becomes null, the fragment would use `userId = 0L` and `mediaType = null`, which would produce incorrect results.

**Classification**: Caller-contract dependency (latent).

---

### 9. MediaListPageAdapter -> MediaListFragment: `arg_id`, `arg_userName`, `arg_mediaType` not set by adapter

**Severity: LOW**

| | |
|---|---|
| **Source** | `app/src/main/java/com/mxt/anitrend/adapter/pager/index/MediaListPageAdapter.kt:38-41` |
| **Target** | `app/src/main/java/com/mxt/anitrend/view/fragment/list/MediaListFragment.kt` |

`MediaListPageAdapter` only sets `arg_statusIn` per position. It does NOT set `arg_id`, `arg_userName`, or `arg_mediaType`. These must come from the caller via `adapter.params`.

`MediaListFragment.onCreate()` reads all four keys. If `params` is `Bundle.EMPTY` (the default), `userId = 0L`, `userName = null`, `mediaType = null`.

**Caller verification**: `MainActivity` (lines 469-497) and `MediaListActivity` (line 33-48) both pre-populate `params` with `arg_id`, `arg_userName`, and `arg_mediaType` before assigning it to the adapter. So the contract is currently satisfied.

**Impact**: No current impact. The adapter relies on a caller contract that is not enforced at compile time. If a new caller forgets to set `params`, the fragment will silently use default values.

**Classification**: Caller-contract dependency.

---

### 10. SeasonPageAdapter -> MediaBrowseFragment: `arg_asHtml` defaulted but not forwarded

**Severity: LOW**

| | |
|---|---|
| **Source** | `SeasonPageAdapter` (does not set `arg_asHtml`; fragment defaults it) |
| **Target** | `MediaBrowseFragment` |

`MediaBrowseFragment.onCreate()` (lines 69-71) defaults `arg_asHtml = false` in `requestArgs` if not present. However, `makeRequest()` never reads `arg_asHtml` and `MediaBrowseViewModel.load()` has no `asHtml` parameter.

**Impact**: No current impact since the value is always `false`. But the defaulting code creates a false impression that `arg_asHtml` is used.

**Classification**: Read (defaulted) but not forwarded.

---

## Systemic Design Issue: MediaBrowseFragment reads filters from Settings, not bundle

**Severity: HIGH (architectural)**

`MediaBrowseFragment.makeRequest()` (lines 303-351) reads ALL filter parameters from `Settings` preferences, not from the bundle:

| Filter | Source in `makeRequest()` | Bundle key ignored |
|--------|--------------------------|-------------------|
| `format` | `settings.animeFormat` / `settings.mangaFormat` | `arg_format` |
| `seasonYear` | `settings.seasonYear` | `arg_seasonYear` |
| `season` | (not read from anywhere) | `arg_season` |
| `startDateLike` | computed from `settings.seasonYear` (manga only) | `arg_startDateLike` |
| `status` | `settings.mediaStatus` | `arg_status` |
| `genres` | `settings.selectedGenres` via `GenreTagUtil` | `arg_genres` |
| `tags` | `settings.selectedTags` via `GenreTagUtil` | `arg_tags` |
| `sort` | `settings.mediaSort + settings.sortOrder` | `arg_sort` |

Only `arg_mediaType`, `arg_isAdult`, and `arg_page_limit` are read from `requestArgs`.

**Important gate**: The Settings-based filter reads only apply when **both** `isFilterableEnabled` is true AND `isBasicFilter` is NOT true. When `isBasicFilter == true` (set by genre/tag click flows via `MediaBrowseUtil.setBasicFilter(true)`), the inner filter block at line 323 is skipped entirely, so `format`, `seasonYear`, `startDateLike`, `status`, `genres`, and `tags` are all `null`. Only `sort` (line 335, outside the inner block) is still read from Settings. This means the `isBasicFilter` flows are worse than "silently replaced by Settings" - they apply no filter at all except sort.

The `onSharedPreferenceChanged` listener in `FragmentBaseList` triggers `onRefresh()` -> `makeRequest()` when filter settings change, reinforcing that the fragment is designed to reload on Settings changes, not bundle changes.

This means any adapter or activity that passes filter keys via bundle (SeasonPageAdapter, MediaStatsFragment, MediaOverviewFragment) has those keys silently ignored. This is the root cause behind findings 1, 2, 3, and 4.

---

## Clean Flows (no mismatches)

The following adapter and activity flows were verified as clean, with all bundle keys properly read and forwarded to the ViewModel:

### PageAdapters
- `HubPageAdapter` - position 0 (`SuggestionListFragment`) covered by Finding 7; position 1 (`WatchListFragment` with `externalLinks`) clean/latent per Finding 8 analysis
- `MangaPageAdapter` (index) - `arg_mediaType`, `arg_sort`, `arg_page_limit` all flow correctly
- `TrendingPageAdapter` - `arg_mediaType`, `arg_sort`, `arg_page_limit` all flow correctly
- `SearchPageAdapter` - passes `params` through; `arg_search` and `arg_mediaType` flow correctly
- `ReviewPageAdapter` - `arg_mediaType` flows correctly
- `AnimePageAdapter` (detail) - all 8 fragments read `arg_id` + `arg_mediaType` correctly
- `MangaPageAdapter` (detail) - identical to AnimePageAdapter
- `CharacterPageAdapter` - `arg_id`, `arg_mediaType`, `arg_request_type` all flow correctly
- `ProfilePageAdapter` - `arg_id`/`arg_userName`, `arg_type`, `arg_page_limit` all flow correctly
- `FavouritePageAdapter` - `arg_id`, `arg_mediaType` all flow correctly
- `MessagePageAdapter` - `arg_userId`, `arg_message_type` all flow correctly

### Activity -> Fragment
- `MediaActivity` -> detail adapters -> 8 fragments (clean)
- `StudioActivity` -> `StudioMediaFragment` (clean)
- `MessageActivity` -> `MessagePageAdapter` -> `MessageFeedFragment` (clean)
- `CommentActivity` -> `CommentFragment` (clean, no ViewModel)
- `ProfileActivity` -> `ProfilePageAdapter` -> fragments (clean)
- `CharacterActivity` -> `CharacterPageAdapter` -> fragments (clean)
- `FavouriteActivity` -> `FavouritePageAdapter` -> fragments (clean)
- `NotificationActivity` -> `NotificationFragment` (clean, no args)
- `AboutActivity` -> `AboutFragment` (clean, no args)
- `MainActivity` -> `MediaListPageAdapter` -> `MediaListFragment` (clean, params pre-populated)
- `MediaListActivity` -> `MediaListPageAdapter` -> `MediaListFragment` (clean, params pre-populated)

---

## Additional Notes

### Dead code referencing bundle keys
`FeedListFragment.applyBaseFeedRequestArguments()` (lines 141-156) reads `arg_asHtml` from the bundle, but this method is never called anywhere in the codebase. It should not be confused with a live consumption path.

### Base-class hidden reads
`FragmentChannelBase.onCreate()` reads `arg_popular` and `arg_list_model` from `arguments`. This is a hidden consumption path that `WatchListFragment` inherits. Any audit of bundle keys should check base classes, not just the concrete fragment.

### Activity-consumed keys
`arg_activity_tag` is set by `MediaOverviewFragment` and `MediaStatsFragment` in their intents, but it is read by `MediaBrowseActivity.onCreate()` for the toolbar title, not by `MediaBrowseFragment`. This is an activity-level key and should not be classified as "ignored by the fragment."

### Inheritance-based partial reads
`SuggestionListFragment` extends `MediaBrowseFragment`. The parent's `onCreate()` reads `arg_page_limit` into `requestArgs`, but the subclass's overridden `makeRequest()` never uses `requestArgs`. This is a subtle pattern where a key IS read (by parent) but never USED (by child's override).

---

## Inventory

### PageAdapter files analyzed (16 total)

**`adapter/pager/index/` (9):**
1. `AiringPageAdapter.kt`
2. `SeasonPageAdapter.kt`
3. `HubPageAdapter.kt`
4. `MangaPageAdapter.kt`
5. `FeedPageAdapter.kt`
6. `TrendingPageAdapter.kt`
7. `SearchPageAdapter.kt`
8. `ReviewPageAdapter.kt`
9. `MediaListPageAdapter.kt`

**`adapter/pager/detail/` (7):**
10. `StaffPageAdapter.kt`
11. `MangaPageAdapter.kt`
12. `MessagePageAdapter.kt`
13. `ProfilePageAdapter.kt`
14. `CharacterPageAdapter.kt`
15. `FavouritePageAdapter.kt`
16. `AnimePageAdapter.kt`

### Activity -> Fragment flows analyzed (17 total)
1. `MediaBrowseActivity` -> `MediaBrowseFragment` (direct, from MediaOverviewFragment/MediaStatsFragment intents)
2. `MainActivity` (nav_anime) -> `SeasonPageAdapter` -> `MediaBrowseFragment`
3. `MainActivity` (nav_manga) -> `MangaPageAdapter` (index) -> `MediaBrowseFragment` + `MediaLatestList`
4. `MainActivity` (nav_trending) -> `TrendingPageAdapter` -> `MediaLatestList`
5. `MainActivity` (nav_myanime/mymanga) -> `MediaListPageAdapter` -> `MediaListFragment`
6. `MediaListActivity` -> `MediaListPageAdapter` -> `MediaListFragment`
7. `MediaActivity` -> `AnimePageAdapter`/`MangaPageAdapter` (detail) -> 8 fragments
8. `StudioActivity` -> `StudioMediaFragment` (direct)
9. `StaffActivity` -> `StaffPageAdapter` -> 4 fragments
10. `MessageActivity` -> `MessagePageAdapter` -> `MessageFeedFragment`
11. `CommentActivity` -> `CommentFragment` (direct)
12. `ProfileActivity` -> `ProfilePageAdapter` -> `UserOverviewFragment` + `UserFeedFragment`
13. `CharacterActivity` -> `CharacterPageAdapter` -> 4 fragments
14. `FavouriteActivity` -> `FavouritePageAdapter` -> 5 fragments
15. `NotificationActivity` -> `NotificationFragment` (direct)
16. `AboutActivity` -> `AboutFragment` (direct)
17. `MainActivity` (nav_airing) -> `AiringPageAdapter` -> `AiringListFragment` + `WatchListFragment`
