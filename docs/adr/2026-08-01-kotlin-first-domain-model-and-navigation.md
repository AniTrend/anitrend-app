# Kotlin-First Domain Model and Navigation Parameter Refactor

## Status

Proposed

## Scope

Progressively replace production use of:

```text
com.mxt.anitrend.model.entity.**
```

with explicitly classified Kotlin-first models, while:

- Preserving the existing single-module `:app` structure.
- Keeping Android, ObjectBox, and transport concerns outside the domain layer.
- Removing mutable business behaviour from model classes.
- Replacing handwritten `Parcelable` implementations.
- Introducing typed screen parameter objects instead of parcelizing arbitrary application entities.
- Remaining compatible with the existing state synchronisation and mutation architecture refactor.

---

## 1. Architectural decision

The migration shall not produce a one-to-one replacement domain class for every existing entity.

Every class under `com.mxt.anitrend.model.entity.**` must first be assigned one of the following roles:

1. Domain model.
2. Persistence entity.
3. Remote transport model.
4. Presentation or RecyclerView model.
5. Navigation parameter.
6. Framework or container type.
7. Obsolete compatibility type.

Only classes representing immutable application concepts with value semantics should become domain `data class`, sealed type, enum, or value-class candidates.

ObjectBox entities, GraphQL or REST response DTOs, RecyclerView wrappers, and Android navigation arguments must not be relabelled as domain models.

The target dependency direction is:

```text
Remote model ───┐
                ├──> Data mapper ───> Domain model
Local entity ───┘                         |
                                          v
                                     Interactor
                                          |
                                          v
                                   ViewModel state
                                          |
                                          v
                                  Presentation model
```

Navigation is a separate boundary:

```text
UI model or domain identity
          |
          v
Dedicated ScreenParam
          |
          v
Bundle / Intent
          |
          v
Destination resolves current domain state by ID
```

This aligns with the AniTrend architectural convention that the domain layer is pure Kotlin, while database entities, remote models, and mappers belong to the data layer. 

---

## 2. Current architectural problems

### 2.1 Mixed model responsibilities

The existing entity package contains AniList entities, metadata, edges, statistics, Giphy responses, Crunchyroll responses, ObjectBox entities, generic containers, log models, and RecyclerView base items. It therefore cannot be migrated safely as one homogeneous package.   

### 2.2 Mutable business state exists inside entities

`UserBase` exposes mutable `isFollowing` state and mutates it through `toggleFollow()`. It also combines ObjectBox annotations, conversion logic, Android parcelization, identity rules, and application state in one class. 

The same pattern appears in favourite-capable models. `MediaBase` and `StaffBase` both contain mutable `isFavourite` properties and `toggleFavourite()` functions.  

These methods make the model itself an uncontrolled mutation authority. They must be removed.

### 2.3 Handwritten parcelization is widespread and fragile

Classes such as `User` and `MediaList` manually coordinate parcel constructors, `writeToParcel()`, superclass state, field ordering, nullable values, and `CREATOR` declarations.  

This causes several problems:

- Fields can be omitted when a model evolves.
- Parcel read and write order can diverge.
- Superclass and subclass state must be coordinated manually.
- Class-body properties make conversion to `@Parcelize` non-mechanical.
- Parcelability leaks into otherwise reusable application models.
- Large object graphs are passed between screens unnecessarily.

### 2.4 Equality semantics are inconsistent

Several legacy classes override equality using only database or remote identity. Converting these directly into data classes would change equality to compare all primary-constructor properties.

That affects:

- RecyclerView diffing.
- Sets and maps.
- Tests.
- Mutation replacement.
- ObjectBox interactions.
- Any logic expecting two differently populated instances with the same ID to be equal.

Identity and content equality must therefore be designed explicitly during each migration.

### 2.5 `MediaList.clone()` is not a copy

`MediaList.clone()` returns `this`, meaning callers receive the same mutable instance rather than an isolated copy. This directly undermines mutation safety and must not be carried forward. 

---

## 3. Target package structure

The project can remain in `:app`, but package boundaries must become explicit.

```text
com.mxt.anitrend
├── domain
│   ├── user
│   │   ├── model
│   │   └── interactor
│   ├── media
│   │   ├── model
│   │   └── interactor
│   ├── staff
│   │   ├── model
│   │   └── interactor
│   └── ...
├── data
│   ├── local
│   │   └── entity
│   ├── remote
│   │   └── model
│   └── mapper
├── presentation
│   └── <feature>
│       └── model
└── navigation
    ├── extension
    └── model
```

The old `model.entity` package remains temporarily as a compatibility boundary and is deleted after all consumers have migrated.

---

## 4. Domain model standard

A domain model must satisfy all of the following rules.

### 4.1 Pure Kotlin

Domain models must not import:

```text
android.*
androidx.*
io.objectbox.*
com.google.gson.*
kotlinx.parcelize.*
```

Generated GraphQL model types must also not be exposed through domain APIs.

### 4.2 Immutable state

Domain model properties must use `val`.

```kotlin
data class UserRecord(
    val id: Long,
    val name: String?,
    val bannerImage: String?,
    val avatar: ImageRecord?,
    val isFollowing: Boolean,
)
```

Lists and maps must be exposed as immutable snapshots. Callers must never receive mutable collections owned by repositories or stores.

### 4.3 Constructor-defined state

Semantically relevant state belongs in the primary constructor. Avoid mutable class-body properties and partially initialized objects.

Defaults are allowed only when they represent a valid domain default rather than compensating for a mapper that does not know the value.

### 4.4 No mutation methods

The following patterns are prohibited:

```kotlin
fun toggleFollow()
fun toggleFavourite()
fun incrementProgress()
fun mergeFrom(other: Model)
fun markDeleted()
```

A model may expose pure derived information:

```kotlin
val displayName: String
    get() = name?.takeIf(String::isNotBlank) ?: "Unknown"
```

Pure calculations may also be extensions or dedicated value objects.

Not every function must be moved into an interactor. The correct boundary is:

- Mutation, orchestration, validation involving external state, and repository work belong in an interactor.
- Pure deterministic calculations over an immutable value may remain near that value.

Moving every trivial derived property into a use case would create unnecessary indirection.

### 4.5 Explicit identity

Every list-rendered model must have a stable logical identifier.

RecyclerView diffing must distinguish:

```kotlin
areItemsTheSame = old.id == new.id
areContentsTheSame = old == new
```

Do not depend on object hash codes or redefine data-class equality to represent entity identity.

### 4.6 No presentation inheritance

Domain models must not inherit from:

```text
RecyclerItem
RecyclerHeaderItem
Parcelable
Serializable
```

Presentation-specific wrappers may adapt domain models where a heterogeneous adapter requires a common type.

---

## 5. Data model standard

Not every legacy entity should disappear. Some should be moved and narrowed.

### 5.1 Local persistence entities

ObjectBox-compatible classes should live under:

```text
com.mxt.anitrend.data.local.entity
```

They may remain mutable where required by ObjectBox.

They must not:

- Implement `Parcelable`.
- Extend RecyclerView classes.
- Contain UI behaviour.
- Be emitted directly by repositories.
- Be mutated by fragments or adapters.
- Represent canonical state outside the data layer.

Example:

```kotlin
@Entity
internal class UserEntity {
    @Id(assignable = true)
    var id: Long = 0

    var name: String? = null
    var isFollowing: Boolean = false
}
```

### 5.2 Remote models

Giphy, Crunchyroll, AniList transport containers, and generated response representations should live under:

```text
com.mxt.anitrend.data.remote.model
```

Serialization annotations are allowed here.

A remote model should be mapped before leaving the data layer unless the response is genuinely a data-source implementation detail never observed outside that layer.

### 5.3 Mappers

Mappings must be explicit and testable:

```kotlin
internal fun UserEntity.toDomain(): UserRecord

internal fun UserRemoteModel.toDomain(): UserRecord

internal fun UserRecord.toEntity(): UserEntity
```

Bidirectional mapping should only be provided where both directions are actually required. Do not create domain-to-remote mappings for read-only response types.

---

## 6. Business mutation standard

Model mutation shall be replaced by immutable state transitions coordinated through interactors and canonical stores.

For following a user:

```kotlin
data class ToggleUserFollowCommand(
    val userId: Long,
)

class ToggleUserFollowInteractor(
    private val repository: UserRepository,
    private val userStore: UserStore,
) {
    suspend operator fun invoke(command: ToggleUserFollowCommand) {
        val updatedUser = repository.toggleFollow(command.userId)
        userStore.commit(updatedUser)
    }
}
```

The model itself does not toggle its value.

For a local immutable reduction:

```kotlin
fun UserRecord.withFollowState(isFollowing: Boolean): UserRecord =
    copy(isFollowing = isFollowing)
```

That reducer may live in the store or interactor implementation. It must not be called by a view, adapter, or widget.

The existing state synchronisation specification already establishes canonical stores, immutable records, interactors, and repositories as the mutation path. This refactor must extend that architecture rather than introduce another model-mutation mechanism. 

---

## 7. Parcelable and navigation standard

### 7.1 Domain models are not Parcelable by default

`Parcelable` is an Android transport concern. Adding it to all domain models would contradict the pure-Kotlin goal.

A domain model may only implement `Parcelable` under an explicitly documented exception. No exception should be granted merely because the existing screen passes that entity.

### 7.2 Introduce a dedicated screen parameter contract

Take the typed parameter approach from AniTrend v2, where an `IParam` is parcelable and extension functions convert the complete parameter object to and from a `Bundle`.  

For `anitrend-app`, introduce:

```kotlin
package com.mxt.anitrend.navigation.model

import android.os.Parcelable

interface ScreenParam : Parcelable
```

Then define one parameter object per destination or closely related destination family:

```kotlin
@Parcelize
data class UserScreenParam(
    val userId: Long,
    val initialName: String? = null,
) : ScreenParam
```

```kotlin
@Parcelize
data class MediaScreenParam(
    val mediaId: Long,
    val mediaType: String? = null,
) : ScreenParam
```

```kotlin
@Parcelize
data class CommentScreenParam(
    val feedId: Long,
) : ScreenParam
```

The destination uses the ID to resolve current state from its ViewModel, store, or repository.

### 7.3 Typed bundle extensions

The existing app already has SDK-compatible `BundleCompat` parcelable readers.

Build the screen parameter API on top of that. The preferred strategy is an explicit, destination-owned, stable string key constant per parameter. Declare one constant per parameter type and use it as the bundle key in both write and read paths:

```kotlin
private const val ARG_MEDIA_SCREEN = "arg.media.screen"

inline fun <reified T : ScreenParam> T.asBundle(): Bundle =
    bundleOf(ARG_MEDIA_SCREEN to this)

inline fun <reified T : ScreenParam> Bundle.screenParam(): T? =
    BundleCompat.getParcelable(
        this,
        ARG_MEDIA_SCREEN,
        T::class.java,
    )
```

A per-param stable string key is preferred over a class-name-derived key because renaming or moving the parameter class would silently change a derived key, breaking saved-state restoration, deep-link handling, and any persisted intent extras. The key is a wire contract: it must be stable across refactors, versioned when it changes, and unique per destination family. An explicit constant makes the contract reviewable at the call site and keeps the bundle key decoupled from the class name.

A fully qualified class name helper is available for generic, non-destination-specific call sites:

```kotlin
inline fun <reified T : ScreenParam> screenParamKey(): String =
    T::class.java.name
```

Using the fully qualified class name is safer than `simpleName` because it avoids collisions between parameter classes with equal simple names, but it remains a secondary option because the FQCN is not a stable wire contract across refactors.

### 7.4 Pass minimal reconstruction state

Navigation parameters should normally contain:

- Stable IDs.
- Required destination mode.
- Optional display information used before data loads.
- Immutable filter or tab selections.
- Values required to reconstruct a draft that does not yet exist in canonical state.

They should not normally contain:

- Complete users.
- Complete media objects.
- Complete feed objects.
- Mutable lists.
- ObjectBox entities.
- Remote response objects.
- Data passed back solely to repair another screen’s state.

### 7.5 Permitted full snapshots

A complete parcelized object is acceptable only when all of these conditions are met:

1. The object is small.
2. It is immutable.
3. It is screen-specific rather than a canonical entity.
4. The destination cannot reconstruct it from a stable identity.
5. Its parcel format is covered by a round-trip test.
6. It is not used to synchronise committed state between screens.

### 7.6 Replace handwritten Parcelable implementations

Any retained navigation or presentation parameter should use `@Parcelize`.

Migration requires moving every parcelled property into the primary constructor or explicitly annotating unsupported properties. A handwritten implementation must not simply be deleted while state remains in the class body.

After migration, production model code must contain no handwritten:

```text
Parcel constructor
writeToParcel()
describeContents()
Parcelable.Creator
CREATOR
```

---

## 8. Migration classification matrix

Phase 0 must produce a checked-in inventory with at least these columns:

| Field | Meaning |
|---|---|
| Current class | Existing fully qualified name |
| Consumers | Packages and screens importing it |
| Source | Remote, local database, computed, or mixed |
| ObjectBox | Entity, embedded type, converter-backed, or none |
| Serializable | Gson, generated GraphQL, other, or none |
| Parcelable | None, `@Parcelize`, or handwritten |
| Navigation consumer | Bundle, Intent, Fragment argument, or result payload |
| RecyclerView dependency | Base item, stable ID, adapter type |
| Mutable fields | Mutable properties exposed to consumers |
| Model behaviour | Toggle, merge, increment, formatting, validation |
| Target role | Domain, local entity, remote DTO, UI model, screen param, obsolete |
| Target package | Final package |
| Mapper | Required mapping directions |
| Equality rule | Stable identity and content equality |
| Migration owner | Feature or aggregate |
| Exit condition | Imports and compatibility code to remove |

The inventory must be generated from source usage, not inferred only from class names.

---

## 9. Phased implementation

### Phase 0: Complete the model inventory

1. Enumerate every class under `model.entity`.
2. Find all constructors, imports, inheritance, serializers, ObjectBox usage, and navigation usage.
3. Identify handwritten parcel implementations.
4. Identify class-body mutations and model methods.
5. Classify every type using the target-role matrix.
6. Record existing equality and hash behaviour.
7. Identify recursive and cyclic object graphs.
8. Identify public APIs whose signatures expose legacy entities.

The existing mutation migration inventory covers 19 state-related components but is not a complete entity-classification inventory. It can be reused as evidence for feed, media-list, favourite, and follow consumers. 

Exit criteria:

- Every existing class has exactly one planned target role.
- No class is marked “domain” merely because it is consumed by the UI.
- Navigation consumers are known before parcelability is changed.

### Phase 1: Add architectural guardrails

Introduce:

- `ScreenParam`.
- Typed bundle and Intent extensions.
- Architecture tests.
- Mapper naming conventions.
- A temporary compatibility package allowlist.

Architecture baselines must be allowlists, not blanket ignore rules:

- Each baseline is a sorted, path-based allowlist of explicit fully qualified names or directory paths.
- Every entry requires review in the PR that introduces it, with a reason recorded next to the entry.
- A baseline entry must be removed in the same PR that migrates the underlying violation.
- Baselines are checked in with the architecture test configuration and are subject to the normal review process.

The following existing exposures are scoped exceptions and must be allowlisted as such, not treated as domain-model violations:

- Generated GraphQL enum and input types already consumed by domain commands, stores, interactors, and ViewModels (`MediaListStatus`, `MediaType`, `FuzzyDateInput`, `ScoreFormat`, `LikeableType`, `ReviewRating`, `ReviewSort`, `ActivityType`, `MediaListSort`). These are transport and command inputs at the existing data boundary, not domain models.
- `MaterialSearchView.SavedState`, a handwritten `Parcelable.Creator` owned by a custom view. It is view-state serialization, not a domain or navigation model.

These exceptions remain outside any “no generated GraphQL exposure” and “no handwritten Parcelable” rule until their owning code is migrated, and each exception is tracked as a named baseline entry with a migration owner.

Architecture tests should fail when:

- Domain imports Android or ObjectBox.
- A new class is added under `model.entity`.
- A domain class exposes a `var`.
- A domain class extends `Parcelable`.
- Presentation imports a local persistence entity.
- A new handwritten Parcelable implementation is introduced.
- A new mutation method is added to a model.

Exit criteria:

- New code cannot expand the legacy architecture.
- Existing violations are explicitly allowlisted rather than ignored globally.

### Phase 2: Complete existing canonical aggregates

Migrate the aggregates already represented by new domain records:

- Feed.
- Feed replies.
- Media-list entries.
- Shared user and media summaries.
- Fuzzy dates and paging metadata where already introduced.

Tasks:

1. Replace remaining legacy feed and media-list signatures with domain records.
2. Move all conversion into data mappers.
3. Replace adapter mutations with immutable list submission.
4. Remove activity result payloads used to return updated entities.
5. Replace full-entity navigation with ID-based screen parameters.
6. Delete obsolete mutation and compatibility methods.

Exit criteria:

- Feed and media-list UI code does not import legacy entities.
- A failed mutation cannot modify an adapter-held instance.
- Navigation does not carry canonical feed or media-list objects.

### Phase 3: User, follow, and favourite aggregates

This phase removes the highest-risk model behaviour:

- `UserBase.toggleFollow()`.
- `MediaBase.toggleFavourite()`.
- `CharacterBase.toggleFavourite()`.
- `StaffBase.toggleFavourite()`.
- `StudioBase.toggleFavourite()`.

Introduce immutable records and typed commands such as:

```text
UserRecord
CharacterRecord
StaffRecord
StudioRecord
MediaRecord

ToggleUserFollowCommand
ToggleFavouriteCommand
```

Favourite and follow results must commit through their owning store or canonical aggregate.

Exit criteria:

- No model exposes follow or favourite mutation methods.
- All screens observing an entity converge after a successful mutation.
- Failure leaves committed state unchanged.
- Model instances are never directly mutated by widgets or ViewModels.

### Phase 4: Core AniList detail models

Migrate bounded aggregates separately:

1. Media details and metadata.
2. Characters and character edges.
3. Staff and staff edges.
4. Studios.
5. Reviews.
6. Threads.
7. Notifications.
8. Recommendations.
9. User statistics.
10. Collections and connection metadata.

Avoid introducing one massive `MediaRecord` containing every possible screen field. Prefer aggregate-specific records:

```text
MediaSummaryRecord
MediaDetailRecord
MediaRelationRecord
MediaStatisticsRecord
MediaListEntryRecord
```

This prevents every screen from depending on an oversized recursive model graph.

Exit criteria per aggregate:

- Data sources map into domain records.
- UI and interactors use only domain or UI models.
- Navigation uses dedicated parameters.
- Legacy classes for the aggregate have no production consumers.

### Phase 5: External and infrastructure model cleanup

Classify and relocate:

- Giphy DTOs.
- Crunchyroll RSS and episode DTOs.
- Generic GraphQL containers.
- Paging and edge containers.
- Logging models.
- RecyclerView header and grouping models.

These should not automatically become domain types.

Expected destinations include:

```text
data.remote.giphy.model
data.remote.crunchyroll.model
data.remote.graphql.model
presentation.common.model
logging.model
```

Exit criteria:

- External API response formats are isolated from domain consumers.
- Generic containers do not leak into ViewModel state.
- RecyclerView inheritance is absent from domain types.

### Phase 6: Remove the compatibility layer

1. Remove all remaining production imports of `model.entity`.
2. Delete unused entity classes.
3. Move retained persistence and remote classes to their final packages.
4. Delete handwritten parcel implementations.
5. Delete mutation methods and mutable merge helpers.
6. Remove legacy equality and clone implementations.
7. Remove temporary architecture-test allowlists.
8. Delete the `com.mxt.anitrend.model.entity` root package.

Exit criteria:

```text
grep/import search for com.mxt.anitrend.model.entity returns no production consumers
```

Test fixtures may retain archived references only when validating migrations.

---

## 10. Pull request strategy

This migration must not be one pull request.

Use one bounded aggregate per PR:

```text
refactor(domain): introduce typed navigation parameters
refactor(feed): replace legacy feed entities with immutable records
refactor(media-list): remove mutable media-list entity consumers
refactor(user): move follow state mutation into interactor
refactor(media): replace favourite mutation with canonical state
refactor(staff): migrate staff models and navigation
```

Each PR must:

1. Compile independently.
2. Preserve current behaviour.
3. Remove the compatibility code made obsolete by that PR.
4. Avoid unrelated formatting or dependency upgrades.
5. Include mapper, state, navigation, and regression tests.
6. Update the migration inventory.

The repository contribution rules also require changes to be discussed first and individual suggestions or features to be submitted separately. 

---

## 11. Test requirements

### Domain tests

- Equality and copy behaviour.
- Derived property behaviour.
- Null and default handling.
- Collection immutability expectations.

### Mapper tests

- Remote to domain.
- Local entity to domain.
- Domain to local entity where required.
- Missing and nullable fields.
- Converter-backed ObjectBox fields.
- Enum or string compatibility.
- Numeric precision changes such as `Float` to `Double`.

### Interactor tests

- Successful mutation commits the returned state.
- Failed mutation leaves state unchanged.
- Two mutations for the same resource are serialized.
- A stale response cannot replace a newer committed value.
- Follow and favourite updates propagate to every relevant query.

### Parcelable tests

Only screen parameter classes require parcel tests:

- Parcel round trip retains all fields.
- Null fields survive.
- Bundle extraction is type-safe.
- Missing arguments return a controlled error or null.
- Process recreation can reconstruct the destination from the parameter.

### Navigation tests

- Destination loads from stable identity.
- A stale preview does not override canonical state.
- Large entities are not stored in Fragment arguments.
- Results are not used to repair originating application state.

### Persistence tests

- Existing ObjectBox data can still be read.
- Entity IDs remain stable.
- Converter-backed fields retain compatibility.
- Any entity schema changes include the required ObjectBox model updates.

### Architecture tests

- Domain remains Android-free.
- Data entities do not leak into presentation.
- No new `model.entity` classes are added.
- No handwritten Parcelable implementations remain after the final phase.
- No mutable model-side toggle, merge, increment, or delete functions remain.

---

## 12. Definition of done

The refactor is complete when all of the following are true:

1. `com.mxt.anitrend.model.entity.**` has no production consumers and is removed.
2. Domain models are immutable Kotlin-first values.
3. Domain models contain no Android, ObjectBox, or serialization framework dependencies.
4. Persistence entities are confined to the data layer.
5. Remote models are confined to the data layer.
6. RecyclerView-specific models are confined to presentation.
7. No domain model implements `Parcelable`.
8. All retained Android parameter objects use `@Parcelize`.
9. No handwritten parcel constructor, `writeToParcel`, or `CREATOR` remains in migrated model code.
10. Screens receive typed `ScreenParam` objects instead of unrelated individual extras.
11. Screens primarily navigate using stable IDs rather than complete canonical entities.
12. No model mutates follow, favourite, progress, like, delete, or save state.
13. Mutation behaviour is owned by interactors and canonical stores.
14. RecyclerView state uses immutable list snapshots and stable identifiers.
15. Equality changes are explicitly tested for every migrated aggregate.
16. ObjectBox compatibility and all application flavours pass.
17. The model migration inventory contains no unresolved classifications.

---

## 13. Explicit non-goals

This refactor does not require:

- Moving the project to multiple Gradle modules.
- Migrating the UI to Compose.
- Migrating to Navigation 3.
- Replacing ObjectBox.
- Replacing Gson or the GraphQL client.
- Rewriting all repositories simultaneously.
- Making domain models parcelable for convenience.
- Preserving legacy inheritance hierarchies.
- Creating one universal entity model shared by every screen.

---

## 14. Required first implementation slice

The first implementation PR should contain only the navigation foundation and enforcement layer:

1. Add `ScreenParam`.
2. Add typed Bundle and Intent parameter extensions.
3. Add representative `UserScreenParam`, `MediaScreenParam`, and `CommentScreenParam`.
4. Migrate one low-risk destination from separate extras or a full entity.
5. Add parcel round-trip and destination reconstruction tests.
6. Add architecture rules preventing new handwritten parcelization and new legacy entity classes.
7. Add the complete model classification inventory.

The second implementation PR should migrate `UserBase` follow state because it demonstrates the complete target architecture:

```text
ObjectBox/remote user
        |
        v
User mapper
        |
        v
UserRecord
        |
        v
ToggleUserFollowInteractor
        |
        v
UserStore
        |
        v
ViewModel and immutable UI state
```

This proves model immutability, mapper separation, typed navigation, and business mutation ownership before applying the design to larger media graphs.

The first destination migration is `StudioActivity`. It must follow this exact sequence so the legacy deep-link flow keeps working while the new parameter contract is introduced:

1. Run `IntentBundleUtil(intent).checkIntentData(this)` first, before any argument parsing, so deep links such as `anilist.co/studio/{id}` still inject the legacy `KeyUtil.arg_id` extra into the intent.
2. Bridge the legacy wire key into the typed parameter: read the `KeyUtil.arg_id` extra and construct `StudioScreenParam(studioId = id)`. The bridge is a single conversion point inside the activity, not a parcel path for `StudioBase`.
3. Keep the legacy `KeyUtil.arg_id` deep-link wire key only as an interim boundary. `IntentBundleUtil.injectIntentParams()` continues to write it until the deep-link parsing is migrated to emit the typed parameter directly.
4. Only after the bridge is in place, remove the handwritten `StudioBase` Parcelable and `toggleFavourite()`, migrating those consumers to `StudioRecord` and `ToggleFavouriteCommand`.

The bridge exists solely to keep deep-link behaviour identical during the cutover; it must not become the long-term navigation API.
