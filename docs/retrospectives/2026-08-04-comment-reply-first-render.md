# Retrospective: Comment Reply First-Render Regression

## Symptom

Opening an activity with replies could show an empty comment screen on the
first load. Pull-to-refresh or reopening the screen could make the reply
appear, which initially made the problem look like a cache or refresh issue.

## Evidence collected

- The GraphQL request completed successfully with an HTTP 200 response.
- The decoded state contained one reply.
- The adapter eventually received one item.
- The visible list still had zero measured height during the failing first
  render.
- Direct device inspection confirmed that the reply became visible after the
  layout and adapter state were updated in the correct order.

## Root causes

1. The screen submitted data to an async list adapter and immediately updated
   the surrounding loading/content state. The layout could be evaluated
   before the adapter committed its new list.
2. The nested reply RecyclerView used `setHasFixedSize(true)` while its height
   was `wrap_content`. This allowed the parent content container to settle at
   zero height even though the adapter had an item.
3. The content state layout used `match_parent` inside a `wrap_content` parent,
   which made the zero-height result more likely.
4. Rapid or repeated loads needed request identity so an older response could
   not overwrite the state for a newer request.
5. A refresh-only verification path was insufficient because it bypassed the
   first-open timing and measurement path where the regression occurred.

## Durable guardrails

- Treat first open and refresh as separate behaviors. Verify both from a clean
  process and with a persisted authenticated session when the feature requires
  authentication.
- For a data-dependent content transition, submit the adapter list first and
  update the surrounding state from the adapter commit callback when the UI
  depends on the committed item measurement.
- Do not use `setHasFixedSize(true)` on a nested RecyclerView whose content
  height is driven by `wrap_content`.
- Keep parent and child measurement contracts explicit. A `match_parent`
  state container must have a bounded parent; otherwise use `wrap_content`.
- Give each request a monotonic identity and ignore success or failure results
  that do not belong to the current request.
- Verify the full path in layers: network response, ViewModel state count,
  adapter item count, measured layout bounds, and visible accessibility text.
- When adding a constructor dependency, update DI wiring and the module
  definition-count test in the same change.
- Keep callback branches explicit instead of adding labeled returns that raise
  static-analysis complexity warnings.

## Verification record

- Focused `CommentViewModelTest` passed.
- Koin module verification passed.
- App debug compilation and APK assembly passed.
- Direct device verification confirmed first-open reply visibility and refresh
  persistence.
- CI passed Android unit tests, validation, Codacy, GitGuardian, and JUnit
  reporting.

## Constraints for future work

- Do not modify `InMemoryFeedStore.reduceFeedDetailLoaded()` without a new
  impact analysis and explicit regression coverage.
- Preserve the canonical-store and stale-response rules in
  `docs/architecture/state-synchronization-and-mutation-refactor.md`.
- Keep unrelated redesign changes out of focused bug-fix commits.
