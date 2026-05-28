# ADR: Replace ProgressLayout Snapshot Dependency with Local Widget

## Status
Accepted

## Context
The app depended on `com.github.nguyenhoanglam:ProgressLayout:master-SNAPSHOT` from JitPack for state-based content/loading/error layouts. This SNAPSHOT dependency is no longer reliably resolvable, causing build failures. The library is small — essentially a `FrameLayout` that shows/hides content, a loading spinner, and an error state.

## Decision
Replace the external SNAPSHOT dependency with a local widget in `com.mxt.anitrend.widget.ProgressLayout` backed by a `ProgressLayoutState` enum. The local widget preserves the same public API:
- `showLoading()`
- `showContent()`
- `showError(drawable, message, actionText, action)`
- `showEmpty(drawable, message)`
- `isLoading`, `isContent`, `isError`

## Consequences
- Build no longer depends on an unreliably resolvable SNAPSHOT artifact
- Full control over the widget's behavior and styling
- Slightly more code to maintain in-repo (~130 lines for the widget + 34 lines for the state enum)
- All usages across XML layouts and Kotlin files updated to reference the new package
