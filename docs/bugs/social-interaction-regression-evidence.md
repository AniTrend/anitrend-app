# Social Interaction Regression Evidence and Remediation Plan

**Date:** 2026-08-06  
**Branch under test:** `fix/social-mutation-ui-state`  
**Device:** Android Emulator `Custom_API_36`, API 36, serial `emulator-5554`  
**Build:** `appDebug`, rebuilt and installed with Android CLI and Gradle

## Executive finding

The earlier unit and instrumentation checks passed, but the authenticated Argent run exposed three live failures. The evidence below distinguishes confirmed causes from hypotheses that still require runtime request evidence.

| Area | Authenticated result | Current confidence |
|---|---|---|
| Review vote response | `RateReview` POST succeeded, then the UI showed `Attempt to invoke virtual method 'long ...'`; no committed UI state was observed. | Root cause confirmed in response mapping. |
| Profile Followers/Following | Both destinations opened with the correct title, but each remained on the loading spinner for more than 10 seconds. | Symptom confirmed; precise request failure is not yet proven. |
| Feed users follow control | Users sheet opened and remained without follow/unfollow controls after expansion. | Visibility conditions and ID-loss defect confirmed in code; exact live visibility branch still needs runtime state capture. |
| Profile Favourites | `Users Favourites` opened successfully from the loaded profile overview. | Passing path. |

## Evidence collection

### Build and runtime setup

The following setup completed successfully:

```text
android emulator start Custom_API_36
bash .github/scripts/setup-config.sh
./gradlew :app:assembleAppDebug :app:installAppDebug --no-daemon --console=plain
```

The app launched as `com.mxt.anitrend`. Argent discovery was used before every interaction. The authenticated session was already present on the emulator; no credentials are recorded here.

Prior automated evidence also exists:

- `:app:testAppDebugUnitTest`: 1071 tests passed.
- Focused mapper, ViewModel, holder registry, profile state, and follow convergence tests passed.
- Direct Android instrumentation: 9/9 passed across `AboutPanelWidgetInstrumentationTest`, `VoteWidgetInstrumentationTest`, and `FollowStateWidgetInstrumentationTest`.

Those checks are necessary but insufficient. The authenticated live run is the decisive evidence for the failures documented below.

## Issue 1: Review vote response does not converge the UI

### Observed behavior

On `Series Reviews`, the authenticated user tapped the first review's up-vote control.

- The UI emitted: `Attempt to invoke virtual method 'long ...'`.
- No reliable vote-state convergence was observed.
- The request log showed a successful `POST https://graphql.anilist.co/` for `RateReview`.
- The response contained a valid review payload with `id: 32611`, but `user: null` and `media: null`.

### Confirmed code path

```text
VoteWidget
  -> ReviewViewModel/BrowseReviewViewModel.rateReview
  -> RateReviewInteractor
  -> BrowseRepository.rateReview
  -> RateReview GraphQL POST
  -> ReviewRecordMapper.toReviewRecord
  -> ReviewStore
  -> review StateFlow
  -> adapter rebind
```

Relevant source:

- GraphQL schema declares `Review.user` nullable at `app/src/main/graphql/schema.graphql:3261` and `Review.media` nullable at `:3241`.
- The response requests both fields in `app/src/main/graphql/mutations/review/RateReview.graphql:1-5` and `app/src/main/graphql/fragments/review/ReviewFragment.graphql:12-17`.
- Legacy DTO fields are declared non-null with defaults in `app/src/main/java/com/mxt/anitrend/model/entity/anilist/Review.kt:28-29`.
- Plain Gson deserialization can write JSON null into those Kotlin fields through `app/src/main/java/com/mxt/anitrend/model/api/converter/AniGraphConverter.kt:36-47` and `AniGraphResponseConverter.kt:12-14`.
- `ReviewRecordMapper.toReviewRecord` dereferences `user.id` and `media.id` without a null guard at `app/src/main/java/com/mxt/anitrend/data/mapper/ReviewRecordMapper.kt:20-34`.
- `BrowseRepository.rateReview` wraps the mapping path at `app/src/main/java/com/mxt/anitrend/repository/BrowseRepository.kt:363-388`.
- `RateReviewInteractor` maps the successful response before committing at `app/src/main/java/com/mxt/anitrend/domain/review/interactor/RateReviewInteractor.kt:34-42`.
- `MutationInteractorSupport` converts thrown failures to `MutationResult.Failure` at `app/src/main/java/com/mxt/anitrend/domain/interactor/MutationInteractorSupport.kt:18-31`.
- `VoteWidget` then surfaces the failure and resets the flipper at `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/VoteWidget.kt:149-159`.

### Why existing tests missed it

- `RateReviewInteractorTest` constructs reviews with non-null user and media defaults at `app/src/test/java/com/mxt/anitrend/domain/review/interactor/RateReviewInteractorTest.kt:131-143`.
- `ReviewRecordMapperTest` covers empty defaults and null user names, but cannot construct the Gson-hidden-null state with normal Kotlin assignment at `app/src/test/java/com/mxt/anitrend/data/mapper/ReviewRecordMapperTest.kt:58-86`.

### Resolution

1. Make `ReviewRecordMapper.toReviewRecord` safe for Gson-injected nulls, treating null user/media the same as absent summaries, or migrate this response to the generated nullable GraphQL type and map explicitly.
2. Keep `ReviewStore` as the only committed state owner.
3. Add a production-Gson deserialization regression test with JSON `"user": null` and `"media": null`.
4. Add an interactor test proving a successful HTTP response with null nested objects does not crash and either commits a null-summary record or returns a controlled failure.
5. Re-run the authenticated vote journey and verify both the request and visible vote control state after the response.

### Acceptance criteria

- A successful `RateReview` response with null `user` and `media` never produces an NPE or generic virtual-method toast.
- The vote control exits loading through store convergence or a controlled failure effect.
- The review count/state is correct without manual refresh.

## Issue 2: Followers and Following sheets remain loading

### Observed behavior

On the authenticated profile overview:

- Profile counters loaded: `Following 122`, `Followers 131`, `Favourites 1.2 K`.
- Tapping Followers opened a sheet titled `Followers: 131`, but `Busy, please wait!` remained visible for more than 10 seconds.
- Tapping Following opened a sheet titled `Following: 122`, with the same persistent loading state.
- Favourites opened `Users Favourites` successfully.

### Confirmed code path

```text
AboutPanelWidget click
  -> BottomSheetListUsers
  -> onStart
  -> onRefresh
  -> requestType switch
  -> UserListViewModel.loadFollowers/loadFollowing
  -> UserRepository.getFollowers/getFollowing
  -> UserFollowers GraphQL query
  -> UserListViewModel.UiState
  -> BottomSheetListUsers render
```

Relevant source:

- Entry and request type assignment: `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/AboutPanelWidget.kt:153-186`.
- Argument resolution and fallback behavior: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetListUsers.kt:94-116`.
- Load trigger and refresh path: `BottomSheetListUsers.kt:200-228` and `:287-311`.
- State rendering and error handling: `BottomSheetListUsers.kt:313-358`.
- Request state conversion: `app/src/main/java/com/mxt/anitrend/viewmodel/UserListViewModel.kt:27-73`.
- Repository calls: `app/src/main/java/com/mxt/anitrend/repository/UserRepository.kt:128-152`.
- Null GraphQL data failure: `app/src/main/java/com/mxt/anitrend/repository/AbstractRepository.kt:21-27`.

### What is confirmed versus unknown

Confirmed:

- The sheet receives the expected title and opens.
- The sheet enters loading state.
- The spinner does not disappear within the 10-second verification window.
- The request and render path converts failures into `UiState.Error`, but the live run did not capture which failure branch occurred.

Not yet proven:

- Whether the API response was unsuccessful, had null page data, failed DTO conversion, used an invalid request type, or was otherwise interrupted.
- Whether the spinner is a request failure that failed to reach the error renderer or a separate lifecycle/render issue.

### Resolution

1. Reproduce with logcat and Chucker request/response capture around one Followers request and one Following request.
2. Add `UserListViewModel` tests for success, HTTP/API failure, null page data, and cancellation. Do not convert `CancellationException` into a user-visible error.
3. Add a deterministic test for `BottomSheetListUsers.resolve()` covering typed request arguments, legacy arguments, invalid values, and absent values.
4. Ensure every terminal state hides the spinner, including error and empty states.
5. Fix refresh-after-error behavior so the adapter is cleared before replacing page-one data; current append logic at `BottomSheetListUsers.kt:313-334` can duplicate entries after retry.
6. Re-run both authenticated sheet journeys and require the spinner to disappear into content, empty, or explicit error UI.

### Acceptance criteria

- Followers and Following each leave loading within a bounded timeout.
- Successful requests render rows.
- API or conversion failures render retry/error UI, never an indefinite spinner.
- Retry does not duplicate the first page.

## Issue 3: Feed users sheet has no follow/unfollow control

### Observed behavior

On authenticated Status Feeds:

- The feed loaded and the first likes control opened `Total Likes: 3`.
- The sheet displayed rows for `MeitanteiKates`, `nsen`, and `THEKiimphaa`.
- After expanding the sheet, rows still had no visible follow/unfollow control.
- Because no control was rendered, no follow/unfollow request could be initiated from this journey.

### Confirmed code path and visibility rules

- Feed sheet construction: `app/src/main/java/com/mxt/anitrend/view/sheet/BottomSheetUsers.kt:62-76` and `:212-219`.
- Adapter bind: `app/src/main/java/com/mxt/anitrend/adapter/recycler/index/UserAdapter.kt:84-90`.
- Layout includes the widget at `app/src/main/res/layout/adapter_user.xml:40-44`.
- Visibility is controlled by `FollowStateWidget.setUserModel` at `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/FollowStateWidget.kt:80-97`.

The widget is hidden when:

- `currentUser == null`.
- The row is the current user's own row.

The sheet supplies `databaseHelper.currentUser` at `BottomSheetUsers.kt:66-70`, which is read from ObjectBox by `app/src/main/java/com/mxt/anitrend/data/DatabaseHelper.kt:49-59`.

### Confirmed separate defect: user IDs are lost

- `UserBase.id` is marked `@IgnoredOnParcel` at `app/src/main/java/com/mxt/anitrend/model/entity/base/UserBase.kt:26-28`.
- `BottomSheetUsers.Builder.setModel` parcels the full `UserBase` list at `BottomSheetUsers.kt:212-219`.
- On sheet recreation, IDs can therefore return as `0`.
- `FollowStateWidget` can dispatch `userId = 0` at `FollowStateWidget.kt:130-141`.
- `BottomSheetUsers.rebindUserIfPresent` cannot match store records when the item ID is zero at `BottomSheetUsers.kt:116-123`.

This explains non-functional toggles even when a control is visible. The live reason all controls were absent still needs runtime capture of `databaseHelper.currentUser` and each row's self/current-user comparison. The strongest candidate is a null current-user object at bind time, but that is not asserted as fact from the live run alone.

### Resolution

1. Preserve stable user IDs across the feed likes sheet boundary. Prefer an ID-safe parcel model or pass immutable identity-plus-display data instead of parceling `UserBase` with an ignored ID.
2. Add a direct test that a feed likes list survives bundle round trip with each user ID intact.
3. Add visibility tests for current user present, current user absent, self row, and another user row.
4. Add a dispatch test proving the clicked row sends its real user ID to `ToggleUserFollowInteractor`.
5. Add a store convergence test proving a successful toggle updates the visible row without reopening the sheet.
6. Capture runtime `currentUser` availability and row IDs in a debuggable test or inspectable test fixture before finalizing the visibility branch.

### Acceptance criteria

- Another user's row visibly shows Follow or Following on first render.
- The current user's own row remains correctly hidden or otherwise intentionally non-actionable.
- Tapping the control sends the actual user ID, never `0`.
- Success changes the control state without manual refresh.
- Failure exits loading and shows explicit retry/error feedback.

## Delivery plan

### Phase A: close the confirmed vote and ID defects

- Null-safe review response mapping and production-Gson regression tests.
- ID-safe feed users-sheet model boundary and round-trip tests.
- Follow control visibility and real-ID dispatch tests.

### Phase B: diagnose and harden user-list loading

- Capture one Followers and one Following request with response/error evidence.
- Add ViewModel and sheet state tests for success, empty, failure, cancellation, and retry.
- Ensure terminal states always hide loading and retry replaces page-one data.

### Phase C: authenticated acceptance run

- Rebuild and install with Android CLI.
- Use Argent discovery before each interaction.
- Verify vote request and post-response UI state.
- Verify Followers, Following, and Favourites destinations.
- Verify feed users initial control, actual toggle request, successful convergence, and failure handling.

## Current status

The remediation is implemented on `fix/social-mutation-ui-state` but has not yet received a post-fix authenticated Argent acceptance run.

Implemented resolutions:

- Review responses now tolerate Gson-hidden null `user`/`media` values, and existing reviews receive a rating-only patch so cached display metadata cannot be erased by a partial mutation response.
- Feed users now cross the sheet boundary through parcel-safe `UserSheetModel` identity data. Adapter binding supplies the current-user context before the model, and the follow widget re-evaluates visibility when that context arrives.
- Followers/Following state collection now runs on the dialog's Fragment lifecycle from `onCreate`, rather than the nonexistent view lifecycle. Success, error, and retry behavior are covered by a real dialog regression test.
- Followers/Following follow failures now surface immediately through the sheet's existing notification convention instead of relying on the widget's 10-second fallback.

Deterministic verification after remediation:

- Full app unit suite: 169 suites, 1089 tests, 0 failures/errors.
- `compileAppDebugKotlin`, `compileAppDebugAndroidTestKotlin`, and `assembleAppDebug`: BUILD SUCCESSFUL.
- Phase 3 device acceptance is pending. The API 36 AVD reached boot completion in its log but became ADB-inaccessible; Argent and Android CLI retries failed, and a stale orphaned QEMU process remains. No post-fix authenticated UI result is being claimed until the emulator is recoverable.
- The follow-failure unit seam is covered by `BottomSheetListUsersFollowFailureTest`. Full coroutine/interactor/Toast integration remains a device acceptance obligation once the emulator is available.
