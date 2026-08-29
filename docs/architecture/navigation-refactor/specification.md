# AniTrend navigation refactor specification

**Status:** Navigation 2 internal-destination migration implemented in the working tree; boundary review and enforcement recorded, validation gates open
**Target:** AndroidX Navigation 2 with Fragment destinations
**Date:** 16 August 2026

The wrap-up record for this branch is
[`full-wrap-up-audit.md`](full-wrap-up-audit.md). It is the controlling plan for
closing the branch: it registers the observed architecture, the issue register
(NFR-001 through NFR-016), the coverage gaps G-001 through G-014, and the
acceptance criteria that must produce recorded evidence before the branch can
be called wrapped up. Phase markers in this specification describe implemented
tree states, not verified behavior.

Future work that is not required for this migration round is tracked in
[`future-work.md`](future-work.md). That backlog is the authoritative record
for retained Activity decisions, runtime validation gaps, and the later Compose
and Navigation 3 transition.

## Purpose

This document is the control plane for the strangler migration from activity and
pager-driven application navigation to one `MainActivity` shell containing the
primary Navigation 2 `NavHostFragment`. It is deliberately not a Compose or
Navigation 3 migration. Navigation 3 is reserved for a later migration after
the relevant Fragment and View screens have become Compose destinations.

The current repository already uses Navigation 2.9.8, and the former Settings
flow provided the XML `NavHostFragment` pattern that was moved into the root
host. The migration therefore extends an existing approach instead of
introducing a second navigation representation. The former `nav_settings_graph.xml`
is deleted: Settings destinations are registered directly in the root graph
because Navigation 2 resolves ids from the current graph.

The implementation provides a primary root host in `MainActivity` with a
bounded direct-child destination registry in `nav_root.xml` (28 fragment
destinations). This keeps all migrated routes addressable from one Navigation 2
graph while preserving the existing Settings destination actions. The root
graph starts at `AnimeFragment`; the legacy index pager bridge has been
removed.

## Target architecture

```text
MainActivity
├── window and insets
├── drawer
├── toolbar and application chrome
└── primary NavHostFragment
    └── direct-child destinations and their local section state
```

The target is not an arbitrary activity count. Activities may remain for
external ingress, authentication, system/window-specific behavior, or a
temporary compatibility boundary. The success metric is zero activities used
as ordinary internal application destinations after the migration completes.

## Non-negotiable invariants

### NAV-001: one navigation owner

`MainActivity` owns the primary application `NavHostFragment`. Nested graphs
remain allowed for future feature-local navigation, but migrated application
routes are direct children of the primary graph so sibling feature navigation
resolves reliably through Navigation 2 global actions.

### NAV-002: NavController stays in the UI layer

`NavController` must not be injected into ViewModels, interactors,
repositories, stores, adapters, or custom views. Fragments translate UI
callbacks into navigation operations.

### NAV-003: identity-only navigation

Destination arguments use the existing typed `ScreenParam` contract and its
stable wire keys. Pass identity and presentation-independent data only. Do not
pass complete domain or persistence entities. Do not introduce Safe Args, a
generic route model, or another argument framework alongside `ScreenParam`.

### NAV-004: navigation is not state synchronisation

Navigation changes the displayed destination. A destination must not return a
mutated domain object to repair another screen. Mutations commit through the
repository and canonical store, and observing screens render store state.

### NAV-005: no replacement pager architecture

Replacing `ViewPager2` with a Fragment switcher that creates one child Fragment
per old page is not a completed migration. Such a switcher preserves the old
architecture under a new name.

### NAV-006: one logical screen equals one Fragment

Pager entries must be classified as either independently navigable destinations
or local sections of one logical screen. Local sections use screen state, not a
second application navigation stack.

### NAV-007: one Fragment is not a god Fragment

Unified Fragments may delegate rendering to ordinary section renderers,
controllers, binders, or custom views. Those components must remain view-only,
must not own navigation, and must not be child Fragments used to emulate a
pager.

## Pager classification rule

For every current pager entry, answer:

```text
Can this content have its own deep link, back-stack entry, title, or
independent navigation identity?
    yes -> Navigation destination
    no  -> Is it mutually-exclusive presentation or filtering of one screen?
               yes -> local section state
               no  -> record the unresolved decision in the inventory
```

Selector guidance:

- Two to five compact mutually-exclusive modes: single-select chips or a segmented control.
- Many sections: horizontally scrolling selector or menu.
- Filters: filter chips.
- Actions: buttons or menu actions.
- Top-level destinations: drawer or Navigation component.
- Independently addressable screens: Navigation destinations.

Do not force eight Media sections into a cramped fixed selector.

## Explicit top-level stack semantics

The root graph now defines these behaviors for the migrated drawer routes:

- Selecting the current top-level destination is a no-op.
- Selecting a different root destination pops to the `AnimeFragment` start
  destination with state saving, then uses `launchSingleTop` and `restoreState`.
  This prevents duplicate root entries while retaining destination state.
- System Back from a top-level destination uses the existing exit confirmation.
  Back from a detail destination uses the Navigation 2 back stack, or finishes
  an externally launched MainActivity entry.
- Local section selection is owned by each destination and is restored through
  saved instance state or its existing ViewModel state owner.
- Detail routes use ordinary destination options rather than root pop-up
  semantics, so a detail opened from another destination can return correctly.

## Migration phases

Phase markers are implemented tree states on the current branch, not claims of
verified behavior. Each phase's validation gates are listed in the audit's
Section 6.

1. **Phase 0, inventory:** establish this specification, the historical
   and pager inventory, destination contracts, and verification gates. No
   production navigation code changes in this phase. **Implemented.**
2. **Phase 1, host foundation:** convert `MainActivity` progressively into the
   root shell, add the XML root graph, and make drawer entries target destinations.
   **Implemented for the current migration round.**
3. **Phase 2, small infrastructure:** add destination-owned navigation
   extensions on top of the existing `ScreenParam` helpers. Do not add a global
   mutable navigation manager. **Implemented.**
4. **Phase 3, leaf destinations:** migrate low-risk internal destinations as
   complete vertical slices, including callers, tests, and manifest cleanup.
   **Implemented.**
5. **Phase 4, Settings:** register the existing Settings destinations under the
   root host and remove `SettingsActivity` after parity is verified. **Implemented;
   parity verification is part of the audit gates.**
6. **Phase 5, pager extraction:** extract reusable section rendering while old
   page Fragments temporarily consume it. Do not rewrite pager screens atomically.
   **Implemented for migrated application destinations.**
7. **Phase 6, detail families:** migrate Character, Staff, Studio, Profile, and
   Media in that dependency order unless the inventory records a different safe
   order. **Implemented for the current detail families.**
8. **Phase 7, state/result destinations:** remove activity-result state
   synchronisation, including the Comment and Feed path. **Implemented for migrated paths.**
9. **Phase 8, external ingress:** route deep links, notifications, shortcuts,
   sharing, OAuth returns, and restored intents through the root host after their
   destinations exist. **Routing implementation complete; exhaustive runtime
   coverage is tracked as FW-001.** The route-coverage comparison against the
   actual tests (two warm assertions plus nine newer focused tests in
   `MainActivityExternalIngressTest`) is recorded in `verification.md`.
10. **Phase 9, special activities:** evaluate remaining window and platform
    boundaries individually. Do not remove `VideoPlayerActivity` for an activity
    count metric alone. **Current boundaries documented; individual follow-up is
    tracked as FW-003.**
11. **Phase 10, pager removal:** delete pager infrastructure only after its final
    consumer is gone, then enforce the architectural rule in CI. **Implemented for
    migrated destinations; CI integration follow-up is FW-007.**

## Scope control

Each migration PR establishes one architectural fact and one bounded vertical
slice. If a task requires changing a contract not listed in the inventory, stop
that part, record the blocker, and update the inventory in a separate planning
change. Do not invent a new navigation architecture in an implementation PR.

## Wrap-up record

The branch is not wrap-up-ready on implementation presence alone. The audit
(`full-wrap-up-audit.md`) lists what must be true before closure: the NFR-007
repro recorded for both drawer-switch directions with the remediation applied
(the rotation/process-death and PUSHED-origin matrix remains follow-up under
AC-EMU-02 and G-013), the static and unit gate, the instrumentation gate, the
emulator matrix (AC-EMU-01 remains explicitly infrastructure-blocked with no
recorded chip evidence), and the documentation and hygiene passes.
Route-policy decisions approved by the user are recorded in audit Section 5.2
and are binding on later phases.
