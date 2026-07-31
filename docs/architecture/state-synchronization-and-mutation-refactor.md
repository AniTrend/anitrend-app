# AniTrend State Synchronisation and Mutation Architecture Refactor Specification

**Status:** Proposed  
**Specification version:** 1.0  
**Date:** 29 July 2026  
**Primary scope:** Feed, comments, likes, replies, media-list entries, RecyclerView state, repository mutation propagation  
**Secondary scope:** Reviews, favourites, follows, and other widget-driven mutations after the primary architecture is proven  
**Implementation model:** Incremental, spec-driven migration within the existing single-module `:app` project

---

## 1. Executive decision

AniTrend shall replace its current mutation-event propagation architecture with a state-driven architecture built around:

1. Domain-specific canonical stores.
2. Immutable application and UI state.
3. ViewModels as screen-level state owners.
4. Explicit user actions and mutation commands.
5. Repositories that commit successful results into canonical state.
6. Lifecycle-independent mutation completion.
7. RecyclerView adapters and custom views that render state and emit actions only.
8. Per-resource mutation serialisation.
9. Deterministic tests covering navigation, pagination, lifecycle interruption, concurrency, and failure recovery.

The migration shall not replace EventBus with another global event bus abstraction. Repository `SharedFlow` mutation notifications shall be removed as business-state propagation mechanisms.

The target architecture is:

```text
UI event
   |
   v
ViewModel
   |
   v
Use case / interactor
   |
   v
Repository
   |
   +----> Network data source
   |
   v
Canonical store
   |
   v
Observable immutable state
   |
   v
ViewModel state reduction
   |
   v
UI rendering
```

The repository's existing architectural direction already establishes that new and refactored domain features should follow `UI -> ViewModel -> interactor/use case -> repository/data source`, and that custom views must remain view-only.

This specification extends that direction by defining how application state and cross-screen mutations must actually be owned and synchronised.

---

## 2. Problem statement

The application has moved away from presenter-driven, activity-centric data handling and removed much of its EventBus usage. However, the replacement architecture currently distributes state across several mutable and independently scoped components:

- ViewModel `StateFlow` instances
- Repository mutation `SharedFlow` instances
- RecyclerView adapter collections
- Mutable GraphQL and legacy entity objects
- Widget-local mutable state
- Activity result payloads
- Bottom sheet model copies
- Application-scoped mutation callbacks

The result is not a single unidirectional state model. It is a collection of synchronisation mechanisms whose behaviour depends on collector lifecycle, pagination position, object identity, RecyclerView recycling, and callback timing.

The practical symptoms include:

- Media-list save and progress updates not consistently appearing across screens.
- Feed activity edits not consistently updating all feed surfaces.
- Likes becoming inconsistent between feed lists and comment detail.
- Reply likes being lost when holders recycle or screens stop collecting.
- Reply counts diverging across list and detail screens.
- Activity results being used to manually repair originating screen state.
- Older paginated items failing to receive mutation updates.
- Failed optimistic mutations leaving locally mutated model objects behind.
- Application-scoped work succeeding after the initiating view has discarded its callback.

These are architectural consistency failures rather than isolated UI defects.

---

## 3. Current implementation findings

### 3.1 Repository mutation flows are transient broadcasts

`AbstractRepository` currently exposes a `MutableSharedFlow` configured with `replay = 0` and `extraBufferCapacity = 64`.

This configuration does not retain a mutation for future collectors. When no collector exists, only replay values can be retained, and no replay cache has been configured. `emit` or `tryEmit` can therefore complete while the mutation is unavailable to any later collector.

This is acceptable for best-effort telemetry. It is not acceptable as the only mechanism synchronising committed application data.

### 3.2 Feed ViewModels and adapters own different list versions

`FeedListViewModel` exposes only the most recently loaded page in its state. Its mutation reducer searches that current page when applying feed deletes and like updates.

`FeedListFragment`, however, appends normal page responses to its adapter and only replaces the adapter dataset when `replaceExisting` is true.

After page two is loaded:

- The ViewModel owns page two.
- The adapter owns pages one and two.
- A mutation to a page-one item cannot be reduced by the ViewModel.
- The stale adapter item remains visible.

The same design is duplicated in `UserFeedViewModel`.

### 3.3 Application-scoped mutation callbacks target ephemeral views

`WidgetMutationCoordinator` launches operations into an injected application `CoroutineScope`, then invokes a callback on the main dispatcher.

The coordinator and repositories are Koin singletons, and the injected application scope remains active for the process lifetime.

Widgets correctly ignore callbacks after recycling or detachment. `FavouriteWidget`, for example, returns without applying a result when it is recycled or no longer attached.

The problem is that the successful mutation has no durable completion destination other than:

- The discarded callback.
- A repository mutation event that may also be missed.

Long-running work may use an external scope when it must survive the originating screen, but successful work must commit into a durable data-layer state that later observers can read.

### 3.4 Mutable models are modified before server success

`AutoIncrementWidget` changes the current `MediaList` object before executing the save:

- Progress is incremented.
- Status may change.
- Start or completion dates may change.

Failure handling resets the loading indicator but does not restore the entity.

`MediaListAdapter` passes the adapter-held entity directly into this widget.

`BottomSheetSeriesManage` follows a similar model. Its form-building function explicitly mutates the supplied `MediaList` instance before the request completes.

This permits failed writes to contaminate local state.

### 3.5 Comment detail uses a parallel manual synchronisation channel

`CommentFragment` directly mutates its local `FeedList`, reply list, like lists, and reply count. It also publishes a complete updated `FeedList` through `CommentActivity`.

`CommentActivity` returns that complete entity through an activity result.

`FeedListFragment` receives the entity and manually injects it into its ViewModel.

This duplicates the repository mutation-flow path and attempts to repair state through parcelled entities.

### 3.6 RecyclerView adapters are mutable application-state containers

`RecyclerViewAdapter` owns a mutable collection and exposes imperative replacement and update functions.

Its current `onItemRangeChanged()` calculation uses the old item count as the starting range and may calculate a negative difference:

```kotlin
val startRange = itemCount
val difference = swap.size - startRange
data = ArrayList(swap)
notifyItemRangeChanged(startRange, difference)
```

`getItemId()` also derives stable IDs from object hash codes when stable IDs are enabled.

RecyclerView diffing requires submitted lists and the relevant item properties to remain immutable while in use.

---

## 4. Architectural objectives

The refactor must achieve all of the following.

### 4.1 State consistency

Any screen observing the same logical entity must converge on the same committed state without requiring:

- Activity result payloads.
- Sticky events.
- Manual adapter mutation.
- Screen refreshes.
- Shared object references.
- A currently active collector at mutation time.

### 4.2 Explicit ownership

Every application entity must have one canonical owner.

The source of truth owns mutation and exposes immutable state. Android's architecture guidance defines the source of truth as the exclusive owner that modifies data and exposes it through immutable types.

### 4.3 Lifecycle independence

A mutation that is intended to complete after navigation must finish into canonical state.

It must not depend on:

- A Fragment remaining started.
- A ViewHolder remaining attached.
- A callback listener still being registered.
- The originating activity still existing.

### 4.4 Deterministic rendering

A RecyclerView item must render exclusively from its current immutable UI model.

The same model must produce the same visual result regardless of:

- Previous ViewHolder occupants.
- Previous callbacks.
- Pagination history.
- Navigation history.

### 4.5 Testability

The architecture must allow state transitions, mutation ordering, error handling, pagination merging, and lifecycle-independent completion to be unit tested without Android views.

### 4.6 Incremental migration

The application must remain buildable and usable after every phase.

No phase may require:

- A complete Compose migration.
- A single-activity migration.
- Replacing ObjectBox.
- Introducing Room.
- Migrating all repositories at once.
- Rewriting every RecyclerView adapter before proving the design.

---

## 5. Non-goals

The following work is explicitly outside the required scope unless a later phase states otherwise:

1. Migrating the application to Jetpack Compose.
2. Migrating navigation to Navigation 3.
3. Splitting the single `:app` module.
4. Replacing Koin.
5. Replacing Retrofit or the generated GraphQL request system.
6. Replacing ObjectBox with Room.
7. Migrating KAPT to KSP.
8. Performing dependency or build-tool upgrades.
9. Redesigning visual components.
10. Implementing complete offline write queues.
11. Introducing a Redux or third-party MVI framework.
12. Creating one generic global store for every domain.
13. Adding a generic application event bus under another name.
14. Making all mutations optimistic.

The repository remains a single-module Gradle project, and existing build, flavour, GraphQL, and dependency conventions must be preserved.

---

## 6. Architectural principles and invariants

These rules are mandatory.

### 6.1 Single source of truth

For each migrated domain:

- The canonical store is the only mutable owner of committed entity state.
- Repositories and interactors may request store updates.
- ViewModels may observe store state.
- UI components may not modify store records directly.
- Adapters may not own a competing application-state copy.
- Navigation payloads may not be used to synchronise committed entities.

### 6.2 Unidirectional data flow

State flows downward:

```text
Store -> ViewModel -> Fragment/Activity -> Adapter -> View
```

Actions flow upward:

```text
View -> Adapter callback -> Fragment/Activity -> ViewModel -> Interactor
```

Android strongly recommends ViewModels exposing UI state and receiving UI actions through method calls or equivalent action dispatch.

### 6.3 Immutable boundaries

The following values must be immutable snapshots:

- Canonical store state.
- Domain records exposed by stores.
- ViewModel `UiState`.
- RecyclerView item models.
- Lists submitted to adapters.
- Mutation command inputs.
- Form drafts passed into mutation commands.

Legacy mutable GraphQL and ObjectBox entities may exist inside data-source or mapper boundaries during migration, but they must not be exposed as writable canonical state.

### 6.4 Business state is not an event

The following must not be delivered through a transient event stream:

- Updated feeds.
- Updated replies.
- Like collections.
- Reply counts.
- Media-list entries.
- Media progress.
- Deleted-entity state.
- Follow state.
- Favourite state.

Business state must be represented in observable store or ViewModel state.

### 6.5 UI events become state

A ViewModel-originated UI action that must not be lost must be represented in `UiState` with an identifier and explicit acknowledgement.

Examples:

- Save completed notification.
- Save failed notification.
- Delete confirmation result.
- Navigation after a successful operation.

Android's UI event guidance recommends that ViewModel-originated events result in UI state updates so they can survive lifecycle and configuration changes.

A best-effort stream may still be used for non-critical analytics or logging.

### 6.6 Server-authoritative mutations by default

During the primary migration:

- Likes are updated after server success.
- Feed edits are updated after server success.
- Reply creation and deletion are updated after server success.
- Media-list saves and deletes are updated after server success.
- Media progress increments are updated after server success.

Optimistic mutations are prohibited until a revision and rollback protocol has been implemented and tested.

### 6.7 Per-resource serialisation

Mutations affecting the same logical resource must execute sequentially.

Examples:

- Two progress increments for one media-list entry.
- Save followed by delete for one feed.
- Repeated like toggles for one activity.
- Reply edit followed by reply delete.
- Two saves from separate screens for the same media-list entry.

Mutations affecting unrelated resources may execute concurrently.

### 6.8 Stale responses must not overwrite newer state

Every store write must carry enough ordering information to reject an older response when a newer operation has already committed.

The initial implementation may use a monotonically increasing local revision assigned when the operation begins.

### 6.9 No dual ownership after phase completion

During transitional phases, compatibility paths may coexist temporarily.

At each phase exit gate, the migrated domain must have exactly one production propagation path. Compatibility code must be deleted rather than left dormant indefinitely.

---

## 7. Terminology

### 7.1 Domain record

An immutable representation of application data that is independent of Android views.

Examples:

- `FeedRecord`
- `FeedReplyRecord`
- `MediaListRecord`
- `UserSummaryRecord`

### 7.2 Canonical store

The process-level source of truth for a domain.

Examples:

- `FeedStore`
- `MediaListStore`

The initial store may be in memory. A later phase may back it with ObjectBox.

### 7.3 Query snapshot

The ordered IDs and paging metadata for one logical list request.

Examples:

- Home following feed.
- Global activity feed.
- User feed.
- Media feed.
- Current anime list.
- Completed manga list.

### 7.4 Mutation command

An immutable instruction describing requested business work.

Examples:

- `ToggleLikeCommand`
- `SaveReplyCommand`
- `IncrementMediaProgressCommand`

### 7.5 Resource key

A stable key used to serialise operations affecting the same logical entity.

Examples:

```kotlin
sealed interface ResourceKey {
    data class Feed(val feedId: Long) : ResourceKey
    data class Reply(val replyId: Long) : ResourceKey
    data class MediaListByMedia(val mediaId: Long) : ResourceKey
    data class Review(val reviewId: Long) : ResourceKey
}
```

### 7.6 Operation state

The status of an active or recently completed mutation.

```kotlin
sealed interface OperationStatus {
    data object Idle : OperationStatus

    data class Running(
        val operationId: String,
    ) : OperationStatus

    data class Failed(
        val operationId: String,
        val message: String,
    ) : OperationStatus
}
```

Committed entity state and operation state are separate concerns.

---

## 8. Proposed package structure

The project remains in `:app`.

```text
app/src/main/java/com/mxt/anitrend/
├── data/
│   ├── mapper/
│   │   ├── FeedRecordMapper.kt
│   │   └── MediaListRecordMapper.kt
│   └── store/
│       ├── feed/
│       │   ├── FeedStore.kt
│       │   ├── FeedStoreState.kt
│       │   ├── FeedStoreChange.kt
│       │   └── InMemoryFeedStore.kt
│       ├── medialist/
│       │   ├── MediaListStore.kt
│       │   ├── MediaListStoreState.kt
│       │   ├── MediaListStoreChange.kt
│       │   └── InMemoryMediaListStore.kt
│       └── mutation/
│           ├── MutationExecutor.kt
│           ├── MutationRegistry.kt
│           ├── MutationStatus.kt
│           ├── ResourceKey.kt
│           └── KeyedMutex.kt
├── domain/
│   ├── feed/
│   │   ├── model/
│   │   └── interactor/
│   ├── medialist/
│   │   ├── model/
│   │   └── interactor/
│   └── like/
│       ├── model/
│       └── interactor/
├── repository/
├── viewmodel/
└── view/
```

The exact package names may be adjusted to match existing conventions, but the separation of responsibilities must remain.

The implementation must not introduce new Gradle modules.

---

## 9. Canonical domain records

### 9.1 Feed record

The exact field set shall be based on all existing rendering requirements.

A minimum conceptual form is:

```kotlin
data class FeedRecord(
    val id: Long,
    val type: String?,
    val text: String?,
    val createdAt: Long,
    val user: UserSummaryRecord?,
    val messenger: UserSummaryRecord?,
    val recipient: UserSummaryRecord?,
    val media: MediaSummaryRecord?,
    val likes: List<UserSummaryRecord>,
    val replyCount: Int,
    val siteUrl: String?,
    val revision: Long,
)
```

### 9.2 Feed reply record

```kotlin
data class FeedReplyRecord(
    val id: Long,
    val activityId: Long,
    val reply: String?,
    val createdAt: Long,
    val user: UserSummaryRecord?,
    val likes: List<UserSummaryRecord>,
    val revision: Long,
)
```

### 9.3 Media-list record

```kotlin
data class MediaListRecord(
    val id: Long,
    val mediaId: Long,
    val status: MediaListStatus?,
    val score: Double,
    val scoreRaw: Int?,
    val progress: Int,
    val progressVolumes: Int,
    val repeat: Int,
    val priority: Int,
    val private: Boolean,
    val hiddenFromStatusLists: Boolean,
    val customLists: List<String>,
    val advancedScores: Map<String, Double>,
    val notes: String?,
    val startedAt: FuzzyDateRecord?,
    val completedAt: FuzzyDateRecord?,
    val media: MediaSummaryRecord?,
    val revision: Long,
)
```

### 9.4 Identity rules

Identity must never depend on:

- Object reference.
- `hashCode()`.
- RecyclerView position.
- Current page.
- Mutable content.

Use server IDs whenever available.

For a media-list entry not yet created, the temporary business identity is its `mediaId`.

---

## 10. Feed store design

### 10.1 State

```kotlin
data class FeedStoreState(
    val feedsById: Map<Long, FeedRecord> = emptyMap(),
    val repliesById: Map<Long, FeedReplyRecord> = emptyMap(),
    val replyIdsByFeedId: Map<Long, List<Long>> = emptyMap(),
    val queries: Map<FeedQueryKey, FeedQuerySnapshot> = emptyMap(),
)
```

### 10.2 Query key

```kotlin
data class FeedQueryKey(
    val scope: FeedScope,
    val userId: Long?,
    val mediaId: Long?,
    val activityType: ActivityType?,
    val isFollowing: Boolean?,
    val isMixed: Boolean?,
)
```

The key must include all arguments that change the logical result set.

### 10.3 Query snapshot

```kotlin
data class FeedQuerySnapshot(
    val orderedFeedIds: List<Long>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val lastUpdatedAtMillis: Long,
)
```

### 10.4 Store contract

```kotlin
interface FeedStore {
    val state: StateFlow<FeedStoreState>

    suspend fun apply(change: FeedStoreChange)

    fun observeFeed(feedId: Long): Flow<FeedRecord?>

    fun observeReplies(feedId: Long): Flow<List<FeedReplyRecord>>

    fun observeQuery(
        key: FeedQueryKey,
    ): Flow<FeedQueryResult>
}
```

### 10.5 Change contract

```kotlin
sealed interface FeedStoreChange {
    data class PageLoaded(
        val queryKey: FeedQueryKey,
        val page: Int,
        val feeds: List<FeedRecord>,
        val pageInfo: PageInfoRecord?,
    ) : FeedStoreChange

    data class FeedUpserted(
        val feed: FeedRecord,
    ) : FeedStoreChange

    data class FeedDeleted(
        val feedId: Long,
        val revision: Long,
    ) : FeedStoreChange

    data class ReplyUpserted(
        val feedId: Long,
        val reply: FeedReplyRecord,
    ) : FeedStoreChange

    data class ReplyDeleted(
        val feedId: Long,
        val replyId: Long,
        val revision: Long,
    ) : FeedStoreChange

    data class FeedLikesReplaced(
        val feedId: Long,
        val likes: List<UserSummaryRecord>,
        val revision: Long,
    ) : FeedStoreChange

    data class ReplyLikesReplaced(
        val feedId: Long,
        val replyId: Long,
        val likes: List<UserSummaryRecord>,
        val revision: Long,
    ) : FeedStoreChange
}
```

For `FeedUpserted` and `ReplyUpserted`, the `revision` field on the embedded `FeedRecord` or `FeedReplyRecord` serves as the ordering information required by section 13.5. For `FeedDeleted`, `ReplyDeleted`, `FeedLikesReplaced`, and `ReplyLikesReplaced`, the explicit `revision` field carries the operation revision. Every store change carries revision information; the store reducer must reject any change whose revision is older than the currently committed revision for the affected resource.

### 10.6 Atomicity requirements

A reply upsert or delete must atomically update:

- `repliesById`
- `replyIdsByFeedId`
- Parent feed `replyCount`

No collector may observe an updated reply collection with an old reply count.

A feed delete must atomically remove:

- The feed record.
- Its ID from all query snapshots.
- Related reply mappings.
- Related pending operation metadata when safe.

---

## 11. Media-list store design

### 11.1 State

```kotlin
data class MediaListStoreState(
    val entriesById: Map<Long, MediaListRecord> = emptyMap(),
    val entryIdByMediaId: Map<Long, Long> = emptyMap(),
    val queries: Map<MediaListQueryKey, MediaListQuerySnapshot> = emptyMap(),
)
```

### 11.2 Query key

```kotlin
data class MediaListQueryKey(
    val userId: Long?,
    val userName: String?,
    val mediaType: MediaType?,
    val statuses: Set<MediaListStatus>,
    val sort: MediaListSort?,
)
```

### 11.3 Store contract

```kotlin
interface MediaListStore {
    val state: StateFlow<MediaListStoreState>

    suspend fun apply(change: MediaListStoreChange)

    fun observeEntryByMediaId(
        mediaId: Long,
    ): Flow<MediaListRecord?>

    fun observeQuery(
        key: MediaListQueryKey,
    ): Flow<MediaListQueryResult>
}
```

### 11.4 Required changes

```kotlin
sealed interface MediaListStoreChange {
    data class CollectionLoaded(
        val queryKey: MediaListQueryKey,
        val entries: List<MediaListRecord>,
        val pageInfo: PageInfoRecord?,
    ) : MediaListStoreChange

    data class EntryUpserted(
        val entry: MediaListRecord,
    ) : MediaListStoreChange

    data class EntryDeleted(
        val entryId: Long,
        val mediaId: Long?,
        val revision: Long,
    ) : MediaListStoreChange
}
```

For `EntryUpserted`, the `revision` field on the embedded `MediaListRecord` serves as the ordering information required by section 13.5. For `EntryDeleted`, the explicit `revision` field carries the operation revision. Every store change carries revision information; the store reducer must reject any change whose revision is older than the currently committed revision for the affected resource.

### 11.5 Query membership rules

When an updated entry changes status:

- Remove it from query snapshots it no longer matches.
- Add it to loaded query snapshots it now matches when sufficient query information exists.
- Preserve configured ordering.
- Never force a network refresh solely to make the local list internally consistent.

A background refresh may still be scheduled to reconcile with the server.

---

## 12. Repository responsibilities

Repositories remain the data-layer gateway.

For migrated methods, a repository must:

1. Validate and normalise command input.
2. Execute the GraphQL request.
3. Validate the response.
4. Map the response into immutable domain records.
5. Commit the result to the canonical store.
6. Return a mutation outcome that describes operation success or failure.

Repositories must not require UI code to manually apply the returned entity.

Example:

```kotlin
suspend fun saveMediaListEntry(
    command: SaveMediaListEntryCommand,
): MutationResult = withContext(ioDispatcher) {
    runCatching {
        val response = browseService
            .saveMediaListEntry(command.toRequest())
            .execute()

        val entity = validateResponse(response)
        val record = mediaListRecordMapper.map(entity)

        mediaListStore.apply(
            MediaListStoreChange.EntryUpserted(record),
        )

        MutationResult.Success
    }.getOrElse { throwable ->
        MutationResult.Failure(
            message = throwable.message ?: "Unable to save media-list entry",
            cause = throwable,
        )
    }
}
```

The caller observes the resulting store state. It does not patch an adapter with `entity`.

Repositories define the single source of truth and reconcile network and local data.

---

## 13. Mutation execution pipeline

### 13.1 Command flow

```text
View emits action
    |
ViewModel validates screen-specific context
    |
Interactor receives immutable command
    |
MutationExecutor obtains resource lock
    |
MutationRegistry marks operation Running
    |
Repository performs network request
    |
Repository commits authoritative response into store
    |
MutationRegistry clears Running or records Failure
    |
ViewModel-observed store state updates UI
```

### 13.2 Mutation executor

```kotlin
interface MutationExecutor {
    suspend fun <T> execute(
        resourceKey: ResourceKey,
        operationKey: OperationKey,
        block: suspend () -> T,
    ): T
}
```

### 13.3 Keyed mutex

`KeyedMutex` must:

- Serialise commands sharing the same `ResourceKey`.
- Permit unrelated commands to execute concurrently.
- Remove unused mutex entries after no active or waiting operation remains.
- Remain deterministic under coroutine cancellation.
- Never hold the internal map lock while executing network work.

### 13.4 Operation IDs

Each operation receives a unique `operationId`.

The operation ID must appear in:

- Mutation logs.
- `MutationRegistry`.
- Failure state.
- Tests involving stale responses.
- Optional crash reports.

### 13.5 Revision protocol

A local revision must increase whenever an operation begins for a resource.

The repository response commits only when:

```text
responseRevision >= currentCommittedRevision
```

An older response must be logged and ignored.

### 13.6 Cancellation rules

ViewModel cancellation and mutation cancellation are separate decisions.

Use `viewModelScope` when the operation should stop with the screen.

Use an application or repository scope only when the operation is explicitly required to finish after navigation.

For the primary domains:

| Mutation | Required lifetime |
|---|---|
| Toggle like | May finish after navigation |
| Save feed activity | May finish after navigation |
| Delete feed activity | May finish after navigation |
| Save reply | May finish after navigation |
| Delete reply | May finish after navigation |
| Save media-list entry | Must finish after navigation |
| Delete media-list entry | Must finish after navigation |
| Increment media progress | Must finish after navigation |

Operations that finish after navigation must commit exclusively through the store.

No application-scoped coroutine may directly hold or invoke:

- `View`
- `Fragment`
- `Activity`
- View binding
- RecyclerView holder
- Adapter listener
- Dialog
- Context other than an application context required by the data layer

---

## 14. Mutation registry

### 14.1 Purpose

The mutation registry tracks in-flight and failed work independently of committed entity state.

```kotlin
interface MutationRegistry {
    val state: StateFlow<Map<OperationKey, OperationStatus>>

    suspend fun markRunning(
        operationKey: OperationKey,
        operationId: String,
    )

    suspend fun markFailed(
        operationKey: OperationKey,
        operationId: String,
        message: String,
    )

    suspend fun clear(
        operationKey: OperationKey,
        operationId: String,
    )
}
```

### 14.2 UI derivation

ViewModels combine:

- Store state.
- Query state.
- Mutation registry state.
- Screen-local input state.

Example:

```kotlin
val state: StateFlow<FeedUiState> =
    combine(
        feedStore.observeQuery(queryKey),
        mutationRegistry.state,
        refreshState,
    ) { query, operations, refresh ->
        FeedUiState(
            items = query.feeds.map { feed ->
                feedUiModelMapper.map(
                    feed = feed,
                    operation = operations[OperationKey.feedLike(feed.id)],
                )
            },
            refreshState = refresh,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState.Loading,
    )
```

### 14.3 Failure acknowledgement

A failure that must be shown to the user remains in ViewModel state until acknowledged.

```kotlin
data class UiMessage(
    val id: String,
    val text: String,
)
```

The UI calls:

```kotlin
viewModel.onMessageShown(message.id)
```

Critical outcomes must not depend on a collector being active at the exact emission time.

---

## 15. ViewModel requirements

A migrated ViewModel must:

1. Own the complete state rendered by its screen.
2. Observe stores rather than repository mutation events.
3. Merge all loaded pages into one query representation.
4. Receive semantic user actions.
5. Delegate business mutations to interactors.
6. Expose immutable `StateFlow<UiState>`.
7. Keep Android `View`, `Context`, Fragment, Activity, and adapter references out of the ViewModel.
8. Derive loading and per-item operation state.
9. Reject duplicate actions while an equivalent operation is active where appropriate.
10. Persist only navigation and form state in `SavedStateHandle`, not canonical application entities.

Example:

```kotlin
sealed interface FeedAction {
    data class ToggleLike(
        val feedId: Long,
    ) : FeedAction

    data class DeleteFeed(
        val feedId: Long,
    ) : FeedAction

    data class SubmitReply(
        val feedId: Long,
        val text: String,
    ) : FeedAction

    data class DeleteReply(
        val feedId: Long,
        val replyId: Long,
    ) : FeedAction

    data object Retry : FeedAction
}
```

```kotlin
fun onAction(action: FeedAction) {
    when (action) {
        is FeedAction.ToggleLike -> toggleLike(action.feedId)
        is FeedAction.DeleteFeed -> deleteFeed(action.feedId)
        is FeedAction.SubmitReply -> submitReply(action)
        is FeedAction.DeleteReply -> deleteReply(action)
        FeedAction.Retry -> refresh()
    }
}
```

---

## 16. RecyclerView requirements

### 16.1 Adapter role

Adapters may:

- Render immutable item models.
- Forward click and long-click actions.
- Apply DiffUtil payload updates.
- Own purely visual resources needed for binding.

Adapters may not:

- Call repositories.
- Resolve repositories through Koin.
- Resolve mutation coordinators through Koin.
- Launch business coroutines.
- Modify domain records.
- Maintain a canonical list independent of the ViewModel.
- Infer operation success.
- Update another screen.

### 16.2 Required adapter model

Migrated adapters should use `ListAdapter` or an `AsyncListDiffer`.

```kotlin
data class FeedItemUiModel(
    val id: Long,
    val type: FeedItemType,
    val headline: CharSequence,
    val body: CharSequence?,
    val likes: List<UserUiModel>,
    val likeCount: Int,
    val isLikedByCurrentUser: Boolean,
    val replyCount: Int,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val isLikePending: Boolean,
    val isDeletePending: Boolean,
)
```

### 16.3 Diffing

```kotlin
object FeedItemDiff : DiffUtil.ItemCallback<FeedItemUiModel>() {
    override fun areItemsTheSame(
        oldItem: FeedItemUiModel,
        newItem: FeedItemUiModel,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: FeedItemUiModel,
        newItem: FeedItemUiModel,
    ): Boolean = oldItem == newItem
}
```

No submitted list or item may be mutated after submission.

### 16.4 Stable IDs

When enabled, stable IDs must use actual stable domain IDs.

For heterogeneous lists, IDs must include an entity-type namespace to prevent collisions.

### 16.5 Legacy adapter containment

The generic `RecyclerViewAdapter` may remain for non-migrated screens.

Migrated screens must not continue using its mutable list as their source of truth.

---

## 17. Custom view requirements

Custom views remain view-only, consistent with `AGENTS.md`.

A custom view may:

- Display an immutable render model.
- Display content, loading, disabled, and failure visuals.
- Emit user actions.
- Reset purely visual state during recycling.

A custom view may not:

- Own a repository.
- Own a ViewModel.
- Resolve Koin business dependencies.
- Mutate a domain entity.
- Call `WidgetMutationCoordinator`.
- Launch business work.
- Decide committed application state.

Example:

```kotlin
data class FavouriteWidgetState(
    val count: Int,
    val isLiked: Boolean,
    val isEnabled: Boolean,
    val isLoading: Boolean,
)
```

```kotlin
fun render(state: FavouriteWidgetState) {
    // Render only.
}
```

```kotlin
fun setOnToggleListener(listener: (() -> Unit)?) {
    // Emit user action only.
}
```

The widget must not receive `Result<List<UserBase>>` callbacks.

---

## 18. Navigation requirements

### 18.1 Identity-only navigation

Navigation destinations should receive stable identities and presentation-independent arguments.

The comment destination should receive:

```text
feedId
```

It should not receive the complete mutable `FeedList` as the source of truth.

### 18.2 Activity results

Activity results may return:

- Explicit user selections.
- File URIs.
- Authentication results.
- Small result identifiers where navigation APIs require them.

Activity results must not return a complete application entity to synchronise another screen.

The following compatibility paths must eventually be removed:

- `CommentActivity.extraUpdatedFeed`
- `CommentActivity.updateResult(feed)`
- `CommentFragment.publishUpdatedFeedResult()`
- `FeedListViewModel.applyReturnedFeed()`
- `FeedListFragment.applyUpdatedFeedResult()`

### 18.3 Deep links

Deep links must resolve an ID and load current state from the canonical store and repository.

No deep link should require a pre-parcelled entity.

---

## 19. Paging requirements

### 19.1 Immediate architecture

Paging 3 is not required for the initial migration.

The initial canonical store must correctly merge manually requested pages.

### 19.2 Page merging rules

For page one:

- Replace the query's ordered ID list.
- Upsert all returned entities.
- Replace paging metadata.
- Clear obsolete loaded-page metadata.

For later pages:

- Upsert all returned entities.
- Append previously unseen IDs.
- Preserve the server order.
- Never duplicate IDs.
- Update paging metadata.

### 19.3 Refresh concurrency

Each query must track a request generation.

When a refresh begins, increment the generation.

A response may update a query only when its generation matches the active generation.

This prevents an old page or refresh response from overwriting newer query state.

### 19.4 Future Paging 3 migration

Paging 3 may be introduced after:

- Canonical entity ownership exists.
- Entity immutability exists.
- Query-key semantics are stable.
- Mutation updates can update visible entities without forced refresh.

Paging 3 must not be used to avoid solving canonical entity synchronisation.

---

## 20. Persistence strategy

### 20.1 Initial implementation

The first canonical stores shall be in-memory process stores.

This reduces scope and allows the architecture to be proven before database schema work.

### 20.2 ObjectBox integration

After the in-memory architecture is stable, evaluate ObjectBox-backed stores for:

- Process-death restoration.
- Offline feed and media-list reading.
- Reduced network refetching.
- Background reconciliation.
- Cross-session consistency.

ObjectBox remains the preferred existing persistence technology unless a documented capability gap justifies another database.

### 20.3 Offline-first target

A future offline-first architecture should expose the local source as the exclusive read source for higher layers, with network results updating that local source. This is the Android-recommended model for offline-first repositories.

Full offline-first support is not required to complete the primary refactor.

---

## 21. Logging and observability

Every mutation must log structured fields:

```text
operationId
operationType
resourceKey
screen
startedAt
completedAt
durationMs
result
storeRevisionBefore
storeRevisionAfter
ignoredAsStale
```

Logs must not include:

- Access tokens.
- Private message text.
- User notes.
- Authentication headers.
- Personally sensitive content.

Required log stages:

1. Action accepted.
2. Action rejected as duplicate.
3. Resource lock acquired.
4. Network request started.
5. Network result received.
6. Store commit started.
7. Store commit completed.
8. Response ignored as stale.
9. Operation completed.
10. Operation failed.

This logging must make it possible to distinguish:

- Network failure.
- Mapping failure.
- Store failure.
- Lifecycle-detached UI.
- Duplicate action suppression.
- Stale response rejection.
- Rendering failure.

---

## 22. Multi-phase implementation plan

## Phase 0: Specification lock and behavioural baseline

### Objective

Establish reproducible failing scenarios and architecture constraints before production behaviour changes.

### Required work

1. Add this specification under:

```text
docs/architecture/state-synchronization-and-mutation-refactor.md
```

2. Update `AGENTS.md` with concise mandatory rules covering:
   - Canonical stores.
   - No business-state mutation flows.
   - No repository access from adapters or widgets.
   - Immutable RecyclerView items.
   - Identity-only navigation.
   - Per-resource mutation serialisation.

3. Add baseline regression tests reproducing:
   - Feed page-one like after page two is loaded.
   - Reply like while comment fragment is stopped.
   - Media progress save failure after local increment.
   - Mutation completing after holder recycling.
   - Comment detail returning with changed reply count.
   - Two progress increments issued rapidly.
   - Older network response arriving after a newer response.

4. Add test fixtures and fake repositories where required.

5. Document existing classes and call paths in a migration inventory.

### Migration inventory

The inventory must include at minimum:

- `AbstractRepository`
- `BaseRepository`
- `BrowseRepository`
- `FeedRepository`
- `WidgetMutationCoordinator`
- `FavouriteWidget`
- `StatusDeleteWidget`
- `AutoIncrementWidget`
- `FeedAdapter`
- `CommentAdapter`
- `MediaListAdapter`
- `FeedListViewModel`
- `UserFeedViewModel`
- `MediaBrowseViewModel`
- `MediaListViewModel`
- `FeedListFragment`
- `CommentFragment`
- `CommentActivity`
- `BottomSheetSeriesManage`

### Tests

Tests in this phase may initially fail when explicitly marked as architectural regression tests, but the repository's normal CI test suite must continue passing.

Do not commit permanently ignored tests without a linked phase and removal condition.

### Exit criteria

- Specification is committed.
- Migration inventory exists.
- Every reported inconsistency has a reproducible automated test or a documented reason why instrumentation is required.
- No production architecture has yet been replaced.
- Existing builds and tests remain green.

### Prohibited shortcuts

- Do not increase `SharedFlow.replay`.
- Do not add delays to tests or production code.
- Do not force screen refreshes as a fix.
- Do not modify adapter items directly to make baseline tests pass.

---

## Phase 1: Contain mutable-state and RecyclerView defects

### Objective

Stop newly initiated mutations from corrupting local mutable objects before the canonical stores are introduced.

### Required work

1. Fix or deprecate `RecyclerViewAdapter.onItemRangeChanged()`.
2. Remove mutable `hashCode()` stable-ID behaviour from migrated adapters.
3. Introduce immutable mutation commands:
   - `SaveMediaListEntryCommand`
   - `IncrementMediaProgressCommand`
   - `ToggleLikeCommand`
   - `DeleteFeedCommand`
   - `DeleteReplyCommand`
4. Replace `AutoIncrementWidget.updateModelState()` model mutation with creation of an immutable command.
5. Introduce `MediaListDraft` for the manage bottom sheet.
6. Ensure form editing modifies only the draft.
7. On successful save, accept only the repository response as committed state.
8. On failure, keep the original model unchanged.
9. Add operation duplicate protection in the UI as an immediate containment measure.

### Media-list increment rule

Given committed progress `N`:

- Construct command with requested progress `N + 1`.
- Do not set the bound entity to `N + 1`.
- Display loading state.
- On success, render the authoritative response.
- On failure, render committed progress `N`.

### Tests

- Failed increment leaves progress unchanged.
- Successful increment applies server-returned progress.
- Status and dates remain unchanged on failure.
- Bottom-sheet cancellation does not mutate the source entity.
- Bottom-sheet failure does not mutate the source entity.
- RecyclerView stable IDs remain constant across content changes.

### Exit criteria

- No migrated widget mutates a domain entity before success.
- Baseline failed-write tests pass.
- Existing repository mutation streams may still exist.
- Existing callbacks may temporarily remain.
- Build and complete unit suite pass.

### Prohibited shortcuts

- Do not deep-copy through parcelisation as the permanent draft mechanism.
- Do not mutate and then manually roll back the same object.
- Do not suppress errors to preserve UI appearance.

---

## Phase 2: Make ViewModels own complete rendered list state

### Objective

Eliminate the split where ViewModels own the latest page and adapters own accumulated pages.

### Required work

1. Refactor `FeedListViewModel` to track all loaded pages.
2. Refactor `UserFeedViewModel` using the same query-accumulation abstraction.
3. Refactor `MediaBrowseViewModel` so emitted state contains the complete loaded result represented by the screen.
4. Define immutable item UI models.
5. Migrate the selected feed adapter path to `ListAdapter`.
6. Make fragments call `submitList(state.items)`.
7. Remove adapter append logic for migrated screens.
8. Keep pagination metadata inside ViewModel state.
9. Add request-generation protection for refresh and pagination.
10. Ensure mutations can target any loaded entity, not only the latest page.

### Suggested transitional state

```kotlin
data class PagedFeedUiState(
    val items: List<FeedItemUiModel>,
    val loadedPages: Set<Int>,
    val pageInfo: PageInfoRecord?,
    val isInitialLoading: Boolean,
    val isRefreshing: Boolean,
    val isAppending: Boolean,
    val errorMessage: UiMessage?,
)
```

### Tests

- Load page one, then page two, then update an item from page one.
- Load duplicate IDs across pages and verify one rendered item.
- Refresh after pagination and verify old pages are replaced.
- Older refresh response cannot overwrite newer refresh.
- Adapter state equals ViewModel state after every update.
- Rotation does not cause duplicate pages.

### Exit criteria

- Migrated screen adapters no longer own independently accumulated domain data.
- ViewModel state exactly represents the rendered list.
- Page-one mutation after later pagination passes.
- Repository mutation streams may still provide temporary updates, but the ViewModel can reduce them against the complete list.

### Prohibited shortcuts

- Do not call `notifyDataSetChanged()` to conceal incorrect state ownership.
- Do not keep a second mutable list in the Fragment.
- Do not refresh the entire list after every mutation.

---

## Phase 3: Introduce canonical in-memory stores

### Objective

Create durable process-level state owners for feed and media-list entities.

### Required work

1. Add immutable feed and media-list domain records.
2. Add network/entity-to-record mappers.
3. Add `FeedStore`.
4. Add `MediaListStore`.
5. Add deterministic in-memory implementations.
6. Register stores as Koin singletons.
7. Update repository read methods to hydrate stores.
8. Update repository mutation methods to commit successful responses to stores.
9. Add query-key and query-snapshot support.
10. Add atomic store change reducers.
11. Add revision handling.
12. Add store invariant validation in debug builds and tests.

### Store invariants

Feed store:

- Every ID in a feed query exists in `feedsById`.
- Every reply ID in `replyIdsByFeedId` exists in `repliesById`.
- Parent `replyCount` equals the number of known replies when the complete reply list is loaded.
- No query contains duplicate feed IDs.

Media-list store:

- Every `entryIdByMediaId` value exists in `entriesById`.
- Every query ID exists in `entriesById`.
- No query contains duplicate entry IDs.
- An entry appears only in queries whose local predicates it satisfies.

### Tests

- Store upsert is deterministic.
- Store delete removes all references.
- Reply upsert atomically changes reply count.
- Reply delete atomically changes reply count.
- Query page merge preserves order.
- Duplicate page IDs are removed.
- Stale revisions are rejected.
- Two concurrent updates cannot produce an invalid store.

### Exit criteria

- Repositories hydrate stores on reads.
- Repositories commit successful mutations to stores.
- Store tests cover every change type.
- Existing UI may still temporarily consume old repository responses while store observation is introduced.

### Prohibited shortcuts

- Do not store adapter or View objects.
- Do not expose mutable internal maps or lists.
- Do not store the same mutable GraphQL entity instance received from Retrofit.
- Do not create one global `ApplicationStore`.

---

## Phase 4: Introduce mutation executor and interactors

### Objective

Centralise mutation ordering, operation state, lifecycle behaviour, and error handling.

### Required work

1. Add `ResourceKey`.
2. Add `OperationKey`.
3. Add `KeyedMutex`.
4. Add `MutationExecutor`.
5. Add `MutationRegistry`.
6. Add operation ID generation.
7. Add feed and media-list mutation interactors.
8. Move mutation orchestration out of `WidgetMutationCoordinator`.
9. Define which operations use an external application scope.
10. Ensure external-scope work only commits to stores.
11. Add structured mutation logs.
12. Return `MutationResult`, not entities for manual UI patching.

### Required interactors

At minimum:

- `ToggleLikeInteractor`
- `SaveFeedInteractor`
- `DeleteFeedInteractor`
- `SaveReplyInteractor`
- `DeleteReplyInteractor`
- `SaveMediaListEntryInteractor`
- `DeleteMediaListEntryInteractor`
- `IncrementMediaProgressInteractor`

A shared internal implementation may reduce duplication, but public responsibilities must remain explicit.

### Tests

- Commands on the same resource execute sequentially.
- Commands on different resources execute concurrently.
- Cancellation while waiting for a lock does not leak the lock.
- Failed operations clear or update registry state.
- Stale operation response cannot overwrite a newer commit.
- Operation completing after ViewModel destruction updates the store.
- No callback to a destroyed view occurs.

### Exit criteria

- Interactors provide the only approved entry point for migrated mutations.
- Mutation operation state is observable.
- Per-resource serialisation is proven by tests.
- External scope is confined to the data or domain layer.

### Prohibited shortcuts

- Do not implement serialisation with arbitrary delays or debounce.
- Do not use one global `Mutex`.
- Do not keep callbacks to Views.
- Do not use repository mutation `SharedFlow` as the completion signal.

---

## Phase 5: Feed and comment vertical-slice migration

### Objective

Fully migrate feed lists, comment detail, replies, and activity likes to the canonical architecture.

### Required work

1. Refactor `FeedListViewModel` to observe `FeedStore`.
2. Refactor `UserFeedViewModel` to observe `FeedStore`.
3. Refactor other visible feed ViewModels in the same entity domain:
   - `MediaFeedViewModel`
   - `MessageFeedViewModel`
4. Add a dedicated comment-detail ViewModel.
5. Change comment navigation to pass `feedId`.
6. Load current detail state through `FeedStore`.
7. Route feed and reply actions through ViewModels and interactors.
8. Replace `FeedAdapter` and `CommentAdapter` coordinator dependencies with action lambdas.
9. Refactor `FavouriteWidget` and `StatusDeleteWidget` into render-only controls.
10. Remove direct local `FeedList` and reply mutation from `CommentFragment`.
11. Remove activity-result entity synchronisation.
12. Remove migrated feed and like mutation collectors.
13. Ensure reply count is derived from canonical state.
14. Ensure feed detail and all list surfaces observe the same feed record.

### Required deletions

After compatibility is no longer used:

- `CommentActivity.extraUpdatedFeed`
- `CommentActivity.updateResult()`
- `CommentFragment.publishUpdatedFeedResult()`
- `FeedListViewModel.applyReturnedFeed()`
- `UserFeedViewModel.applyReturnedFeed()`
- Feed-related `BaseMutation.LikeToggled` consumption
- Feed-related `FeedMutation` consumption
- Feed and comment use of `WidgetMutationCoordinator`

### Tests

#### Feed list and detail

- Like an activity from the list and verify detail state.
- Like an activity from detail and verify every loaded feed query.
- Edit an activity from detail and verify all lists.
- Delete an activity from detail and verify removal from all lists.
- Delete an activity from a list and verify detail resolves a deleted state.

#### Replies

- Add a reply and verify reply count in every feed surface.
- Edit a reply and verify detail state.
- Like a reply and navigate away before completion.
- Delete a reply and verify count and collection atomically.
- Open comment detail by deep link with no preloaded feed entity.

#### Lifecycle

- Rotate during every mutation.
- Stop the Fragment during every mutation.
- Recycle the initiating holder during every mutation.
- Destroy the originating activity before completion.
- Recreate comment detail and observe the committed result.

#### Paging

- Mutate an item from an older loaded page.
- Refresh while a mutation is running.
- Receive the refresh response before the mutation response.
- Receive the mutation response before the refresh response.
- Verify the newest authoritative state wins.

### Exit criteria

- Feed and comment business state no longer depends on repository mutation flows.
- No feed or reply entity is returned through activity results.
- Feed and comment adapters are render-only.
- Feed and reply widgets are render-only.
- All feed surfaces converge on the same store state.
- All required tests pass.

### Prohibited shortcuts

- Do not refresh all feed screens after every mutation.
- Do not add sticky events.
- Do not use Fragment Result API to return full entities.
- Do not keep old and new mutation paths active after the phase exits.

---

## Phase 6: Media-list vertical-slice migration

### Objective

Fully migrate media-list entry state, list management, media detail state, and progress increment operations.

### Required work

1. Refactor `MediaListViewModel` to observe `MediaListStore`.
2. Update media browse and media detail ViewModels to combine media data with `MediaListStore` entries by `mediaId`.
3. Route manage-sheet save and delete through the owning ViewModel or a scoped sheet ViewModel.
4. Remove direct Koin lookup of `WidgetMutationCoordinator` from `BottomSheetSeriesManage`.
5. Route progress increment through `IncrementMediaProgressInteractor`.
6. Convert `MediaListAdapter` to immutable item models and action callbacks.
7. Refactor `AutoIncrementWidget` into a render-only control.
8. Update query memberships locally when status changes.
9. Apply server-authoritative results to every screen observing the media.
10. Remove media-list mutation-event collectors.
11. Delete media-list responsibilities from `WidgetMutationCoordinator`.

### Sheet ownership options

Approved options:

#### Option A: Activity or Fragment ViewModel

The sheet emits `MediaListDraft` through a callback or Fragment Result containing only the draft command data. The owning ViewModel executes the mutation.

#### Option B: Sheet-scoped ViewModel

The bottom sheet obtains a ViewModel that calls the same interactor and observes the canonical store.

The sheet-scoped ViewModel may not become a second source of committed media-list state.

### Tests

- Save a new list entry from media detail and verify media browse and user list.
- Edit an existing entry and verify every observing screen.
- Change status and verify query membership changes without refetch.
- Delete an entry and verify it disappears from user list and media detail.
- Increment progress from an older paginated item.
- Trigger two increments rapidly and verify sequential final progress.
- Fail an increment and verify committed progress remains unchanged.
- Close the screen before save completion and verify state on return.
- Open manage sheet from two screens and issue competing saves.
- Verify stale response rejection.

### Exit criteria

- `MediaListStore` is the canonical runtime owner.
- No media-list view or adapter mutates committed entities.
- No media-list operation depends on a view callback for state propagation.
- Media-list repository mutation events have no production consumers.
- Required tests pass.

### Prohibited shortcuts

- Do not reload the complete media-list collection after every save.
- Do not use mutable `MediaList` instances as drafts.
- Do not keep `WidgetMutationCoordinator` as an adapter dependency.

---

## Phase 7: Remove legacy mutation infrastructure and migrate secondary domains

### Objective

Delete obsolete global mutation broadcasting and apply the proven architecture to remaining widget-driven domains.

### Required work

1. Search for all `mutationEvents` collectors and emitters.
2. Categorise each event as:
   - Business state.
   - Critical UI state.
   - Best-effort telemetry.
3. Migrate remaining business-state cases.
4. Remove `_mutationEvents` and `mutationEvents` from `AbstractRepository`.
5. Remove `emitMutationEvent()`.
6. Delete unused mutation sealed classes.
7. Delete `WidgetMutationCoordinator` when its final consumer is removed.
8. Migrate:
   - Review ratings.
   - Review saves and deletes.
   - Follow state.
   - Favourite state.
   - Other remaining mutation widgets.
9. Add canonical stores only when cross-screen state requires them.
10. Use local ViewModel state for screen-private data.

### Store selection rule

Create a canonical domain store when any of the following are true:

- The entity is displayed on multiple screens.
- The same entity appears in multiple lists.
- Mutations may outlive the initiating screen.
- The entity participates in pagination.
- Background work may change the entity.
- A later screen must observe a completed mutation.

Do not create a store for a purely local one-screen form with no shared committed state.

### Tests

- No business mutation is lost when no UI collector exists.
- No adapter or widget resolves a repository.
- No repository publishes business-state mutation events.
- Secondary-domain list and detail screens converge after mutation.

### Exit criteria

- `AbstractRepository` no longer implements mutation broadcasting.
- `WidgetMutationCoordinator` is deleted.
- No business-state EventBus replacement remains.
- Repository writes commit to state owners.
- Full test suite passes.

---

## Phase 8: Persistence and offline-read evaluation

### Objective

Determine and implement the appropriate persistence level after runtime architecture is proven.

### Required work

1. Document which store data must survive process death.
2. Audit existing ObjectBox entities and queries.
3. Decide whether feed and media-list domain records can map cleanly to ObjectBox.
4. Add ObjectBox-backed implementations where justified.
5. Preserve store interfaces so ViewModels remain unchanged.
6. Add startup hydration.
7. Add network refresh reconciliation.
8. Define data expiry.
9. Define account-change clearing.
10. Define logout clearing.
11. Define schema migration strategy.
12. Evaluate Paging 3 after local query support exists.

### Mandatory account isolation

Stored user-specific data must be keyed or cleared according to authenticated account identity.

Logging out must not expose the previous user's:

- Media lists.
- Private feed data.
- Messages.
- Notifications.
- Pending mutations.

### Tests

- Process recreation restores required state.
- Logout clears account-specific state.
- Switching accounts does not leak state.
- Network refresh updates local canonical state.
- Offline reads return local state.
- Expired state follows documented refresh behaviour.

### Exit criteria

- Persistence decisions are documented per domain.
- Required persistent domains use ObjectBox-backed stores.
- Non-persistent domains explicitly document process-lifetime semantics.
- ViewModels remain unaware of persistence implementation.

---

## Phase 9: Enforcement, documentation, and completion

### Objective

Prevent architectural regression after the migration.

### Required work

1. Update `AGENTS.md`.
2. Update the existing ViewModel-first architecture document.
3. Add this document to the repository architecture index.
4. Add static-analysis or test checks where practical.
5. Add architecture examples for:
   - List screen.
   - Detail screen.
   - Mutation.
   - Bottom sheet.
   - RecyclerView widget.
6. Add a pull-request checklist.
7. Remove temporary compatibility adapters and mappers.
8. Remove deprecated mutation APIs.
9. Search for prohibited patterns.
10. Run full verification.

### Suggested automated checks

A lightweight custom test or lint rule should flag:

- `Repository` injection into adapters.
- `WidgetMutationCoordinator` references.
- `mutationEvents` declarations.
- `koinOf<Repository>()` from views or adapters.
- `CoroutineScope(...)` creation inside widgets.
- Application entity extras used as synchronisation results.
- Direct domain-model mutation inside adapters and widgets.

### Final verification commands

The repository's documented commands must be used:

```bash
bash .github/scripts/setup-config.sh
./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug --no-daemon
./gradlew :app:assembleGithubDebug
./gradlew test --stacktrace
```

These commands are defined in the repository agent guidance.

### Exit criteria

- All phases are complete.
- All obsolete compatibility paths are deleted.
- Documentation reflects actual code.
- Architecture checks pass.
- All unit and instrumentation tests pass.
- No known state synchronisation defect remains open without a documented exception.

---

## 23. Testing strategy

## 23.1 Unit tests

Required test categories:

### Store reducer tests

Use real store implementations.

Do not mock the store under test.

### Mapper tests

Verify network and legacy entities map into immutable records without sharing mutable collections.

### ViewModel tests

Use:

- Fake stores.
- Fake interactors.
- `kotlinx-coroutines-test`.
- Injected dispatchers.

Verify exact state sequences where meaningful.

### Mutation executor tests

Test:

- Lock ordering.
- Concurrency.
- Cancellation.
- Failure.
- Stale response rejection.
- Registry cleanup.

### Repository tests

Use fake services or deterministic response fixtures.

Verify that successful responses commit to stores and failures do not.

## 23.2 Integration tests

Test a real repository, mapper, and in-memory store together.

Required scenarios:

- Fetch page and observe query.
- Mutate entity and observe every query.
- Delete entity and verify reference cleanup.
- Load detail and list in either order.
- Complete mutation with no active UI collector.

## 23.3 Instrumentation tests

Use `ActivityScenario` or `FragmentScenario` where compatible.

Required scenarios:

- Holder recycling during mutation.
- Rotation during mutation.
- Navigation away during mutation.
- Return after mutation completion.
- Deep-link detail loading.
- Bottom-sheet dismissal during save.
- RecyclerView scroll and rebind after update.

## 23.4 Test naming

Test names should describe:

```text
given condition
when action
then expected state
```

Example:

```kotlin
@Test
fun `given two loaded feed pages when page one item is liked then canonical query emits updated item`() 
```

## 23.5 No timing-dependent tests

Tests must not depend on:

- `delay()`.
- Real network timing.
- Thread sleeps.
- Arbitrary retries.
- Current wall-clock time without an injected clock.

---

## 24. Pull request and issue execution protocol

The repository requires significant changes to be discussed before implementation and recommends individual pull requests for individual suggestions or features.

## 24.1 Parent tracking issue

Create one parent architecture issue containing:

- Problem statement.
- Link to this specification.
- Phase list.
- Dependency graph.
- Rollout strategy.
- Global definition of done.
- Links to every child issue.

## 24.2 Child issues

Create one child issue per phase or independently reviewable vertical slice.

Do not create one PR containing the entire migration.

Suggested issue breakdown:

1. Document architecture and baseline regressions.
2. Contain mutable media-list and RecyclerView defects.
3. Make feed ViewModels own accumulated page state.
4. Introduce canonical feed and media-list stores.
5. Introduce mutation executor and registry.
6. Migrate feed and comment vertical slice.
7. Migrate media-list vertical slice.
8. Remove repository mutation broadcasting.
9. Migrate remaining mutation domains.
10. Evaluate ObjectBox persistence.
11. Add architecture enforcement and final cleanup.

## 24.3 Branch and PR rules

Each implementation branch must:

- Start from current `develop`.
- Address one issue.
- Avoid unrelated formatting or dependency changes.
- Include tests in the same PR.
- Update documentation when a contract changes.
- Preserve `app` and `github` flavour behaviour.
- Preserve generated GraphQL request conventions.
- Include before-and-after architecture notes.

## 24.4 PR description requirements

Every PR must state:

- Specification phase.
- Issue link.
- Problem addressed.
- Architectural invariant introduced.
- Files and components changed.
- Compatibility path retained.
- Compatibility path removed.
- Tests added.
- Verification commands executed.
- Known limitations.
- Follow-up dependencies.
- Rollback procedure.

## 24.5 Agent handoff requirements

An autonomous agent beginning any phase must first read:

1. `AGENTS.md`
2. `CONTRIBUTING.md`
3. This specification
4. Existing ViewModel-first architecture specification
5. `@DESIGN.md` when UI rendering changes
6. The parent issue
7. The phase issue
8. All directly related tests

The agent must not reinterpret the architecture through implementation convenience.

Any deviation requires:

- Explicit written rationale.
- Impact analysis.
- Updated tests.
- Updated specification or issue acceptance criteria.
- Maintainer approval before merging.

---

## 25. Rollout strategy

## 25.1 Vertical slices

Migrate one complete domain path at a time.

A complete vertical slice includes:

- Read path.
- Mutation path.
- Store.
- ViewModel.
- UI rendering.
- Navigation.
- Tests.
- Removal of the old path.

Do not migrate only repository emitters without migrating consumers.

## 25.2 Compatibility adapters

Temporary adapters may translate store state into legacy UI models.

They must:

- Be clearly marked transitional.
- Have a linked removal phase.
- Not allow writes back into the store.
- Not recreate SharedFlow mutation broadcasts.

## 25.3 Feature flags

A feature flag may be used only when:

- Both implementations can safely coexist.
- State is not duplicated independently.
- Rollback requires disabling the new renderer.
- The canonical store remains authoritative.

Do not run two mutation pipelines against the same resource.

## 25.4 Rollback

Each phase must remain revertible at PR granularity.

Rollback must not require database downgrades until the persistence phase.

---

## 26. Risks and mitigations

## 26.1 Partial migration creates dual state

**Risk:** Old mutation events and new stores both update UI.

**Mitigation:** Define one authoritative path per migrated vertical slice and remove the old consumer before phase exit.

## 26.2 Mutable legacy models leak through mappers

**Risk:** Store records share mutable lists or nested objects with Retrofit entities.

**Mitigation:** Mapper tests must mutate the source entity after mapping and verify the domain record remains unchanged.

## 26.3 In-memory stores grow without bounds

**Risk:** Long sessions accumulate entities and query snapshots.

**Mitigation:** Add explicit query invalidation and retention policies after functional correctness. Do not optimise prematurely by compromising ownership.

## 26.4 Competing mutations produce stale commits

**Risk:** Network responses complete out of order.

**Mitigation:** Per-resource serialisation plus revision rejection.

## 26.5 Process death loses in-memory state

**Risk:** The initial store does not survive process death.

**Mitigation:** Document process-lifetime semantics and refetch after recreation. Add persistence only after the architecture is proven.

## 26.6 Full entity mapping is large

**Risk:** Immutable domain records require broad mapper work.

**Mitigation:** Map one vertical slice at a time. Include only rendering and mutation-relevant fields, then expand explicitly when needed.

## 26.7 ViewModel becomes oversized

**Risk:** Moving orchestration out of views creates large ViewModels.

**Mitigation:** Put reusable business sequencing into interactors and mapping into dedicated mappers. ViewModels remain screen state holders.

## 26.8 Generic abstractions hide domain behaviour

**Risk:** A universal store or mutation abstraction becomes difficult to reason about.

**Mitigation:** Keep stores domain-specific. Share only proven infrastructure such as keyed locking and operation tracking.

---

## 27. Global definition of done

The architecture migration is complete only when all of the following are true:

1. Feed, reply, like, and media-list committed state has canonical owners.
2. ViewModels expose complete immutable screen state.
3. RecyclerView adapters render immutable snapshots.
4. Custom views emit actions and render state only.
5. Application-scoped mutations commit into stores.
6. No business-state result depends on a lifecycle-bound callback.
7. No complete entity is returned through activity results for synchronisation.
8. No migrated screen manually patches another screen's state.
9. No repository `SharedFlow` broadcasts committed business state.
10. Per-resource mutation serialisation is implemented.
11. Stale responses cannot overwrite newer state.
12. Failed non-optimistic writes leave committed state unchanged.
13. Pagination and mutations operate on the same canonical entities.
14. Rotation, navigation, recycling, and stopped collectors do not lose committed mutations.
15. Old compatibility paths are deleted.
16. Documentation matches production code.
17. Architecture regression tests are present.
18. Full repository verification passes.

---

## 28. Explicitly prohibited implementation patterns

The following approaches do not satisfy this specification:

```kotlin
MutableSharedFlow(replay = 1)
```

Used as a replacement for a canonical store.

```kotlin
repository.mutationEvents.collect {
    adapter.notifyDataSetChanged()
}
```

Used to refresh mutable adapter state.

```kotlin
activityResultLauncher.launch(
    Intent().putExtra("updated_entity", entity)
)
```

Used to synchronise application state.

```kotlin
viewHolderScope.launch {
    repository.save(...)
}
```

Used to execute business mutations from a holder.

```kotlin
koinOf<Repository>()
```

Called from an adapter or custom view.

```kotlin
model.progress++
repository.save(model)
```

Used without an immutable draft and explicit optimistic rollback protocol.

```kotlin
repository.save(...).onSuccess {
    view.setModel(it)
}
```

Used as the only committed-state application mechanism.

```kotlin
refreshAllScreens()
```

Used as the standard response to one entity mutation.

```kotlin
GlobalScope.launch
```

Used anywhere.

```kotlin
CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

Created ad hoc in adapters, widgets, Fragments, or interactors without explicit ownership and cancellation.

---

## 29. Architectural decisions summary

| Decision | Outcome |
|---|---|
| Global mutation events | Rejected for business state |
| Canonical state | Domain-specific stores |
| Initial persistence | In-memory |
| Long-term persistence | Evaluate ObjectBox |
| UI architecture | ViewModel-driven UDF |
| Adapter model | Immutable list snapshots |
| Widget role | Render state and emit actions |
| Mutation completion | Commit into canonical store |
| Mutation ordering | Per-resource serialisation |
| Initial optimistic updates | Prohibited |
| Navigation state transfer | Stable IDs only |
| Paging 3 | Deferred until state ownership is fixed |
| Compose migration | Out of scope |
| New Gradle modules | Out of scope |
| Generic MVI framework | Not required |
| Repository `SharedFlow` | Removed for committed business state |

---

## 30. External standards basis

This specification is consistent with the following current platform guidance:

- Android recommends a clearly defined data layer, repositories exposing application data, ViewModels exposing UI state, and unidirectional data flow.
- A source of truth should exclusively own mutation and expose immutable data.
- `SharedFlow` replay and buffering semantics do not make a zero-replay stream a durable state mechanism.
- Lifecycle-aware collection starts and stops with the UI lifecycle, so collectors must observe reproducible state rather than depend on receiving every transient business event.
- ViewModel-originated critical UI behaviour should be represented through UI state so it is not lost across lifecycle transitions.
- RecyclerView diffing requires immutable lists and relevant item properties.
- A future offline-first implementation should use a local canonical source as the exclusive higher-layer read source.

The next step is to convert this specification into one parent architecture issue and narrowly scoped phase issues, preserving the phase gates and prohibited shortcuts verbatim.
