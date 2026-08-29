# Navigation refactor future work

**Status:** Planned follow-up work after the Navigation 2 internal-destination migration
**Authority:** This file is the authoritative backlog for navigation work that is
not required to complete the current migration round.

The ordinary application navigation migration is implemented in the working
tree; the wrap-up gates are recorded in
[`full-wrap-up-audit.md`](full-wrap-up-audit.md) (issue register NFR-001
through NFR-016, coverage gaps G-001 through G-014, acceptance criteria
AC-STAT/AC-INS/AC-EMU/AC-DOC). The items below are deliberately not
prerequisites for that implementation. They capture retained platform
boundaries, runtime validation gaps, and later architectural work so they are
not mistaken for unfinished destination migrations.

## Backlog

| ID | Workstream | Depends on | Completion evidence | Status |
| --- | --- | --- | --- | --- |
| FW-001 | Exhaustively verify cold and warm external ingress for AniList links, notifications, shortcuts, sharing, OAuth returns, restored intents, and `singleTop` delivery | Corresponding root destinations and `ScreenParam` contracts | Automated or deterministic tests cover each listed ingress in both cold and warm process cases, with no duplicate root entries | Planned. Ingress routing is implemented in the tree (NFR-001 through NFR-004, see the audit); partial cold-start coverage exists in `MainActivityExternalIngressTest` (two warm assertions plus nine newer focused tests), and cold-start coverage for the remaining manifest paths plus the warm `/user/<name>/animelist` case is still open (G-001, G-003, AC-INS-01) |
| FW-002 | Run device-level lifecycle and state-restoration validation across migrated destinations | Stable emulator or device test setup | Rotation, view recreation, Back, Up, root switching, local section restoration, and return-from-detail behavior are recorded and passing | Partially advanced. The NFR-007 drawer-switch repro was recorded in both directions and the regression run passed on the recovered emulator (the API 36 AVD reached boot completion but became ADB-inaccessible, retries failed, and a stale orphaned QEMU process remained; a cold AVD restart recovered it for the repro and the regression run, see the audit's NFR-007 entry). The remediation (restoreState disabled for ROOT media-list navigation) and `MediaListSavedStateNavigationTest` are in the tree. Remaining: the rotation/process-death and PUSHED-origin matrix (AC-EMU-02, G-013) and the media chip matrix (AC-EMU-01, NFR-008 device confirmation), which is explicitly infrastructure-blocked with no recorded evidence; no other emulator acceptance criterion has recorded evidence (`docs/bugs/social-interaction-regression-evidence.md` lines 252-253). Applies to AC-EMU-01 through AC-EMU-11 |
| FW-003 | Evaluate retained Activity boundaries individually: `SplashActivity`, `LoginActivity`, `WelcomeActivity`, `ImagePreviewActivity`, `GiphyPreviewActivity`, and `VideoPlayerActivity` | FW-001 and FW-002; separate product decisions for window-specific behavior | Each Activity has a recorded keep, route, or removal decision with ingress, lifecycle, and window behavior evidence | Planned |
| FW-004 | Modernize onboarding without treating its `ViewPager2` as application navigation | Product-approved onboarding replacement and parity criteria | `WelcomeActivity` either remains an explicitly justified onboarding boundary or is replaced with an equivalent local-state flow; no application pager infrastructure is reintroduced | Planned |
| FW-005 | Reduce `CommonActivity` compatibility debt and remove legacy presenter wiring where no longer needed | All current Activity consumers audited; no new presenter usage | Compatibility dependencies are removed only after zero-consumer checks and targeted tests | Planned |
| FW-006 | Review section parity and request behavior for unified detail screens | FW-002 | Former pager pages have documented equivalent sections or destinations, authentication visibility is preserved, and section changes do not duplicate requests | Planned. The section mapping invariant is fixed and unit-asserted (NFR-008, `MediaFragmentSectionOrderTest`); the duplicate-request review includes Profile's `onStart` re-render (NFR-015) and runs on FW-002's device evidence |
| FW-007 | Integrate architecture enforcement and targeted ingress checks into the normal CI signal | CI workflow ownership and deterministic test data | CI visibly rejects new migrated-scope pagers, forbidden navigation dependencies, duplicate root graph registries, and unclassified Activity boundaries | Planned |
| FW-008 | Reassess graph modularization only if a concrete feature-local graph need appears | Navigation 2 runtime evidence and a bounded feature need | Any new nested graph is proven with destination-level navigation tests and does not create another application-level host or router | Deferred |
| FW-009 | Plan the eventual Navigation 2 to Navigation 3 transition | Separate Compose migration completed for the relevant destinations | All intended destinations are Compose destinations, the Compose navigation contract is approved, and a separate migration plan is implemented and verified | Deferred |

## Retained boundaries

The remaining Activities are not ordinary internal navigation destinations.
Their current roles are:

- `SplashActivity`: launcher and startup coordinator.
- `LoginActivity`: OAuth/browser return boundary.
- `WelcomeActivity`: onboarding window with the remaining production
  `ViewPager2`.
- `ImagePreviewActivity` and `GiphyPreviewActivity`: preview windows.
- `VideoPlayerActivity`: player window with explicit orientation and playback
  behavior.

No Activity should be removed to satisfy an arbitrary Activity-count target.
Removal requires an equivalent route, lifecycle behavior, external ingress
handling, and window behavior. `CommonActivity` is a compatibility base class,
not an ordinary destination.

## Current validation boundary

The current round has local evidence for compilation, unit tests, Android test
source compilation, APK assembly, targeted emulator ingress tests, and static
architecture scans, recorded at the time for earlier states of the tree. This
documentation pass does not re-verify any of it. The planned runtime work above
is broader than those targeted checks. In particular, the warm media and share
ingress tests (two warm assertions plus nine newer focused tests in
`MainActivityExternalIngressTest`) do not constitute exhaustive cold-start,
notification, shortcut, OAuth, rotation, process-death, or device-matrix
coverage, and the emulator acceptance criteria (AC-EMU-01 through AC-EMU-11)
have no recorded device evidence except the NFR-007 drawer-switch repro and
regression run, which were recorded on the recovered emulator; AC-EMU-01
remains explicitly infrastructure-blocked (`docs/bugs/social-interaction-regression-evidence.md`
lines 252-253). See the audit for the full evidence comparison (AC-DOC-02) and
the open coverage gaps G-001 through G-014.

## Work selection rules

Future implementation tasks must:

1. Reference one or more IDs from this backlog.
2. Preserve the Navigation 2, identity-only `ScreenParam`, and single-host
   invariants in `specification.md`.
3. Update the relevant inventory entry before changing a retained boundary.
4. Define the test and rollback evidence before deleting compatibility code.
5. Avoid introducing Navigation 3, a second argument framework, child-fragment
   pager replacements, or a process-wide navigation manager.

When an item is completed, update its status and add links or commands for the
evidence. If implementation reveals a new architectural decision, record it in
the specification and inventory rather than silently expanding a task.
