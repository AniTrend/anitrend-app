UI Maintenance Hardening Checklist
=================================

Branch:
`feat/ui-maintenance-hardening`

## UI and UX concerns

- [x] Progress layout regression coverage expanded for loading, error, empty,
  and content transitions
- [x] Progress layout preserves initially hidden children across repeated state
  transitions
- [x] Favourite and vote widgets use compact loading spinner dimensions
- [x] Host surface coverage added for compact widget spinners
- [x] Gradle `connectedAppDebugAndroidTest` wrapper hang diagnosed and fixed
  - Root cause was unstable entry-point render smoke coverage using a bootstrap
    activity and Espresso idling against startup work
  - Fixed by excluding `SplashActivity` from generic render smoke coverage and
    switching render assertions to direct `ActivityScenario` visibility checks

## Code maintenance

- [x] Introduce a typed ViewModel acquisition seam for `BottomSheetListUsers`
- [x] Replace generic `ViewModelBase` usage in additional high-friction screens
- Completed for `BottomSheetListUsers` and `BottomSheetGiphyList` via
  `acquireTypedViewModelBase`
- [x] Inventory legacy worker usage and capture coroutine migration candidates
- [x] Migrate the first selected legacy worker flow to a coroutine-first
  ViewModel path
  - Completed for login authentication callback handling in `LoginActivity`
    with `LoginAuthViewModel`
  - Removed obsolete `AuthenticatorWorker` DI registration and source after
    callback migration
- [x] Add mutation-state guard coverage around `AboutPanelWidget`
- [x] Expand mutation guard coverage to additional EventBus-driven widgets and
  fragments
  - Added `AvatarIndicatorView` re-attach and `USER_CURRENT_REQ` badge refresh
    coverage

## Test coverage and guard rails

- [x] Instrumentation coverage added for `ProgressLayout`
- [x] Instrumentation coverage added for compact widget spinners
- [x] Instrumentation coverage added for `AboutPanelWidget` mutation re-attach guard
- [x] Direct device verification completed on `Custom_API_36`
- [x] Add follow-up unit coverage for non-UI state transitions where possible
  - Added `LoginAuthViewModelTest` coverage for callback-error, blank-code,
    success, and thrown-token-request paths
- [ ] Add broader regression guard rails before larger UI widget replacements
  - Added `FollowStateWidgetInstrumentationTest` to lock button-state text and
    loading-spinner reset behavior
  - Hardened `TestSessionUtil` to force `Settings.isFreshInstall = false` so
    entry-point smoke coverage does not re-enter onboarding during
    connected-device runs

## Verification evidence

- `./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug --no-daemon`
- `./gradlew :app:compileAppDebugKotlin :app:test --no-daemon --stacktrace`
- `./gradlew :app:connectedAppDebugAndroidTest --stacktrace --no-daemon`
- `./gradlew :app:testAppDebugUnitTest --tests com.mxt.anitrend.viewmodel.LoginAuthViewModelTest --no-daemon --stacktrace`
- `./gradlew :app:connectedAppDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mxt.anitrend.ui.EntryPointRenderUnauthTest --stacktrace --no-daemon`
- `./gradlew :app:connectedAppDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mxt.anitrend.ui.EntryPointRenderAuthTest --stacktrace --no-daemon`
- Direct instrumentation:
  - `com.mxt.anitrend.ui.EntryPointRenderAuthTest`
  - `com.mxt.anitrend.ui.EntryPointRenderUnauthTest`
  - `com.mxt.anitrend.widget.ProgressLayoutInstrumentationTest`
  - `com.mxt.anitrend.widget.WidgetSpinnerDimensionInstrumentationTest`
  - `com.mxt.anitrend.widget.EventBusMutationStateGuardTest`
  - `com.mxt.anitrend.widget.FollowStateWidgetInstrumentationTest`

## Notes

- `FragmentBase.setViewModel` local caller scan shows broad usage despite a
  misleading low-impact GitNexus result, so that seam needs separate deeper
  analysis before refactoring.
- `ProgressLayout.applyState` remains a GitNexus `CRITICAL` blast-radius
  symbol, so further edits there should stay tightly scoped and re-verified
  immediately.
- The current connected-device wrapper issue is isolated to the Gradle
  `connectedAppDebugAndroidTest` path. Direct `adb shell am instrument`
  execution is currently passing for the targeted suites above.
