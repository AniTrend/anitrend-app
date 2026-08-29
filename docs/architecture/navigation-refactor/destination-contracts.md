# Navigation destination contracts

**Status:** Navigation 2 route contracts observed in the working tree; coverage claims reconciled with the actual tests

This document records the argument contracts that Navigation 2 destinations
must preserve. It is intentionally separate from implementation details so
destination refactors do not silently change saved-state or external ingress
wire keys.

The root host uses the existing `ScreenParam` bundle contract for migrated
detail navigation. Legacy scalar extras are accepted only at ingress bridges
that still need to decode historical intents.

The wrap-up record for the branch is
[`full-wrap-up-audit.md`](full-wrap-up-audit.md); the media-list route-origin
contract (NFR-002) and the `/user/<name>/animelist|mangalist` policy (NFR-001)
are recorded there with their producer tables.

## Argument rules

1. Use a typed `ScreenParam` for destination identity.
2. Store it in a `Bundle` with `asBundle()` and read it with `screenParam<T>()`.
3. Keep compatibility reads for legacy scalar extras only until all callers are
   migrated and the inventory removal criterion is met.
4. Do not pass complete entities, mutable lists, repository results, or activity
   result payloads as navigation state.
5. A new destination-specific parameter requires an explicit stable key and a
   round-trip test before production callers are migrated.

## Existing stable keys

Observed in `app/src/main/java/com/mxt/anitrend/navigation/extension/ScreenParamExt.kt`:

| Parameter | Stable key | Current identity |
| --- | --- | --- |
| `UserScreenParam` | `arg.user.screen` | User id and optional initial name |
| `MediaScreenParam` | `arg.media.screen` | Media id and optional media type |
| `CommentScreenParam` | `arg.comment.screen` | Activity/feed identity |
| `StudioScreenParam` | `arg.studio.screen` | Studio id |
| `ReviewScreenParam` | `arg.review.screen` | Review identity and media identity |
| `CharacterScreenParam` | `arg.character.screen` | Character id |
| `StaffScreenParam` | `arg.staff.screen` | Staff id |
| `ImagePreviewScreenParam` | `arg.image.preview.screen` | Image URL or identity |
| `GiphyPreviewScreenParam` | `arg.giphy.preview.screen` | Giphy URL |
| `VideoPlayerScreenParam` | `arg.video.player.screen` | Video URL |
| `UserListScreenParam` | `arg.user.list.screen` | User list identity |
| `TrailerScreenParam` | `arg.trailer.screen` | Trailer id and site |
| `SettingsCategoryScreenParam` | `arg.settings.category.screen` | Settings category id |

The fallback fully qualified class name in `screenParamKey<T>()` is a
compatibility fallback, not permission to create anonymous route contracts.
Migrated destinations should use an explicit key from this table or add one
through a reviewed contract change.

## Graph ownership

These graph identifiers are registered under the root Navigation 2 host.
`nav_root.xml` contains 28 fragment destinations, the verified count used by
all navigation-refactor documents.

| Graph | Intended ownership | Initial destinations |
| --- | --- | --- |
| `nav_root.xml` | Root host, direct-child destinations, and common actions | All migrated application destinations |

The earlier split graph files were removed: Navigation 2 resolves destination
ids from the current graph, so Settings destinations are registered directly in
`nav_root.xml` (the former `nav_settings_graph.xml` is deleted). Keeping
migrated destinations direct children of the one primary graph preserves the
one-host contract without adding a router fragment or a second navigation
owner.

Destination ids are registered in the root host. Media and shared-content
routes use the same host while preserving their existing identity or ingress
argument contracts.

## External URI contracts

Current manifest filters that need equivalent root-host routes before their
activities can be removed:

| URI path | Current activity | Target destination |
| --- | --- | --- |
| `https://anilist.co/user...` | `MainActivity` | `ProfileFragment` |
| `https://anilist.co/user/<name>/animelist` and `/mangalist` | `MainActivity` | `ProfileFragment`, then a pushed typed `MediaListFragment` preserving Profile beneath the list (NFR-001) |
| `https://anilist.co/anime...` | `MainActivity` | `MediaFragment` |
| `https://anilist.co/manga...` | `MainActivity` | `MediaFragment` |
| `https://anilist.co/character...` | `MainActivity` | `CharacterFragment` |
| `https://anilist.co/staff...` | `MainActivity` | `StaffFragment` |
| `https://anilist.co/actor...` | `MainActivity` | `StaffFragment` |
| `https://anilist.co/studio...` | `MainActivity` ingress | Studio detail destination |
| `https://anilist.co/activity...` | `MainActivity` ingress | Comment destination |
| `ACTION_SEND` `text/plain` | `MainActivity` | `SharedContentFragment` |

Other exported ingress contracts are documented in the inventory. The
route-coverage claim below is compared against the actual tests (AC-DOC-02 in
the audit):

- `MainActivityExternalIngressTest` contains exactly two warm assertions
  (warm `/anime/1` lands on `mediaFragment`; warm `ACTION_SEND` lands on
  `sharedContentFragment`), both delivered through the `onNewIntent` seam
  because ActivityScenario cannot deliver a warm `singleTop` intent through
  the platform dispatch.
- The same file contains nine newer focused tests with cold-start assertions
  for `/user/<name>/animelist`, `/user/<name>/mangalist`, plain `/user`,
  numeric user ids, `/anime/1` and `/activity/1` back semantics, and the
  `ROUTE_MEDIA_LIST` entry. `SharedContentDismissalTest`,
  `DrawerMediaListNavigationTest`, `LoginShortcutIntentTest`,
  `ToolbarHomeNavigationTest`, and `LoggingFragmentTest` add focused
  back-stack, origin, shortcut, and dismissal coverage.
- Not covered by any test in the tree: cold-start assertions for the remaining
  manifest paths (`/studio`, `/character`, `/staff`, `/actor`, `/manga`,
  notification summary) and the warm `/user/<name>/animelist` case (G-001,
  G-003). Those are acceptance criteria (AC-INS-01), not claimed evidence.
- Device execution remains part of runtime validation (AC-EMU-01 through
  AC-EMU-11). The NFR-007 drawer-switch repro and regression run were recorded
  on the recovered emulator (see the audit's NFR-007 infrastructure status);
  AC-EMU-01 remains explicitly infrastructure-blocked with no emulator chip
  results claimed, and no other emulator acceptance criterion has recorded
  evidence (see the audit's Section 6.3 note).

## Navigation extension policy

Destination-owned functions such as `NavController.navigateToMedia(param)`
must remain thin UI extensions that call the registered action and
`param.asBundle()`. They must not create a process-wide mutable manager and
must not be callable from domain or state layers.
