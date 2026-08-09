# Persistence Evaluation, Phase 8

## Scope and current phase decision

Phase 8 is an evaluation-first phase. Section 20 of `docs/architecture/state-synchronization-and-mutation-refactor.md` states that full offline-first support is not required to complete the primary refactor. The current in-memory canonical store architecture is already proven across the earlier phases.

This document records the persistence decision per domain, the current ObjectBox fit, expiry expectations, and the account-clearing rules required before any ObjectBox-backed canonical store is introduced.

## Current store state by domain

### Feed store

`FeedStoreState` currently holds:

- `feedsById`, canonical `FeedRecord` items.
- `repliesById`, canonical `FeedReplyRecord` items.
- `replyIdsByFeedId`, reply membership per feed.
- `queries`, per-query snapshots with ordered ids, page info, loaded pages, generation, and `lastUpdatedAtMillis`.

`FeedRecord` and `FeedReplyRecord` both carry nested summary data and like lists. Query snapshots are paged, high-churn, and tied to timeline ordering.

### Media list store

`MediaListStoreState` currently holds:

- `entriesById`, canonical `MediaListRecord` items.
- `entryIdByMediaId`, the lookup from media id to entry id.
- `queries`, per-query snapshots with ordered ids, page info, loaded pages, and `lastUpdatedAtMillis`.

`MediaListRecord` contains user-owned progress and status data, plus user-specific fields such as notes, private state, custom lists, advanced scores, and owner identity.

### Review store

`ReviewStoreState` currently holds:

- `reviewsById`, wrapped `Review` objects plus revision metadata.
- `queries`, per-query snapshots with ordered ids, page info, loaded pages, generation, and `lastUpdatedAtMillis`.

The store uses `Review`, which is currently a mutable parcelable model, not an ObjectBox entity.

## Process-death survival evaluation

### Feed

Decision: process-lifetime only.

Rationale:

- Feed state is timeline data with high churn and lower recovery value than user-owned media-list state.
- The store keeps ordered page snapshots and nested like data that are most valuable only during the active process lifetime.
- Section 20 allows non-persistent domains to explicitly remain process-lifetime only.

Required survival level:

- No process-death restoration is required in this phase.
- On process restart, feed screens should repopulate from network as they do today.

### Media list

Decision: ObjectBox-backed is justified, but deferred from this changeset.

Rationale:

- Media-list state is user-critical and user-owned.
- The store already has stable canonical ownership, immutable records, deterministic query keys, and revision-based mutation handling.
- This is the domain with the highest value for offline read and process-death recovery.

Required survival level:

- Media-list entries and query snapshots should survive process death once a persistent store is introduced.
- ViewModels must continue depending on `MediaListStore`, not on a persistence-specific implementation.

Implementation status in this phase:

- Evaluated and documented.
- Not implemented in ObjectBox yet.
- Follow-up work would still need startup hydration and network reconciliation before switching the Koin binding.

### Review

Decision: process-lifetime only for now.

Rationale:

- Reviews are moderately valuable for reread, but they are less user-critical than media-list progress and status state.
- The current store wraps mutable `Review` models rather than a purpose-built immutable review record.
- Introducing review persistence now would add ObjectBox mapping work without the same value density as media-list recovery.

Required survival level:

- No process-death restoration is required in this phase.
- Re-fetch from network remains acceptable after restart.

## ObjectBox entity audit

### Current ObjectBox usage

The current entity audit under `app/src/main/java/com/mxt/anitrend/` found these ObjectBox entities:

- `AuthBase`
- `Genre`
- `MediaTag`
- `NotificationHistory`
- `User`
- `UserBase`
- `VersionBase`
- `WebToken`

`DatabaseHelper` shows the current ObjectBox usage pattern:

- A single `BoxStore` is built from `MyObjectBox.builder().androidContext(...).build()` in `Modules.kt`.
- `DatabaseHelper` exposes box access through `getBoxStore()`.
- Current persisted state is mostly session/authentication, notification history, and metadata.
- `invalidateBoxStores()` clears `WebToken`, `AuthBase`, `User`, `UserBase`, `VersionBase`, and `NotificationHistory` on logout-like flows.

### Reuse and extension assessment

What can be reused:

- The existing `BoxStore` lifecycle and `DatabaseHelper` access pattern.
- Existing ObjectBox converter usage patterns, for example `User` and `UserBase` already persist nested or converted fields.

What does not already exist:

- No existing ObjectBox entity for feed state.
- No existing ObjectBox entity for media-list canonical records.
- No existing ObjectBox entity for reviews.
- No existing persisted query-snapshot model for canonical store queries.

### Mapping evaluation by record type

#### FeedRecord and FeedReplyRecord

Mapping fit: poor for current-phase persistence.

Reasons:

- `FeedRecord` includes nested `UserSummaryRecord`, optional `MediaSummaryRecord`, and a list of liked users.
- `FeedReplyRecord` includes nested user data and like lists.
- `FeedStoreState` also relies on `replyIdsByFeedId` and query snapshots with ordered pagination state.
- None of these shapes match an existing ObjectBox entity in the repo.

This data can be persisted, but not cleanly with direct reuse of current entities. It would require new flattened entities or converter-backed persistence models plus explicit snapshot storage.

#### MediaListRecord

Mapping fit: acceptable, with new canonical persistence models.

Reasons:

- `MediaListRecord` is a stable user-owned record keyed by entry id and media id.
- The record includes nested and collection fields such as `customLists`, `advancedScores`, optional `MediaSummaryRecord`, and optional fuzzy dates.
- Existing ObjectBox usage in the repo already shows converter-based persistence for complex fields.
- The store query model is simpler than feed because it does not also manage reply graphs.

This does not map cleanly to any current ObjectBox entity, but it is the best candidate for a new ObjectBox-backed canonical store.

#### Review

Mapping fit: weak for now.

Reasons:

- The store currently keeps the mutable `Review` model directly.
- `Review` is not an ObjectBox entity.
- Persisting `ReviewStoreState` would require either introducing a new review persistence model or converting the current model to a persistence-safe canonical record.

## Persistence decision per domain

### Feed

- Decision: process-lifetime only.
- Persistence recommendation: do not add ObjectBox backing in the current refactor.
- Reason: low persistence value relative to complexity and volume.

### Media list

- Decision: ObjectBox-backed in a follow-up implementation.
- Persistence recommendation: this is the first domain that should gain disk-backed canonical state.
- Reason: highest user value and best fit for offline read.
- This deferral is explicitly permitted by Section 20.3 of `docs/architecture/state-synchronization-and-mutation-refactor.md` while offline-first persistence remains a follow-up concern.

### Review

- Decision: process-lifetime only for now.
- Persistence recommendation: defer until there is a stronger product requirement or a review-domain immutable persistence model.
- Reason: moderate value, weaker mapping fit than media-list.

## Data expiry policy

### Feed

- Active implementation: process-lifetime only, so no disk expiry policy applies today.
- If a future disk cache is ever added, expire snapshots after 24 hours and refresh from network on next read.

### Media list

- Proposed persistent expiry: 7 days from `lastUpdatedAtMillis` for query snapshots.
- Explicit refresh or successful mutation should replace the relevant canonical query state immediately.
- Entry records should remain available for offline read until replaced, but stale queries older than 7 days should trigger a network refresh when connectivity exists.

### Review

- Active implementation: process-lifetime only, so no disk expiry policy applies today.
- If later persisted, expire query snapshots after 72 hours and refresh on next connected read.

## Account isolation and logout clearing

### Current account handling

`DatabaseHelper` currently persists one active account's auth and user state in ObjectBox and clears those boxes through `invalidateBoxStores()`.

Current persisted logout clearing already removes:

- `WebToken`
- `AuthBase`
- `User`
- `UserBase`
- `VersionBase`
- `NotificationHistory`

### Required account isolation rules for canonical stores

- Any future persistent user-specific canonical data must be keyed by authenticated account identity.
- For media-list persistence, `ownerUserId` must be treated as mandatory persisted partitioning data.
- Feed data containing private or authenticated-only timelines must never be shared across accounts.
- Review persistence, if introduced later, should either be account-keyed or fully cleared on logout because user ratings and private review visibility are account-sensitive.

### Logout clearing rule

Logout and account switch must clear:

- Media lists.
- Private feed data.
- Messages.
- Notifications.
- Pending mutations.

Implementation status in this phase:

- In-memory feed, media-list, and review stores now expose `clear()`.
- Logout now calls a dedicated `AccountStoreClearer`, which clears the three in-memory stores and the in-memory `MutationRegistry`.
- Notification history and auth/session boxes continue to clear through `DatabaseHelper.invalidateBoxStores()`.

Message persistence does not currently have a dedicated canonical store in this phase, so there is no extra message-store code to clear yet.

## Schema migration strategy

There is no new ObjectBox schema in this changeset.

When media-list persistence is introduced, the migration strategy should be:

1. Add new dedicated persistence entities rather than persisting the current store state maps directly.
2. Keep the public `MediaListStore` contract unchanged.
3. Hydrate the canonical store from ObjectBox on startup before the first UI observation.
4. Reconcile persisted records with fresh network responses by applying normal store changes, not by mutating ViewModels.
5. Commit generated ObjectBox model changes with the entity introduction.
6. Treat schema evolution as additive where possible, with explicit migration review whenever field identity changes.

Because no new ObjectBox-backed canonical store is introduced here, there is no active schema migration to execute yet.

## Paging 3 evaluation

Decision: defer Paging 3 for canonical stores, with one explicit network-only pilot exception.

Status:

- Media recommendations (Phase 1 pilot): the screen runs on a genuine AndroidX Paging 3 network-only vertical slice. Paging owns network page orchestration (`PagingSource` over `MediaRepository`, `Pager` + cached `Flow<PagingData>` in the ViewModel, load states and a load-state footer in the fragment). There is no local cache, no RemoteMediator, and no shared mutable page state. This pilot is explicitly allowed as a single-screen exception and does not change the store-level decision below.
- Canonical-store-backed paging (feed, media list, review): still deferred. Paging 3 would add the most value after there is a real local query layer to page from, especially for disk-backed media-list reads.
- Feed and review are intentionally remaining process-lifetime only for now, so Paging 3 does not unlock enough immediate value there.
- Media-list is the only justified persistence candidate, and that persistence layer is still evaluation-only in this phase.

The pilot does not weaken the canonical-store requirements: the in-memory stores remain the exclusive mutable owners of committed entity state in their domains, and no store domain (feed, media list, review) pages through Paging 3 yet. Store-driven manual query merging stays in place for those domains.

Recommendation:

- Keep the current store-driven manual query merging for feed, media list, and review.
- Re-evaluate Paging 3 for canonical stores after an ObjectBox-backed media-list store and local query hydration exist; that work would use Paging 3 with a `RemoteMediator`, which the network-only pilot deliberately does not introduce.
- The media-recommendations pilot is the reference for the eventual network-source pattern (source over the repository, cached flow in the ViewModel, load states in the fragment) but not a template for store-backed paging.

## Phase 8 outcome

- Feed remains process-lifetime only.
- Media-list is the only domain justified for future ObjectBox backing.
- Review remains process-lifetime only for now.
- Logout/account-switch clearing is now explicit for the in-memory canonical stores and pending mutation registry.
- Paging 3 for canonical stores is deferred until local persistent query support exists. The media-recommendations screen is the single allowed network-only Paging 3 pilot (no local cache, no RemoteMediator); it is not a precedent for store-backed paging.
