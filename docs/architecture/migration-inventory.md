# Migration Inventory: State Synchronisation and Mutation Refactor

**Date:** 29 July 2026
**Spec:** `docs/architecture/state-synchronization-and-mutation-refactor.md` (Phase 0)

This inventory documents the 19 classes and supporting types that participate in the current mutation propagation architecture, their mutable state, call relationships, and Koin registrations. It is the baseline for the phased migration.

---

## Call path: feed like toggle

1. **Widget click**: `FavouriteWidget.onClick(...)` triggers `listener?.onToggleLike(id, likeableType, onResult)` (`FavouriteWidget.kt:101-126`)
2. **Adapter delegates to coordinator**: `FeedAdapter.favouriteListener` calls `coordinator.toggleLike(...)` (`FeedAdapter.kt:52-58`); `CommentAdapter.favouriteListener` does the same for replies (`CommentAdapter.kt:42-48`)
3. **Coordinator calls repository**: `WidgetMutationCoordinator.toggleLike(...)` calls `baseRepository.toggleLike(...)` (`WidgetMutationCoordinator.kt:33-43`)
4. **Repository emits mutation event**: `BaseRepository.toggleLike(...)` emits `BaseMutation.LikeToggled(...)` on `_mutationEvents` (`BaseRepository.kt:109-126`)
5. **Consumers update state**: `FeedListViewModel` collects `baseRepository.mutationEvents`, updates matching `FeedList.likes`, re-emits `UiState.Success(replaceExisting = true)` (`FeedListViewModel.kt:56-75,131-146`); `UserFeedViewModel` mirrors (`UserFeedViewModel.kt:57-76,136-151`); `CommentFragment` collects directly and updates `feedList.likes` or `FeedReply.likes` (`CommentFragment.kt:183-190,445-471`)
6. **UI refresh**: `FeedListFragment` consumes `FeedListViewModel.state` and replaces adapter data via `mAdapter.onItemsInserted(filtered)` when `replaceExisting` is true (`FeedListFragment.kt:81-97,175-202`); `CommentFragment` pushes updated replies to `mAdapter.onItemsInserted(replies)` (`CommentFragment.kt:458-465`)

---

## 1. AbstractRepository

- **File**: `app/src/main/java/com/mxt/anitrend/repository/AbstractRepository.kt`
- **Signature**: `abstract class AbstractRepository<T : Any>(protected val ioDispatcher: CoroutineDispatcher)` (`:20-22`)
- **Mutable state**: `_mutationEvents = MutableSharedFlow<T>(replay = 0, extraBufferCapacity = 64)` (`:23`); `mutationEvents: SharedFlow<T> = _mutationEvents.asSharedFlow()` (`:24`)
- **Mutation propagation**: Base event bus via `emitMutationEvent(event)` and direct child access to `_mutationEvents` (`:26-28`)
- **Relationships**: Superclass of `BaseRepository`, `BrowseRepository`, `FeedRepository`
- **Koin**: None

## 2. BaseRepository

- **File**: `app/src/main/java/com/mxt/anitrend/repository/BaseRepository.kt`
- **Signature**: `class BaseRepository(...): AbstractRepository<BaseMutation>` (`:43-47`)
- **Mutable state**: Inherited `_mutationEvents`; cached read models `cachedGenres`, `cachedTags` (`:49-55`)
- **Mutation propagation**: Emits `BaseMutation.LikeToggled` in `toggleLike(...)` (`:109-126`); `BaseMutation.FavouriteToggled` in `toggleFavourite(...)` (`:129-155`)
- **Relationships**: Called by `WidgetMutationCoordinator.toggleLike(...)`; consumed by `FeedListViewModel`, `UserFeedViewModel`, `CommentFragment`; read by `MediaBrowseViewModel` for caches
- **Koin**: `single { BaseRepository(baseService = get(), boxQuery = get()) }` (`Modules.kt:527`)

## 3. BrowseRepository

- **File**: `app/src/main/java/com/mxt/anitrend/repository/BrowseRepository.kt`
- **Signature**: `class BrowseRepository(...): AbstractRepository<BrowseMutation>` (`:46-49`)
- **Mutable state**: Inherited `_mutationEvents`
- **Mutation propagation**: Emits `BrowseMutation.MediaListDeleted` (`:196-208`), `MediaListSaved` (`:224-263`), `MediaListsUpdated` (`:265-279`), review mutations (`:210-221,282-320`)
- **Relationships**: Called by `WidgetMutationCoordinator`; consumed by `MediaBrowseViewModel`, `MediaListViewModel`
- **Koin**: `single { BrowseRepository(browseService = get()) }` (`Modules.kt:521`)

## 4. FeedRepository

- **File**: `app/src/main/java/com/mxt/anitrend/repository/FeedRepository.kt`
- **Signature**: `class FeedRepository(...): AbstractRepository<FeedMutation>` (`:32-35`)
- **Mutable state**: Inherited `_mutationEvents`
- **Mutation propagation**: Emits `FeedMutation.FeedSaved` (`:90-116`), `ReplySaved` (`:118-135`), `FeedDeleted` (`:137-149`), `ReplyDeleted` (`:151-163`)
- **Relationships**: Called by `WidgetMutationCoordinator`; consumed by `FeedListViewModel`, `UserFeedViewModel`, `CommentFragment`
- **Koin**: `single { FeedRepository(feedService = get()) }` (`Modules.kt:526`)

## 5. WidgetMutationCoordinator

- **File**: `app/src/main/java/com/mxt/anitrend/coordinator/WidgetMutationCoordinator.kt`
- **Signature**: `class WidgetMutationCoordinator(...)` (`:22-31`)
- **Mutable state**: Constructor-held collaborators; `databaseHelper: DatabaseHelper` exposes current user state (`:30`)
- **Mutation propagation**: Central widget-to-repository bridge for likes (`:33-44`), feed deletes (`:71-93`), media-list delete/save (`:95-157`)
- **Relationships**: Calls `BaseRepository`, `BrowseRepository`, `FeedRepository`; used by `FeedAdapter`, `CommentAdapter`, `MediaListAdapter` constructors; injected into `FeedListFragment`, `CommentFragment`, `BottomSheetSeriesManage`
- **Koin**: `single { WidgetMutationCoordinator(...) }` (`Modules.kt:529-539`)

## 6. FavouriteWidget

- **File**: `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/FavouriteWidget.kt`
- **Signature**: `class FavouriteWidget ... : FrameLayout(...), CustomView, View.OnClickListener` (`:24-32`)
- **Mutable state**: `model: MutableList<UserBase>?` (`:43`), `likeType: String?` (`:44`), `modelId: Long` (`:45`), `listener: Listener?` (`:47`), `recycled = false` (`:48`), `currentUser: UserBase?` (`:49`)
- **Mutation propagation**: Consumes click, emits callback through `Listener.onToggleLike(...)` (`:34-40,101-126`); replaces local `model` from callback success (`:118-121,138-157`)
- **Relationships**: Listener implemented in `FeedAdapter` (`:52-58`), `CommentAdapter` (`:42-48`)
- **Koin**: None

## 7. StatusDeleteWidget

- **File**: `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/StatusDeleteWidget.kt`
- **Signature**: `class StatusDeleteWidget ... : FrameLayout(...), CustomView, View.OnClickListener` (`:21-29`)
- **Mutable state**: `requestType: Int` (`:41-42`), `feedList: FeedList?` (`:43`), `feedReply: FeedReply?` (`:44`), `listener: Listener?` (`:45`), `recycled = false` (`:46`)
- **Mutation propagation**: Resolves feed or reply id, calls `Listener.onDeleteFeed(...)` (`:31-37,105-137`)
- **Relationships**: Listener implemented in `FeedAdapter` (`:60-72`), `CommentAdapter` (`:50-62`)
- **Koin**: None

## 8. AutoIncrementWidget

- **File**: `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/AutoIncrementWidget.kt`
- **Signature**: `class AutoIncrementWidget ... : LinearLayout(...), CustomView, View.OnClickListener` (`:28-36`)
- **Mutable state**: `status: String?` (`:63-64`), `model: MediaList?` (`:65`), `currentUser: String?` (`:67`), `currentUserFull: UserBase?` (`:68`), `listener: Listener?` (`:69`), `recycled = false` (`:70`)
- **Mutation propagation**: **Mutates `currentModel` in place before save**, including `status`, `startedAt`, `progress`, `completedAt` (`:146-161`); emits save callback through `Listener.onSaveMediaListEntry(...)` (`:38-59,162-179`); replaces local model with saved result on success (`:181-186`)
- **Relationships**: Listener implemented by `MediaListAdapter.autoIncrementListener` (`MediaListAdapter.kt:91-116`)
- **Koin**: None

## 9. FeedAdapter

- **File**: `app/src/main/java/com/mxt/anitrend/adapter/recycler/index/FeedAdapter.kt`
- **Signature**: `class FeedAdapter(context: Context, private val coordinator: WidgetMutationCoordinator) : RecyclerViewAdapter<FeedList>` (`:36-39`)
- **Mutable state**: Inherited `data: MutableList<FeedList>`; `messageType: Int` (`:47-49`); `userRepository by lazy { koinOf() }` (`:50`); `favouriteListener`, `deleteListener` (`:52-72`)
- **Mutation propagation**: Owns adapter dataset; binds `FavouriteWidget` and `StatusDeleteWidget` to coordinator-backed listeners (`:145-158,202-218,269-285,321-332`)
- **Relationships**: Uses `WidgetMutationCoordinator`; instantiated by `FeedListFragment` (`:75`), `CommentFragment` (`:77`)
- **Koin**: None, created manually in fragments

## 10. CommentAdapter

- **File**: `app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/CommentAdapter.kt`
- **Signature**: `class CommentAdapter(context: Context, private val coordinator: WidgetMutationCoordinator) : RecyclerViewAdapter<FeedReply>` (`:29-32`)
- **Mutable state**: Inherited `data`; `favouriteListener`, `deleteListener` (`:42-62`)
- **Mutation propagation**: Owns reply adapter dataset; binds `FavouriteWidget` and `StatusDeleteWidget` to coordinator-backed listeners (`:76-107`)
- **Relationships**: Uses `WidgetMutationCoordinator`; instantiated by `CommentFragment` (`:76`)
- **Koin**: None

## 11. MediaListAdapter

- **File**: `app/src/main/java/com/mxt/anitrend/adapter/recycler/index/MediaListAdapter.kt`
- **Signature**: `class MediaListAdapter(context: Context, private val coordinator: WidgetMutationCoordinator) : RecyclerViewAdapter<MediaList>` (`:36-39`)
- **Mutable state**: Inherited `data`; `currentUser: String?` (`:40`); `autoIncrementListener` (`:91-116`)
- **Mutation propagation**: Owns media-list adapter dataset; filter mutates `clone` and `data` (`:60-85`); binds `AutoIncrementWidget` and delegates save to coordinator (`:148-161`)
- **Relationships**: Uses `WidgetMutationCoordinator`; implements listener for `AutoIncrementWidget` (`:91-116`)
- **Koin**: None

## 12. FeedListViewModel

- **File**: `app/src/main/java/com/mxt/anitrend/viewmodel/FeedListViewModel.kt`
- **Signature**: `class FeedListViewModel(...): ViewModel()` (`:21-25`)
- **Mutable state**: `_state = MutableStateFlow<UiState>(UiState.Loading)` (`:36`)
- **Mutation propagation**: Consumes `feedRepository.mutationEvents` for `FeedSaved`/`FeedDeleted` (`:39-54`); consumes `baseRepository.mutationEvents` for `LikeToggled` (`:56-75`); produces `UiState.Success(... replaceExisting = true)` through `replaceCurrentPage(...)` (`:131-146`). **Only owns the most recently loaded page.**
- **Relationships**: Calls `FeedRepository.getFeedList(...)`; driven by `FeedListFragment`
- **Koin**: `viewModel { FeedListViewModel(feedRepository = get(), baseRepository = get()) }` (`Modules.kt:571`)

## 13. UserFeedViewModel

- **File**: `app/src/main/java/com/mxt/anitrend/viewmodel/UserFeedViewModel.kt`
- **Signature**: `class UserFeedViewModel(...): ViewModel()` (`:21-25`)
- **Mutable state**: `_state = MutableStateFlow<UiState>(UiState.Loading)` (`:36-38`)
- **Mutation propagation**: Mirrors `FeedListViewModel` pattern: consumes `feedRepository.mutationEvents` (`:40-55`) and `baseRepository.mutationEvents` (`:57-76`). **Only owns the most recently loaded page.**
- **Relationships**: Calls `FeedRepository.getFeedList(...)`; no direct relationship to other listed UI classes
- **Koin**: `viewModel { UserFeedViewModel(feedRepository = get(), baseRepository = get()) }` (`Modules.kt:566`)

## 14. MediaBrowseViewModel

- **File**: `app/src/main/java/com/mxt/anitrend/viewmodel/MediaBrowseViewModel.kt`
- **Signature**: `class MediaBrowseViewModel(...): ViewModel()` (`:25-29`)
- **Mutable state**: `_state = MutableStateFlow<UiState>(UiState.Loading)` (`:42`); `loadedMedia = linkedMapOf<Long, MediaBase>()` (`:44`)
- **Mutation propagation**: Consumes `BrowseRepository.mutationEvents` to patch `media.mediaListEntry` in already-loaded media (`:47-67`); re-emits through `emitUpdatedMedia()` (`:136-148`)
- **Relationships**: Calls `BrowseRepository.getMediaBrowse(...)`; reads `BaseRepository` caches
- **Koin**: `viewModel { MediaBrowseViewModel(baseRepository = get(), browseRepository = get()) }` (`Modules.kt:546`)

## 15. MediaListViewModel

- **File**: `app/src/main/java/com/mxt/anitrend/viewmodel/MediaListViewModel.kt`
- **Signature**: `class MediaListViewModel(...): ViewModel()` (`:28-33`)
- **Mutable state**: `_state = MutableStateFlow<UiState>(UiState.Loading)` (`:45`); `currentItems: List<MediaList>` (`:48`); `currentPageInfo: PageInfo?` (`:49`); last-loaded filter snapshot (`:51-54`)
- **Mutation propagation**: Consumes `BrowseRepository.mutationEvents` (`:56-69`); on save, merges or removes `MediaList` entries in-memory (`:161-183`); `mergeFrom(...)` **mutates existing `MediaList` instance field-by-field** (`:215-235`)
- **Relationships**: Calls `BrowseRepository.getMediaListCollection(...)`; directly manipulates `MediaList` model instances
- **Koin**: `viewModel { MediaListViewModel(browseRepository = get(), userRepository = get(), settings = get()) }` (`Modules.kt:548`)

## 16. FeedListFragment

- **File**: `app/src/main/java/com/mxt/anitrend/view/fragment/list/FeedListFragment.kt`
- **Signature**: `open class FeedListFragment : FragmentBaseList<FeedList, PageContainer<FeedList>>()` (`:43`)
- **Mutable state**: `settings` (`:45`), `mutationCoordinator` (`:47`), `feedListViewModel` (`:49`), `commentActivityLauncher` (`:51-57`)
- **Mutation propagation**: Instantiates `FeedAdapter(ctx, mutationCoordinator)` (`:75`); consumes `FeedListViewModel.state` and refreshes adapter (`:81-97,175-202`); **consumes feed updates returned from `CommentActivity.extraUpdatedFeed`** (`:51-57`); applies returned feed into `FeedListViewModel.applyReturnedFeed(...)` (`:171-173`)
- **Relationships**: Uses `FeedAdapter`, `WidgetMutationCoordinator`, `FeedListViewModel`; launches `CommentActivity` and reads `extraUpdatedFeed`
- **Koin**: None, uses `inject()` and `viewModel()`

## 17. CommentFragment

- **File**: `app/src/main/java/com/mxt/anitrend/view/fragment/detail/CommentFragment.kt`
- **Signature**: `class CommentFragment : FragmentBaseComment()` (`:47`)
- **Mutable state**: `feedAdapter: FeedAdapter` (`:48`); injected `mutationCoordinator` (`:50`), `baseRepository` (`:52`), `feedRepository` (`:54`); inherited from `FragmentBaseComment`: `userActivityId` (`FragmentBaseComment.kt:48`), `feedList: FeedList?` (`:49`), `mAdapter: RecyclerViewAdapter<FeedReply>` (`:54`)
- **Mutation propagation**: Instantiates `CommentAdapter` and `FeedAdapter` (`:76-78`); submits replies/edits directly through `FeedRepository` (`:146-176`); **collects `feedRepository.mutationEvents` and `baseRepository.mutationEvents` directly, no ViewModel intermediary** (`:183-190`); updates `feedList`, reply collections, and adapters in `handleFeedMutation`, `handleBaseMutation`, `appendReply` (`:418-488`); **pushes synchronised result back via `CommentActivity.updateResult(...)`** (`:490-493`)
- **Relationships**: Uses `CommentAdapter`, `FeedAdapter`, `WidgetMutationCoordinator`, `BaseRepository`, `FeedRepository`; calls `CommentActivity.updateResult(...)`
- **Koin**: None

## 18. CommentActivity

- **File**: `app/src/main/java/com/mxt/anitrend/view/activity/detail/CommentActivity.kt`
- **Signature**: `class CommentActivity : AppCompatActivity()` (`:16`)
- **Mutable state**: Companion constant only
- **Mutation propagation**: Defines `const val extraUpdatedFeed = "extra_updated_feed"` (`:18-20`); `updateResult(feed: FeedList)` writes `setResult(RESULT_OK, Intent().putExtra(extraUpdatedFeed, feed))` (`:22-27`); creates `CommentFragment` from incoming extras (`:49-53`)
- **Relationships**: Hosts `CommentFragment`; result contract consumed by `FeedListFragment`
- **Koin**: None

## 19. BottomSheetSeriesManage

- **File**: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetSeriesManage.kt`
- **Signature**: `class BottomSheetSeriesManage : BottomSheetDialogFragment()` (`:56`)
- **Mutable state**: `mediaBase: MediaBase` (`:63`), `mediaListModel: MediaList` (`:64`), `isAnime: Boolean` (`:65`), large mutable view state set (`:68-97`), `advancedScoreSliders` (`:91`)
- **Mutation propagation**: **Initializes or creates mutable `MediaList` model from `mediaBase.mediaListEntry`** (`:135-143`); **mutates `mediaListModel` from form selection and save payload assembly** (`:390-452`); calls `coordinator.saveMediaListEntry(...)` (`:476-533`); on success, replaces `mediaListModel` and writes back to `mediaBase.mediaListEntry` (`:501-506`); calls `coordinator.deleteMediaListEntry(mediaListModel.id)` (`:536-579`)
- **Relationships**: Uses `WidgetMutationCoordinator`; owns and mutates `MediaList`
- **Koin**: None

---

## Supporting types

### RecyclerViewAdapter

- **File**: `app/src/main/java/com/mxt/anitrend/base/custom/recycler/RecyclerViewAdapter.kt`
- **Signature**: `abstract class RecyclerViewAdapter<T>(context: Context) : RecyclerView.Adapter<RecyclerViewHolder<T>>(), Filterable, RecyclerChangeListener<T>` (`:25-29`)
- **Mutable state**: `var data: MutableList<T> = ArrayList()` (`:31-32`); `protected var clone: MutableList<T>? = null` (`:33`)
- **`onItemRangeChanged` bug**: `startRange = itemCount`, `difference = swap.size - startRange`, then `data = ArrayList(swap)` and `notifyItemRangeChanged(startRange, difference)` (`:77-82`). May calculate a negative difference.
- **`getItemId` / hashCode**: If stable IDs enabled, returns `data[position].hashCode().toLong()` (`:49`). Not stable across content changes.
- **Update methods**: Full replace `onItemsInserted(swap)` (`:61-64`); append `onItemRangeInserted(swap)` (`:66-75`)

### BaseMutation sealed class

- **File**: `app/src/main/java/com/mxt/anitrend/repository/BaseRepository.kt` (`:26`)
- **Variants**: `LikeToggled(users, targetId, targetType)` (`:27-31`); `FavouriteToggled(result, animeId, mangaId, characterId, staffId, studioId)` (`:33-40`)

### FeedMutation sealed class

- **File**: `app/src/main/java/com/mxt/anitrend/repository/FeedRepository.kt` (`:22`)
- **Variants**: `FeedSaved(feed)` (`:23`); `FeedDeleted(id)` (`:24`); `ReplySaved(reply, activityId)` (`:25-28`); `ReplyDeleted(id)` (`:29`)

### FeedList model

- **File**: `app/src/main/java/com/mxt/anitrend/model/entity/anilist/FeedList.kt`
- **Mutability**: All constructor properties are `var` (`:18-30`); additional `var replies: List<FeedReply>? = null` (`:32-33`). **Mutable.**

### MediaList model

- **File**: `app/src/main/java/com/mxt/anitrend/model/entity/anilist/MediaList.kt`
- **Mutability**: All primary state fields are mutable `var` (`:19-40`). **Mutable.** `clone()` returns `this`, not a copied object (`:98-102`).

---

## Koin registrations summary

| Class | Registration | Location |
|---|---|---|
| `BrowseRepository` | `single` | `Modules.kt:521` |
| `FeedRepository` | `single` | `Modules.kt:526` |
| `BaseRepository` | `single` | `Modules.kt:527` |
| `WidgetMutationCoordinator` | `single` | `Modules.kt:529-539` |
| `MediaBrowseViewModel` | `viewModel` | `Modules.kt:546` |
| `MediaListViewModel` | `viewModel` | `Modules.kt:548` |
| `UserFeedViewModel` | `viewModel` | `Modules.kt:566` |
| `FeedListViewModel` | `viewModel` | `Modules.kt:571` |

Widgets, adapters, fragments, activities, and bottom sheets are not Koin-registered; they are created manually or via framework inflation.
