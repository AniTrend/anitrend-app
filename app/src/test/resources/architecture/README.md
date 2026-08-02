# Kotlin-First Domain / Navigation Architecture Baselines

Companion baseline resources for
`KotlinFirstArchitectureEnforcementTest` (ADR
`docs/adr/2026-08-01-kotlin-first-domain-model-and-navigation.md`, section 9, Phase 1).

These files are **allowlists, not blanket ignore rules**. Every entry is a
reviewed exception that exists in the current legacy code. Each entry must be
removed **in the same PR that migrates the underlying violation**. Do not add
new entries to make new code pass.

## Format and matching semantics

All baselines are plain text, one entry per line, sorted lexicographically
(`LC_ALL=C sort`). Lines that are blank are ignored. There are no comments in
the data files; explanations live in this README and in the test failure
messages, which quote ADR section 9.

| File | Entry format | Match key | New violation detection |
|---|---|---|---|
| `model-entity-baseline.txt` | `app/src/main/java/.../*.kt` (relative path) | relative file path | any current file under `model.entity` not listed |
| `parcelable-baseline.txt` | `path:normalized source line` | `path` + trimmed line content | any current source line containing `writeToParcel`, `describeContents`, `Parcelable.Creator`, or `CREATOR` not listed for that path |
| `domain-graphql-baseline.txt` | `path:import com.mxt.anitrend.graphql.generated.X` | exact `path` + import line | any GraphQL import in `domain` not listed |
| `mutation-methods-baseline.txt` | `path:fun <name>(` (normalized declaration) | `path` + `fun <name>(` | any new member function declaration matching the ADR mutation names |

Removals always pass: if an entry disappears from source the baseline entry is
simply stale and must be deleted in the migration PR that removed it.

## Why each exception exists

### `model-entity-baseline.txt` (95 entries)

`com.mxt.anitrend.model.entity.**` is the legacy compatibility package that the
ADR deletes after all consumers migrate (ADR sections 3 and 9, Phase 6). The 95
current Kotlin files are frozen at their pre-migration state. Phase 1 forbids
adding classes here so the legacy surface can only shrink, never grow.

### `parcelable-baseline.txt` (76 entries)

ADR section 9 Phase 1 and section 7.6 prohibit new handwritten Parcelable code.
The current violations are the two documented scoped exceptions (ADR section 9,
Phase 1):

- Handwritten `Parcelable` implementations inside `model/entity/**` (legacy
  entity classes awaiting migration, e.g. `Media`, `User`, `MediaList`).
- `MaterialSearchView.SavedState` (lines 629-638 of
  `base/custom/view/search/MaterialSearchView.kt`), a `Parcelable.Creator`
  owned by a custom view for view-state serialization, not a domain or
  navigation model.

New Parcelable code, including new lines added to these legacy files, fails.

### `domain-graphql-baseline.txt` (10 entries)

ADR section 4.1 and section 9 Phase 1 forbid `android.*`, `androidx.*`,
`io.objectbox.*`, `com.google.gson.*`, and `kotlinx.parcelize.*` imports in
`domain`, and forbid exposing generated GraphQL types through domain APIs. The
only GraphQL imports allowed are the documented existing enum/input exceptions
from ADR section 9 Phase 1:

`MediaListStatus`, `MediaType`, `FuzzyDateInput`, `ScoreFormat`, `LikeableType`,
`ReviewRating`, `ReviewSort`, `ActivityType`, `MediaListSort`.

Only the ones actually present in `domain` today are listed, path-precise. These
are transport and command inputs at the existing data boundary, not domain
models. This is not a blanket domain-package exemption: any other GraphQL import
or any Android/ObjectBox/Gson import in `domain` fails.

### `mutation-methods-baseline.txt` (5 entries)

ADR section 4.4 prohibits model mutation methods. The ADR method names detected
are `toggle`, `mergeFrom`/`merge`, `increment`, `markDeleted`,
`copyForEditing`, `delete`, and `save`, only where they are member function
declarations of model classes (repository and ViewModel owners are out of
scope). The baseline lists the pre-existing legacy entity violations:
`UserBase.toggleFollow()` and the four `toggleFavourite()` implementations
(`MediaBase`, `CharacterBase`, `StaffBase`, `StudioBase`). New member mutation
methods anywhere in `model/entity/**` or `domain/**/model/**` fail.
