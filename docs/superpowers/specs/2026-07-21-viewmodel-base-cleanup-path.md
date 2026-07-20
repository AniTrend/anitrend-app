# ViewModelBase Cleanup Path

Date: 2026-07-21
Project: AniTrend Android app
Scope: Define when `ViewModelBase` deprecation becomes honest, what remains coupled to it today, and the smallest next migration that proves the replacement path for API-backed screens.

## 1. Decision

Do not deprecate `ViewModelBase` yet.

The current codebase has a proven replacement only for local-only screens:
- `app/src/main/java/com/mxt/anitrend/viewmodel/LoggingViewModel.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/base/LoggingActivity.kt`

That replacement does not prove the API-backed path that still relies on:
- `ViewModelBase.requestData(...)`
- `RequestHandler`
- `KeyUtil.*_REQ`
- `params: Bundle`

Warn-level deprecation on `ViewModelBase` before an API-backed screen is migrated would create warnings with no credible replacement path.

## 2. Current remaining dependency surface

### 2.1 Dead `setViewModel(...)` calls

These call `setViewModel(true)` but never read the resulting `viewModel` property afterwards:
- `app/src/main/java/com/mxt/anitrend/view/activity/index/SplashActivity.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/index/SearchActivity.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/detail/MediaListActivity.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/detail/MediaBrowseActivity.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/detail/FavouriteActivity.kt`
- `app/src/main/java/com/mxt/anitrend/view/activity/base/SharedContentActivity.kt`

These are cleanup opportunities, but they do not prove the API-backed replacement path.

### 2.2 Single-entity detail activities still bound to `ViewModelBase`

These are the strongest migration candidates because they do not combine paging with list-state coupling:
- `MediaActivity`
- `ProfileActivity`
- `CharacterActivity`
- `StaffActivity`
- `StudioActivity`

Shared shape today:
1. read one or two intent args
2. write them into `viewModel?.params`
3. call `viewModel?.requestData(KeyUtil.*_REQ, context)`
4. consume cached result through `getModel()` or `onChanged(...)`

### 2.3 Fragment list and tab surfaces blocked by pagination and request dispatch

Search, favourite, list, detail-tab, and group fragments still depend on some combination of:
- `presenter.currentPage`
- `presenter.setPageInfo(...)`
- `viewModel?.requestData(...)`
- `viewModel?.params`

These should not drive the first `ViewModelBase` replacement.

### 2.4 Bottom-sheet-specific helpers

Bottom-sheet code still uses `acquireTypedViewModelBase(...)` and local `setViewModel(...)` copies. This is a separate migration surface and should remain out of scope until a bottom-sheet reference shape exists.

## 3. Next migration target

### Chosen target: `StudioActivity`

`StudioActivity` is the smallest credible API-backed migration that makes a future `ViewModelBase` deprecation honest.

Why `StudioActivity`:
- single `arg_id` contract
- single `STUDIO_BASE_REQ` request type today
- minimal presenter usage, mainly `settings.isAuthenticated`
- no activity-level pagination
- deep-link capable, which proves the next navigation shape without changing `IntentBundleUtil`
- lighter UI and state surface than `MediaActivity` or `ProfileActivity`

## 4. Target replacement shape

`StudioActivity` should move to:

```text
AppCompatActivity -> StudioViewModel -> generated GraphQL operation API
```

### 4.1 Activity responsibilities
- read typed args from a colocated companion contract
- observe `StateFlow`
- render loading, success, and error UI
- keep framework-only work in the activity shell
- inject simple collaborators such as `Settings` directly when needed

### 4.2 ViewModel responsibilities
- extend `androidx.lifecycle.ViewModel` directly
- expose `StateFlow<StudioUiState>`
- call the generated operation API directly, without `RequestHandler`
- replace mutable `params: Bundle` with typed method inputs such as `load(studioId: Long)`
- centralize error handling in pure helper functions, not a new base class

### 4.3 Request path

The migrated request should call the generated operation API directly. The proof target is:
- `StudioBase.request(id)` rather than `requestData(KeyUtil.STUDIO_BASE_REQ, context)`

This removes one real screen from the `RequestHandler` dispatch table and proves that API-backed direct ViewModels are viable in this repo.

## 5. What not to deprecate yet

Do not add warn-level deprecation yet for:
- `ViewModelBase`
- `ViewModelBase.requestData(...)`
- `RequestHandler`
- `WidgetPresenter.requestData(...)`
- `KeyUtil.*_REQ`
- EventBus-facing APIs such as `notifyAllListeners(...)`

These remain active and do not yet have a proven, repeatable replacement in a shipped screen.

## 6. Proof required before `ViewModelBase` deprecation

Before adding `@Deprecated(level = WARNING)` to `ViewModelBase` or its main request path, require all of the following:

1. at least one API-backed activity migrated to a direct ViewModel using generated operation APIs
2. at least one API-backed fragment migrated to the same shape
3. error handling replacement documented and shared through pure helpers
4. typed method inputs replacing the `params: Bundle` pattern in the migrated screen
5. CI guard preventing new `setViewModel(...)` / `ViewModelBase` usage outside legacy allowlists

## 7. Scope for the next implementation phase

The next code phase after this planning artifact should focus on:
1. removing the six dead `setViewModel(...)` calls only if they do not distract from the main migration
2. migrating `StudioActivity` to a direct `StudioViewModel`
3. leaving `StudioMediaFragment` and other fragment surfaces untouched until the activity-side pattern is proven

## 8. Constraints

- do not introduce a shared `BaseApiViewModel`
- do not introduce repository or use-case layers unless a real domain boundary appears for the target screen
- do not bridge direct ViewModels back into `MutableLiveData` for legacy observers
- keep migration screen-by-screen, not framework-first

## 9. Outcome

This document makes the current state explicit:
- `ViewModelBase` is still legacy but not yet honestly deprecatable
- `StudioActivity` is the next smallest API-backed proof target
- successful `StudioActivity` migration is the gate that unlocks a later `ViewModelBase` deprecation phase
