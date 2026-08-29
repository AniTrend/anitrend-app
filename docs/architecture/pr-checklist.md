# Architecture PR Checklist

Every architecture PR description must include:

- Specification phase
- Issue link
- Problem addressed
- Architectural invariant introduced
- Files and components changed
- Compatibility path retained
- Compatibility path removed
- Tests added
- Verification commands executed
- Known limitations
- Follow-up dependencies
- Rollback procedure

For navigation migration PRs, also include:

- Navigation migration phase
- Future-work backlog ID, when the PR advances a tracked follow-up
- Legacy destination and new destination
- Migration inventory entry
- `ScreenParam` contract and stable wire key
- Internal callers migrated
- External callers and ingress migrated
- Pager pages classified and removed, if applicable
- Activity and manifest removal status
- Compatibility path and its removal condition
