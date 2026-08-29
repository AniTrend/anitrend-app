# Navigation migration agent task template

Use this template for every bounded migration issue. Fill all fields from
`migration-inventory.md` before assigning implementation work.

## Objective

Migrate `<LegacyActivity>` to `<TargetFragment>` as defined by the navigation
migration specification. Do not redesign the application's navigation
architecture.

- Future-work backlog IDs, when applicable: `<FW-...>`

## Required reading

1. `docs/architecture/navigation-refactor/specification.md`
2. `docs/architecture/navigation-refactor/migration-inventory.md`
3. `docs/architecture/navigation-refactor/destination-contracts.md`
4. `docs/architecture/navigation-refactor/verification.md`
5. `docs/architecture/navigation-refactor/full-wrap-up-audit.md`
6. `docs/architecture/state-synchronization-and-mutation-refactor.md`
7. `docs/architecture/pr-checklist.md`
8. The listed Activity, pager adapter, callers, and ScreenParam contract

The wrap-up audit registers every open navigation item (NFR-001 through
NFR-016), the coverage gaps G-001 through G-014, and the approved route
policies in its Section 5.2. Check the audit before assigning or starting work:
if the task touches a registered item, reference its NFR or G id in the
assignment and record the evidence in the audit's issue register on completion.

## Existing behavior

- Legacy destination: `<...>`
- ScreenParam and legacy extras: `<...>`
- Internal callers: `<...>`
- External entry points: `<...>`
- Pager pages: `<...>`
- Activity results: `<...>`
- Special lifecycle or window behavior: `<...>`

## Target

- Destination: `<TargetFragment>`
- Graph: `<graph and destination id>`
- Screen parameter: `<ScreenParam>`
- Sections: `<none or explicit section model>`
- Selector: `<none, chips, segmented, menu, or other>`
- Back behavior: `<inventory contract>`
- State restoration: `<state owner and requirements>`

## Architectural constraints

- Use AndroidX Navigation 2 and Fragment destinations.
- Do not introduce another Activity.
- Do not introduce another ViewPager or page adapter.
- Do not emulate the old pager with child Fragments.
- Do not inject `NavController` into a ViewModel, interactor, repository, store,
  adapter, or custom view.
- Do not pass domain or persistence entities through navigation.
- Do not introduce Safe Args or another argument framework.
- Reuse the declared `ScreenParam` contract and stable wire key.
- Preserve canonical-store state ownership.
- Do not perform unrelated cleanup or refactors.
- Do not remove compatibility code until all known callers and ingress paths are
  migrated.

## Implementation sequence

1. Inspect every listed dependency and caller.
2. Verify the inventory against current source before editing.
3. Extract reusable section rendering if required, without changing the route.
4. Implement the target Fragment and register its Navigation 2 destination.
5. Migrate scoped internal callers through Fragment-owned navigation callbacks.
6. Migrate external ingress only when explicitly included in this task.
7. Add navigation, lifecycle, restoration, and state-architecture tests.
8. Remove the legacy Activity only when it has zero remaining callers and no
   unhandled external ingress.
9. Remove its manifest declaration and obsolete owned pager code.
10. Run repository-standard verification.

For retained Activity or post-migration validation work, replace the migration
sequence with the specific backlog item sequence and record its evidence in
`docs/architecture/navigation-refactor/future-work.md`.

## Scope control

If implementation requires changing a contract not described in the inventory,
stop that part and record a blocker. Do not invent a new architecture.

## Definition of done

- The target Fragment is the production destination for the scoped path.
- All scoped callers use Navigation 2.
- Arguments use the declared `ScreenParam` contract.
- Back behavior and restoration match the inventory.
- No prohibited dependency has been introduced.
- Legacy components within scope have zero references and are deleted, or the
  retained compatibility path has an explicit removal condition.
- Tests and repository verification pass.
- The PR description includes phase, invariant, compatibility path, tests,
  limitations, dependencies, and rollback procedure.
