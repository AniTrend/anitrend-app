# Incremental XML-to-Compose migration inventory

**Status:** Complete for app-owned collections: lists, grids, grouped detail sections, carousels, pagers, and the navigation drawer use Compose collections. XML activity/fragment shells, selected stateless View interop, and Navigation 2 remain. Earlier slice tables and checkpoints are historical snapshots, not an outstanding Recycler backlog; this does not mark the broader XML-to-Compose program complete. The final frozen-code verification is recorded below.

## Baseline and constraints

- Migration start baseline: `AniTrend/anitrend-app` `develop` at `78f28e93`; this shipping branch was rebased onto `develop` at `4fc7ff325` before final verification.
- The app is a single `:app` module. Android build conventions and common dependencies live in `buildSrc`.
- The app is View-based at baseline, with Navigation 2 and ViewBinding. ViewBinding remains enabled during incremental adoption.
- The root Navigation 2 graph has 28 destinations. Its route and ingress baseline is already recorded in `navigation-refactor/destination-contracts.md` and covered by the existing navigation instrumentation tests. This slice does not change routes or ingress behavior.
- The existing Material 3 XML theme remains the source of truth. It defines light, dark, and AMOLED schemes, the condensed type family, and the button shape tokens. No dynamic color or full-theme migration is introduced.

## Candidate shortlist

| Rank | Layout | Status | Complexity | Custom or library UI dependencies | Consumers and rationale |
|---|---|---|---:|---|---|
| 1 | `app/src/main/res/layout/item_settings_divider.xml` | Migrated (second slice) | 1/5 | 1 Material `MaterialDivider` | Two Settings Fragment consumers; smallest hierarchy but more reused than the selected single-host screen. |
| 2 | `app/src/main/res/layout/fragment_changelog_m3.xml` | Migrated (first screen) | 2/5 | 4 Material views, 1 shared `RichMarkdownTextView` | One `ChangelogFragment` consumer; static asset/version and one existing View bridge. Selected for the first whole-screen slice. |
| 3 | `app/src/main/res/layout/item_settings_row_value.xml` | Migrated (third slice) | 3/5 | 3 Material text views, 1 AppCompat image view | Used by two Settings Fragment owners; several optional values and visibility states. |
| 4 | `app/src/main/res/layout/item_load_state_footer.xml` | Migrated (fourth slice) | 3/5 | 1 custom `CustomProgress`, 2 platform `TextView`s | Two Paging list consumers: `SearchFragment` and `MediaRecommendationsSection`; loading and retry states add interaction behavior. |
| 5 | `app/src/main/res/layout/item_settings_category_card.xml` | Migrated (fifth slice) | 2/5 | 1 Material `MaterialCardView`, 1 `ShapeableImageView`, 2 `MaterialTextView`s, 1 AppCompat image view | One `SettingsHubFragment` owner, repeated 7–9 times from the registry; static labels/icons with Fragment-owned navigation. |
| 6 | `app/src/main/res/layout/item_settings_section_card.xml` and `item_settings_row_switch.xml` | Migrated (sixth slice) | 3/5 | 1 MaterialCardView, header views, and 1 MaterialSwitch | Two Settings Fragment owners; the complete section tree is Compose while Fragments retain preference, dialog, and navigation ownership. |
| 7 | `app/src/main/res/layout/widget_delete.xml` | Migrated (eighth slice) | 3/5 | Compose `Icon`, Material 3 circular progress indicator, pool-aware `ComposeView` in `StatusDeleteWidget` | Four adapter layouts retain the wrapper; confirmation and RecyclerView listener cleanup stay with the existing owner. |
| 8 | `app/src/main/res/layout/widget_fuzzy_date.xml` | Migrated (seventh slice) | 3/5 | 1 shared stateless Compose field hosted twice in ComposeView; native DatePickerDialog remains platform UI | One `BottomSheetSeriesManage` owner; mutable draft dates and per-component picker seeds, with no RecyclerView fan-out. |

The simplest rows have multiple consumers. Changelog has one host and read-only state, so its lower regression fan-out outweighs its single rich-text View bridge.

## First migrated slice: Changelog

**Candidate:** `app/src/main/res/layout/fragment_changelog_m3.xml`, consumed only by `ChangelogFragment`.

The layout was a scrollable, centered screen containing the announcement icon, heading, version, shared `RichMarkdownTextView`, and a tonal Dismiss button. The simpler Settings divider and value rows are reused across multiple consumers; Changelog has one host, no business state, and a single known View interop boundary.

### Behavior contract

- Inputs: `BuildConfig.versionName` and the packaged `assets/changelog.md`.
- Actions: Dismiss invokes `findNavController().navigateUp()` from the existing Fragment host.
- Asset failure: log `IOException` with Timber and leave the Markdown view blank, matching the old path.
- Navigation: keep the existing `ChangelogFragment` destination and `navigateToChangelog()` caller; no graph changes.
- State: the Fragment loads the immutable version and asset once per view creation. `ChangelogScreen` is stateless; `rememberScrollState()` owns only scroll position. No ViewModel or Koin Compose dependency is added for static local content.
- Accessibility: the repeated announcement icon is decorative, the Compose title has heading semantics, the tonal button exposes its label and click action, and the screen runs Compose accessibility checks on the API 36 emulator.

### Interoperability and removal boundary

- `ChangelogFragment` returns a `ComposeView` with a stable ID and `DisposeOnViewTreeLifecycleDestroyed`.
- `fragment_changelog_m3.xml` is removed after confirming its single source use. There were no Changelog-specific legacy tests to delete.
- `RichMarkdownTextView` remains hosted with `AndroidView`. It provides the existing Markwon formatting and link behavior and is still used by comments, feed, profile, reviews, and detail layouts. Do not remove its class or Markwon dependency until those other consumers are migrated.
- The Compose theme maps only the color roles and type/shape values required by migrated UI. Colors resolve from the active XML theme, preserving light, dark, and black themes. Existing XML themes remain for the View-based app.

## Second migrated component: Settings divider

**Candidate:** `app/src/main/res/layout/item_settings_divider.xml`, inflated by `CustomizeSettingsFragment` and `SettingsCategoryLegacyFragment`.

The layout was a full-width `MaterialDivider` inserted between settings rows. The Customize screen has two row dividers; the tested Content category has three. The section header `MaterialDivider` in `item_settings_section_card.xml` remains View-based and is outside this slice.

### Behavior contract

- Inputs and actions: none. Divider remains decorative and non-clickable. Settings row state, preference handling, and Navigation 2 destinations are unchanged.
- Layout: full width, 1dp thickness, no insets or margins. Compose `HorizontalDivider` uses `MaterialTheme.colorScheme.outlineVariant`.
- Theme: `AniTrendTheme` maps the current XML `colorOutlineVariant` role. The light resource is `#CAC4D0`; the dark and AMOLED resources are `#49454F`.
- Lifecycle: the Fragment owners rebuild row views when settings state changes. Each divider ComposeView receives a unique ID and uses `DisposeOnDetachedFromWindowOrReleasedFromPool`; the divider owns no state.
- Accessibility: the separator has no click action. `SettingsDividerComposeTest` checks divider counts and 1dp height in both Fragment owners.

### Interoperability and removal boundary

- A shared `LinearLayout.addSettingsComposeContent()` host keeps ComposeView creation, theme setup, layout parameters, and detach disposal behavior identical for both settings components.
- `item_settings_divider.xml` was removed after a source search found no remaining references. No divider-specific legacy tests existed. The inline section-header divider and other `MaterialDivider` resources remain untouched.

## Third migrated component: Settings value row

**Candidate:** `app/src/main/res/layout/item_settings_row_value.xml`, used by `CustomizeSettingsFragment` and `SettingsCategoryLegacyFragment`.

The View layout served two current presentations. Choice rows display a title, optional summary, current value, and chevron; the owning Fragment still opens the existing choice dialog and updates its existing preference or ViewModel. Info rows display a title and summary in a disabled, non-clickable state, without a value or chevron. Customize has three Choice rows; the Content category has four Info rows.

### Behavior contract

- `SettingsValueRow` is stateless. It receives resolved text, enabled state, content description, optional value and chevron, and an optional click callback. It owns no preferences, dialogs, ViewModels, or navigation.
- Typography and colors reuse the existing Compose theme: BodyLarge title, BodySmall summary, LabelLarge value, `onSurface`, `onSurfaceVariant`, and `primary` roles. The title uses a local `22.sp` line height because the XML TextView measured 65px while the Compose default measured 72px on the API 36 baseline; the override removes the 2dp row-height drift without changing the shared theme.
- Row spacing uses the existing `lg_margin`, `md_margin`, and `xl_margin` resources; minimum height stays 56dp. The disabled alpha is shared with the remaining View-based settings toggle row.
- Choice rows keep their accessible title/value description and click action. Info row text stays disabled and non-clickable; neither Info row exposes a value or chevron.

### Interoperability and removal boundary

- `SettingsComposeHost.addSettingsComposeContent()` hosts each row in the shared settings ComposeView boundary. Fragment state, dialog ownership, and preference writes remain in their existing owners.
- `item_settings_row_value.xml` was removed after a source search found no remaining references. The temporary XML height-comparison and screenshot-capture tests were removed after parity was verified. The separate View-based toggle layout remains.

## Fourth migrated component: Paging load-state footer

**Candidate:** `app/src/main/res/layout/item_load_state_footer.xml`, shared by Anime/Manga results in `SearchFragment` and the recommendations list in `MediaRecommendationsSection`.

### Behavior contract

- `LoadStateFooterUiState.from` remains the pure mapping: Loading shows the localized label and small spinner; Error shows the localized retry action; both NotLoading values map to no controls. Paging 3.5.1 `LoadStateAdapter` displays Loading and Error by default and suppresses NotLoading.
- Retry stays owned by each existing Paging adapter callback. The shared footer does not own paging state, data loading, navigation, or screen-level errors.
- The centered row keeps `spacing_small` vertical insets (24dp), the two `md_margin` progress/text gaps (4dp each), and `spacing_medium` retry-text padding (32dp per side). Text uses Compose `bodyMedium` and `onSurface`, with the existing `Loading` and `button_try_again` resources. Retry exposes button role and click action semantics.
- Both production consumers still use `LoadStateFooterAdapter`. Search attaches it for Anime and Manga; `MediaRecommendationsSection` attaches it to recommendations and clears the RecyclerView adapter in `destroyView`.

### Interoperability and removal boundary

- `LoadStateFooterAdapter` hosts `LoadStateFooter` inside a uniquely identified ComposeView and uses `DisposeOnDetachedFromWindowOrReleasedFromPool` for RecyclerView pooling.
- `CustomProgress` remains inside `AndroidView` with the native small-progress style. A Material 3 indeterminate indicator rendered a visibly larger arc than the XML baseline; retaining the existing widget preserves its platform animation. The widget remains used by `custom_recycler_loading.xml`, `custom_recycler_loading_footer.xml`, and `activity_splash.xml`.
- `item_load_state_footer.xml` was removed after a source search found zero references. The adapter and load-state mapper remain. Neither production consumer's state or navigation behavior changed.

### Baseline and tests

- XML and Compose output from the shared adapter were captured on `Custom_API_36` at 1080×2400. Text size, placement, colors, row centering, and insets align; the native spinner can show a different animation phase between captures.
- `LoadStateFooterUiStateTest` covers Loading, Error, and both NotLoading cases. `LoadStateFooterComposeTest` drives the real adapter in a RecyclerView and verifies Loading, Error, retry click, and NotLoading behavior.


## Fifth migrated component: Settings category card

**Candidate:** `app/src/main/res/layout/item_settings_category_card.xml`, previously inflated only by `SettingsHubFragment` for its visible category entries.

### Behavior contract

- `SettingsCategoryRegistry.categories` still determines list order, Firebase-gated Privacy visibility, and the authenticated Account entry. The Fragment retains the category icon map and navigation decisions: Account and Customize use their dedicated destinations; other categories keep the typed `categoryId` and legacy argument bridge.
- `SettingsCategoryCard` is stateless. It displays the existing title, summary, and icon resources, exposes the title as its content description, and presents a button-role click action. The screen state and navigation remain in `SettingsHubFragment`.
- The card uses the existing XML roles and dimensions: `colorSurfaceContainer`, zero elevation, 12dp corners, 16dp content padding and bottom spacing, a 40dp circular `colorSecondaryContainer` icon with 8dp inset, TitleSmall and BodySmall text, and a 24dp chevron with 4dp start spacing. `AniTrendTheme` now maps the existing `colorSurfaceContainer` and `titleSmall` XML tokens.
- The existing `NestedScrollView` remains the scroll owner. One uniquely identified ComposeView hosts the category Column inside `settings_sections` and uses `DisposeOnViewTreeLifecycleDestroyed`.

### Interoperability and removal boundary

- `SettingsHubFragment` still owns Firebase/auth visibility and destination routing. No navigation graph, registry, or category argument changes.
- `item_settings_category_card.xml` was removed after a source search found zero references. `fragment_settings_m3.xml`, the separate legacy section-card layout, and other Settings UI remain View-based.

### Baseline and tests

- Signed-out XML and Compose hub screenshots were captured on `Custom_API_36` at 1080×2400. The seven visible cards align in size, spacing, icons, typography, colors, and order.
- `SettingsCategoryRegistryTest` continues to cover category order and Firebase/auth rules. `SettingsHubComposeTest` covers card count/content descriptions/clickability and Customize, General, and Account navigation.


## Sixth migrated slice: Settings section and toggle row

**Candidates:** `app/src/main/res/layout/item_settings_section_card.xml` and its child `item_settings_row_switch.xml`, previously consumed by `CustomizeSettingsFragment` and `SettingsCategoryLegacyFragment`.

### Existing behavior and migration boundary

- The card shell is non-interactive. `CustomizeSettingsFragment` owns ViewModel-backed choice state; `SettingsCategoryLegacyFragment` resolves category visibility and reads/writes the existing `Settings` preferences. Both Fragments retain dialog callbacks and preference side effects.
- Section rows are Choice, Info, or Toggle. Choice rows keep their existing MaterialAlertDialog choices and accessible title/value description. Info rows stay disabled and non-clickable. Toggle rows preserve checked state, state-dependent summaries, the notification workaround's disabled state, and the existing on/off accessibility description.
- The user selected a fully Compose section UI rather than a Compose shell around AndroidView. `SettingsSectionCard` now owns the card/header/content tree. `SettingsValueRow`, `SettingsToggleRow`, and `SettingsDivider` render children directly in that Compose tree; there is no AndroidView or per-row ComposeView inside the card.
- A single lifecycle-safe ComposeView remains at the existing `fragment_settings_m3.xml` Fragment host boundary. The Fragments keep section visibility, state, dialogs, preference writes, navigation, and side effects. No routing or storage contract changes.
- The shared `settingsRowTitleStyle()` preserves the existing 22sp title line-height used by both value and toggle rows. The toggle row has a 56dp minimum touch target, Switch role/state semantics, an accessible on/off description, and no click action while disabled.
- The header uses `SettingsDivider`. Customize renders one header plus two row dividers (three total); Content renders one header plus three row dividers (four total). The old `section_content` ViewBinding measurement test is removed because the rows now share one Compose tree; semantic 1dp checks remain.
- `item_settings_section_card.xml` and `item_settings_row_switch.xml` were removed after a source search found no remaining references.

### Baseline and coverage

- XML section-card screenshots for Customize and Content were captured on `Custom_API_36` at 1080×2400. Compose captures were visually compared; card geometry, header, icons, typography, colors, row spacing, and divider placement aligned without material drift.
- The XML and Compose toggle rows were captured in the same enabled/off state with a summary and visually aligned. Temporary screenshot-capture resources and tests were removed after comparison.
- `SettingsSectionCardComposeTest` covers the header/card and divider counts in both owners. `SettingsToggleRowComposeTest` covers switch state, enabled/disabled semantics, accessibility descriptions, and persisted user interaction. `SettingsValueRowComposeTest` continues to cover Choice/Info rows and the existing choice dialog.
- `SettingsDividerComposeTest` continues to check displayed non-interactive dividers and 1dp height in both owners.
- `SettingsCategoryRegistryTest` verifies that Privacy toggle summaries follow the checked state.

## Compose build and test foundation

- Compose compiler plugin follows the existing Kotlin `2.4.10` version and is applied by the app build convention.
- Compose BOM is `2026.09.00`; `:app` enables Compose while retaining ViewBinding.
- The app includes Compose UI, Foundation, Material 3, Activity Compose, Preview tooling, and Android Compose UI test dependencies.
- AndroidTest host activities required by `ActivityScenario` live in the debug app APK, with debug-only IDs and manifest entries. This fixes the pre-existing AndroidTest compile failures without changing production variants.
- `ChangelogComposeTest` exercises the routed screen, title/version, Markdown body through the native View bridge, accessibility checks, and the Dismiss back-stack contract. The temporary baseline-capture test was removed after screenshots were saved outside the repository.

## Verification record

- Baseline before changes: `:app:compileAppDebugKotlin :app:assembleAppDebug` and full `./gradlew test` succeeded.
- XML baseline and Compose current screenshots were captured on the `Custom_API_36` emulator at 1080×2400 and visually compared. Header, icon, spacing, typography, colors, version, and rendered Markdown align. The transient XML ScrollView thumb was not reproduced by Compose's default `verticalScroll`; no custom scrollbar was added.
- Focused `ChangelogComposeTest`: 1 test, 0 failures.
- Before the sixth slice, `SettingsDividerComposeTest` had two tests, including a ViewBinding measurement tied to `section_content`. The host-measurement test was removed when the section content became a single Compose tree; the divider count and semantics test remains.
- `SettingsValueRowComposeTest`: 1 test, 0 failures. It verifies Choice and Info semantics, value/chevron visibility, the existing choice dialog, non-clickable disabled Info rows, and row heights of 56dp, 74dp, and 58dp.
- XML and Compose screenshots for both settings owners were captured on `Custom_API_36` at 1080×2400 and compared. Row divider pixel bands matched exactly after the local BodyLarge line-height adjustment; the visible values, chevrons, and Info rows align.
- Temporary instrumentation screenshots captured XML and Compose Loading/Error states before the capture scaffold was removed.
- Focused `LoadStateFooterComposeTest`: 1 test, 0 failures. It exercises the RecyclerView-hosted ComposeView, accessible retry click action, and state transitions.
- `SettingsHubComposeTest`: 3 tests, 0 failures. It exercises the signed-out category list and all three navigation branches.
- Full verification command: `./gradlew :app:spotlessCheck test :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon`.
- Last full verification before the seventh slice: `BUILD SUCCESSFUL` in 22m33. The connected suite reported 123 tests, 0 failures, 0 errors, 0 skipped; Spotless, both debug unit-test variants, and both debug assembly tasks passed.
- The first full run timed out `LoadStateFooterComposeTest` at its 1m Compose test deadline. Logcat showed `MainActivity` startup took 24.2s and skipped 626 frames; the adapter-only test now uses `ProgressLayoutTestActivity`. The focused test passed in 28.358s.
- Final full gate after that test-host change: `BUILD SUCCESSFUL` in 12m36; connected suite 125 tests, 0 failures, 0 skipped. `spotlessCheck` passed; both debug unit-test tasks and both debug assembly tasks were `UP-TO-DATE`.
- The initial eighth-slice full run reported four Espresso `RootViewPicker.RootViewWithoutFocusException`s. Afterward, the API 36 emulator displayed a Digital Wellbeing ANR dialog, and test logcat showed WindowManager contention. The dialog was dismissed and the emulator rebooted; no source changes were made before the retry.
- Final eighth-slice gate after reboot: `BUILD SUCCESSFUL` in 13m07. The connected suite reported 127 tests, 0 failures, and 0 skipped in 10m29. `spotlessCheck`, both debug unit-test tasks, and `assembleAppDebug` were `UP-TO-DATE`; `assembleGithubDebug` completed successfully.
- Gradle reports an existing ObjectBox configuration-cache serialization warning and existing deprecation warnings. They do not fail the build or tests.
- `FuzzyDateFieldComposeTest`: 1 test, 0 failures. It covers TBA, year-only, month-year, and full-date text, the 48dp target, accessible description, and picker callback.
- `FuzzyDateManageSheetComposeTest`: 1 test, 0 failures. It covers per-component seeding for year-only and month-year dates, date selection, Clear-to-current-date behavior, and unchanged committed dates after cancelling the sheet.
- XML and Compose field captures were compared both intrinsically and at the two-column date-card width. Icon, pill geometry, 48dp height, and spacing align. Compose reveals more of month/year and full-date strings than the legacy TextInputEditText clipped at the same width; the field remains single-line, and date model/persistence behavior is unchanged.
- `FuzzyDateWidgetBaselineCaptureTest` was a temporary screenshot scaffold and was removed after captures. A source audit found no `FuzzyDateWidget`, `WidgetFuzzyDateBinding`, or `widget_fuzzy_date.xml` references.

## Seventh migrated slice: Fuzzy date field

**Candidate:** `app/src/main/res/layout/widget_fuzzy_date.xml`, formerly inflated by `FuzzyDateWidget` and used twice in `BottomSheetSeriesManage` for the start and completion date fields.

### Existing behavior and migration boundary

- The former `FuzzyDateWidget` held a mutable `FuzzyDate?`. Tapping opened the platform `DatePickerDialog`, seeded each known date component independently, and used current date values for missing components. Clear wrote `(day = 0, month = 0, year = 0)`; `DateUtil.convertDate` rendered invalid dates as `TBA` and formatted partial year/month values.
- `BottomSheetSeriesManage` now owns `startedAtDate` and `completedAtDate` state. Its existing `MediaListDraft` copy remains the save boundary; cancelling the sheet does not mutate the committed `MediaList` entry. The platform picker and its Clear button remain unchanged in behavior.
- `FuzzyDateField` is a stateless Compose component with an accessible button description, 48dp minimum target, XML theme colors and calendar icon, and single-line BodyMedium value text. Two lifecycle-safe ComposeViews replace the date views at their original IDs. The sheet labels, field order, parent layout, save flow, and other sheet sections remain XML/View-based.
- `widget_fuzzy_date.xml` and the obsolete `FuzzyDateWidget` class were removed after the source reference audit. `FuzzyDate`, `DateUtil`, and GraphQL date persistence were not changed.

### Baseline and visual comparison

- XML component baselines for unset (`TBA`), year-only, month-year, and full dates were captured on `Custom_API_36` at 1080×2400. Compose captures were compared at both intrinsic and two-column sheet widths.
- Field geometry, calendar icon, pill shape, and touch target align. Compose reveals more of month/year and full-date strings than the legacy TextInputEditText clipped at the same two-column width. The new field remains single-line and accessible; date values, picker seeding, and Clear semantics are preserved.
- The temporary XML/Compose screenshot capture test was removed after the comparison.




## Eighth migrated slice: feed delete action

**Candidate:** `app/src/main/res/layout/widget_delete.xml`, formerly inflated by `StatusDeleteWidget` and included in `adapter_comment.xml`, `adapter_feed_message.xml`, `adapter_feed_progress.xml`, and `adapter_feed_status.xml`.

### Existing behavior and migration boundary

- `StatusDeleteWidget` remains the adapter-facing `FrameLayout`, immutable state owner, and confirmation boundary. The adapters still own visibility, pending-state mapping, and per-row callbacks; no mutation or persistence flow changed.
- `StatusDeleteAction` is stateless. It renders the same 18dp red vector or 18dp Material progress indicator, reuses the 6dp XML padding and theme primary spinner color, exposes a labelled button plus loading description, and reproduces the horizontal slide transition.
- The wrapper hosts the composable in a uniquely identified ComposeView with pool-aware disposal. It retains the existing DialogUtil positive Ok and Cancel flow, busy feedback, and render state API. A confirmation callback runs only while the listener captured at click time remains bound and the widget is attached and unrecycled; recycling clears that listener and resets state.
- `widget_delete.xml` and its generated `WidgetDeleteBinding` use were removed after a zero-reference audit. The four outer row layouts and the `StatusDeleteWidget` adapter boundary remain.

### Baseline, tests, and visual comparison

- XML idle, disabled, and loading captures were recorded on `Custom_API_36` at 1080×2400 before edits. Compose captures were taken in the same host after the migration.
- Idle and disabled captures align in icon size, red color, position, and resting background. Loading captures align in indicator size and primary color; the indeterminate arc phase differs between captures.
- `StatusDeleteWidgetComposeTest`: 2 tests, 0 failures. It checks idle/disabled/loading semantics, active confirmation callback delivery, and no confirmation or stale listener dispatch after recycling or rebinding.
- The temporary screenshot capture test was removed after comparison. Screenshot artifacts remain outside the repository.

## Ninth migrated slice: share type Spinner row

**Migrated candidate:** `app/src/main/res/layout/adapter_spinner_item.xml`, removed after a zero-reference audit. `SharedContentFragment` remains the owner of Spinner selection and `getItemSelected()` share conversion.

### Implementation and behavior

- `ShareTypeOptionRow` renders the localized label and mapped icon with the XML row's 12dp padding, 16dp icon, and 8dp gaps. `IconArrayAdapter` hosts it in reusable `ComposeView`s for selected and dropdown rows. Both row methods are overridden, so the required `ArrayAdapter` layout argument is `0`.
- The Spinner dropdown uses a separate `PopupWindow`. Its `PopupDecorView` lacked a `ViewTreeLifecycleOwner`, so Compose's window recomposer crashed while attaching a row. `getDropDownView()` installs the existing Fragment view owner on the popup root only when missing; each row `ComposeView` also receives the owner and disposes on detach or pool release.
- `ShareTypeSpinnerComposeTest` compares native XML text geometry, selects the trailing Video option through the real Spinner popup, waits for the popup to dismiss to one selected Compose label, then selects Picture. A diagnostic screenshot confirmed all five popup options are visible; the test avoids comparing `getGlobalVisibleRect` across popup/window coordinate spaces.
- The Fragment's five-position icon map, Spinner selection, and share conversion remain unchanged. `ShareTypeSpinnerHostTest` exercises the real share-sheet popup and selecting Picture.

### Baselines and comparison

- XML selected-row and five-option dropdown baselines: `/tmp/share-spinner-selected-xml.png` and `/tmp/share-spinner-dropdown-xml.png`.
- Compose selected-row and dropdown captures: `/tmp/share-spinner-selected-compose.png` and `/tmp/share-spinner-dropdown-compose.png`. Captures use `Custom_API_36` at 1080×2400; visual inspection found matching row geometry, typography, icon mapping, popup bounds, and background.
- `ShareTypeSpinnerHostTest` passed on `Custom_API_36` (1 test, 0 failures); it opens the actual share-sheet dropdown and verifies selecting Picture updates the Spinner position.

### Full verification gate

- Command: `./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon`. Result: `BUILD SUCCESSFUL`.
- `spotlessCheck`, `assembleAppDebug`, and `assembleGithubDebug` passed.
- Each debug unit-test variant ran 1,291 tests with 0 failures and 3 skipped.
- `connectedAppDebugAndroidTest` on `Custom_API_36` ran 129 tests with 0 failures and 0 skipped, including both Spinner tests.

### Re-ranked remaining used layouts

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `adapter_tag.xml` | 3/5 | One `TagAdapter` consumer in `MediaOverviewSection`; spoiler-colored tag name, rank, click and long-click callbacks, and `CardViewBase`. |
| 2 | `custom_tab.xml` | 4/5 | One `ProfileFragment` owner; programmatic tabs update the selected profile section, coupling the TabLayout to Profile state and content rendering. |

Empty generic frame resources are structural containers with no active Java/Kotlin inflation or binding caller. `custom_recycler_loading_footer.xml` has no `RecyclerStateFooterViewHolder` instantiation in source; these are not candidates for this slice.

## Tenth migrated slice: media tag row

**Migrated candidate:** `app/src/main/res/layout/adapter_tag.xml`, removed after a zero-reference audit. `CardViewBase` remains the RecyclerView interaction and surface root; `MediaOverviewSection` remains the state, dialog, and navigation owner.

### Implementation and preserved behavior

- `MediaTagRow` is a stateless Compose row with `@Preview`. `TagViewHolder` projects `MediaTag` to `MediaTagRowState` and updates one pooled `ComposeView` per recycled holder with `DisposeOnDetachedFromWindowOrReleasedFromPool`.
- The programmatic `CardViewBase` retains the original 16dp content padding, 8dp radius, compatibility padding, selectable foreground, click/focus target, and RecyclerView layout parameters. `TagAdapter` keeps root click and long-click listener ownership; `MediaOverviewSection` still opens `DialogUtil` and routes More through `onOpenTag`.
- The row preserves the 24dp loyalty icon and accent tint, 16dp icon gap, 8dp rank gap, single-line caption ellipsis, `%` rank formatting, primary accent for icon/rank, on-surface-variant normal text, and orange spoiler text. The Compose output clips `Mystery Story` to the XML width.
- `MediaOverviewSection.toMediaTag()` remains unchanged and supplies `isMediaSpoiler=false`; the screenshot's spoiler sample tests the adapter's supported `MediaTag` input branch, not the current GraphQL record value. The v2 `SuggestionChip`/`OutlinedButton` and details sheet were not adopted because they would change the v1 layout/action contract.
- Removed `adapter_tag.xml`, its generated `AdapterTagBinding` use, `SpoilerTagTextView`, and the unused spoiler styleable after zero-reference checks. `R.drawable.ic_loyalty_white_24dp` and `R.color.colorStateOrange` remain used elsewhere.
- `TagAdapterComposeTest` checks label/rank semantics, normal/accent/spoiler text colors, and name/rank non-overlap. `MediaOverviewTagInteractionTest` exercises the real section dialog and More callback.

### Baselines and comparison

- XML baseline: `/tmp/tag-adapter-xml-baseline.png`. Compose capture: `/tmp/tag-adapter-compose.png`. Both use `Custom_API_36` at 1080×2400 with controlled `TagAdapter` inputs.
- Visual inspection found matching card geometry, icon size/tint, caption layout and clipping, rank placement, normal text color, and spoiler orange.
- The temporary capture test was removed; screenshot artifacts remain outside the repository.

### Full verification gate

- Command: `./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon`. Result: `BUILD SUCCESSFUL`.
- `spotlessCheck`, `assembleAppDebug`, and `assembleGithubDebug` passed.
- Each debug unit-test variant ran 1,291 tests with 0 failures and 3 skipped.
- `connectedAppDebugAndroidTest` on `Custom_API_36` ran 131 tests with 0 failures and 0 skipped, including `TagAdapterComposeTest` and `MediaOverviewTagInteractionTest`.
- Gradle discarded the configuration cache because ObjectBox's `:app:objectboxPrepareBuild` task cannot serialize a `DefaultProject` instance; the build still exited successfully.

### Candidate ranking after the tenth slice

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `widget_profile_about_panel.xml` | 3/5 | One `AboutPanelWidget` in `ProfileOverviewSection`; three accessible stat cells and existing unit/instrumentation coverage. The widget retains stat state and sheet/navigation actions. |
| 2 | `widget_avatar_indicator.xml` | 3/5 | One MainActivity navigation-header owner; a custom Glide-backed `AvatarImageView` and unread-count indicator add image interop risk. |
| 3 | `widget_profile_stats.xml` | 4/5 | One `ProfileFragment` header owner; four stat cells with count formatting, Snackbars, and media-list callbacks. |
| 4 | `widget_vote.xml` | 4/5 | Two review item layouts; rating state, in-flight mutations, recycling, and instrumentation coverage increase risk. |
| 5 | `widget_favourite.xml` | 4/5 | Four feed/comment adapter layouts; favorite/loading transitions have high consumer fan-out. |

`widget_toolbar_favourite.xml` is a distinct higher-fan-out action-view layout used by four detail fragments.

### Lazy-list host cutover

- `MediaOverviewSection` now renders `TagGrid` directly in `tagsComposeView`, a two-column `LazyVerticalGrid` using the existing `grid_list_x2` resource. The ComposeView keeps the section's visibility toggle (`tagItems` non-empty) and the existing dialog, More, and browse-callback ownership.
- `TagGridComposeTest` passed 2/2 in the full connected run on `emulator-5554` (`Custom_API_36`) covering name/rank text semantics and non-overlap; the bounds query uses unmerged text nodes so measurements target the text nodes instead of the merged clickable card. `MediaOverviewTagInteractionTest` continues to cover the overview tag click dialog and browse callback (1/1).
- After checking all `app/src` references, `TagAdapter` and its adapter-host test `TagAdapterComposeTest` were removed; no adapter consumers remain.


## Eleventh migrated slice: Profile tab strip

**Migrated candidate:** `app/src/main/res/layout/custom_tab.xml`, included by `fragment_profile.xml` and configured by `ProfileFragment.setupSections()`. The XML layout is deleted after the usage audit.

### Contract and ownership

- The tabs remain Overview, Progress, and Status. `ProfileFragment.section` remains the selected-state owner, defaults to Overview, restores/saves `KEY_SECTION`, maps selected indices to `Section`, and calls `renderSection()`. ProfileOverviewSection, ProfileFeedSection, and their navigation callbacks remain Fragment-owned and unchanged.
- `ProfileSectionTabs` is a stateless Material 3 component. It keeps the AppBarLayout host height, centers equal-width scrollable cells while they fit, and allows horizontal scrolling for wider localized labels or compact hosts. It uses the existing `labelLarge`, primary/on-surface-variant roles, 12dp `mg_margin` label padding, and the XML indicator width/color contract.
- `fragment_profile.xml` keeps the `profile_sections` view-binding id and `?attr/actionBarSize` height, replacing the include with a `ComposeView`. The Fragment sets `DisposeOnViewTreeLifecycleDestroyed` and uses `AniTrendTheme`.
- The four-destination profile selector in the separate v2 feature is not adopted; this migration preserves the existing three local Profile sections.
- `ProfileTabComposeTest` hosts the real ProfileFragment and checks all three labels, selected semantics, and callback-driven selection transitions. RED on the original XML implementation was `Overview` not displayed in the Compose semantics tree; the focused post-migration test passed.

### Visual baseline

- `/tmp/profile-tabs-xml-baseline.png` and `/tmp/profile-tabs-compose-baseline.png` were captured on `Custom_API_36` at 1080×2400, with Overview selected and Progress/Status unselected. Both captures use the Profile AppBarLayout background and the same ComposeView height as the production host.
- Direct image inspection and text/indicator pixel bounds show matching typography, color, and layout. The three text glyph masks match in pixel area; the final text bounds differ by at most 3 physical pixels and the 3dp indicator bounds by at most 2 pixels at this capture size.
- The temporary screenshot-capture instrumentation test was removed after comparison.

### Full verification gate

- Command: `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Result: `BUILD SUCCESSFUL`.
- `spotlessCheck`, `assembleAppDebug`, and `assembleGithubDebug` passed.
- Each debug unit-test variant ran 1,291 tests with 0 failures and 3 skipped.
- `connectedAppDebugAndroidTest` on `Custom_API_36` ran 132 tests with 0 failures and 0 skipped, including `ProfileTabComposeTest`.

### Resource audit

- Repository-wide search found no `custom_tab`, `CustomTabBinding`, or `smart_tab` source consumers after the host replacement; only historical inventory notes remained before this update.
- `custom_tab.xml` and its generated `CustomTabBinding` are no longer used. The `profile_sections` binding remains for the ComposeView host.

## Twelfth migrated slice: profile about stats

**Migrated candidate:** `app/src/main/res/layout/widget_profile_about_panel.xml`, removed after a zero-reference audit. `AboutPanelWidget` remains the adapter-facing `FrameLayout` and stat/action owner; `ProfileOverviewSection` remains the profile screen and navigation owner. The outer `fragment_user_about.xml` and its `FragmentUserAboutBinding` remain unchanged.

### Implementation and preserved behavior

- `AboutPanelStatsRow` is a stateless three-cell Compose row with `@Preview`, equal column weights, localized button descriptions, a 48dp minimum click target, and the XML spacing/color roles. Counts still use `WidgetState.valueFormatter`.
- `AboutPanelWidget` hosts the row in a pool-aware `ComposeView`. It retains the `NotLoaded`/`Failed`/`Loaded` click mapping, five-minute user-stat reset, loading toast, loaded-zero list-sheet behavior, and favourites/user callbacks. The unused lifecycle argument to `setUserId` and the XML child-ID click path were removed.
- `AniTrendTheme` maps `labelMedium` and `titleMedium` to the existing condensed font family and XML-compatible font padding. Label weight is Normal to match the XML capture.
- `AboutPanelWidgetComposeTest` checks accessible labels, formatted counts, favourites navigation, and displayed-count persistence after reattachment. `AboutPanelWidgetInstrumentationTest` checks loaded-zero/nonzero follower/following sheets and NotLoaded/Failed no-sheet behavior. Focused results: 2 Compose tests and 5 interaction tests passed with 0 failures each.
- The AvatarIndicator reattachment coverage lives in `AboutPanelWidgetComposeTest` (displayed-count persistence after reattachment) and `AvatarIndicatorComposeTest` (launcher/avatar rendering and badge counts); the legacy `EventBusMutationStateGuardTest` and AboutPanel ViewBinding/reflection tests were replaced by these Compose contracts and removed.

### Baselines and comparison

- XML baseline: `/tmp/about-panel-xml-baseline.png`. Compose capture: `/tmp/about-panel-compose-font-padding.png`. Both were captured on `Custom_API_36` at 1080×2400 with Following 36, Followers 1,200 (`1.2 K`), and Favourites 0.
- Side-by-side inspection shows matching cell widths, label/count baselines, colors, and parent geometry after matching XML font padding. Pixel comparison of the stat-row crop found a 0.455/255 mean channel difference; 0.46% of pixels exceeded 12/255, limited to glyph rasterization.
- The temporary screenshot-capture test was removed; image artifacts remain outside the repository.

### Full verification gate

- Command: `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Result: passed.
- `spotlessCheck`, `assembleAppDebug`, and `assembleGithubDebug` passed.
- Each debug unit-test variant ran 1,291 tests with 0 failures and 3 skipped.
- `connectedAppDebugAndroidTest` on `Custom_API_36` ran 133 tests with 0 failures and 0 skipped, including both About panel Compose/integration test classes.

### Remaining candidate ranking

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `widget_avatar_indicator.xml` | 3/5 | One MainActivity navigation-header owner; a custom Glide-backed `AvatarImageView` and unread-count indicator add image interop risk. |
| 2 | `widget_profile_stats.xml` | 4/5 | One `ProfileFragment` header owner; four stat cells with count formatting, Snackbars, and media-list callbacks. |
| 3 | `widget_vote.xml` | 4/5 | Two review item layouts; rating state, in-flight mutations, recycling, and instrumentation coverage increase risk. |
| 4 | `widget_favourite.xml` | 4/5 | Four feed/comment adapter layouts; favorite/loading transitions have high consumer fan-out. |

`widget_toolbar_favourite.xml` remains a distinct higher-fan-out action-view layout used by four detail fragments.

## Program-scope checkpoint after the twelfth slice

At this checkpoint, the Changelog screen and twelve Compose components (five Settings components, one Paging component, one media-list component, one feed action, one share-type Spinner row, one media tag row, one profile tab strip, and one profile about panel) were migrated; the research plan remained incomplete. The initial shortlist was exhausted, and `widget_avatar_indicator.xml` was selected as the next candidate. The subsequent sections below record the later migration slices. Broader work still includes other feature surfaces, stateful screen migrations, adaptive layouts, Navigation 3 cutover after destination contracts are preserved, and final View/XML theme retirement after a global consumer audit. Continue each screen through the XML-to-Compose skill's candidate, analysis, baseline capture, Preview, test, usage replacement, and zero-reference removal steps.

## Thirteenth migrated slice: avatar indicator

**Migrated candidate:** `app/src/main/res/layout/widget_avatar_indicator.xml`, removed after a zero-reference audit. `custom_nav_header.xml` retains `drawer_avatar_indicator` as the `ComposeView` host; `AvatarIndicatorView`, its generated binding, and `dot_indicator.xml` are removed.

### Implementation and preserved behavior

- `MainActivity` owns `AvatarIndicatorState`. Its existing `MainViewModel.state` collector calls `updateUI()` on success, and `setupUserItems()` supplies the current avatar URL and unread count. `onAvatarClicked()` remains the login, notifications, and profile navigation owner; it hides the unread badge before opening notifications. The host uses `DisposeOnViewTreeLifecycleDestroyed`.
- `AvatarIndicator` is a stateless Compose component with `@Preview`. The avatar image stays on the existing Glide path through `AvatarImageView.setImage`; `AndroidView.onRelease` clears the Glide target through `AvatarImageView.onViewRecycled()`. The image subtree receives only image inputs, separate from unread-badge state.
- The badge retains the XML `colorPrimary` and `colorOnPrimary` roles, 20dp circle, 10sp bold decimal count, and hidden-at-zero behavior. `AvatarIndicatorComposeTest` checks button semantics, callback, unread counts 2 and 10, hide/zero transitions, and ComposeView reattachment. The pre-migration Compose contract failed on the View-only layout; the post-migration focused test passed.

### Baselines and comparison

- XML captures: `/tmp/avatar-indicator-xml-empty.png` and `/tmp/avatar-indicator-xml-unread.png`. Compose captures: `/tmp/avatar-indicator-compose-empty.png` and `/tmp/avatar-indicator-compose-unread.png`. All captures are 1080×2400 on `Custom_API_36`.
- The 360×360 avatar crop is pixel-identical for both empty and unread states after a 0.5dp absolute text offset: mean channel difference 0/255 and zero pixels above 12/255.
- The temporary Compose screenshot-capture test was removed after comparison; the image artifacts remain outside the repository.

### Full verification gate

- Command: `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Result: passed.
- `spotlessCheck`, both debug unit-test variants, connected instrumentation, and both debug APK assemblies passed.
- Each debug unit-test variant ran 1,290 tests with 0 failures and 3 skipped.
- `connectedAppDebugAndroidTest` on `emulator-5554` (`Custom_API_36`) ran 133 tests with 0 failures and 0 skipped, including `AvatarIndicatorComposeTest` and 11 `MainActivityExternalIngressTest` cases.

### Resource audit

- An `app/src` search for `AvatarIndicatorView`, `WidgetAvatarIndicatorBinding`, `widget_avatar_indicator`, `R.id.notification_count`, and `dot_indicator` returned no matches.

### Remaining candidate ranking

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `widget_profile_stats.xml` | 4/5 | One `ProfileFragment` header owner; four stat cells with count formatting, Snackbars, and media-list callbacks. |
| 2 | `widget_vote.xml` | 4/5 | Two review item layouts; rating state, in-flight mutations, recycling, and instrumentation coverage increase risk. |
| 3 | `widget_favourite.xml` | 4/5 | Four feed/comment adapter layouts; favorite/loading transitions have high consumer fan-out. |



## Fourteenth migrated slice: profile stats row

**Migrated candidate:** `app/src/main/res/layout/widget_profile_stats.xml`, removed after a zero-reference audit. `ProfileFragment` keeps `profile_stats_widget` as the `ComposeView` host with its existing background and AppBar scroll behavior; `ProfileStatsWidget`, `WidgetProfileStatsBinding`, and the legacy stat-cell IDs are removed.

### Implementation and preserved behavior

- `ProfileFragment` owns the nullable `UserStatisticsRecord` state, updates it from `ProfileViewModel.loadStats` in `viewLifecycleOwner.lifecycleScope`, and clears it on `onDestroyView`. The `ComposeView` uses `DisposeOnViewTreeLifecycleDestroyed`.
- `ProfileStatsRow` is a stateless Compose component with `@Preview`. Four equal-width cells retain the XML labels/content descriptions, button semantics, 48dp minimum target, 8dp vertical padding, 2dp label/value gap, theme colors, and typography. `ProfileFragment` still owns media-list navigation and the anime-time/chapter Snackbars.
- Formatting remains unchanged: no stats shows `..`; loaded zero totals show `0`; zero time/chapter values show `..`; total counts abbreviate at 1,000, manga chapters abbreviate above 1,000, and anime watch time retains hour/day/year thresholds. The stat-row UI test covers placeholders, counts, all duration units, accessible click targets, media-list callbacks, and Snackbar values. `ProfileTabComposeTest` verifies the Fragment host exposes the stat action.
- `ProfileStatsRow` values use `FontWeight.Normal` for `titleMedium` after screenshot comparison showed the XML `MaterialTextView` glyphs rasterized lighter than the default Compose weight.

### Baselines and comparison

- XML captures: `/tmp/profile-stats-xml-placeholder.png` and `/tmp/profile-stats-xml-loaded.png`. Compose captures: `/tmp/profile-stats-compose-placeholder.png` and `/tmp/profile-stats-compose-loaded.png`. All are 1080×2400 on `Custom_API_36`.
- In the 1080×200 row crop at y=1100–1300, the placeholder state differs by 0.09/255 mean channel intensity (197 of 216,000 pixels above 12/255); the loaded state differs by 1.09/255 (2,239 of 216,000). Remaining differences are confined to value glyph rasterization and 1px cell-centering rounding; labels, background, and row geometry match without clipping.
- Both temporary screenshot-capture tests and their emulator-side files were removed after comparison. PNG artifacts remain outside the repository for review.

### Full verification gate

- Command: `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Final result after a clean `Custom_API_36` reboot: passed.
- Each debug unit-test variant ran 1,290 tests with 0 failures and 3 skipped. Connected instrumentation ran 134 tests with 0 failures and 0 skipped, including `ProfileStatsRowComposeTest` and `ProfileTabComposeTest`; `spotlessCheck` and both debug APK assemblies passed.
- The first full-gate attempt hit Espresso `RootViewWithoutFocusException` in three popup/spinner tests. `dumpsys activity lastanr` recorded a MainActivity focus-event timeout, while WindowManager/logcat showed launcher/system-server ANRs and high system CPU pressure. A clean no-snapshot emulator boot removed the stale ANR windows; the full rerun passed.

### Resource audit and next candidate

- An `app/src` search for `ProfileStatsWidget`, `WidgetProfileStatsBinding`, `widget_profile_stats`, and all legacy stat-cell IDs returned no matches. The `profile_stats_widget` ID remains only as the Fragment's ComposeView host.

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `widget_favourite.xml` | 4/5 | Four feed/comment adapter layouts; favorite/loading transitions have high consumer fan-out. |

## Fifteenth Compose slice: vote row

**Selected candidate:** `app/src/main/res/layout/widget_vote.xml` (current rank 1, complexity 4/5). Two review-card layouts share the single `ReviewAdapter` owner: `adapter_review.xml` for browse/banner cards and `adapter_series_review.xml` for media-review cards. `BrowseReviewFragment` and `MediaReviewSection` collect the same rate-result contract; `widget_favourite.xml` remains next because it has four distinct feed/comment consumer layouts.

### Candidate audit and XML baselines

- `ReviewAdapter` is the sole production `VoteWidget` owner. It forwards `(reviewId, ReviewRating?)` actions and maps mutation outcomes through `ReviewHolderRegistry`, whose tests cover holder rebinding and recycling. `BrowseReviewViewModelRateTest` protects canonical store outcomes; the legacy `VoteWidgetInstrumentationTest` behavior is now covered by Compose adapter tests.
- Each vote control shows `rating` for up votes and `max(ratingAmount - rating, 0)` for down votes. Tapping the active direction sends `NO_VOTE`; an inactive direction sends that vote. A logged-out click shows `info_login_req`; clicking a currently loading direction shows `busy_please_wait`. Mutation failures show their message, either outcome resets both controls, and a 10-second Handler fallback clears stale loading. Counts change only on canonical record rebind.
- The XML control is two 18dp icons inside clickable/focusable flippers, each padded 8dp, with 16dp between them. Text uses bold Material3 BodyMedium (`sans-serif-condensed`). Inactive icons use `colorGrey600` (or white in banner mode); active up/down icons use `colorStateGreen`/`colorStateOrange`; spinner color uses `colorAccent`, mapped to the app's M3 primary. The old view has no direction content descriptions or selected semantics.
- XML state captures are `/tmp/vote-xml-unselected.png`, `/tmp/vote-xml-up-selected.png`, `/tmp/vote-xml-down-selected.png`, and `/tmp/vote-xml-banner.png`, captured at 1080×2400 on `Custom_API_36` before implementation changes. The separate indeterminate spinner capture is not used for pixel comparison because its displayed frame is time-dependent.

### Compose implementation and preserved behavior

- `ReviewVoteRow` is stateless and includes `@Preview`. It exposes separate button roles, localized up/down descriptions, selected state, and a described compact loader. Its typography, colors, dimensions, 18dp icon/indicator, and count spacing map the existing XML values; down-vote counts remain clamped at zero.
- `ReviewAdapter.VoteController` owns the bound `ReviewRecord`, per-direction loading state, result Toast, login/busy Toasts, and existing 10-second fallback. It preserves non-optimistic ratings, routes outcomes through the stable holder registry, and resets state on rebind/recycle. Both review layouts keep `review_vote` as the `ComposeView` host with pool-aware disposal.
- `BrowseReviewFragment` and `MediaReviewSection` retain their `rateReviewEvents` collectors and existing action callback signature. `ReviewVoteRowComposeTest` covers accessibility labels, selected state, counts including zero, and the 18dp busy indicator. `ReviewAdapterVoteComposeTest` covers both item layouts, rating transitions, mutation results, the logged-out gate, and holder recycling.

### Visual comparison

- Compose captures are `/tmp/vote-compose-unselected.png`, `/tmp/vote-compose-up-selected.png`, `/tmp/vote-compose-down-selected.png`, and `/tmp/vote-compose-banner.png`. XML and Compose captures are 1080×2400 on `Custom_API_36`.
- Side-by-side review of the stable 480×200 row crop (x=300–780, y=1100–1300) found mean channel deltas of 2.58–3.42/255 and 2.47–2.49% of pixels above 12/255. Icon silhouettes, colors, count spacing, banner treatment, and row placement match without clipping; remaining differences are edge rasterization.
- The XML indeterminate-loader frame is excluded from pixel comparison because its displayed frame varies over time; Compose semantics and the 18dp indicator size are covered by the component test.

### Resource audit

- An `app/src` search for `VoteWidget`, `WidgetVoteBinding`, `widget_vote`, and the legacy vote child IDs returned no matches. The thumb drawables remain used by `ReviewVoteRow`.
- `WidgetSpinnerDimensionInstrumentationTest` now covers only the separate `widget_toolbar_favourite.xml` action-view spinner; `FavouriteActionComposeTest` asserts the feed/comment favorite indicator size. Vote indicator sizing remains covered by `ReviewVoteRowComposeTest`.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed successfully.
- Each debug unit-test variant ran 1,287 tests with 0 failures and 3 skipped. Connected instrumentation on `emulator-5554` ran 140 tests with 0 failures and 0 skipped, including 3 `ReviewVoteRowComposeTest` cases and 6 `ReviewAdapterVoteComposeTest` cases.
- `assembleAppDebug` and `assembleGithubDebug` produced `app-debug.apk` and `github-debug.apk`.

## Sixteenth Compose slice: favorite action

**Selected candidate:** `app/src/main/res/layout/widget_favourite.xml` (rank 1, complexity 4/5). Its four hosts are `adapter_comment.xml`, `adapter_feed_message.xml`, `adapter_feed_progress.xml`, and `adapter_feed_status.xml`. `FeedListAdapter` is constructed by `FeedListFragment`, `MediaFeedSection`, `ProfileFeedSection`, and `CommentFragment`; `CommentListAdapter` owns reply rows. The legacy `FeedAdapter` has no constructor callsite in `app/src`, but still binds the shared layouts, so it will be updated rather than deleted. `widget_toolbar_favourite.xml` and `FavouriteToolbarWidget` remain a separate toolbar action-view used by four detail fragments and are out of scope.

### Candidate audit and XML baselines

- Before this migration, `FavouriteWidget` was render-only: owners passed `FavouriteWidgetState(count, isLiked, isEnabled, isLoading)` and owned the toggle callback. `FeedListViewModel` and `CommentViewModel` supplied canonical liked/pending state; `CommentFragment` forwarded feed and reply actions. The Compose control remains presentational and reports clicks to these owners.
- The XML renders a single 18dp heart/count row with 4dp padding, bold Material3 BodyMedium (`sans-serif-condensed`), grey inactive and red active hearts, and an accent-colored 18dp spinner. Disabled state keeps the heart tint and applies disabled text color. The `ViewFlipper` is clickable/focusable; the old control has no favorite-action description or selected semantics. Recycle clears the listener and blocks stale clicks.
- XML baselines are `/tmp/favourite-xml-unliked.png`, `/tmp/favourite-xml-liked.png`, `/tmp/favourite-xml-disabled.png`, and `/tmp/favourite-xml-loading.png`, captured at 1080×2400 on `Custom_API_36` before implementation changes. The XML capture test passed and was removed after capture. The indeterminate loading frame is excluded from pixel comparison because its displayed arc varies over time.

### Compose implementation and owner integration

- `FavouriteAction` is stateless, includes `@Preview`, and receives count, liked, enabled, and loading state. It preserves the XML icon, tint, count formatting, 18dp spinner, 4dp padding, and horizontal loading transition; it adds localized action descriptions, button role, selected semantics, and a described loading indicator.
- `FavouriteActionController` installs the composable in the existing row `ComposeView`, uses `DisposeOnDetachedFromWindowOrReleasedFromPool`, forwards clicks to the existing adapter callbacks, and clears stale callbacks/state on recycle. Its neutral state remains enabled like the legacy view, while the recycled guard still blocks stale clicks.
- `FeedListAdapter`, `CommentListAdapter`, and the unreferenced legacy `FeedAdapter` now bind the same Compose control. The four parent layouts already had `ComposeView` hosts at current HEAD, so their host tags did not need replacement; the obsolete nested `widget_favourite.xml` was the View that was removed. ViewModel mutation, error/outcome, and pending-reset/timeout ownership is unchanged.
- `FavouriteActionComposeTest` covers canonical count/liked/enabled semantics and pending progress. `FavouriteActionAdapterComposeTest` covers feed/reply callback IDs, pending/canonical rebinds, and recycled-holder behavior. Both focused suites passed: 2 component tests and 3 adapter tests, with 0 failures.

### Visual comparison

- Compose captures are `/tmp/favourite-compose-unliked.png`, `/tmp/favourite-compose-liked.png`, `/tmp/favourite-compose-disabled.png`, and `/tmp/favourite-compose-loading.png`; XML and Compose captures are 1080×2400 on `Custom_API_36`.
- The stable crop `(x=300..780, y=1100..1300)` has mean RGB deltas of 0.612, 0.655, and 0.356/255 for unliked, liked, and disabled states; 0.543%, 0.562%, and 0.544% of crop pixels exceed a 12/255 channel delta. The heart/count bounds differ by at most 2px, with no visible color, spacing, or clipping mismatch.
- The loading capture is excluded because the indeterminate arc is time-dependent; the component test verifies the compact indicator size and loading semantics.

### Resource audit

- A source search for `FavouriteWidget`, `FavouriteWidgetState`, `WidgetFavouriteBinding`, and `R.layout.widget_favourite` returned no matches. No source-set copy of `widget_favourite.xml` remains. The four feed/comment layouts retain their `ComposeView` hosts.
- `FavouriteWidgetStateTest`, the temporary XML screenshot harness, `FavouriteWidget.kt`, and `widget_favourite.xml` were removed. `widget_toolbar_favourite.xml`, `FavouriteToolbarWidget`, and its dimension test remain separate and unchanged. Generic `widget_flipper`/`widget_like` IDs remain used by other widgets.

### Next candidate shortlist

| Rank | Layout | Complexity | Current contract and risk |
|---|---|---:|---|
| 1 | `widget_toolbar_favourite.xml` | 4/5 | Separate toolbar action-view, not a feed/comment like count. `FavouriteToolbarWidget` maps canonical `FavouriteFlag` and loading state; `CharacterFragment`, `MediaFragment`, `StaffFragment`, and `StudioFragment` own four menu-action callsites. Preserve menu visibility, initial-store fallback, callbacks, busy feedback, and toolbar lifecycle. Audit this path independently; do not assume the row component is interchangeable. |

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, then `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both completed without reported violations or task failures.
- Each debug unit-test variant ran 1,285 tests with 0 failures and 3 skipped. Connected instrumentation on `emulator-5554` ran 144 tests with 0 failures and 0 skipped, including all 2 `FavouriteActionComposeTest` and 3 `FavouriteActionAdapterComposeTest` cases.
- `assembleAppDebug` and `assembleGithubDebug` produced `app-debug.apk` and `github-debug.apk`.

## Seventeenth Compose slice: toolbar favorite action-view

**Selected candidate:** `app/src/main/res/layout/widget_toolbar_favourite.xml` (rank 1, complexity 4/5). `custom_menu.xml`, `media_base_menu.xml`, and `staff_menu.xml` instantiate `FavouriteToolbarWidget` with `app:actionViewClass` and `showAsAction="always"`. The four owners are `CharacterFragment`, `MediaFragment`, `StaffFragment`, and `StudioFragment`.

### Candidate audit and XML baselines

- The fragments keep the action hidden for unauthenticated users. `CharacterFragment`, `MediaFragment`, and `StaffFragment` use `settings.isAuthenticated`; `StudioFragment` uses `StudioViewModel.isAuthenticated()` and hides the action if its view is missing. Each owner maps the canonical `FavouriteFlag` and loading flow through `FavouriteWidgetRenderState.fromFlag`, with the initially loaded entity flag as fallback. All four clear their toggle callback when the fragment view is destroyed.
- The four detail ViewModels route toggles through `ToggleFavouriteInteractor`, expose committed `FavouriteFlag` state, and clear `favouriteLoading` in `finally`. Existing ViewModel tests cover typed request routing, loading reset, and canonical-store convergence; interactor/repository tests cover failed requests. No fragment or ViewModel code changed.
- Before migration, the custom action view showed a 24dp filled/outline icon in a 48dp clickable/focusable ViewFlipper with 12dp padding and a compact 18dp accent spinner. The drawable tint resolves to toolbar `titleColor`; spinner color resolves to `colorAccent`. A click before state arrives shows `text_activity_loading`; an in-flight click shows `busy_please_wait`; otherwise the callback is forwarded.
- XML baselines are `/tmp/favourite-toolbar-xml-unliked.png`, `/tmp/favourite-toolbar-xml-liked.png`, and `/tmp/favourite-toolbar-xml-loading.png`, captured at 1080×2400 on `Custom_API_36` with `custom_toolbar.xml` and `media_base_menu.xml`. The indeterminate loading arc is excluded from pixel comparison.

### Compose implementation and preserved owners

- `FavouriteToolbarAction` is stateless and includes `@Preview`. It preserves the 24dp icon, 12dp clickable padding, 18dp loading indicator, theme tint, and horizontal loading transition. It adds localized add/remove descriptions, button role, selected state, and a described loader.
- `FavouriteToolbarWidget` remains as the minimal `MenuItem.actionViewClass` interoperability host. Its `WidgetToolbarFavouriteBinding` child layout was removed; it creates a pool-aware `ComposeView` and installs content only after the action view attaches. `EntryPointRenderAuthTest` exposed AppCompat measuring this menu action before attachment; the wrapper now defers composition, hides on detach, and restores it after attach. The existing `render`, `setOnToggleAction`, toast, and recycle APIs remain unchanged.
- `FavouriteToolbarActionComposeTest` covers add/remove semantics and callback delivery, plus loading, busy-click non-dispatch, and the 18dp indicator. Both tests passed. `EntryPointRenderAuthTest` passed after the attachment fix (1 test, 0 failures). `FavouriteWidgetRenderStateTest` and the four ViewModel suites remain the state and request-owner contracts.

### Visual comparison

- Compose node captures are `/tmp/favourite-toolbar-compose-unliked.png`, `/tmp/favourite-toolbar-compose-liked.png`, and `/tmp/favourite-toolbar-compose-loading.png`. Stable Compose node images are 144×144; the matching XML action-view crop is `(x=645..789, y=24..168)` from the full-screen baselines.
- The unliked and liked crops have mean per-channel deltas of 0.003 and 0.002/255, 0% of pixels over 12/255, and maximum channel delta 1/255. The toolbar surface color, icon geometry, tint, and state match. The loading capture is excluded because the indeterminate arc varies over time; the Compose test asserts loader semantics and 18dp sizing.

### Resource audit and next candidate ranking

- Source searches found no `WidgetToolbarFavouriteBinding`, `R.layout.widget_toolbar_favourite`, `R.id.widget_like`, or `@+id/widget_like` references. No source-set copy of `widget_toolbar_favourite.xml` remains. The `FavouriteToolbarWidget` wrapper remains intentionally because the three menu resources instantiate it as their action view.
- `WidgetSpinnerDimensionInstrumentationTest` and the temporary baseline-capture harness were removed; compact spinner sizing is now covered by `FavouriteToolbarActionComposeTest`. `FavouriteWidgetRenderStateTest` remains because its immutable store-to-render mapping is still used by the four owners.
- The next low-complexity metadata-row shortlist is `adapter_genre.xml` (rank 1, complexity 2/5, one `GenreAdapter` owner in `MediaOverviewSection`), `adapter_link.xml` (rank 2, complexity 2/5, one `LinkAdapter` owner in `MediaStatsSection`), and `adapter_studio.xml` (rank 3, complexity 2/5, a conditional favorite marker and two `StudioAdapter` owners in `FavouriteFragment` and `SearchFragment`). Revalidate the shortlist during the next candidate audit.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both completed without reported violations or task failures.
- Each debug unit-test variant ran 1,285 tests with 0 failures and 3 skipped. Connected instrumentation ran 145 tests with 0 failures and 0 skipped; `FavouriteToolbarActionComposeTest` passed 2 tests and `EntryPointRenderAuthTest` passed `renderEntryPoints`.
- `assembleAppDebug` and `assembleGithubDebug` produced `app-debug.apk` and `github-debug.apk`.


## Eighteenth Compose slice: genre adapter row

**Selected candidate:** `app/src/main/res/layout/adapter_genre.xml` (rank 1, complexity 2/5 from the remaining metadata-row shortlist). Alternatives retained for later ranking are `adapter_link.xml` and `adapter_studio.xml`.

### Candidate audit and XML baseline

- `GenreAdapter` is the sole production adapter owner, used by `MediaOverviewSection`. The section shows `genre_recycler` only when `displayData.genres` is non-empty, uses a two-column vertical `StaggeredGridLayoutManager`, and forwards root-container clicks to `onOpenGenre`; long-click is intentionally a no-op. `Genre` contributes only its nullable `genre` label; `GenreAdapter.onBindViewHolder` does not read `isSelected` or mutate entity state.
- The XML row is a clickable/focusable `CardViewBase` with a selectable foreground, 8dp radius, 16dp content padding, a 24dp label-outline icon tinted by `colorAccent`, 16dp icon-to-label spacing, and `SingleLineTextView` styled as `Base.TextAppearance.AppCompat.Caption`. The RecyclerView host adds an 8dp margin. `CardViewBase` applies compatibility padding and keeps the existing MaterialCardView surface treatment.
- No existing `GenreAdapter` unit or instrumentation tests were found. The baseline `/tmp/adapter-genre-xml.png` was captured at 1080×2400 on `Custom_API_36` with two production `GenreAdapter` rows (`Slice Of Life`, `Adventure`) in the same two-column layout manager and 8dp host margin.


### Compose implementation and adapter behavior

- `MediaMetadataRow` is a shared stateless icon-label composable with `@Preview` and a memoized caption mapping. `GenreAdapter` uses it for the label-outline row; both adapters retain the established `CardViewBase` shell, selectable foreground, and `ItemClickListener` ownership.
- `GenreAdapterComposeTest` covers accessible text exposure and forwards the existing genre click to `MediaOverviewSection.onOpenGenre`. Both focused tests passed. The `MediaOverviewSection` visibility, data owner, and no-op long-click behavior are unchanged.

### Visual comparison

- The Compose card capture is `/tmp/adapter-genre-compose-row.png` (516×186), compared with the corresponding first-card crop `(x=24..540, y=24..210)` from `/tmp/adapter-genre-xml.png`. The row-content crop `(x=35..430, y=60..140)` has mean per-channel delta 2.67/255 and 3.37% of pixels over 12/255. The icon bounds and mean color match exactly; the caption bounds differ by 1px horizontally/vertically with nearly identical mean color.
- `drawToBitmap` does not include the card's external hardware shadow; the unchanged `CardViewBase` retains its production surface/elevation/foreground. No visible content clipping or color change remains.

### Resource audit and next candidate ranking

- Searches for `AdapterGenreBinding`, `R.layout.adapter_genre`, `R.id.genre_name`, and `@+id/genre_name` returned no source references. `adapter_genre.xml`, `GenreXmlBaselineCaptureTest`, and `GenreComposeCaptureTest` were removed after the zero-reference audit. `GenreAdapter` remains as the RecyclerView owner; `CardViewBase` and `ic_label_outline_white_24dp` remain used.
- After `adapter_genre.xml`, the scoped metadata-row shortlist ranks `adapter_link.xml` first (complexity 2/5, one `LinkAdapter` owner in `MediaStatsSection`) and `adapter_studio.xml` second (complexity 2/5, two `StudioAdapter` owners and a conditional favorite marker). Revalidate both during the next candidate audit.


### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed without reported violations or task failures.
- Unit reports: `:app:testAppDebugUnitTest` and `:app:testGithubDebugUnitTest` each ran 1,285 tests with 0 failures and 3 skipped.
- Connected instrumentation: 147 tests, 0 failures, 0 skipped on `emulator-5554` (`Custom_API_36`). `GenreAdapterComposeTest` passed both cases.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

### Lazy-list host cutover

- `MediaOverviewSection` now renders `GenreGrid` directly in `genre_grid`, using a two-column `LazyVerticalGrid` with the existing `grid_list_x2` resource and XML margin. Its viewport is bounded because the ComposeView remains inside the section's `NestedScrollView`.
- `GenreGridComposeTest` passed 2/2 in the full connected run on `emulator-5554` (`Custom_API_36`), covering two-column placement, text semantics, and click routing to the owner.
- After checking all `app/src` references, `GenreAdapter` and its adapter-host test `GenreAdapterComposeTest` were removed; no adapter consumers remain.

## Nineteenth Compose slice: external link row

### Candidate audit and XML baseline

- `adapter_link.xml` remains the highest-ranked unconverted metadata row after `adapter_genre.xml` (complexity 2/5). `LinkAdapter` has one production owner, `MediaStatsSection`. The section shows `linksRecycler` only when `externalLinks` is non-empty and lays rows out in a two-column vertical `StaggeredGridLayoutManager`; the RecyclerView host adds `@dimen/lg_margin` (8dp).
- `ExternalLink` carries nullable `url` and `site`; the row displays only `site`. Its clickable/focusable `CardViewBase` uses the selectable foreground, 16dp content padding, 8dp corners, and compatibility padding. The row is a 24dp `ic_link_white_24dp` tinted by `colorAccent`, a 16dp gap, and a single-line `SingleLineTextView` using `Base.TextAppearance.AppCompat.Caption`.
- `MediaFragment` owns actions: a click with a non-null URL launches `ACTION_VIEW` for that URI; a long-click copies the URL through `ClipboardManager` and calls `showCopiedMessage()`. A null URL is ignored by both owner paths. `LinkAdapter` has no recycling cleanup beyond its no-op `onViewRecycled()`.
- No existing `LinkAdapter` or external-link interaction tests were found. The XML baseline `/tmp/adapter-link-xml-row.png` is a 516×186 `drawToBitmap` capture of the attached first row (`Twitter`) on `Custom_API_36`, after verifying the view is shown and measured. The initial shell `screencap` artifact was blank, so the view-level capture is the usable baseline; it excludes external hardware shadow, which remains owned by the unchanged `CardViewBase`.
- The next candidate remains `adapter_studio.xml` (complexity 2/5, two `StudioAdapter` owners and a conditional favorite marker). The link row stays first because it has one owner and only static icon/label content; revalidate the shortlist after this slice.


### Compose contract and pre-migration RED

- `LinkAdapterComposeTest` checks the `site` label in Compose semantics. Before production edits, both tests failed at `assertIsDisplayed()` because the XML `TextView` does not expose `Twitter` in the Compose semantics tree; the rendered XML row is present in the saved baseline.
- Observed after migration: Compose `performTouchInput` on the nested text semantics node did not invoke the enclosing native card listener, while down/up `MotionEvent`s dispatched through the attached item view did. The final tap test uses the Android view-dispatch path; the long-click test invokes the native item's `performLongClick`. The `CardViewBase` remains the single click/long-click action owner.

### Shared Compose implementation and adapter behavior

- `MediaMetadataRow` now backs both `GenreAdapter` and `LinkAdapter`. It retains the shared icon size, accent tint, 16dp icon gap, single-line caption style and ellipsis. `LinkAdapter` displays `ExternalLink.site.orEmpty()`; `url` remains exclusively in the existing owner callbacks. The Preview shows both genre and external-link variants.
- `LinkAdapter` constructs the same `CardViewBase` root programmatically, sets `R.id.container`, and hosts `MediaMetadataRow` in a `ComposeView` using `DisposeOnDetachedFromWindowOrReleasedFromPool`. Its click/long-click handlers still call `RecyclerViewHolder.performClick`/`performLongClick`; `MediaStatsSection` and `MediaFragment` action owners are unchanged. `LinkViewHolder` is private; the symbol search found no external references.
- Focused results on `Custom_API_36`: `LinkAdapterComposeTest` passed 2/2; `GenreAdapterComposeTest` passed 2/2 after sharing the row.

### Visual comparison and resource audit

| Measure | XML baseline | Compose capture | Difference |
|---|---:|---:|---|
| Row capture size | 516 × 186 px | 516 × 186 px | Same bounds |
| Icon bounds | `(60, 78)-(120, 108)` | Same | Exact |
| Caption bounds | `(175, 81)-(285, 107)` | `(175, 82)-(285, 108)` | Compose glyphs 1px lower |
| Caption ink mean RGB | `(95.2, 91.6, 100.8)` | Same | Same mean |
| Mean per-channel delta | n/a | n/a | 0.447/255 across the row; 1.357/255 in content crop `(35, 60)-(430, 140)` |
| Pixels with any RGB delta over 12/255 | n/a | n/a | 0.601% across the row; 1.826% in the content crop |
| Maximum channel delta | n/a | n/a | 173, confined to caption antialiasing pixels |

- Both captures are `drawToBitmap` images of the same attached `CardViewBase` item; the external hardware shadow is excluded. The native wrapper still owns production elevation, surface, and selectable foreground. The 1px caption offset is the only geometric difference; icon bounds and caption color are unchanged.
- The pre-delete source audit found no consumers of `AdapterLinkBinding`, `R.layout.adapter_link`, `R.id.link_text`, or `@+id/link_text` beyond the adapter/layout being migrated. Those resource/binding searches returned no matches after deletion; `LinkViewHolder` had no external references and is private.
- Removed `adapter_link.xml`, `LinkXmlBaselineCaptureTest.kt`, and `LinkComposeCaptureTest.kt` after comparison. The permanent `LinkAdapterComposeTest` remains. Temporary emulator captures and host PNGs were removed.
- After removing `adapter_link.xml`, `adapter_studio.xml` is the next candidate in the scoped metadata-row shortlist (complexity 2/5, two `StudioAdapter` owners, conditional favorite marker); revalidate it during the next audit.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed without reported violations or task failures.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 149 | 0 | 0 |

- `LinkAdapterComposeTest` passed 2/2 and `GenreAdapterComposeTest` passed 2/2 in the connected suite on `emulator-5554` (`Custom_API_36`).
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

### Lazy-list host cutover

- `MediaStatsSection` now renders `LinkGrid` directly in `links_compose_view`, using a two-column `LazyVerticalGrid` with the existing `grid_list_x2` resource and XML margin. Its viewport is bounded because the ComposeView remains inside the section's `NestedScrollView`. Tap still opens the link URL and long-press still copies it to the clipboard before the owner callback.
- `LinkGridComposeTest` passed 2/2 in the full connected run on `emulator-5554` (`Custom_API_36`), covering site text semantics, tap forwarding to the open owner, and long-press forwarding to the copy owner.
- After checking all `app/src` references, `LinkAdapter` and its adapter-host test `LinkAdapterComposeTest` were removed; no adapter consumers remain. `MediaOverviewTagInteractionTest` migrates the overview tag click to a Compose row click while keeping its dialog and browse-callback assertions.

## Twentieth Compose slice: studio adapter row

### Candidate audit and XML baselines

- `adapter_studio.xml` is the remaining item in the scoped metadata-row shortlist (complexity 2/5). `StudioAdapter` serves `FavouriteFragment.Section.STUDIOS` and `SearchFragment.Section.STUDIO`; both owners use a two-column vertical `StaggeredGridLayoutManager` and route `StudioBase` clicks to `navigateToStudio(StudioScreenParam(id))`.
- `StudioBase` exposes `id`, nullable `name`/`siteUrl`, and `isFavourite`. `StudioAdapter` binds only `name` and toggles the favorite marker from `isFavourite`; the marker is decorative, has no content description, and is not an action. The adapter binds root clicks but no long-click listener; both owner long-click paths are no-ops for studios. `onViewRecycled()` has no cleanup work.
- The XML root is a clickable/focusable `CardViewBase` with `R.id.container`, selectable foreground, and an 8dp item margin. `CardViewBase` supplies 16dp content padding, 8dp corners, and compatibility padding. The inner horizontal row gives the single-line `studio_name` all remaining width and conditionally shows a 16dp star with 8dp start spacing, tinted `colorStateYellow`. `SingleLineTextView` enables one line and end ellipsis; the XML sets no explicit `textAppearance`. The attached XML text probe reported 14sp, zero letter spacing, font padding enabled, and color `#1d1b20`.
- No `StudioAdapter`/favorite-marker tests were found. Before edits, `/tmp/adapter-studio-xml-not-favourite.png` (`Studio One`) and `/tmp/adapter-studio-xml-favourite.png` (`Studio Two`) were captured as 492×171 `drawToBitmap` images on `Custom_API_36`, using the production two-column adapter and both `isFavourite` states. The native `CardViewBase` is retained in the migration, so the captures omit only its external hardware shadow.


### Compose contract and pre-migration RED

- `StudioAdapterComposeTest` checks Compose semantics for both studio names, asserts exactly one favorite-marker test tag across one non-favorite and one favorite model, and verifies tapping the native card dispatches the studio ID to its existing owner.
- Before production edits, both tests failed at `assertIsDisplayed()` for `Studio One` because the XML `TextView` is not exposed in the Compose semantics tree. The Compose marker and click assertions run after that check and remain the post-migration contract.


### Compose implementation and adapter behavior

- `StudioRow` is stateless and previews both favorite states. It uses the XML-probed 14sp `bodyMedium` role, zero letter spacing, Android font padding, `onSurface` text, and a conditional decorative star. The Compose star keeps the same 16dp box, 8dp start gap, yellow tint, and no content description.
- `StudioAdapter` now builds its `CardViewBase` root programmatically with the original 8dp RecyclerView margins, clickable/focusable state, and selectable foreground. A pool-safe `ComposeView` hosts `StudioRow`; the holder updates the name and `isFavourite` state and retains native root click routing. Both Fragment owners and their no-op studio long-click paths are unchanged.
- `StudioAdapterComposeTest` passed both tests on `Custom_API_36` after the typography adjustment.

### Visual comparison

| State | XML name bounds | Compose name bounds | XML star bounds | Compose star bounds | Mean row delta | Pixels over 12/255 |
|---|---|---|---|---|---:|---:|
| Not favorite | `(56, 70)-(257, 103)` | Same | n/a | n/a | 0.021/255 | 0% |
| Favorite | `(56, 70)-(262, 103)` | Same | `(396, 67)-(432, 102)` | `(396, 68)-(432, 103)` | 0.112/255 | 0.256% |

- Both states measure 492×171px. The star's Compose antialiasing bounds are 1px lower; mean tint and text bounds match. Maximum channel delta is 201 in the favorite icon's antialiased pixels. Both captures are view-level `drawToBitmap` images and exclude the external hardware shadow; `CardViewBase` retains the production surface and elevation.

### Resource audit and next candidate ranking

- Before deletion, searches found `AdapterStudioBinding`, `R.layout.adapter_studio`, `R.id.studio_name`, `R.id.studio_favourite`, `@+id/studio_name`, and `@+id/studio_favourite` only in the layout/old adapter references. After removing the XML, those searches returned no matches. `StudioViewHolder` has no external references and is private.
- Removed `adapter_studio.xml` and the temporary XML/Compose capture and style-diagnostic tests after comparison; the temporary emulator and host PNGs are also cleaned. `StudioAdapterComposeTest` remains; `ic_star_grey_600_24dp`, `colorStateYellow`, and `CardViewBase` remain used.
- The scoped metadata-row shortlist is exhausted. Re-rank the remaining production-used XML layouts during the next candidate audit rather than assuming a winner from the previous shortlist.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed without reported violations or task failures.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 151 | 0 | 0 |

- `StudioAdapterComposeTest` passed 2/2 on `emulator-5554` (`Custom_API_36`). The full connected suite also includes the two Link and two Genre row tests from earlier slices.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

## Twenty-first Compose slice: shared group-title row

### Candidate audit and XML baselines

- `adapter_entity_group.xml` is the lowest-UI-complexity remaining shared row: a 23-line `FrameLayout` with a caption title aligned start and an optional count aligned end. The root has an 8dp item margin; both `SingleLineTextView`s use `TextAppearance.AppCompat.Caption`. `GroupTitleViewHolder` binds `RecyclerHeaderItem.getTitle()` and `size`; it hides the count for sizes below one and has no click or long-click action.
- The shared holder has nine construction sites across eight adapters: `CharacterAdapters`, `MediaCharacterAdapter`, `MediaStaffRoleAdapter`, two classes in `StaffAdapters`, `GroupCharacterAdapter`, `GroupCharacterStaffAdapter`, `GroupSeriesAdapter`, and `GroupStaffRoleAdapter`. All consume the same `RecyclerHeaderItem` title/count contract. `GroupCharacterAdapter` and `GroupCharacterStaffAdapter` set header rows full-span; those adapter layout-manager policies remain outside the shared row.
- Existing `GroupingUtilTests` cover header generation/title/count behavior; no UI tests for `GroupTitleViewHolder` or `adapter_entity_group.xml` were found. The next alternatives are riskier: `adapter_ranking.xml` uses `RichMarkdownTextView`, dynamic HTML, and two click targets; `adapter_log_entry.xml` exposes selectable messages with level-dependent colors; staff/character rows include loaded images and favorite state. The shared group header was selected despite nine inflations because one existing holder owns a small, non-interactive visual contract.
- Matching XML baselines captured on `Custom_API_36` through `GroupCharacterAdapter`: `/tmp/group-title-xml-counted-latest.png` (`Adaptations`, count 20) and `/tmp/group-title-xml-zero-count-latest.png` (`1984`, count hidden), both 1032×49px view captures.

### Compose contract and pre-migration RED

- `GroupTitleComposeTest` exercises the shared title/count row through `GroupCharacterAdapter`: it checks a formatted title, a visible positive count, a zero-count header title, and the absence of the `0` count node.
- Before production edits, the test failed at `assertIsDisplayed()` for `Adaptations`; the XML `TextView` was rendered but absent from the Compose semantics tree. The failure isolates the missing Compose row semantics.

### Compose implementation and owner behavior

- `GroupTitleRow` uses a start-aligned title and end-aligned positive count; zero counts are omitted. It has a Preview for both states. `GroupTitleViewHolder` hosts it in a pool-safe root `ComposeView`, binds `getTitle()` and `size` as state, and keeps the 8dp RecyclerView item margins. The nine callers now pass their parent view group; full-span policies stay in their existing adapters.
- The Caption mapping is centralized as `mediaCaptionTextStyle()` and reused by `GroupTitleRow`, `MediaMetadataRow`, and its Genre/Link callers. No click or long-click handler was added; group headers remain non-interactive.
- `GroupTitleComposeTest` passed its title, positive-count, and zero-count semantics contract after migration. `GroupingUtilTests` remain unchanged.

### Visual comparison

| State | XML capture | Compose capture | Pixel difference |
|---|---|---|---|
| Counted header | 1032×49px | 1032×49px | Mean per-channel delta 0/255; 0% over 12/255; maximum 0 |
| Zero-count header | 1032×49px | 1032×49px | Mean per-channel delta 0/255; 0% over 12/255; maximum 0 |

- Compared the matching XML and Compose `drawToBitmap` captures of the same full-span row, aligned 1:1. Both states match pixel-for-pixel; the Compose output preserves the XML title/count positions and zero-count visibility.

### Resource audit and next candidate ranking

- Before deletion, source grep found no `AdapterEntityGroupBinding`, `R.layout.adapter_entity_group`, `R.id.catalog_header_title`, or `R.id.catalog_header_count` consumers; the XML was the only definition of those child IDs. After XML removal, the resource/binding search still returned no matches. `GroupTitleViewHolder` remains with nine updated parent-view callsites.
- Removed `adapter_entity_group.xml` and temporary XML/Compose capture tests after comparison; emulator and host PNGs were cleaned. `GroupingUtilTests` remain the permanent data-grouping coverage.
- The group-header leaf is removed from the candidate set. Re-rank the remaining production layouts during the next audit; no next candidate is assumed.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed without reported violations or task failures.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 152 | 0 | 0 |

- `GroupTitleComposeTest` passed 1/1 on `emulator-5554` (`Custom_API_36`).
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

## Twenty-second Compose slice: log-entry row

### Candidate audit and XML baselines

Ranked the remaining simple adapter rows by hierarchy, state, owner fan-out, and external UI dependencies:

| Rank | XML layout | Complexity | Custom/external UI dependencies | Owner and risk |
|---|---|---:|---|---|
| 1 | `adapter_log_entry.xml` | 2/5 | None; two MaterialTextViews | One adapter and one screen owner; five severity colors and per-message selection |
| 2 | `adapter_giphy.xml` | 3/5 | `BrandImageView`, Glide | One bottom-sheet owner; remote image loading, click/long-click, and recycle cleanup |
| 3 | `adapter_ranking.xml` | 4/5 | `CardViewBase`, `AppCompatTintImageView`, `RichMarkdownTextView` | One stats-section owner; dynamic HTML and two click targets |
| 4 | `adapter_character.xml` | 4/5 | `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView` | Two adapters; loaded image, favorite state, and click routing |
| 5 | `adapter_staff.xml` | 4/5 | `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView` | Five adapters; loaded image, favorite state, and several owner callbacks |

- The legacy `LogEntryAdapter`/`adapter_log_entry.xml` pair was replaced by `LogEntryList` over `List<LogEntry>`. It renders `LogEntry.level.name` and `message`; the five levels map to red, orange, blue, accent, and grey resources. `LoggingFragment` retains reverse list layout and ViewModel-backed filtering. `LogEntryComposeListTest` covers rendered text, reverse order, level colors, state replacement, and row semantics; `LoggingFragmentTest` covers parsing, filters, and clearing.
- Captured the XML row with each of the five `LogEntry.Level` values before production edits using the production adapter on `Custom_API_36`. The `drawToBitmap` captures are each 1080×81px; message selection remains scoped to one row, matching the existing per-TextView behavior.

### Compose contract and adapter behavior

- Before implementation, the Compose UI contract failed because the XML MaterialTextViews for `ERROR` and `select this log` were not exposed as Compose semantics nodes.
- `LogEntryRow` is stateless over severity, message, and holder-owned `SelectionState`. It resolves Body1 text color from the active XML theme's `android:textColorPrimary`, reuses `mediaCaptionTextStyle()` for bold Caption labels, maps the same five color resources, and baseline-aligns the label with the message.
- `LogEntryComposeListTest` covers rendered text, reverse ordering, level-color pixels, and state replacement on `emulator-5554` (`Custom_API_36`). `LogEntryList` keeps selection per row through the native `SelectionState`; cross-row selection is not added, following the prior AniTrend decision.
- Only the message is inside `SelectionContainer`, whose standard Copy toolbar is documented by [Android Compose text selection guidance](https://developer.android.com/develop/ui/compose/text/user-interactions). The permanent test exercises selection state; it does not separately click the system Copy action.
- The row-only phase hosted `LogEntryRow` in a pool-safe root `ComposeView` with match-parent/wrap-content RecyclerView layout params; the logging list-host cutover replaced that adapter entirely, and `LogEntryList` now renders the rows directly. `LoggingFragment` retains reverse-list layout, filtering, and data ownership; the row has no click or long-click callbacks.
- The screen renders the app's live log file: `FileLoggerTree` appends startup and stack-trace lines during tests, and `LogParser` accumulates non-header lines onto the preceding entry's message. `LoggingFragmentTest` therefore asserts the fixture entries by substring after scrolling them into view rather than exact row totals or exact message equality; its targeted run passed 7/7 on `emulator-5554`.

### Visual comparison

| States | XML and Compose size | Mean RGB delta | Pixels over 12/255 | Maximum RGB delta |
|---|---|---:|---:|---:|
| ERROR, WARNING, INFO, DEBUG, VERBOSE | 1080×81px each | 0/255 | 0% | 0 |

- Compared the `drawToBitmap` output of each production-adapter row with its pre-edit XML baseline at 1:1. Every RGBA pixel matched in all five states, including level colors, Body1 text color, caption baseline, message start position, and vertical padding.

### Resource audit and next candidate ranking

- Before deletion, source search found no `AdapterLogEntryBinding`, `R.layout.adapter_log_entry`, `R.id.log_level`, or `R.id.log_message` callers outside the migrated adapter/layout. After deleting `adapter_log_entry.xml`, the resource and child-ID search returned no matches. `LoggingFragment` now renders `LogEntryList`; `LogEntryAdapter` is removed.
- Removed the temporary `LogEntryComposeCaptureTest` and its host/emulator PNG artifacts after comparison. `LogEntryComposeListTest` and `LoggingFragmentTest` are the permanent behavior coverage; the legacy RecyclerView adapter test was deleted after its tests moved to these Compose contracts.
- Re-rank the remaining production-used layouts in the next candidate audit; the shortlist above is not treated as a permanent order after the log row is removed.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Formatting and the full gate completed successfully.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 154 | 0 | 0 |

- `LogEntryAdapterComposeTest` passed 2/2. `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- The first full connected run failed with Espresso focus errors while the emulator displayed “Process system isn't responding” and `dumpsys power` reported `mIsInteractive=false`. After choosing `Wait`, the `Custom_API_36` emulator returned to the launcher and the full gate passed on the retry.



## Twenty-third Compose slice: Giphy image row

### Candidate audit and XML baseline

| Rank | XML layout | Complexity | Custom/external UI dependencies | Owner and risk |
|---|---|---:|---|---|
| 1 | `adapter_giphy.xml` | 3/5 | One `BrandImageView`, Glide | One adapter owner; animated image request, click, long-click, and recycle cleanup |
| 2 | `adapter_episode.xml` | 4/5 | `WideImageView`, `SingleLineTextView`, Glide | Episode adapter in the channel-fragment family; title, duration, image, and two actions |
| 3 | `adapter_feed_slide.xml` | 4/5 | `WideImageView`, `AppCompatImageView`, Glide | Three feed/media owners; play overlay and image actions |
| 4 | `adapter_user.xml` | 4/5 | `AvatarImageView`, `SingleLineTextView`, `FollowStateWidget`, Glide | Three list/sheet callers; current-user gating and follow callback state |
| 5 | `adapter_ranking.xml` | 4/5 | `CardViewBase`, `AppCompatTintImageView`, `RichMarkdownTextView` | One stats-section owner; dynamic HTML and separate root/text click targets |

- `adapter_giphy.xml` is one `BrandImageView` root with a 4dp margin, clickable/focusable selectable foreground, a 3.3:4 measured image ratio, and a top-end `GIPHY` badge tinted from `colorOnSurface`. `GiphyAdapter` loads `preview_gif` or falls back to `original` with Glide cross-fade and center-crop, then clears the request on recycling.
- `BottomSheetGiphy` is the sole adapter owner. Three existing composer entry points open it: `CommentFragment`, `SharedContentFragment`, and `BottomSheetComposer`. Tap selects the Giphy and closes the sheet; long-press opens `GiphyPreviewActivity` with `downsized_large`. The sheet/ViewModel continue to own search, paging, loading, and errors.
- Before edits, no Compose image-loader dependency or adapter-row test existed. `GiphyPreviewActivityTest` covered only preview URL parsing, and the XML row had no `contentDescription`; the migrated Compose row now uses `Giphy.title` or localized `title_giphy_preview` fallback.
- Captured the pre-edit XML row through the production `GiphyAdapter` on `Custom_API_36`, using local `reg_bg.jpg` through the same Glide path in a three-column `StaggeredGridLayoutManager`. The `drawToBitmap` baseline is 336×277px at `/tmp/giphy-xml-baseline/xml-giphy (1).png`.

### Compose contract and adapter behavior

- The pre-implementation RED showed that the XML row exposed neither `Giphy.title` nor the localized `title_giphy_preview` as accessibility content. `GiphyAdapterComposeTest` now checks both title states and verifies the existing tap and long-press listener callbacks from one RecyclerView host.
- `GiphyResultRow` is stateless over its `Giphy` model and callbacks. It preserves `preview_gif` then `original` image selection and the existing Glide cross-fade/center-crop through `BrandImageView` in `AndroidView`, retaining the custom image ratio and GIPHY badge. The Compose constraints and `BrandImageView.onMeasure` now share the `GIPHY_IMAGE_WIDTH`/`GIPHY_IMAGE_HEIGHT` dimensions. The Compose row exposes the title, or localized fallback when the title is blank, as one button-role semantics node. Its `@Preview` uses local `reg_bg.jpg`.
- `GiphyAdapter` now hosts the row in a pool-safe `ComposeView`. Recycle clears the bound model; releasing the row clears the Glide target. `BottomSheetGiphy` remains the owner of item selection, preview navigation, search, paging, and loading/error state.

### Visual comparison

| Sample | XML pixels | Compose pixels | Mean RGBA delta | Changed pixels | Maximum channel delta |
|---|---:|---:|---:|---:|---:|
| Local `reg_bg.jpg` image row | 336×277 | 336×277 | 0/255 | 0/93,072 (0%) | 0 |

- Compared the `BrandImageView.drawToBitmap()` result in the production Compose adapter with the pre-edit XML capture at 1:1. Every pixel matched, including the image crop, 3.3:4 geometry, and GIPHY badge.

### Resource audit and next candidate ranking

- Before deletion, source search found only `adapter_giphy.xml`'s `giphy_image` declaration. No source references to `AdapterGiphyBinding` or `R.layout.adapter_giphy` remained. After deleting the layout, searches for `adapter_giphy`, `AdapterGiphyBinding`, and `giphy_image` under `app/src` returned no matches.
- Removed the temporary `GiphyXmlBaselineCaptureTest` and `GiphyComposeCaptureTest` after comparing captures. `BrandImageView` remains because the Compose row uses its existing badge, measurement, and image rendering.
- With the selected row removed, `adapter_episode.xml` is now first in the preceding shortlist. Re-rank the production-used layouts again before selecting the next slice.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. Both commands completed successfully.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 155 | 0 | 0 |

- `GiphyAdapterComposeTest` passed 1/1 on `emulator-5554` (`Custom_API_36`).
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.


## Twenty-fourth Compose slice: Episode row

### Candidate audit and XML baseline

Re-ranked remaining production adapter rows, including simpler-looking character and feed-slide layouts, using current owner count, row state, custom-view dependencies and recycle/action risk:

| Rank | XML layout | Complexity | Custom/external UI dependencies | Owner and risk |
|---:|---|---:|---|---|
| 1 | `adapter_episode.xml` | 3/5 | `MaterialCardView`, `WideImageView`, `SingleLineTextView`, Glide | One live adapter/screen owner; image plus title/duration overlays, one link dialog action, and no stateful custom widget |
| 2 | `adapter_character_staff.xml` | 2/5 | `AspectImageView`, two `SingleLineTextView`s, `AppCompatTintImageView`, Glide | One live Staff roles owner, but a second source-level adapter still binds this XML and the live row sits in a paged/grouped ListAdapter |
| 3 | `adapter_ranking.xml` | 3/5 | `CardViewBase`, `AppCompatTintImageView`, `RichMarkdownTextView` | One stats-section owner; dynamic markup plus separate root/text click targets |
| 4 | `adapter_character.xml` | 2/5 | `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView`, Glide | Three screen families and two adapter implementations; favourite marker state |
| 5 | `adapter_feed_slide.xml` | 2/5 | `WideImageView`, `AppCompatImageView`, Glide | Adapter preview mode plus direct YouTube fragment host, content-type state and nested-list lifecycle |

- The XML hierarchy is a clickable `MaterialCardView` containing a full-width `WideImageView`, top-end duration badge and bottom title overlay. `EpisodeAdapter` binds the first thumbnail URL, title, and duration formatted as `m:ss`; null or malformed durations display `00:00` (`adapter_episode.xml:1-67; EpisodeAdapter.kt:25-30,49-57`). `WideImageView` uses the 0.95 measurement factor and `app_bar_height` fallback, Glide cross-fade/center-crop, and `reg_bg` when the URL is absent (`WideImageView.kt:18-79; KeyUtil.kt:15`).
- `WatchListFragment` is the only current `EpisodeAdapter` construction site (`WatchListFragment.kt:89-104`). The inherited `FragmentChannelBase` click listener checks `target.id == R.id.series_image`, opens the episode title/description dialog, then launches the link or shows the premium-show toast. The long-click callback is currently a no-op (`FragmentChannelBase.kt:299-329`). `R.id.series_image` is declared by other live layouts, so it remains available after this row XML is removed.
- The row has no dedicated instrumentation/UI coverage; `EpisodeUtil_GetActualTitleTest` covers only title parsing. The Compose contract will check displayed title/duration and that click/long-click reach the existing adapter listener with the preserved target ID.
- Captured the XML row through the production `EpisodeAdapter` on `Custom_API_36`, with local `reg_bg.jpg`, one `StaggeredGridLayoutManager` column, title `Compose migration baseline episode`, and duration `23:41`. The root `drawToBitmap` baseline is 1032×607px at `/tmp/episode-xml-baseline.png`.

### Compose implementation and behavior

- `EpisodeRow` in `ui/episode/EpisodeRow.kt` is stateless over the visible title, adapter-formatted duration, thumbnail URL and owner callbacks. It uses the existing theme `cardColor`, resource-backed corner/elevation/spacing tokens, the XML title gradient, and the original duration icon/badge. The title remains single-line because `SingleLineTextView` sets `isSingleLine=true` at runtime, despite the XML `maxLines="2"` attribute.
- The row retains `WideImageView` in `AndroidView` for its existing Glide cross-fade/center-crop request, fallback image and custom measure behavior. The `ComposeView` holder uses `DisposeOnDetachedFromWindowOrReleasedFromPool`; recycle clears row state and releases the Glide target. Its nested image keeps `R.id.series_image`, which `FragmentChannelBase` checks before opening the episode dialog. No WatchListFragment or callback owner changes were required.
- The permanent `EpisodeAdapterComposeTest` checks the title, formatted and malformed-duration states, and click/long-click delivery with the original target ID. The RED failed on the XML row's missing Compose semantics; the focused migrated test passed 1/1.

### Visual comparison

Compared equal-size PixelCopy captures from `Custom_API_36`: the XML side used a temporary `RecyclerView.Adapter` inflating `AdapterEpisodeBinding`, not the already-migrated production `EpisodeAdapter`; the Compose side used the production adapter. Both rendered the same local `reg_bg.jpg`, title and `23:41` duration at 1032×607px.

| Region | Pixels | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Image interior, away from overlays and card edges | 720×460 | 0/255 | 0/331,200 (0%) | 0 |
| Duration badge crop | 272×160 | 5.27/255 | 1,961/43,520 (4.51%) | 247 |
| Title/gradient crop | 1,032×107 | 4.28/255 | 3,694/110,424 (3.35%) | 200 |
| Full card | 1,032×607 | 1.75/255 | 10,287/626,424 (1.64%) | 247 |

- Side-by-side inspection shows matching crop, card dimensions, title and duration placement, badge shape and gradient. Remaining pixel changes cluster around TextView/Compose text and vector-icon antialiasing and rounded card edges; the image interior is pixel-identical.

### Resource audit and next candidate

- Before removal, the only `AdapterEpisodeBinding` source reference was the temporary XML capture harness; no production `R.layout.adapter_episode` callers remained. The migrated card reuses `R.id.series_image`, which is still declared by other live layouts and remains the fragment owner's click-target contract. `WideImageView`, `gradient_shadow.xml`, and `bubble_background.xml` retain other consumers.
- Removed `adapter_episode.xml`, `EpisodeXmlBaselineCaptureTest`, and `EpisodeComposeCaptureTest` after the XML/Compose comparison. Post-removal searches under `app/src` found no `AdapterEpisodeBinding`, `R.layout.adapter_episode`, or `adapter_episode.xml` references. The permanent `EpisodeAdapterComposeTest` remains.

- Added `episode_duration_padding` to preserve the XML badge's 10dp padding and `colorGradientShadowEnd` as the shared end color for `gradient_shadow.xml` and the Compose gradient. The existing XML drawable remains for its other layout consumers.
- The post-Episode audit confirmed `adapter_character_staff.xml` remains the lowest-risk candidate. `AdapterCharacterStaffBinding` is instantiated by the live `StaffCharacterRolesAdapter` in `StaffFragment.createAnimeRolesSection()` and by `GroupCharacterStaffAdapter`; searches under `app/src` found no callsites for `GroupCharacterStaffAdapter`. No existing unit or instrumentation tests cover the live staff-role row.

### Full verification gate

- The first connected run stopped after four tests and failed two existing focus-sensitive tests. After a clean no-snapshot emulator boot, the first isolated Spinner run stalled during activity teardown while logcat recorded long HWUI frames and system-process contention. The focused Spinner and search test classes then passed, followed by the complete gate. No source changes were made to address the transient failures.
- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. The final gate completed successfully.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 156 | 0 | 0 |

- `EpisodeAdapterComposeTest` passed 1/1 in the connected suite.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

## Twenty-fifth Compose slice: staff character role row

### Candidate audit

- The sole live construction is `StaffCharacterRolesAdapter` in `StaffFragment.createAnimeRolesSection()`. It displays `CharacterStaffBase` data in a paged `ListAdapter`; clicks navigate to the character, long-click remains unsupported, and the favorite marker follows `character.isFavourite`.
- The row contract is the character image with `AspectImageView`'s existing aspect-ratio, Glide cross-fade, and center-crop behavior; single-line character and media titles; and the top-end favorite marker. Nullable titles remain empty. Recycling clears Compose row state, and `AndroidView.onRelease` clears its Glide target.
- `GroupCharacterStaffAdapter` had a source declaration but no callsites under `app/src`; the live `StaffCharacterRolesAdapter` is the only production owner.
- The post-removal ranking favors one-owner rows over the slightly simpler multi-owner character card: `adapter_ranking.xml` leads, followed by `adapter_character.xml` and `adapter_feed_slide.xml`.
- Captured the production XML adapter row with a local `reg_bg` image and one favorite plus one standard state using the three-column detail-grid span. PixelCopy baselines are `/tmp/staff-character-xml-favorite.png` and `/tmp/staff-character-xml-standard.png`, each 360×621px.

### Compose implementation and behavior

- `StaffCharacterRoleRow` is stateless, uses the XML `cardColor`, 8dp corners and padding, 2dp elevation, and the shared AppCompat-caption mapping. It retains `AspectImageView` in `AndroidView` for the existing aspect ratio and Glide center-crop/cross-fade. The clickable card forwards through `R.id.container`; the star is shown only for favorite characters.
- `StaffCharacterRolesAdapter` hosts the row in a pool-safe `ComposeView`; recycling clears row state and `AndroidView.onRelease` clears the Glide target. `StaffFragment` remains the click/navigation owner. `StaffCharacterRolesComposeTest` passed 3/3 after the pre-migration RED exposed missing Compose titles, favorite semantics, and click forwarding.

### Visual comparison

Compared equal-size PixelCopy captures on `Custom_API_36` with the same local `reg_bg` image, titles, three-column grid, and favorite/standard states. The image crop and star placement are nearly pixel-identical; the remaining differences are concentrated in text and card-edge antialiasing.

| State or region | Pixels | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Favorite full row | 360×621 | 1.91/255 | 8,982/223,560 (4.02%) | 208 |
| Standard full row | 360×621 | 1.89/255 | 8,971/223,560 (4.01%) | 208 |
| Image and favorite overlay, both states | 296×418 | 0.005/255 | 6/123,728 (0.00%) | 159 |
| Caption and card bottom, both states | 312×147 | 0.95/255 | 1,734/45,864 (3.78%) | 152 |

- XML captures: `/tmp/staff-character-xml-favorite.png` and `/tmp/staff-character-xml-standard.png`. Compose captures: `/tmp/staff-character-compose-favorite.png` and `/tmp/staff-character-compose-standard.png`.

### Resource audit and next candidate

- Removed the unused `GroupCharacterStaffAdapter.kt`, `adapter_character_staff.xml`, and both temporary PixelCopy capture tests after source search found no `AdapterCharacterStaffBinding`, `R.layout.adapter_character_staff`, or `GroupCharacterStaffAdapter` references. `R.id.container`, `R.id.character_img`, and `R.id.favourite_indicator` remain used by other layouts; `R.id.character_name` and `R.id.media_title` have no remaining references.
- Re-ranked the strongest remaining production-used rows by hierarchy, owner fan-out, custom dependencies, state/actions, and existing test coverage:

| Rank | XML | Complexity | Live owners | Principal risk |
|---:|---|---:|---|---|
| 1 | `adapter_ranking.xml` | 3/5 | `RankAdapter` in `MediaStatsSection` | `RichMarkdownTextView`, root and text click targets, and root long-click |
| 2 | `adapter_character.xml` | 2/5 | `MediaCharacterAdapter` plus `GroupCharacterAdapter` across Media detail, Favorites, and Search | Glide image, favorite marker, and distinct click/action-mode behavior |
| 3 | `adapter_feed_slide.xml` | 2/5 | `ImagePreviewAdapter` and `YouTubeEmbedFragment` | Preview and YouTube fragment hosting/lifecycle |

- `adapter_ranking.xml` leads the next audit: one live owner and no image request state, with the rich-text and click contracts to verify before selection.

### Full verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`. The gate completed successfully.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 159 | 0 | 0 |

- `StaffCharacterRolesComposeTest` passed 3/3 in the connected suite.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

## Twenty-sixth Compose slice: ranking row

### Candidate audit and XML baseline

- The post-staff ranking selected `adapter_ranking.xml` for the next audit: one live `RankAdapter` owner in `MediaStatsSection`, with no image request state. The lower-hierarchy `adapter_character.xml` has two adapters across Media detail, Favorites, and Search; `adapter_feed_slide.xml` spans preview and YouTube fragment owners.
- `RankAdapter` binds `MediaRank.typeHtml` through `RichMarkdownTextView`. The type HTML varies by rank, context, season/year, and format; `RATED` rows use a star and `POPULAR` rows a heart. The view uses Markwon with HTML support, text selection, URL linkification, and `BetterLinkMovementMethod`.
- Both `container` and `sub_container` clicks reach `MediaStatsSection.onOpenRank`, which navigates to media browse using the rank filter. Root long-click reaches the section's no-op callback. The row remains a `CardViewBase` with 16dp content padding, 8dp radius, compatibility padding, and an AppCompat Caption text view. `MediaStatsSection` uses the `grid_list_x2` StaggeredGrid span; the RecyclerView has an 8dp margin. No existing tests cover `RankAdapter`.
- Captured rated (`#64 HIGHEST RATED SUMMER 2015 (Manga)`) and popular (`#1 MOST POPULAR (Anime)`) fixtures through production `RankAdapter` using the `grid_list_x2` span and the owner's 8dp RecyclerView margin. PixelCopy baselines are `/tmp/rank-xml-rated.png` and `/tmp/rank-xml-popular.png`, both 588×319px.

### Compose implementation and behavior

- `RankRow` keeps `RichMarkdownTextView` in `AndroidView` so Markwon HTML formatting, text selection, linkification, and `BetterLinkMovementMethod` remain unchanged. `TextViewCompat` applies the XML AppCompat Caption style; the Preview uses `HtmlCompat` in inspection mode, while runtime uses the existing Markwon extension.
- The Compose card uses the XML `colorSurfaceContainerLow` surface, 8dp radius, 2dp elevation, and 16dp content padding. Its outer 2dp horizontal and 3dp vertical insets reproduce `CardViewBase.useCompatPadding` with `preventCornerOverlap=false`. The title and heart/star icons retain their XML resources and dimensions.
- The initial row-only Compose step used `RankAdapter` to host each row in a pool-safe `ComposeView`; its root used `R.id.container`, and the embedded `RichMarkdownTextView` retained `R.id.sub_container`. Root click/long-click and text click continued through the original `MediaStatsSection` listener.
- That adapter-host version's `RankAdapterComposeTest` passed 2/2, covering rated/popular text, star/heart states, native selectable text, the root and `sub_container` click targets, and root long-click forwarding. The list-host cutover below replaces it with behavior tests against the native lazy grid.

### Visual comparison

Compared rated and popular PixelCopy captures from the production `RankAdapter` on `Custom_API_36`, using the same `grid_list_x2` host and fixture values as the XML captures. Both XML and Compose rows are 588×319px. Surface and rich-text samples match exactly; remaining changes are confined to vector icon rasterization and a few shadow-edge pixels.

| State or region | Pixels | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Rated full row | 588×319 | 0.073/255 | 320/187,572 (0.17%) | 198 |
| Popular full row | 588×319 | 0.102/255 | 268/187,572 (0.14%) | 199 |
| Card surface, both states | 360×35 | 0/255 | 0/12,600 (0.00%) | 0 |
| Rich text, both states | 290×135 | 0/255 | 0/39,150 (0.00%) | 0 |
| Star icon | 90×80 | 1.621/255 | 320/7,200 (4.44%) | 198 |
| Heart icon | 90×80 | 1.856/255 | 201/7,200 (2.79%) | 199 |

- XML captures: `/tmp/rank-xml-rated.png`, `/tmp/rank-xml-popular.png`. Compose captures: `/tmp/rank-compose-rated.png`, `/tmp/rank-compose-popular.png`.

### XML removal and next candidate

- Removed `adapter_ranking.xml` and the temporary XML/Compose PixelCopy capture tests after a search under `app/src` returned no remaining `adapter_ranking`, `AdapterRankingBinding`, `ranking_type`, or capture-test references. The focused behavior test was later migrated from `RankAdapterComposeTest` to `RankingsGridComposeTest`.
- Kept `R.id.sub_container` in `custom_ids.xml` because the `RankRow`-embedded `RichMarkdownTextView` and its focused Compose test still use it. The former `ranking_type` view ID had no remaining consumer.
- The post-removal audit selected `adapter_character.xml` for the next slice: two adapter owners (`MediaCharacterAdapter` and `GroupCharacterAdapter`) across Media detail, Favorites, and Search. The source-backed contract and baseline follow below.
- `adapter_feed_slide.xml` remains an alternative with `ImagePreviewAdapter` and `YouTubeEmbedFragment` owners plus Glide transformations and a play overlay. `adapter_user.xml` has filtering and follow-mutation state; `adapter_staff.xml` has broader adapter fan-out.

### Lazy-list host cutover

- `MediaStatsSection` now renders `RankingsGrid` directly in `ranking_compose_view`, using a two-column `LazyVerticalGrid` with the existing `grid_list_x2` resource and XML margin. Its viewport is bounded because the ComposeView remains inside the section's `NestedScrollView`.
- `RankingsGridComposeTest` passed 2/2 on `emulator-5554` (`Custom_API_36`), covering rated/popular formatting, icons, selectable rich text, click forwarding, and long-click semantics.
- Verification: `jenv exec ./gradlew :app:compileAppDebugKotlin :app:compileAppDebugAndroidTestKotlin` succeeded; `ANDROID_SERIAL=emulator-5554 jenv exec ./gradlew :app:connectedAppDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mxt.anitrend.widget.RankingsGridComposeTest` passed with 2 tests and 0 failures/errors/skips.
- After checking all `app/src` references, `RankAdapter` was removed; no adapter consumers remain.

### Full verification gate

- `:app:spotlessCheck` passed after `:app:spotlessApply` formatted the ranking adapter, row, and instrumentation test.
- `:app:testAppDebugUnitTest` and `:app:testGithubDebugUnitTest` each passed 1,285 tests with 0 failures and 3 skipped.
- `:app:connectedAppDebugAndroidTest` passed 161 tests with 0 failures and 0 skipped on `Custom_API_36(AVD)`.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` succeeded and produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed for all six Play Store changelog files; each is within the 500-character limit.
- Gradle reported a configuration-cache serialization warning for `:app:objectboxPrepareBuild`; the complete verification build still succeeded.

## Twenty-seventh Compose slice: character row

### Candidate audit and XML baselines

- The earlier candidate ranking placed `adapter_character.xml` immediately after `adapter_ranking.xml`. Its remaining alternatives carry broader fan-out or more state: `adapter_staff.xml` has five adapters, `adapter_user.xml` owns filtering and follow mutations, and `adapter_feed_slide.xml` has Glide transformations plus a YouTube fragment owner.
- `CharacterBase` supplies nullable `name`, `image`, and `isFavourite`. The row shows `name?.fullName` in `SingleLineTextView`, uses `AspectImageView` with `KeyUtil.AspectRatio` (`1.37f`), loads `ImageBase.extraLarge` or falls back to `large` through Glide center-crop with a 350ms cross-fade, and clears the image request on recycle. The top-end star appears only when `isFavourite` is true.
- The XML `MaterialCardView` uses the theme `cardColor`, 8dp corners, 2dp elevation, 8dp item margin, and a selectable root click target. The name is single-line and ellipsized. `MediaCharacterAdapter` receives the parent-owned character click; `GroupCharacterAdapter` preserves role headers and forwards `R.id.container` clicks. Media detail, Favorites, and Search all use the three-column `grid_giphy_x3` span. Their long-click owners do not add a character-row action.
- Before this slice, `GroupTitleComposeTest` covered group-header titles/counts only; no character-row test covered content, favorite state, image rendering, or actions.
- Captured the production `GroupCharacterAdapter` row with local `R.drawable.reg_bg` image fixtures and favorite/standard states on `Custom_API_36`. The three-column PixelCopy baselines are `/tmp/character-xml-favorite.png` and `/tmp/character-xml-standard.png`, each 328×536px. Both preserve the source image crop, card surface/shadow, ellipsized name, and star visibility.

### Ranked alternatives

Complexity scores are estimates from hierarchy, visible state, custom-view dependencies, and live owners; rank also weighs migration risk.

| Rank | Layout | Complexity | Custom/external dependencies | Rationale |
|---|---|---:|---|---|
| 1 | `adapter_character.xml` | 4/5 | `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView` | Two adapters share the same row across three surfaces; one favorite state and one root click. |
| 2 | `adapter_feed_slide.xml` | 4/5 | `WideImageView`, `AppCompatImageView`, Glide | Two-child leaf; `ImagePreviewAdapter` through three visible feed/comment adapter classes plus `YouTubeEmbedFragment`; multiple content transforms and host constraints. |
| 3 | `adapter_user.xml` | 4/5 | `AvatarImageView`, `SingleLineTextView`, `FollowStateWidget`, Glide | Three owners plus filtering, current-user context, follow mutation, and recycled-listener cleanup. |
| 4 | `adapter_media_header.xml` | 4/5 | `AspectImageView`, `SeriesStatusWidget`, `SeriesTitleView`, `SingleLineTextView` | One live `CharacterActorsAdapter` owner; `GroupActorAdapter` has no construction callsite. More custom fields and click/long-click behavior. |
| 5 | `adapter_staff.xml` | 4/5 | `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView`, Glide | Simple favorite card, but five adapter owners increase regression scope. |

- `adapter_latest_anime.xml` and `adapter_latest_manga.xml` remain a more complex alternative: two row variants with status, image, rating, title, airing/year text, and multiple click targets in one adapter.

### Compose implementation and behavior

- `CharacterRow` is stateless over the display name, resolved image URL, and favorite state. It retains `AspectImageView` in `AndroidView` to preserve the XML image ratio and Glide request behavior; `onRelease` clears the target. `Card(onClick)` provides the root button semantics, while the star remains decorative.
- Both `MediaCharacterAdapter` and `GroupCharacterAdapter` now use pool-safe `ComposeView` holders with the XML 8dp margins. The Media adapter keeps its parent-owned character click, and the grouped adapter keeps `GroupTitleViewHolder` and its full-span header policy. Holder state uses `neverEqualPolicy()` so a same-instance mutable `CharacterBase` rebind still refreshes the row; recycling clears that state.
- `CharacterRowComposeTest` passed 3/3. It checks accessible row text, favorite/standard marker visibility, image host presence, clickable card semantics in both adapters, and same-instance name/favorite updates in the grouped adapter.

### Visual comparison

Compared XML and Compose PixelCopy captures on `Custom_API_36` with the same local `reg_bg` image, fixture names, three-column host, and favorite/standard states. All captures are 328×536px. Mean RGB delta is normalized by 255; changed pixels use a per-channel threshold of 12/255.

| State or region | Pixels | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Favorite full row | 328×536 | 0.0083/255 | 8,186/175,808 (4.66%) | 208 |
| Standard full row | 328×536 | 0.0082/255 | 7,735/175,808 (4.40%) | 208 |
| Image interior, both states | 268×372 | 0.0001/255 | 42/99,696 (0.04%) | 176 |
| Name glyphs, both states | 252×80 | 0.0004/255 | 0/20,160 (0.00%) | 2 |
| Favorite star crop | 47×50 | 0.000016/255 | 0/2,350 (0.00%) | 1 |

- XML captures: `/tmp/character-xml-favorite.png`, `/tmp/character-xml-standard.png`. Compose captures: `/tmp/character-compose-favorite.png`, `/tmp/character-compose-standard.png`. The remaining visible deltas are concentrated at card shadow and rounded-edge pixels.

### XML removal and next candidate

- Removed `adapter_character.xml` and the temporary PixelCopy capture test after a source search found no `AdapterCharacterBinding`, `R.layout.adapter_character`, or `@layout/adapter_character` callers. `R.id.character_img` and `R.id.character_name_text` remain generated by `fragment_character_overview.xml`; `R.id.favourite_indicator` remains used by `adapter_staff.xml`.
- `adapter_feed_slide.xml` is now the next audit target from the ranked shortlist. Reconfirm its adapter and fragment owners, content-type branches, image transformations, click behavior, and baseline before any changes.

### Full verification gate

- `spotlessCheck` passed after `spotlessApply` formatted the new row, adapters, and tests.
- `:app:testAppDebugUnitTest` and `:app:testGithubDebugUnitTest` each passed 1,285 tests with 0 failures and 3 skipped.
- `:app:connectedAppDebugAndroidTest` passed 164 tests with 0 failures and 0 skipped on `Custom_API_36(AVD)`; the result file contains exit code `0`.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` succeeded and produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- The first full run had six Espresso focus failures. `dumpsys window` and a screenshot showed a `Pixel Launcher isn't responding` ANR, not an AniTrend test assertion. A clean `adb emu kill` followed by `android emulator start --cold Custom_API_36` restored the launcher; the isolated spinner test and complete suite then passed.
- The successful run emitted a nonfatal logcat-reader stream-closed warning and the existing ObjectBox configuration-cache serialization warning; neither affected the exit code or reports.

## Twenty-eighth Compose slice: feed slide

### Candidate audit and XML baselines

- `adapter_feed_slide.xml` remains the next choice after re-ranking. It is a shallow `FrameLayout` with one `WideImageView` and a playback overlay. `WideImageView` has an existing Compose `AndroidView` precedent in `EpisodeRow`, while the competing `adapter_media_header.xml` has four custom view types and only one live adapter owner; `GroupActorAdapter` has no construction callsite.
- Direct layout owners are `ImagePreviewAdapter` and `YouTubeEmbedFragment`. `ImagePreviewAdapter` is created by `StatusContentWidget`, which is embedded in `adapter_feed_status.xml`, `adapter_feed_message.xml`, and `adapter_comment.xml`; `fragment_user_about.xml` contains the widget but `ProfileOverviewSection` hides it. The other direct owner is `fragment_series_overview.xml#youtube_view`, a match-width, wrap-height host.
- The adapter maps `img` to the source URL, transition name, `CenterInside`, rounded corners, 250ms cross-fade, and no play overlay. `youtube` uses `RegexUtil.getYoutubeThumb`, `CenterCrop`, the play overlay, and the same rounding/transition; `webm` and other types use `RegexUtil.NO_THUMBNAIL` with the overlay. `ImagePreviewAdapter` clears Glide on recycle. `YouTubeEmbedFragment` loads its thumbnail with center-crop/cross-fade, opens the external YouTube URL on tap, and reports a missing app with a toast. `StatusContentWidget` dispatch callbacks are nullable; source search found no assignments to those callbacks, so this migration does not invent new feed actions.
- Only `feed_status_image` is clickable/focusable; neither the image nor the decorative play overlay has a content description, and `PreviewHolder` registers no long-click listener. The standalone fragment nulls its binding in `onDestroyView` after forwarding the image tap to an external YouTube intent.
- No existing test covers `ImagePreviewAdapter` row visuals or overlay states. `ImagePreviewActivityTest` covers the separate preview activity, and fragment bundle tests cover YouTube arguments only.
- Captured deterministic XML references with local `R.drawable.reg_bg`: the live `StatusContentWidget` image row is 1032×2231px; a direct XML binding fixture sized like `fragment_series_overview.xml#youtube_view` uses a local thumbnail and playback overlay at 1080×2276px. Files: `/tmp/feed-slide-xml-carousel-image.png` and `/tmp/feed-slide-xml-youtube.png`. Local resources avoid network-dependent thumbnail baselines.

### Post-character ranked shortlist

Complexity scores are estimates from hierarchy, custom dependencies, state, and live owners. Rank also weighs cross-host migration risk.

| Rank | Layout | Complexity | Live owner and dependencies | Rationale |
|---|---|---:|---|---|
| 1 | `adapter_feed_slide.xml` | 4/5 | `ImagePreviewAdapter` across three feed/comment adapter families plus `YouTubeEmbedFragment`; `WideImageView`, `AppCompatImageView`, Glide | Two-child leaf and existing `WideImageView` Compose interop pattern; capture both host constraints because the carousel and fragment measure differently. |
| 2 | `adapter_media_header.xml` | 4/5 | `CharacterActorsAdapter` in `CharacterFragment`; `GroupActorAdapter` has no caller; `AspectImageView`, `SeriesStatusWidget`, `SeriesTitleView`, `SingleLineTextView` | Fewer live owners, but four custom view types plus both click and long-click behavior. |
| 3 | `adapter_user.xml` | 4/5 | Three owners; `AvatarImageView`, `SingleLineTextView`, `FollowStateWidget`, Glide | Flat hierarchy but follow mutation, filtering, current-user context, and recycle cleanup. |
| 4 | `adapter_staff.xml` | 4/5 | Five adapter owners; `AspectImageView`, `SingleLineTextView`, `AppCompatTintImageView`, Glide | Simple card with favorite state, high regression fan-out. |
| 5 | `adapter_latest_anime.xml` plus `adapter_latest_manga.xml` | 5/5 | `MediaLatestAdapter`; two layout variants and multiple status, image, rating, title, airing/year widgets | More specialized widgets and state than the leading candidates. |

### Source map and baseline scope

| Source | Range | Audited contract |
|---|---:|---|
| `app/src/main/res/layout/adapter_feed_slide.xml` | 1-29 | Clickable `WideImageView` plus centered playback overlay. |
| `app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/ImagePreviewAdapter.kt` | 26-93 | Image/YouTube/fallback mapping, transition name, transforms, click dispatch, and Glide recycle cleanup. |
| `app/src/main/java/com/mxt/anitrend/base/custom/view/widget/StatusContentWidget.kt` | 31-188 | Horizontal carousel, snapping, feed/comment content parsing, and image/video/YouTube action dispatch. |
| `app/src/main/res/layout/widget_status.xml` and feed/comment row layouts | `widget_status.xml` 9-35; hosts 54-97 | Wrap-content carousel host with padding; `StatusContentWidget` appears in three visible feed/comment adapter layouts. |
| `app/src/main/java/com/mxt/anitrend/view/fragment/youtube/YouTubeEmbedFragment.kt` and `app/src/main/res/layout/fragment_series_overview.xml` | Fragment 82-138; host 27-33 | Direct XML host, centered image/play state, external YouTube intent, and missing-app toast. |
| `app/src/main/java/com/mxt/anitrend/base/custom/view/image/WideImageView.kt` and `app/src/main/java/com/mxt/anitrend/ui/episode/EpisodeRow.kt` | `WideImageView.kt` 33-83; `EpisodeRow.kt` 44-112 | Height measurement/Glide contract and existing Compose `AndroidView` interop pattern. |

- Existing `ImagePreviewActivityTest` and `FragmentBundleRoundTripTest` cover the separate image-preview activity and YouTube arguments, not this row. The YouTube XML reference is a deterministic binding fixture sized like `youtube_view`, with `reg_bg` substituted for the remote thumbnail; it does not assert a live external YouTube request or Fragment navigation.

### Compose implementation and behavior

- `FeedSlideRow` keeps `WideImageView` in `AndroidView` to preserve its image measurement and Glide behavior. It applies the audited `CenterInside`/`CenterCrop` choice, 250ms cross-fade, carousel corner radius, transition name for image slides, and a decorative playback overlay for YouTube/fallback rows.
- `ImagePreviewAdapter` now hosts a pool-safe `ComposeView`; image, YouTube, and fallback mapping and click dispatch remain in the existing adapter/widget path. `YouTubeEmbedFragment` uses the same row with lifecycle-bound Compose disposal and retains its thumbnail update, external YouTube intent, and missing-app toast. Image loading remains view-scoped, while release cleanup uses the application context to avoid reacquiring a destroyed Activity manager.
- The first Compose capture exposed a measurement regression: full-height modifiers expanded the row to the viewport. The row and AndroidView now fill width only; the wrap-content host measures to the native `WideImageView` height. `FeedSlideComposeTest` checks this measurement contract.
- `FeedSlideComposeTest` passes 3/3: visible/clickable image semantics with no playback overlay, wrap-content image measurement, and YouTube playback overlay semantics.

### Visual comparison

Compared PixelCopy captures using `R.drawable.reg_bg` and the same emulator and host constraints. The current XML carousel fixture was inflated in the live `StatusContentWidget` host; the YouTube XML reference is the audited wrap-content fixture for `youtube_view`.

| State | XML / Compose size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Carousel image row | 1032×2231 | 0.000000/255 | 0/2,302,392 | 0/255 |
| YouTube thumbnail and playback | 1080×2276 | 0.000037/255 | 0/2,458,080 | 1/255 |

- Carousel captures: `/tmp/feed-slide-current-xml-carousel-image-latest.png` and `/tmp/feed-slide-compose-carousel-image-fixed.png`.
- YouTube captures: `/tmp/feed-slide-xml-youtube.png` and `/tmp/feed-slide-compose-youtube-fixed.png`.
- The temporary PixelCopy capture test passed 1/1 and was removed after comparison. The separate `FeedSlideComposeTest` remains as the permanent semantics and measurement regression suite.

### XML removal and next candidate

- Removed `adapter_feed_slide.xml` after source search showed no `AdapterFeedSlideBinding`, `R.layout.adapter_feed_slide`, `@layout/adapter_feed_slide`, or `feed_play_back` consumers outside the layout. A second search after deletion returned no matches.
- Retained `R.id.feed_status_image` in `custom_ids.xml`; `ImagePreviewAdapter` and `YouTubeEmbedFragment` still assign it to their ComposeView hosts.
- The next remaining candidate is `adapter_media_header.xml`, ranked second in the pre-migration shortlist. Re-audit its two binding owners, row state/actions, and existing tests before editing.

### Full verification gate

- `:app:spotlessCheck` passed.
- `:app:testAppDebugUnitTest` and `:app:testGithubDebugUnitTest` each passed 1,285 tests with 0 failures and 3 skipped.
- `:app:connectedAppDebugAndroidTest` passed 167 tests with 0 failures and 0 skipped on `Custom_API_36(AVD)`; `test-result-exit-code.txt` contains `0`.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` passed and produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed; all en-GB store changelog files are within 500 characters.
- The first full connected run exposed `Glide.with(imageView)` in `AndroidView.onRelease` after Activity destruction. Release cleanup now uses the application context, and the final connected suite passed.


## Twenty-ninth Compose slice: media header

### Candidate audit and XML baselines

- `adapter_media_header.xml` was the next row after the feed slide. `CharacterActorsAdapter` is the active owner in `CharacterFragment.createActorsSection`; `GroupActorAdapter` has no construction callsite. `DetailListSection` hosts it with the responsive `grid_giphy_x3` `StaggeredGridLayoutManager` and fixed-size RecyclerView measurement; the API 36 capture resolves to three columns.
- The XML row uses an 8dp card margin and radius, 2dp elevation, a 70dp `AspectImageView`, a 4dp gap, and an 8dp text-column margin. `GroupMediaViewHolder` previously bound the cover image, status, preferred title, capitalized subgroup title, and capitalized format. The active Fragment remains the media open and authenticated long-press owner.
- Captured a deterministic 360×335px XML row with `R.drawable.reg_bg`, releasing status, and title/subgroup/format fixture values. The temporary legacy-binding reproduction matched `/tmp/character-media-header-xml.png` pixel-for-pixel.

### Source map and baseline scope

| Source | Range | Audited contract |
|---|---:|---|
| `app/src/main/res/layout/adapter_media_header.xml` | 1-81 | Card geometry, image host, status bar, title, subgroup, and format. |
| `app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/CharacterAdapters.kt` | 107-191 | Active header holder, ComposeView identity, click and long-click forwarding, and unchanged staff row. |
| `app/src/main/java/com/mxt/anitrend/view/fragment/detail/CharacterFragment.kt` | 300-320 | Media open and long-press owners for the character actors section. |
| `app/src/main/java/com/mxt/anitrend/view/fragment/detail/DetailListSection.kt` | 49-55 | Responsive `grid_giphy_x3` staggered-grid host and fixed-size RecyclerView setting. |

### Compose implementation and behavior

- `CharacterMediaHeaderRow` has a Material 3 `Card`, a `@Preview`, Compose title/subgroup/format text, and `AndroidView` hosts for the existing `AspectImageView` and `SeriesStatusWidget`. Image selection, status mapping, title preference, capitalization, spacing, color, and shape come from the existing XML and helpers.
- `CharacterActorsAdapter` now hosts the row in a pool-safe `ComposeView` with the existing `R.id.container` target and 8dp outer margins. It keeps the model in holder state, clears it on recycle, releases Glide through `AndroidView.onRelease`, and forwards both actions through the existing `ItemClickListener`. `CharacterStaffViewHolder` is unchanged.
- `MediaHeaderComposeTest` verifies visible text semantics, image/status hosts, the releasing status color, and click/long-click callback target, index, and model identity.

### Visual comparison

Compared PixelCopy captures from the same API 36 emulator, three-column host, fixture, and window size. The reproduced XML capture is pixel-identical to the saved pre-edit baseline. Image pixels match exactly in the interior; remaining deltas are at card/shadow and rounded-edge pixels plus text overflow glyphs.

| Region | Size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Whole row | 360×335 | 0.009280/255 | 6,036/120,600 (5.01%) | 208/255 |
| Image interior | 192×268 | 0.000000/255 | 0/51,456 (0.00%) | 0/255 |
| Status crop | 85×35 | 0.002638/255 | 80/2,975 (2.69%) | 29/255 |
| Text column crop | 60×190 | 0.007077/255 | 180/11,400 (1.58%) | 199/255 |

- Captures: `/tmp/character-media-header-xml-final.png` and `/tmp/character-media-header-compose-final.png`. The XML fixture bound all three text values and their views had visible bounds, but its PixelCopy baseline rendered no text glyphs at this width; Compose renders the truncated ellipses. The labels remain exposed in Compose semantics. String content is excluded from the migration skill's visual comparison, and the overflow difference is recorded rather than hidden.

### XML removal and next candidate

- Before deletion, source search found `AdapterMediaHeaderBinding` only in the obsolete `GroupActorAdapter` and temporary XML capture. `GroupActorAdapter` had no production construction callsite, and `GroupMediaViewHolder` had no other consumer. Removed `adapter_media_header.xml`, `GroupActorAdapter.kt`, `GroupMediaViewHolder.kt`, and the temporary capture test. A source-only search after removal found no layout, binding, or legacy-holder references. Shared `series_image`, `series_status`, and `series_title` IDs remain used by other layouts; `series_subgroup_title` and `series_format` have no remaining source references.
- After the feed slide and media header migrations, `adapter_user.xml` is rank 1 among the remaining candidates.

| Rank | Layout | Complexity | Remaining owner and migration risk |
|---|---|---:|---|
| 1 | `adapter_user.xml` | 4/5 | Three owners; `AvatarImageView`, `SingleLineTextView`, `FollowStateWidget`, and Glide. Follow mutation, current-user context, and recycling need coverage. |
| 2 | `adapter_staff.xml` | 4/5 | Five adapter owners; favorite state and image cleanup create higher regression fan-out. |
| 3 | `adapter_latest_anime.xml` plus `adapter_latest_manga.xml` | 5/5 | Two variants with status, image, rating, title, airing, year, and multiple click targets. |

### Full verification gate

- `spotlessCheck` passed.
- `:app:testAppDebugUnitTest` and `:app:testGithubDebugUnitTest` each passed 1,285 tests with 0 failures and 3 skipped.
- `:app:connectedAppDebugAndroidTest` passed 168 tests with 0 failures and 0 skipped on the API 36 emulator; `test-result-exit-code.txt` contains `0`.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` passed and produced `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed; all six en-GB store changelog files are within the 500-character limit.


## Thirtieth Compose slice: user row

### Candidate audit and XML baselines

- `adapter_user.xml` was a horizontal selectable row with `cardColor`, `lg_margin` padding, an `avatar_size_md` avatar, two `xl_margin` gaps, a weighted single-line name, and `FollowStateWidget`. The RecyclerView item retained an `md_margin` bottom gap.
- The live owners are `SearchFragment`'s Users section, `BottomSheetListUsers`, and `BottomSheetUsers`. Filtering remains in `UserAdapter`; each screen retains follow mutation, current-user context, and navigation ownership.
- `ImageExtensions.setImage(ImageBase?)` continues to load `large` through `AvatarImageView`, with `avatar_placeholder`, center-crop, a 150ms cross-fade, and circle-crop. `GlideAppModule` configures `ic_emoji_sweat` as the global error drawable; an absent avatar renders that error drawable rather than the per-request loading placeholder.
- Captured the legacy row with local `reg_bg.jpg` in follow, following, and self states. XML baselines: `/tmp/user-row-xml-follow-final.png`, `/tmp/user-row-xml-following-final.png`, and `/tmp/user-row-xml-self-final.png`.

### Source map and baseline scope

| Source | Range | Audited contract |
|---|---:|---|
| `app/src/main/res/layout/adapter_user.xml` | 1-45 | Legacy row bounds, click surface, avatar, name, and follow widget. |
| `app/src/main/java/com/mxt/anitrend/adapter/recycler/index/UserAdapter.kt` | 25-130 | Three owner-facing adapter contract, name filter, click dispatch, follow listener, and RecyclerView lifecycle. |
| `app/src/main/java/com/mxt/anitrend/ui/user/UserRow.kt` | 45-135 | State snapshot, row semantics, avatar and follow widget interop, and release cleanup. |
| `app/src/main/java/com/mxt/anitrend/binding/ImageExtensions.kt` and `app/src/main/java/com/mxt/anitrend/base/custom/glide/GlideAppModule.kt` | `ImageExtensions.kt` 10-22; `GlideAppModule.kt` 58-66 | Per-request placeholder/transforms and the global error drawable. |
| `app/src/androidTest/java/com/mxt/anitrend/widget/UserAdapterComposeTest.kt` | 47-226 | Content, container click, follow transition/rebind, self-user visibility, recycle cleanup, and absent-avatar fallback. |

### Compose contract and adapter behavior

- `UserRowUiState` snapshots the mutable name, avatar, and following flag while retaining the original `UserBase` for `FollowStateWidget`. `UserAdapter` preserves `R.id.container`, its existing click/long-click dispatch, bottom margin, name filtering, and owner callbacks.
- The holder uses a pool-safe `ComposeView` with `DisposeOnDetachedFromWindowOrReleasedFromPool`; recycling clears row state. `UserRow` keeps `AvatarImageView` and `FollowStateWidget` in `AndroidView`, preserving the established Glide request and follow/current-user behavior. Release clears the Glide target and follow listener.
- `UserRowPreview` provides the required Compose Preview. `UserAdapterComposeTest` covers button-role row semantics and click identity, pending and committed follow states, self-user hiding, recycled listeners, and the global Glide error drawable for a null avatar.

### Visual comparison

Compared stable PixelCopy captures from the same API 36 emulator, local image fixture, row width, and app theme.

| State | XML and Compose size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Follow | 1080×204 | 0.007771/255 | 3,429/220,320 (1.56%) | 199/255 |
| Following | 1080×204 | 0.007774/255 | 3,429/220,320 (1.56%) | 199/255 |
| Self | 1080×168 | 0.008476/255 | 2,993/181,440 (1.65%) | 199/255 |

- Captures: `/tmp/user-row-compose-follow-final.png`, `/tmp/user-row-compose-following-final.png`, and `/tmp/user-row-compose-self-final.png`.
- Avatar and action regions match exactly except for at most 2/255 channel delta in the following-state avatar; no pixels in those regions exceed 12/255. All larger differences are confined to text glyph rasterization between `SingleLineTextView` and Compose `Text`. Compose uses `bodyMedium` with font padding enabled; row geometry, image crop, button, and background match.

### XML removal and next candidate ranking

- Before deletion, source search found no `AdapterUserBinding`, `R.layout.adapter_user`, `@layout/adapter_user`, or other `adapter_user` references. Removed `adapter_user.xml` and the temporary `UserAdapterComposeCaptureTest`; a source-only search after removal returned no matches. The three production `UserAdapter` owners and shared `AvatarImageView`/`FollowStateWidget` remain.
- With the user row removed, the next ranked candidates are `adapter_staff.xml` and the paired latest-media rows:

| Rank | Layout | Complexity | Remaining owner and migration risk |
|---|---|---:|---|
| 1 | `adapter_staff.xml` | 4/5 | Four live owners across three adapter classes; `GroupStaffRoleAdapter` has no production construction callsite. Favorite state and image cleanup require coverage. |
| 2 | `adapter_latest_anime.xml` and `adapter_latest_manga.xml` | 5/5 | Two variants in `MediaLatestAdapter`, with status, image, rating, title, airing, year, and multiple click targets. |

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 21.536s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 18.643s |
| `:app:connectedAppDebugAndroidTest` | 171 | 0 | 0 | 4m15.06s |

- `:app:spotlessApply` and the final `:app:spotlessCheck` passed. The focused `UserAdapterComposeTest` passed 3/3 on the API 36 emulator.
- The first full connected run failed in `EntryPointRenderAuthTest.renderEntryPoints` after the emulator stalled and became non-interactive. After a cold boot, the one full connected rerun passed; its exit code is `0` and `renderEntryPoints` passed in 36.852s.
- `:app:assembleAppDebug` and `:app:assembleGithubDebug` passed. APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed. All six en-GB store changelog files remain within 500 characters.
- AniList MockWebServer isolation remains a separate user-requested follow-up in the `anitrend-app.test-anilist-mockwebserver` worktree on branch `test/anilist-mockwebserver`; this Compose migration did not alter Koin, Retrofit, or Glide networking.



## Thirty-first Compose slice: staff row

### Candidate audit and XML baselines

- `adapter_staff.xml` is a selectable, focusable `MaterialCardView` using `cardColor`, `lg_margin` corners and margins, and `sm_margin` elevation. Its `AspectImageView` keeps the custom 1.37 height-to-width measurement; the lower text block uses `lg_margin` padding. The name is single-line and ellipsized. The language uses `TextAppearance.AppCompat.Caption`, a top `sm_margin`, and is hidden for null or blank values. The favorite star is decorative, tinted `colorStateYellow`, and visible only when `StaffBase.isFavourite` is true.
- Current source audit corrects the previous five-owner estimate: four live callsites use three adapter classes. Search and Favorites use `StaffAdapter`; Character detail uses `CharacterActorsAdapter`; Media detail uses `MediaStaffRoleAdapter` through `MediaStaffSection`. Existing row clicks remain owned by those screens. The row has no favorite-toggle action.
- `GroupStaffRoleAdapter` had no production construction callsite. After the live owners migrated, the only remaining `AdapterStaffBinding` consumer was this unused adapter; both the adapter and `adapter_staff.xml` were removed after complete `app/src` reference searches returned no matches. `fragment_staff_overview.xml` retains its distinct `staff_img`, `staff_name_text`, and `staff_language_text` views.
- No existing unit or instrumentation test references `adapter_staff.xml` or `AdapterStaffBinding`. `StaffCharacterRolesComposeTest` covers the distinct `adapter_character_staff.xml` layout and does not cover this staff card.
- The four visible model fields are `StaffBase.name?.fullName`, `image`, `language`, and `isFavourite`. `StaffBase.equals` compares only `id`; Compose holders must use `neverEqualPolicy()` so a same-id rebind observes mutable field changes. `AspectImageView.setImage` selects `extraLarge` then `large`, uses center-crop and a 350ms cross-fade, and returns without loading when the image URL is null.
- Captured the live XML `StaffAdapter` in a three-column `StaggeredGridLayoutManager` with local `reg_bg.jpg`. PixelCopy baselines: `/tmp/staff-row-xml-favorite-stable.png` (360×635, star and Japanese caption visible) and `/tmp/staff-row-xml-standard-stable.png` (360×580, star and language hidden). The XML capture passed 1/1 before migration; the temporary Compose capture passed 1/1 and was deleted after comparison.

### Implementation

- Implemented stateless `StaffRow` with `StaffRowUiState` snapshots, `@Preview`, and an `AndroidView` host for the existing `AspectImageView`; Glide request behavior and the 350ms cross-fade remain unchanged.
- Migrated all four live callsites across `StaffAdapter`, `CharacterActorsAdapter`, and `MediaStaffRoleAdapter` through `ComposeView` with `DisposeOnDetachedFromWindowOrReleasedFromPool`. Owner callbacks and `R.id.container` click targets remain; favorite state is decorative, not actionable.
- Holders use `neverEqualPolicy()` because `StaffBase.equals` compares IDs only. Each holder clears Compose state on recycle, and `StaffRow` clears its Glide request on release.


### Compose UI and adapter contracts

- `StaffAdapterComposeTest` passes 5/5 on the API 36 emulator. It covers favorite/standard content, image loading, same-ID mutable rebind, click routing from all three live adapter classes, and image/Compose-content cleanup after removal.

### Visual comparison

| State | XML and Compose size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---|---:|---:|---:|
| Favorite | 360×635 | 0.014245/255 | 11,962/228,600 (5.23%) | 208/255 |
| Standard | 360×580 | 0.015244/255 | 11,403/208,800 (5.46%) | 208/255 |

- Compose captures: `/tmp/staff-row-compose-favorite.png` and `/tmp/staff-row-compose-standard.png`. Card fill, spacing, image geometry, favorite placement, and the visible caption match; residual pixel deltas are concentrated in name-text rasterization and small image/outline-shadow edges.

### XML removal and next candidate

- Removed `adapter_staff.xml`, the unused `GroupStaffRoleAdapter.kt`, and the temporary capture test. Post-removal searches in `app/src` return no `AdapterStaffBinding`, `adapter_staff`, or `GroupStaffRoleAdapter` references. The similarly named views in `fragment_staff_overview.xml` are a separate screen and remain.
- `MediaLatestAdapter` is instantiated by `MediaLatestList` and is also used by its `TrendingFragment` subclass for Anime, Manga, and Recently Added sections. `MediaLatestList` owns latest-feed loading and request state; `TrendingFragment` owns section selection; `MediaBrowseFragment` owns shared row click/long-click routing.

- Source searches in `app/src/test` and `app/src/androidTest` found no existing `MediaLatestAdapter` or `adapter_latest_*` tests.

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 19.153s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 19.120s |
| `:app:connectedAppDebugAndroidTest` | 176 | 0 | 0 | 21m0.94s |

- `:app:spotlessApply` and `:app:spotlessCheck` passed. Both debug APKs exist: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed; all six en-GB store changelog files remain within the 500-character limit.
- The first combined run timed out at 30 minutes. WindowManager then showed an `Application Not Responding: system` overlay, and the partial test log recorded a popup root-focus failure. A clean `android emulator stop Custom_API_36` / `android emulator start --cold Custom_API_36` was followed by a full rerun, which completed with exit code 0 and all 176 connected tests passing.

## Thirty-second Compose slice: latest media rows

### Candidate audit and XML baselines

- `adapter_latest_anime.xml` and `adapter_latest_manga.xml` share a zero-elevation `MaterialCardView` with a 20dp corner radius, 1dp `colorOutlineVariant` stroke, `colorSurfaceContainer` fill, horizontal/top `lg_margin`, a 6dp status rail, a 104dp `AspectImageView`, and a centered details column. The anime layout uses dynamic `AiringTextView`; manga uses static `label_latest_release` text.
- `MediaLatestAdapter` is instantiated by `MediaLatestList`, which is inherited by `TrendingFragment` for Anime, Manga, and Recently Added sections. `MediaLatestList` uses the one-column `single_list_x1` host. `MediaBrowseFragment` owns row navigation and long-press `MediaActionUtil` behavior; its callbacks accept only `R.id.container`. The adapter currently binds click and long-click listeners to the card, image, status, airing, title, year/type, and rating child IDs, forwarding the root item view.
- The anime row maps `title.userPreferred`, cover image, status color, next-episode date or capitalized status, year/episodes/format, average rating, media-list status, and favorite marker. The manga row maps the same card fields, substitutes `Latest release`, and formats year/chapters/format. `SeriesStatusWidget` maps RELEASING/FINISHED/NOT_YET_RELEASED to blue/green/orange and other statuses to red. `RatingTextView` obtains the score format from injected `BasePresenter`, then renders average score, list-entry status, and favorite state.
- Source searches in `app/src/test` and `app/src/androidTest` found no existing tests for these layouts or `MediaLatestAdapter`. The temporary `MediaLatestAdapterXmlBaselineCaptureTest` passed 1/1 with unauthenticated local fixtures and local `reg_bg` images.
- PixelCopy XML baselines: `/tmp/latest-media-anime-xml.png` and `/tmp/latest-media-manga-xml.png`, both 1032×427 at the live single-column width. The Anime fixture shows an airing episode, blue status rail, rating, favorite and list-status icons; the Manga fixture shows the static release label, green status rail, rating, and no list-status icon.
- `fragment_list.xml` and `fragment_trending.xml` both provide a match-parent RecyclerView host. `TrendingFragment` uses the inherited `single_list_x1` span count, so its Anime/Manga/Recently Added sections share the baseline row width.

### Compose implementation and tests

- Added stateless `MediaLatestRow` with `@Preview`, M3 `Surface` card styling, status rail, title/airing/year text semantics, and pool-safe `ComposeView` hosts for both adapter view types. The image remains the existing Glide-backed `AspectImageView`; `RatingTextView` remains an `AndroidView` to preserve injected user score-format, list-status, and favorite behavior.
- Shared `AiringTextView.formatAiringText`, `SeriesYearTypeTextView.formatText`, and `SeriesStatusWidget.statusColorResource` with the Compose row. The legacy View helpers call the same format/color mappings.
- Holders use `neverEqualPolicy()` for same-ID mutable `MediaBase` updates, clear Compose state on recycle, preserve `R.id.container` as the callback target, and dispatch existing click/long-click owner callbacks.
- `MediaLatestRowComposeTest` passes 4/4 on API 36. It covers Anime/Manga content, status pixels, rating/list/favorite state, click/long-click routing, same-ID rebind, and cover release on row removal. The pre-implementation RED run failed 3 Compose semantic contracts while the existing XML image-release contract passed.
- `MediaLatestAdapterComposeCaptureTest` passed 1/1 and generated `/tmp/latest-media-anime-compose-final.png` and `/tmp/latest-media-manga-compose-final.png`.

### Visual comparison

| Variant | XML and Compose size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---|---:|---:|---:|
| Anime | 1032×427 | 0.006292/255 | 8,086/440,664 (1.83%) | 210/255 |
| Manga | 1032×427 | 0.004153/255 | 4,101/440,664 (0.93%) | 210/255 |

- `MediaLatestRow` text uses the same default font family as the XML's explicit `TextAppearance.Material3` styles; using AniTrend's condensed override produced visibly narrower labels and larger screenshot deltas. Final card geometry, image crop, status rail, rating badge, labels, and line wrapping match; remaining differences are localized to text glyph rasterization and small image/outline pixels.

### XML removal and next candidate

- Removed `adapter_latest_anime.xml`, `adapter_latest_manga.xml`, and the temporary Compose capture test after a source search found no remaining layout or generated-binding references. `SeriesTitleView`, `SeriesStatusWidget`, `AiringTextView`, `SeriesYearTypeTextView`, `AspectImageView`, and `RatingTextView` remain because other live screens and adapters still use them.

| Rank | Remaining candidate | Complexity | Owner and risk |
|---:|---|---:|---|
| 1 | `widget_auto_incrementer.xml` | 3/5 | One `AutoIncrementWidget` owner used by expanded and compact `MediaListAdapter` rows; six state fields, loading/content flip, limit and not-yet-released labels, increment action, and ViewFlipper motion. No existing unit or instrumentation test covers it. |
| 2 | `widget_progress.xml` | 4/5 | `ProgressWidget` and `ScoreWidget` in list and manage-sheet flows; direct numeric editing, decrement/increment, maxima, repeat/volume fields, and draft persistence. |
| 3 | Feed/comment/review rows | 4/5 | Multiple adapter owners, `FavouriteActionController`, status/markdown content, voting, and delete/edit actions increase fan-out.

### Full verification gate

- The initial full run failed `LoggingFragmentTest.clearAction_emptiesAdapterAndLogFile`: `MediaBrowseViewModel.load` caught lifecycle `CancellationException` in `runCatching` and logged it as a request failure after the log clear. The ViewModel now rethrows cancellation before handling ordinary request failures; the focused clear test passed 1/1 after the fix.
- A full connected run on the shared `emulator-5554` reported 180 tests with five failures, including entrypoint and popup/dialog focus errors. The AVD recorded a 5,014 ms focus-event timeout in `co.anitrend.profile.component.screen.ProfileScreen`.
- The later contained run timed out after 60 minutes. Its results directory contains seven `logcat-co.anitrend.data.user.mapper.ProfileSidecarPersistenceTest-*` files from the concurrent `anitrend-v2` instrumentation suite, confirming both sessions used `emulator-5554`. The `co.anitrend` app is the other active workstream, not stray test debris. `co.anitrend` and Chrome were briefly disabled without clearing data, then re-enabled immediately. Future connected runs must use a dedicated AVD or be serialized with that suite.

**Final verification after cold boot on dedicated `medium_phone` (API 36):**

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 20.296s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 20.629s |
| `:app:connectedAppDebugAndroidTest` | 180 | 0 | 0 | 57m30.07s |

- The combined invocation reached its 60-minute deadline before Gradle finalized connected-test results. The standalone connected task completed after a cold boot with exit code `0`; all 180 tests passed.
- `:app:spotlessCheck`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` passed. APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed. All six en-GB store changelog files remain within the 500-character limit.
- Gradle reported the existing ObjectBox configuration-cache serialization warning; it did not fail a task.

### Lazy-list host cutover

- `MediaLatestList` is now a standalone `Fragment` with no `MediaBrowseFragment`/`FragmentBaseList` inheritance, owning paging, refresh, and state. `FragmentMediaLatestBinding` keeps `stateLayout` and `refreshLayout` while `mediaListView` hosts the keyed `LatestMediaList` `LazyVerticalGrid` with `DisposeOnViewTreeLifecycleDestroyed`; `TrendingFragment` subclasses it and embeds the same list under its chip `sectionSelector` through `attachList`.
- Rendered rows accumulate in `MediaLatestViewModel` (`UiState.Success(items, hasMorePages)`; page 1 resets, later pages append). `UiState.Success` and `UiState.Error` are deliberately identity-typed rather than data classes because `MediaBase.equals` is id-only and `StateFlow` conflates equal values: `MediaLatestViewModelTest` pins the accumulation contract and that a same-id reload re-emits a fresh state instance.
- The fragment holds the rendered list with `referentialEqualityPolicy()` and `LatestMediaList` takes a fresh `MediaItems` identity batch per emission: together they defeat structural-equality write rejection and equality-based Compose skipping, so same-id in-place `MediaBase` mutations always rerender (the adapter-era `neverEqualPolicy()` holder contract preserved).
- Load-more keeps the legacy `RecyclerScrollListener.visibleThreshold = 9` first-visible formula plus the swipe-up `CustomSwipeRefreshLayout` gesture; pull-to-refresh resets to page 1. Column count follows the legacy `single_list_x1` span resource (1 column normally, 2 on large-landscape, 3 on xlarge-landscape) via `GridCells.Fixed`.
- UX delta: the inherited `MediaBrowseFragment` options menu no longer attaches to the latest/trending screens. Its five filter items were already hidden there (`isFilterableEnabled = false`), and the sort item only wrote `settings.mediaSort`, which these request paths never read.
- `MediaLatestAdapter` was removed after a zero-consumer `app/src` audit. `MediaLatestRowComposeTest` migrated to the list host and keeps the Anime/Manga content, same-ID rebind, cover release on removal, and action-semantics contracts (targeted run: 4/4 on `emulator-5554`); entry-point smoke tests still open both screens via `ROUTE_TRENDING`. Post-cutover full-suite numbers are recorded in the final gate below.

### Post-cutover verification gate

- Commands: `jenv exec ./gradlew spotlessApply --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet`, followed by `jenv exec ./gradlew spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:connectedAppDebugAndroidTest :app:assembleAppDebug :app:assembleGithubDebug --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet` and `bash .github/scripts/validate-changelogs.sh`. All completed successfully.

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,288 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,288 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 211 | 0 | 0 |

- Connected instrumentation on `Custom_API_36` (`emulator-5554`) enumerates 63 suites, including `LoggingFragmentTest` 7/7, `MediaLatestRowComposeTest` 4/4, `TagGridComposeTest` 2/2, `GenreGridComposeTest` 2/2, `LinkGridComposeTest` 2/2, `RankingsGridComposeTest` 2/2, `LogEntryComposeListTest` 3/3, `MediaOverviewTagInteractionTest` 1/1, and both entry-point render suites. The suite surface replaces the adapter-host tests removed in this phase (Tag/Genre/Link/LogEntry/Rank adapter tests and other superseded instrumentation); the drop from intermediate run totals reflects those removals and test rewrites, with each removed contract recorded in its slice entry above.
- Both debug APKs assembled; `validate-changelogs.sh` passed with every changelog file under 500 characters, including the new `1013001000.txt` (177 characters) recording the menu UX delta.
- Live pass on the installed `app-debug.apk` (`emulator-5554`) verified: trending chips and row rendering; threshold load-more after scrolling (previously unseen rows rendered); pull-to-refresh resetting to page 1; the anime-to-manga section switch rendering manga rows; row-tap navigation to the media screen; the tag-row dialog with its description and `Close`/`More` actions; the two-column tag grid with name/rank cells and the Series Stats link grid; and the logging screen's filter chips narrowing the live log to the selected level.


## Thirty-third Compose slice: media-list progress control

### Candidate audit and XML baselines

- `widget_auto_incrementer.xml` is a clickable and focusable `ViewFlipper` with the `material_curve_black` transparent-black, 16dp rounded background, a 4dp margin, selectable foreground, and platform slide-in-left/slide-out-right animations. Its label uses a bold white 9sp caption with 8dp padding; its indeterminate Material spinner is 12dp and uses `?colorAccent`.
- `MediaListAdapter` hosts the same `AutoIncrementWidget` in `adapter_series_airing.xml` over the cover image and in `adapter_series_airing_compact.xml` over the card. Compact style X2 hides the control. `MediaListFragment` creates the adapter; `AiringListFragment` inherits it and overrides increment resolution to use its own `AiringListViewModel` state.
- `MediaListViewModel` and `AiringListViewModel` derive row states from the media-list store and `MutationRegistry`. The adapter maps anime to episode totals and other media to chapter totals; increment pending selects the spinner, while `canIncrement && !isIncrementPending && !isDeletePending` gates the increment action.
- The visible label prioritizes `TBA` for `NOT_YET_RELEASED`; otherwise it shows `progress/max`, uses `?` for a nonpositive maximum, and omits `+` only at the limit. That plus remains visible when the control is not enabled for another reason. Click handling prioritizes the busy toast, then the increment action, then the type-specific episode/chapter limit toast; other disabled clicks are no-ops. Recycling clears the listener and state and resets the loading child.
- Source searches found no existing tests for `AutoIncrementWidget`, `widget_auto_incrementer.xml`, or `MediaListAdapter` progress behavior. The only existing `MediaListAdapter` test covers its model `DiffUtil` identity/content contract.
- A focused XML capture passed 1/1 with 0 failures and 0 skips on dedicated `medium_phone` (API 36). Five clean 240x144 captures cover active `3/12 +`, unbounded `3/? +`, TBA, capped `12/12`, and loading states: `/tmp/media-list-progress-xml-active.png`, `/tmp/media-list-progress-xml-unbounded.png`, `/tmp/media-list-progress-xml-tba.png`, `/tmp/media-list-progress-xml-capped.png`, and `/tmp/media-list-progress-xml-loading.png`. PNG signatures and image data were verified after reconstructing 2,000-character, indexed log chunks; the earlier single-line log payloads were truncated and were not used.
- The final XML and Compose loading captures record an indeterminate-spinner frame. The 55x55px spinner arc region at `(136,27)-(190,81)` is masked for pixel metrics because its animation phase is time-dependent; its center, 12dp size, and primary color are checked visually and semantically.
### Compose contract RED

- Before production changes, `:app:connectedAppDebugAndroidTest` filtered to `MediaListProgressControlComposeTest` ran 6 tests with 6 failures and 0 skips in 5m31.86s on `medium_phone`. The Compose host tag was displayed, but the legacy Android view exposed no Compose text for active, unbounded, TBA, or capped labels, and its tagged control had no Compose `OnClick` semantics.
- The loading assertion could not find the expected `busy_please_wait` description; that test also hit the 1-minute Compose UI test timeout while hosting the native indeterminate view. The permanent loading contract will target the Compose indicator and use the existing fake-clock test pattern.


### Compose implementation and owner contracts

- Added stateless `MediaListProgressControl` and `MediaListProgressState` in `ui.media`, with an `@Preview`, existing theme dimensions/colors, a button-role click target, and a busy description for the loading indicator. The `MediaListAdapter` remains the state/action owner and keeps the episode/chapter limit and busy toast resources. Its holder uses the existing pool-safe ComposeView strategy, binds the currently owned model, and clears both model and progress state on recycle. Both expanded and compact row layouts now expose a ComposeView with the same ID; compact X2 remains `GONE`.
- Android API 36 `ViewAnimator` defaults `measureAllChildren` to true when no XML value is supplied. The Compose control reserves the hidden label bounds only while loading, matching the XML flipper's stable pill size without exposing duplicate loading text to accessibility.
- `MediaListProgressControlComposeTest` passes 2/2 with 0 failures and 0 skips (39.912s). It covers active and unbounded labels, TBA/capped limits, disabled no-op behavior, increment/limit callbacks, button semantics, loading description, and stable control bounds across the loading transition.
- `MediaListAdapterProgressComposeTest` passes 3/3 with 0 failures and 0 skips (15.924s) on dedicated `medium_phone` (API 36). It verifies expanded-row rebind after recycle, current-model increment dispatch, compact X1 control visibility, and compact X2 `View.GONE` behavior.
- The final Compose capture passed 1/1 with 0 failures and 0 skips (1m28.97s). The capture waits for `onGloballyPositioned` and a `ViewTreeObserver.registerFrameCommitCallback` before `UiAutomation.takeScreenshot`; it reconstructs 2,000-character indexed log chunks and verifies five distinct SHA-256 hashes. The earlier single-line captures and the first frame-raced set, where TBA and capped were identical, were discarded.

### Visual comparison

| State | XML and Compose size | Mean RGB delta | Pixels over 12/255 | Maximum channel delta |
|---|---:|---:|---:|---:|
| Active | 240×144 | 0.00321/255 | 374/34,560 (1.08%) | 216/255 |
| Unbounded | 240×144 | 0.00236/255 | 199/34,560 (0.58%) | 216/255 |
| TBA | 240×144 | 0.00252/255 | 221/34,560 (0.64%) | 208/255 |
| Capped | 240×144 | 0.00424/255 | 327/34,560 (0.95%) | 216/255 |
| Loading, spinner region masked | 240×144 | 0.00031/255 | 101/31,535 (0.32%) | 131/255 |

- Active/unbounded/TBA/capped captures show matching pill geometry, spacing, text size/weight, and colors. Residual static-state pixel differences are limited to text glyph rasterization. Loading-state comparison masks only the animated arc; the pill remains the same size as its label state and the spinner is centered at the same 12dp scale.
- XML captures: `/tmp/media-list-progress-xml-active.png`, `/tmp/media-list-progress-xml-unbounded.png`, `/tmp/media-list-progress-xml-tba.png`, `/tmp/media-list-progress-xml-capped.png`, `/tmp/media-list-progress-xml-loading.png`. Compose captures: `/tmp/media-list-progress-compose-active.png`, `/tmp/media-list-progress-compose-unbounded.png`, `/tmp/media-list-progress-compose-tba.png`, `/tmp/media-list-progress-compose-capped.png`, `/tmp/media-list-progress-compose-loading.png`.
### XML removal and next candidate ranking

- Before deletion, an `app/src` search found the only `AutoIncrementWidget` and `AutoIncrementWidgetState` users in the temporary XML capture harness and the custom-view source. `SeriesProgressTextView` was referenced only by `widget_auto_incrementer.xml`; `WidgetAutoIncrementerBinding` was referenced only by `AutoIncrementWidget`. After removing the temporary harness, `AutoIncrementWidget.kt`, `SeriesProgressTextView.kt`, and `widget_auto_incrementer.xml`, the same source search returned no matches. The obsolete `MediaListUtil` KDoc link was removed; the `series_episodes` IDs remain on the two ComposeView hosts.
- `widget_progress.xml` was migrated in the thirty-fourth slice. The next slice ranks the remaining feed, comment, and review rows and selects `adapter_comment.xml`.

| Rank | Layout | Complexity | Owner and dependency/risk summary |
|---:|---|---:|---|
| 1 | `adapter_comment.xml` | 3/5 | `CommentListAdapter` has one production owner, `CommentFragment`; immutable `CommentReplyUiModel`, one avatar, Markdown, and role-gated actions. Selected for the single owner, precomputed state, and absence of a remote media poster. |
| 2 | `adapter_review.xml` | 4/5 | `ReviewAdapter.ReviewBanner` in `BrowseReviewFragment`; `WideImageView`, overlay, rating, title, and existing Compose vote control. Defer for layered poster geometry and asynchronous vote outcome handling. |
| 3 | `adapter_feed_status.xml` | 4/5 | `FeedListAdapter` is constructed by `FeedListFragment`, `MediaFeedSection`, `ProfileFeedSection`, and `CommentFragment`; Markdown, favorite/comment/edit/delete controls. The unconstructed `FeedAdapter` source still binds this resource. |
| 4 | `adapter_series_review.xml` | 5/5 | `ReviewAdapter.ReviewDefault` in `MediaReviewSection`; avatar, date, title, Markdown summary, rating, cover image, and vote control. |
| 5 | `adapter_feed_progress.xml` | 5/5 | `FeedListAdapter` list/progress holders; avatar, feed/media titles, cover image, favorite/comment/users/delete controls, and multiple screen owners. |
| 6 | `adapter_feed_message.xml` | 5/5 | `FeedListAdapter` message holder; messenger/recipient avatars and names, Markdown, favorite/comment/edit/delete controls, and multiple screen owners. |
### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 21.777s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 19.977s |
| `:app:connectedAppDebugAndroidTest` | 185 | 0 | 0 | 15m43.55s |

- `:app:spotlessCheck`, both debug flavor assemblies, and `bash .github/scripts/validate-changelogs.sh` passed. All six store changelog files stayed within the 500-character limit.
- APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- **Observed:** The first connected run used the automatic software renderer (`lavapipe`/`SwiftShader`) and had five focus-sensitive failures, an 11.461s WindowManager wait, and 2,172 skipped frames. The successful run used the same API 36 `medium_phone` AVD with `-gpu host`; emulator output selected Apple M1 Pro/Metal. The AVD profile was not modified.
- The existing ObjectBox configuration-cache serialization warning remained non-fatal.

## Thirty-fourth Compose slice: numeric progress controls

### Candidate audit and XML baselines

- `widget_progress.xml` is a horizontal stepper with 24dp minus/plus icons around a centered single-line numeric field, a 72dp minimum field width, and an optional `/ max` label hidden until a maximum is set. It uses the Material 3 dense outlined field, `colorOutline`, body-medium text, `materialIconButtonStyle`, and `colorOnSurfaceVariant` icons. The maximum label uses body-medium/on-surface-variant text and 8dp side padding.
- `ProgressWidget` stores an integer current value and maximum. Stepper changes are one; a positive maximum bounds values to `0..max`, while an unset maximum only bounds below at zero. Empty direct input becomes zero; an out-of-range direct edit restores the previous current value.
- `ScoreWidget` reuses the row for `POINT_100`, `POINT_10_DECIMAL`, `POINT_10`, `POINT_5`, and `POINT_3`, with maxima 100, 10.0, 10, 5, and 3. Only decimal format steps by 0.1 and accepts the locale decimal separator. `BottomSheetSeriesManage` falls back to `POINT_100` when current-user media-list options are unavailable.
- `sheet_series_manage_m3.xml` hosts one `ScoreWidget` plus progress, volumes, and repeat `ProgressWidget`s. `BottomSheetSeriesManage` is their only production owner. Anime uses episodes/rewatched and hides volumes; manga uses chapters/reread and shows volumes. `populateFromModel` fills the widgets, completed status can auto-fill progress/volumes, and `handleSave` reads their existing getters into the isolated `MediaListDraft`.
- Existing tests cover form validation and draft construction, source-model isolation on cancel/failure, and the pure status auto-fill decision. No test directly exercised the numeric widget UI.
- **Prior manage-sheet design constraint:** keep this migration UI-only. Preserve all `sheet_manage_*` IDs, `ProgressWidget`/`ScoreWidget` setters and getters, and `BottomSheetSeriesManage` binding/population/save/delete/mutation logic. Retain both custom Views as ComposeView interop hosts and replace only their internal layout.
- XML card captures on `medium_phone` API 36 at 328dp width: anime score/progress `984x613` and `984x635`; manga score/progress `984x613` and `984x876`. Files: `/tmp/numeric-progress-xml-anime-score.png`, `/tmp/numeric-progress-xml-anime-progress.png`, `/tmp/numeric-progress-xml-manga-score.png`, `/tmp/numeric-progress-xml-manga-progress.png`.

### Compose migration and visual comparison

- `ProgressWidget` and `ScoreWidget` now keep their existing outer View IDs and setter/getter APIs while hosting `MediaListNumericControl` in a pool-safe `ComposeView`. `BottomSheetSeriesManage` binding, population, save, delete, mutation, and draft logic were not changed.
- The Compose row uses the existing condensed BodyMedium font and platform text tokens, an outlined 45dp input, 48dp stepper targets, and the existing 8dp maximum-label padding. **Observed:** `OutlinedTextField` at the XML dense height clipped numeric glyphs; a controlled `BasicTextField` with the same themed outline and explicit content alignment restores the XML text bounds without changing input/getter synchronization.
- XML and Compose card captures have identical dimensions. Visual inspection showed matching row geometry, maximum labels, text alignment, icon placement, and card content. Pixel comparison uses aligned 8-bit RGB images; table values show mean per-channel absolute difference and the share of pixels with any channel delta greater than 20/255.

| State | XML/Compose size | Mean RGB delta | Pixels over 20/255 |
|---|---:|---:|---:|
| Anime score | 984x613 | 0.781/255 | 1.11% |
| Anime progress | 984x635 | 1.109/255 | 1.82% |
| Manga score | 984x613 | 0.994/255 | 1.24% |
| Manga progress | 984x876 | 1.192/255 | 1.95% |

- Compose captures: `/tmp/numeric-progress-compose-anime-score.png`, `/tmp/numeric-progress-compose-anime-progress.png`, `/tmp/numeric-progress-compose-manga-score.png`, and `/tmp/numeric-progress-compose-manga-progress.png`. The temporary capture test was removed after comparison.

- The source search returned no matches for `WidgetProgressBinding`, `R.layout.widget_progress`, `R.id.progress_current`, `R.id.progress_maximum`, `R.id.progress_decrement`, `R.id.progress_increment`, or `widget_progress`. `widget_progress.xml` and the temporary capture test were removed; outer `sheet_manage_*` IDs remain.
- `ProgressWidgetComposeTest` passed 4/4 with 0 failures and 0 skips in 28.991s after migration. It covers bounded/unbounded controls, all five score formats and steps, direct entry with max rollback and 80-to-8 decimal scaling, and cancel leaving the original entry unchanged. Before production changes, the same four contracts failed because the XML controls did not expose Compose text/accessibility semantics.
- **Follow-up:** `AniTrendTheme` still leaves `bodyMedium` on the Material default font while its XML counterpart is condensed. This control reuses the existing condensed-family and platform-padding tokens locally. Consider a global `bodyMedium` mapping only after a separate parity audit of existing Compose surfaces.

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 20.578s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 22.307s |
| `:app:connectedAppDebugAndroidTest` | 189 | 0 | 0 | 9m55.63s |

- `:app:spotlessCheck`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` passed. APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed; all six store changelog files were within the 500-character limit.
- Connected tests used a cold-booted `medium_phone` API 36 AVD launched with `-gpu host` on Apple M1 Pro/Metal; the AVD profile was unchanged.
- Gradle reported the existing ObjectBox configuration-cache serialization warning; it did not fail a task.

## Thirty-fifth Compose slice: comment row

### Candidate audit and XML baselines

- `CommentListAdapter` has one production construction in `CommentFragment`. It receives the current-user snapshot and forwards like/delete mutations to `CommentViewModel`; edit/mention remain in `ComposerWidget`; likes remain in `BottomSheetUsers` or the existing no-likes toast; profile navigation receives the avatar `View` for the existing transition.
- `CommentReplyUiModel` is an immutable projection with reply text, user fields, likes, canonical favorite state, and like/delete pending flags. The adapter alone derives ownership with `currentUser?.id == model.userId`. The row's state contract is owner versus other user, favorite canonical/pending state, and delete pending state.
- `adapter_comment.xml` is `CardViewBase` > vertical column > weighted name/time row, spacing, hidden `StatusContentWidget`, Markdown text, spacing, and a bottom `FrameLayout` with likes and favorite/edit/delete/mention controls. `CommentListAdapter.bind` explicitly hides `widget_status`; the displayed body is `RichMarkdownTextView`.
- Theme/layout values: 8dp outer margin and card radius; `CardViewBase` applies 16dp content padding and compat padding; the avatar view is 24dp; `SingleLineTextView` truncates at the end; Markdown has 4dp padding; the custom action controls use existing 8dp padding/spacing and 18dp icons. App light/dark/black theme roles remain the source of card, text, outline, and accent colors.
- Legacy UI dependencies: `AvatarImageView` uses Glide circle crop and the existing placeholder; `RichMarkdownTextView` uses Markwon, web linkification, `BetterLinkMovementMethod`, and selectable text; `FavouriteActionController` hosted the existing Compose favorite; `StatusDeleteWidget` wraps `StatusDeleteAction` and its confirmation dialog. `UsersWidget`, `StatusEditWidget`, and `MentionWidget` were custom clickable views. The row binds no remote media image.
- The legacy holder resolved the current adapter position before dispatch; recycling cleared Glide and reset favorite/delete widgets. The Compose holder preserves current-position routing and `View` transition targets, resets action state on recycle, clears the avatar request, and keeps the source list/model immutable.
- The XML root is clickable/focusable with a selectable foreground but has no adapter root-click handler. The Compose row keeps only the working child actions and does not add a no-op root button.
- Existing tests before migration: `CommentListAdapterDiffTest` covers stable id/content equality, pending flags, and canonical like changes. `FavouriteActionAdapterComposeTest` covers comment favorite dispatch and canonical count/state. No comment-row test covered edit/delete/mention visibility, profile/likes actions, or recycling.
- XML baselines captured on `medium_phone` API 36 for the other-user, current-user, and current-user/delete-pending states. Fixture uses a one-day relative timestamp, a Markdown reply with a link, and three likes. Files: `/tmp/comment-row-xml-other-user.png`, `/tmp/comment-row-xml-current-user.png`, `/tmp/comment-row-xml-current-user-delete-pending.png`.
- The temporary `CommentRowXmlBaselineCaptureTest` passed 2 tests with 0 failures and 0 skips in 42.659s; it wrote the three baseline PNGs to the Gradle connected-test additional-output directory before the app was uninstalled.

### Compose implementation and focused contracts

- `CommentListAdapter` now hosts `CommentReplyRow` in a pool-safe `ComposeView` retaining `R.id.container`. The row uses Compose for its card, header, actions, and semantics; `AvatarImageView`, `RichMarkdownTextView`, and `StatusDeleteWidget` remain AndroidView leaves for Glide, profile-transition, Markwon, and confirmation-dialog behavior.
- Edit, mention, profile, and likes controls expose button semantics with localized descriptions. Favorite/delete descriptions and their existing state holders remain in use. Model ownership, mutations, composer state, likes sheet, and navigation stay in `CommentFragment`/`CommentViewModel`.
- `CommentListAdapterComposeTest` was RED before production changes: 6 tests, 2 failures, 0 errors, 0 skipped. The failures were the missing non-owner mention and owner edit semantics. After migration, all 6 focused tests passed with 0 failures and 0 skips; the suite covers action routing, user-role visibility, pending states, delete confirmation, rebound IDs, and recycling.

### Compose visual comparison

- Captures use the same `medium_phone` API 36 emulator and fixed fixture as the XML baselines. All XML/Compose pairs are 1032x457 pixels. Visual inspection shows matching card bounds, header/body positions, Markdown link styling, and role-specific action placement.

| State | Mean RGB delta per channel | Pixels over 20/255 |
|---|---:|---:|
| Other user | 2.170/255 | 1.39% |
| Current user | 2.198/255 | 1.42% |
| Current user, delete pending | 2.198/255 | 1.42% |

- The XML card interior sample is `(243, 242, 246)`, matching `m3_light_surfaceContainerLow`. `AniTrendTheme` does not map this role into `ColorScheme`, so `CommentReplyRow` uses the existing `getCompatColorAttr(R.attr.colorSurfaceContainerLow)` helper rather than the Material default. The Compose holder applies the XML 8dp RecyclerView margins and CardView compat inset.
- The action row matches the XML `FrameLayout` bottom alignment and top-aligned horizontal child layout. A 35dp minimum for text-based action targets preserves the measured `18dp` icon, `8dp` side padding, and `1dp` legacy line inset.
- XML baselines: `/tmp/comment-row-xml-other-user.png`, `/tmp/comment-row-xml-current-user.png`, `/tmp/comment-row-xml-current-user-delete-pending.png`. Compose captures: `/tmp/comment-row-compose-other-user.png`, `/tmp/comment-row-compose-current-user.png`, `/tmp/comment-row-compose-current-user-delete-pending.png`.
- The temporary `CommentRowComposeCaptureTest` passed 2 tests with 0 failures and 0 skips in 29.644s. Its screenshots were copied out of Gradle's connected-test output for comparison; the capture harness was removed afterward.
- `adapter_comment.xml` was removed after the source search returned no `AdapterCommentBinding`, `R.layout.adapter_comment`, `@layout/adapter_comment`, or `adapter_comment` references under `app/src`. `CommentRowComposeCaptureTest.kt` was also removed. `widget_mention` now has an explicit `res/values/ids.xml` declaration because the removed layout was its only XML ID declaration; the adapter retains the ID as its dispatch/test-tag key.
- **Next candidate:** `adapter_review.xml` (`ReviewAdapter.ReviewBanner` in `BrowseReviewFragment`), ranked ahead of the shared feed rows. Re-index consumers and recapture its current XML states before slice 36 edits.

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 17.161s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 17.248s |
| `:app:connectedAppDebugAndroidTest` | 195 | 0 | 0 | 16m23.87s |

- `:app:spotlessCheck`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` passed. `bash .github/scripts/validate-changelogs.sh` passed; all six store changelog files were within the 500-character limit.
- APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- Connected instrumentation ran on a cold-booted `medium_phone` API 36 AVD with `-gpu host` (Apple M1 Pro/Metal); the AVD profile was unchanged.
- **Observed:** The first full run failed four non-comment-row instrumentation tests while device logs showed severe frame/system stalls and a 45-second GraphQL timeout. After a cold reboot, all four failing test classes passed isolated reruns, followed by the 195-test full run with zero failures. No source changes were made between runs.

## Thirty-sixth Compose slice: review banner

### Candidate audit and XML baselines

- Re-ranked the remaining production rows. `adapter_review.xml` is selected first: `ReviewAdapter` uses `AdapterReviewBinding` only for `ReviewBanner`; `BrowseReviewFragment` constructs the default browse adapter, while `MediaReviewSection` passes `isMediaType = true` and continues to use `adapter_series_review.xml`. Source search found no other `AdapterReviewBinding` or `adapter_review` consumer.

| Rank | Layout | Complexity | Owner and dependency/risk summary |
|---:|---|---:|---|
| 1 | `adapter_review.xml` | 4/5 | One browse banner owner; custom wide image, gradient, rating, and existing Compose vote control. |
| 2 | `adapter_feed_status.xml` | 4/5 | Four `FeedListAdapter` owners plus the unconstructed `FeedAdapter`; Markdown and several row actions. |
| 3 | `adapter_series_review.xml` | 5/5 | `MediaReviewSection` default review row; avatar, date, Markdown, cover image, rating, and vote behavior. |
| 4 | `adapter_feed_progress.xml` | 5/5 | Shared feed owners; avatar, cover, favorite/comment/users/delete actions and progress state. |
| 5 | `adapter_feed_message.xml` | 5/5 | Shared feed owners; messenger and recipient data, avatars, Markdown, and several actions. |

- `adapter_review.xml` is a 20dp `MaterialCardView` with an 8dp margin, `colorSurfaceContainer`, 1dp `colorOutlineVariant` stroke, and zero elevation. Its frame overlays a `WideImageView`, `gradient_shadow`, a top rating/author row, and a bottom title/vote row. The lower section shows `summary` in body-medium text capped at three lines, followed by the `text_read_more` action.
- `ReviewAdapter.ReviewBanner` binds `media?.bannerImage`, `user?.name`, `score`, `media?.titleUserPreferred`, `summary`, `rating`, `ratingAmount`, and `userRating`. `ReviewRecord` is immutable; `summary`, `user`, `media`, `titleUserPreferred`, and `bannerImage` are nullable. The existing mapper maps empty nested user/media identities to null.
- `WideImageView` uses Glide center-crop with a 350ms crossfade and `R.drawable.reg_bg` for a null banner; its measurement uses `KeyUtil.WideAspectRatio`. `CustomRatingBar` is a noninteractive five-star indicator receiving `score * 5 / 100`. `SeriesTitleView` displays the preferred title on one end-ellipsized line. The author pill uses `bubble_background`; the image overlay uses `gradient_shadow`.
- Existing owner behavior: image click opens the media destination; image long-click opens media actions or shows the login-required toast; read-more opens `BottomReviewReader` with profile navigation. The card root has no click handler. `ReviewHolderRegistry` routes mutation outcomes to the holder currently bound to the review id; `VoteController` resets loading on result/recycle and retains its 10-second fallback. Preserve these owners and the separate `MediaReviewSection` row variant.
- Existing tests: `ReviewAdapterVoteComposeTest` covers vote selection/toggling, logged-out dispatch, mutation failure/success, zero-clamped counts, and recycled-holder isolation; `ReviewVoteRowComposeTest` covers vote semantics and pending state. No existing test covers banner content, image accessibility, read-more routing, or browse image click/long-click routing. The clickable/focusable XML image has no content description; the Compose banner will expose the media title with its click action.
- XML baselines use an offline `ReviewRecord` fixture with the production null-banner `reg_bg` fallback: populated author/title/summary and 80/100 selected-up-vote state, plus missing-media/user/summary and zero-vote state. The temporary capture harness clears Glide after load and applies the same local fallback at `CENTER_CROP` to avoid capturing its crossfade. Repeated captures were pixel-identical. Files: `/tmp/review-banner-xml-populated.png` (1032x985) and `/tmp/review-banner-xml-missing-media.png` (1032x865).

### Compose implementation and visual comparison

- `ReviewBannerRow` now composes the card, overlay, author, title, summary, and `ReviewVoteRow` under a pool-safe `ComposeView` in `ReviewAdapter.ReviewBanner`. The adapter remains the model, mutation, and vote-state owner; the `VoteController` is now a state holder shared by this Compose row and the unchanged XML-backed `ReviewDefault` vote host.
- `WideImageView` remains the Glide image leaf with its existing null-banner `reg_bg` fallback and 350ms request transition. `CustomRatingBar` remains the five-star indicator. The read-more `MaterialTextView` remains an AndroidView leaf so `ReviewAdapter` can forward its existing `R.id.review_read_more` target; image and read-more have Compose button semantics. Image clicks and long-clicks still forward `R.id.series_image`; `BrowseReviewFragment` navigation and `MediaReviewSection` default-row ownership were not changed.
- `ReviewBannerVoteComposeTest` was RED before production changes: 8 tests, 2 failures, 0 errors, 0 skipped. The failures exposed the missing Compose semantics for image/content and the absence of Compose-visible review text. After migration, the focused suite passed 8/8 with 0 failures and 0 skips. It covers the new content/accessibility/rebind contracts plus the existing vote selection, mutation failure, logged-out, count-boundary, and recycled-holder behavior.
- The temporary Compose capture passed 1/1 with 0 failures and 0 skips. Both captures use the same local `reg_bg` fallback and fixed `ReviewRecord` fixtures as the XML baselines.

| State | XML size | Compose size | Mean RGB delta | Pixels with any channel delta over 12/255 |
|---|---:|---:|---:|---:|
| Populated, Compose cropped to XML height for comparison | 1032x985 | 1032x989 | 5.117/255 | 44,704/1,016,520 (4.40%) |
| Missing media/user/summary | 1032x865 | 1032x865 | 0.392/255 | 1,558/892,680 (0.17%) |

- **Observed:** Image, gradient, card bounds, text wrapping, vote alignment, and empty-state geometry match closely. The populated Compose row is 4px taller; the remaining pixel delta concentrates on text glyphs and the lower card edge. This is not bit-identical typography, but the capture shows no layout shift or image/fallback regression.
- XML captures: `/tmp/review-banner-xml-populated.png` and `/tmp/review-banner-xml-missing-media.png`. Compose captures: `/tmp/review-banner-compose-populated.png` and `/tmp/review-banner-compose-missing-media.png`.

### XML removal and next candidate

- `adapter_review.xml` was removed after the post-migration `app/src` search returned no `AdapterReviewBinding`, `R.layout.adapter_review`, `@layout/adapter_review`, or `adapter_review` references. The temporary `ReviewBannerComposeCaptureTest` was removed. `series_image`, `series_rating`, `review_read_more`, and `container` remain declared by `adapter_series_review.xml`; no `ids.xml` declaration was needed. `gradient_shadow`, `bubble_background`, and `wide_image_md` remain used elsewhere.
- The next candidate is `adapter_feed_status.xml`. Its four `FeedListAdapter` production owners make it the highest-fan-out remaining row; `adapter_series_review.xml`, `adapter_feed_progress.xml`, and `adapter_feed_message.xml` remain ranked after it. Re-index their consumers and capture the current XML states before slice 37 edits.

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 17.260s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 16.732s |
| `:app:connectedAppDebugAndroidTest` | 197 | 0 | 0 | 15m37.78s |

- `:app:spotlessCheck`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` passed. `bash .github/scripts/validate-changelogs.sh` passed; all six store changelog files remained within the 500-character limit.
- APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- Connected tests ran on `medium_phone` API 36 with `-gpu host` (Apple M1 Pro/Metal); the AVD profile was unchanged.






## Thirty-seventh Compose slice: feed status row

### Candidate audit and XML baselines

- After removing `adapter_review.xml`, `adapter_feed_status.xml` is the top remaining candidate at 4/5 complexity: four active `FeedListAdapter` owners, a rich-text body, avatar loading, and several row actions. The earlier slice-36 ranking places `adapter_series_review.xml` next, followed by `adapter_feed_progress.xml` and `adapter_feed_message.xml`, each at 5/5.
- Production owners: `FeedListFragment`, `ProfileFeedSection`, `MediaFeedSection`, and the origin-feed header in `CommentFragment`. The old `FeedAdapter` has no callsites in `app/src`; its only source match is the declaration, and it still binds `AdapterFeedStatusBinding`. Its removal is required before deleting the shared status layout. The four active owners and their callback/state boundaries remain unchanged.
- The row model is immutable `FeedItemUiModel` with status type `KeyUtil.TEXT`. It supplies author/avatar/profile id, timestamp, Markdown body, canonical favorite count/selection/pending state, reply count, edit/delete eligibility, and delete-pending state. `FeedListAdapter` resolves the current adapter position before dispatch.
- Existing interactions: `widget_favourite` toggles the status id; `widget_users` opens likes; `widget_comment` opens comments; `widget_edit` opens the edit flow; `widget_delete` retains `StatusDeleteWidget` confirmation and pending guards; clicking `user_avatar` forwards its `View` and user id for the existing profile transition. The root `container` provides only a selectable ripple, with no adapter click or long-click action. `StatusContentWidget` is always `GONE`, so this row displays no media attachment.
- Interop contracts: `AvatarImageView` uses Glide center-crop/circle-crop, `avatar_placeholder`, and a 150ms crossfade; `RichMarkdownTextView` uses the Koin-provided Markwon renderer, web linkification, `BetterLinkMovementMethod`, and selectable text. `StatusDeleteWidget` wraps the existing Compose confirmation action. Favorite, comment, users, and edit controls use their existing component resources/state.
- XML geometry and theme: outer card `colorSurfaceContainer`, `colorOutlineVariant` 1dp stroke, zero elevation, 8dp radius and outer horizontal/top margins; `CardViewBase` contributes 16dp content padding. The inner Markdown panel uses `colorSurfaceContainerLow`, `colorOutlineVariant` 1dp stroke, 12dp corners, 8dp padding, then 4dp top/2dp bottom Markdown padding. The avatar is 24dp; header/action spacing and icon targets use the existing 8dp/12dp/18dp resources.
- Compose leaves the card itself non-clickable rather than adding a dead-end button. It adds localized semantics to working child actions; the original favorite/delete descriptions remain.
- Existing coverage before this slice: `FavouriteActionAdapterComposeTest` covers the status favorite's dispatch, pending/canonical state, and recycled-holder isolation. `StatusDeleteWidgetComposeTest` covers delete loading/disabled and stale confirmation behavior. No integration test covered status body/Markdown, avatar fallback, role-specific row semantics, or the complete status row.
- A temporary `FeedStatusXmlBaselineCaptureTest` passed 1/1 with 0 failures and 0 skips in 17.576s on `medium_phone` API 36. Stable fixtures capture an other-user row with linked Markdown, an owner row, and an empty-body owner row with the null-avatar placeholder. Each capture is 1032x526: `/tmp/feed-status-xml-other-user.png`, `/tmp/feed-status-xml-owner.png`, and `/tmp/feed-status-xml-owner-empty.png`.
- `FeedStatusRowComposeTest` ran before production changes on the same AVD: 3 tests, 2 failures, 0 errors, 0 skips in 24.975s. RED is consumer-visible: the XML row has no profile-open label and no accessible edit-action label. The delete-confirmation/rebind contract passed unchanged.

### Compose implementation and visual comparison

- `FeedListAdapter.StatusFeedViewHolder` now hosts `FeedStatusRow` in a `ComposeView` with `DisposeOnDetachedFromWindowOrReleasedFromPool`; its RecyclerView owner, immutable model, current-position click routing, and four screen callbacks remain in the adapter/Fragments. The row maps the original card and panel colors, dimensions, header, actions, owner visibility, and pending states. The CardView compatibility inset is represented by 2dp horizontal and 3dp vertical Compose padding.
- The row uses Compose for the card, header, favorite, likes/comments/edit controls, and accessible button semantics. `AvatarImageView`, `RichMarkdownTextView`, and `StatusDeleteWidget` remain AndroidView leaves for the existing Glide fallback/transition target, Markwon links and selectable text, and confirmation/recycling guard. A per-bind epoch prevents a pending delete confirmation from dispatching against a later binding. `FeedStatusRow` includes `@Preview`.
- The profile, likes, comments, edit, favorite, and delete actions have accessible labels. The inert card root is not promoted to an extra Compose button. The owner integration remains unchanged.
- `FeedStatusRowComposeTest` was RED before production changes: 3 tests, 2 failures, 0 errors, 0 skips in 24.975s, exposing missing profile-open and edit-action semantics. After migration, the three permanent tests passed 3/3 in 24.076s. `FavouriteActionAdapterComposeTest` passed 3/3 with 0 failures and 0 skips in 14.454s. A temporary Compose capture method passed 1/1 in 46.640s within a 4/4 test run, then was removed after exporting its screenshots.
- XML and Compose captures use the same `medium_phone` API 36 fixture, image fallback, and 1032x526 bounds. Visual inspection shows the card/panel edges, avatar, body placement, action alignment, Markdown link styling, and empty-body line align. Remaining pixel differences are concentrated in Compose header/action text and icon rasterization.

| State | XML and Compose size | Mean RGB delta/channel | Pixels with any channel delta over 20/255 |
|---|---:|---:|---:|
| Other user | 1032x526 | 1.773/255 | 7,016/542,832 (1.29%) |
| Owner | 1032x526 | 1.823/255 | 7,421/542,832 (1.37%) |
| Owner, empty body | 1032x526 | 1.823/255 | 7,421/542,832 (1.37%) |

- XML captures: `/tmp/feed-status-xml-other-user.png`, `/tmp/feed-status-xml-owner.png`, `/tmp/feed-status-xml-owner-empty.png`. Compose captures: `/tmp/feed-status-compose-other-user.png`, `/tmp/feed-status-compose-owner.png`, `/tmp/feed-status-compose-owner-empty.png`.

### XML removal and next candidate

- `FeedAdapter.kt` was removed after case-sensitive search found only the declaration and no callers in `app/src`; the project LSP server was unavailable. `adapter_feed_status.xml` was then removed after the post-migration `app/src` search found no `AdapterFeedStatusBinding`, `R.layout.adapter_feed_status`, `@layout/adapter_feed_status`, or `adapter_feed_status` references. `FeedStatusRowComposeTest.kt` remains as the permanent row contract suite; both screenshot capture harnesses were removed.
- No `ids.xml` declarations were needed. `widget_status` and `widget_status_text` remain declared by other layouts, including `adapter_feed_message.xml` and `fragment_user_about.xml`; the status action IDs also remain declared by active feed/message/progress layouts.
- The next source-ranked candidates are:

| Rank | Layout | Complexity | Owner and dependency/risk summary |
|---:|---|---:|---|
| 1 | `adapter_series_review.xml` | 5/5 | `MediaReviewSection` default row; avatar, date, Markdown, cover image, rating, and vote state. |
| 2 | `adapter_feed_progress.xml` | 5/5 | Four `FeedListAdapter` owners; avatar, cover image, favorite/comment/users/delete actions, and progress state. |
| 3 | `adapter_feed_message.xml` | 5/5 | Shared feed owners; messenger and recipient identity/images, Markdown, and mutation actions. |

- The next slice must re-index current consumers and capture its current XML states before edits.

### Full verification gate

| Task | Tests | Failures | Skipped | Duration |
|---|---:|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,285 | 0 | 3 | 17.844s |
| `:app:testGithubDebugUnitTest` | 1,285 | 0 | 3 | 16.920s |
| `:app:connectedAppDebugAndroidTest` | 200 | 0 | 0 | 17m24.88s |

- `:app:spotlessCheck`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` passed. `bash .github/scripts/validate-changelogs.sh` passed; all six Google Play changelog files were within the 500-character limit (136, 491, 468, 476, 287, and 428 characters).
- APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- Connected instrumentation ran on `emulator-5554`, the `medium_phone` API 36 AVD; the AVD profile was unchanged.

## Thirty-eighth Compose slice: series review row

### Candidate audit and XML baselines

- After the feed status slice, `adapter_series_review.xml` remains the top-ranked production row: one active owner and one adapter item type, while `adapter_feed_progress.xml` and `adapter_feed_message.xml` each have four `FeedListAdapter` owners and similar 5/5 structural risk. `MediaReviewSection` constructs `ReviewAdapter(isMediaType = true)` and is the sole active owner of `ReviewAdapter.ReviewDefault`; `BrowseReviewFragment` constructs the banner type and does not consume this XML. `AdapterSeriesReviewBinding` has one production reference in `ReviewAdapter`.
- `ReviewRecord` supplies author/profile, created date, optional media title/cover, Markdown `summary`, score, vote totals, and the current user's vote. `ReviewAdapter.VoteController` plus `ReviewHolderRegistry` own mutation state and async results; `ReviewVoteRow` already renders the vote action in Compose. Preserve that owner and the existing `MediaReviewSection` callbacks.
- Existing child actions: `review_read_more` opens the review reader; `user_avatar` opens the author's profile; `series_image` has a long-press media-action handler but no click handler in `ReviewDefault`. The row root is not an action. `MediaFragment` owns these navigation/action effects.
- XML contract: 20dp rounded `MaterialCardView`, `colorSurfaceContainer`, 1dp `colorOutlineVariant` stroke and 8dp margins; 16dp content padding; 40dp avatar and title/date header; 16sp single-line series title; italic Markdown summary capped at four lines; small indicator `CustomRatingBar`; read-more label and `ReviewVoteRow`; 88dp cover view measured at `KeyUtil.AspectRatio = 1.37`.
- `AspectImageView` loads nonblank covers through Glide center-crop with a 350ms crossfade; null/blank cover URLs return without setting a fallback. `AvatarImageView` uses the existing Glide placeholder/error fallback. **Observed:** a same-ID media-to-null rebind retains the previous cover because the binding setter returns early and cleanup only occurs on holder recycle; the pre-implementation integration test reproduces it.
- Existing coverage: `ReviewAdapterVoteComposeTest` covers banner/default vote dispatch, direction changes, pending/result reset, logged-out gating and recycled vote holders; `ReviewVoteRowComposeTest` covers the shared vote composable. No test covers series-review text/images, title/read-more/profile semantics, or media-to-empty image rebinding.
- The corrected pre-implementation `ReviewAdapterVoteComposeTest` RED run had 10 tests, 2 failures, 0 errors, and 0 skips in 84.860s. It isolated missing profile-open semantics and the stale Glide cover after rebinding to a media-null review; the other eight banner/vote-owner contracts passed.
- `review_read_more` and `series_rating` are explicitly declared in `values/ids.xml` because the migrated layout was their last XML declaration while `ReviewBannerRow`, `BrowseReviewFragment`, and `MediaFragment` still use them. `review_date` and `review_summary` remain declared by `custom_review_template.xml`. `review_vote` had no non-layout consumers and needs no test-tag alias.
- A temporary XML capture passed 1/1; the full `ReviewAdapterVoteComposeTest` class passed 9/9 with 0 failures and skips in 52.358s. Stable `medium_phone` API 36 captures use a populated Markdown/vote row with the avatar fallback and a missing-content row; both are 1032x601: `/tmp/review-series-xml-populated.png` and `/tmp/review-series-xml-missing-content.png`.

### Compose implementation and visual comparison

- `ReviewAdapter.ReviewDefault` now hosts the stateless `ReviewSeriesRow` in a pool-safe `ComposeView`. `ReviewAdapter.VoteController`, `ReviewHolderRegistry`, stable child action IDs, and the `MediaFragment` navigation/action owners remain unchanged. The Compose row renders the card and text; `AvatarImageView`, `RichMarkdownTextView`, `CustomRatingBar`, and `AspectImageView` remain AndroidView leaves for their existing Glide, Markwon, and native RatingBar behavior. `ReviewSeriesRow` includes a Compose Preview.
- `ReviewAdapter.updateCoverImage` clears the previous Glide target and drawable when a rebound review has no cover URL; nonblank URLs still use `AspectImageView` center-crop loading. The rating view remains accessible with native range semantics and a localized score description. Avatar placeholder and recycling behavior remain owned by `AvatarImageView`.
- The post-migration `ReviewAdapterVoteComposeTest` run passed 11 tests, 0 failures, 0 errors, and 0 skips in 1m38.984s on `Custom_API_36`. This included the 10 permanent adapter/vote contracts and a temporary capture-only test; that capture harness was removed after exporting both Compose PNGs.
- XML baselines were captured on `medium_phone` API 36; Compose captures were rendered on `Custom_API_36` API 36 with the same 1080x2400, 480dpi AVD profile. All four item captures are 1032x601: XML `/tmp/review-series-xml-populated.png` and `/tmp/review-series-xml-missing-content.png`; Compose `/tmp/review-series-compose-populated.png` and `/tmp/review-series-compose-missing-content.png`.

| State | Size | Mean absolute RGB delta/channel | Pixels with any channel delta over 20/255 |
|---|---:|---:|---:|
| Populated | 1032x601 | 0.650/255 | 3,550/620,232 (0.57%) |
| Missing content | 1032x601 | 0.107/255 | 992/620,232 (0.16%) |

- Visual inspection shows matching card geometry, avatar fallback, title/date placement, Markwon emphasis/link styling, and read-more/vote alignment. The empty-content state also aligns; remaining populated-state pixel differences are concentrated in text glyph rendering.

### XML removal and next candidate

- `adapter_series_review.xml` is absent from the current worktree. A source search found no `AdapterSeriesReviewBinding`, `R.layout.adapter_series_review`, or `@layout/adapter_series_review` consumers. Only the obsolete layout/binding were removed; the custom view classes remain because other production layouts still use them.
- `review_read_more` and `series_rating` remain explicit `ids.xml` entries for the banner/detail actions and native rating host. The old `review_vote` layout ID had no remaining caller and was not carried forward.
- The next ranked production layouts remain `adapter_feed_progress.xml` (four `FeedListAdapter` owners) followed by `adapter_feed_message.xml`; the next slice must re-index their current consumers and capture stable XML states before edits.

### Full verification gate

| Check | Result |
|---|---|
| `:app:spotlessCheck` | Passed. |
| `:app:testAppDebugUnitTest` | 1,285 tests, 0 failures, 3 skipped; 27.573s. |
| `:app:testGithubDebugUnitTest` | 1,285 tests, 0 failures, 3 skipped; 23.986s. |
| `:app:connectedAppDebugAndroidTest` | 202 tests, 0 failures, 0 skipped; 11m28.18s on API 36 `Custom_API_36`. |
| `:app:assembleAppDebug` and `:app:assembleGithubDebug` | Both debug APKs were produced. |
| `validate-changelogs.sh` | Passed. All six store changelog files are within 500 characters; the longest is 491. |

Debug APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

## Thirty-ninth Compose slice: feed progress row

### Candidate audit and XML baselines

- `FeedListAdapter` has four production constructors: `CommentFragment`, `FeedListFragment`, `MediaFeedSection`, and `ProfileFeedSection`. It renders immutable `FeedItemUiModel` values; their `type` and `hasLikes` determine the holder. `KeyUtil.TEXT` selects `StatusFeedViewHolder`, `KeyUtil.MESSAGE` selects `MessageFeedViewHolder`, `MEDIA_LIST && !hasLikes` selects `ListFeedViewHolder`, and other media activity rows select `ProgressFeedViewHolder`. Before migration, both media holders inflated `adapter_feed_progress.xml`.
- Before migration, both variants showed user/avatar/date, feed headline, English/original media titles, cover, comment count, and conditional delete. The progress variant also exposed likes/users and favorite state; the list variant hid those controls and did not route cover click/long-click. The adapter passed image `View` parameters to open-media/open-profile callbacks, but `FeedListFragment`, `CommentFragment`, `MediaFragment`, and `ProfileFragment` owners ignored them and navigated by IDs; no shared-element transition was active for these rows.
- The XML baseline used `CardViewBase` defaults (16dp content padding, 8dp radius, compat padding) plus horizontal/top 8dp margins, surface-container background and outline stroke. Its nested media context used 12dp corners, 1dp outline and 8dp padding; the cover card was 100dp wide, 16dp corner radius and outline, with `KeyUtil.AspectRatio = 1.37`. The headline was preformatted by `FeedItemUiModel`, rendered at 16sp and capped at two lines; the original title was a selectable AppCompat caption. The footer contained `UsersWidget`, `FavouriteActionController`'s ComposeView, `CommentWidget`, and `StatusDeleteWidget`.
- `FavouriteActionController` owns favorite pending/enabled/recycle behavior; `StatusDeleteWidget` owns loading, busy toast, confirmation, and stale-listener protection. `AvatarImageView` clears Glide on recycle. `AspectImageView` does not clear on recycle and ignores null/blank URLs, so a recycled progress row can retain its previous cover. `FeedItemViewHolder.currentItem()` guards stale action dispatch. Existing `FeedStatusRowComposeTest` and `FavouriteActionAdapterComposeTest` cover `KeyUtil.TEXT` rows, not progress/list variants; `StatusDeleteWidgetComposeTest` covers the reusable delete widget. `MediaListAdapterProgressComposeTest` covers a different adapter and progress control.
- A temporary `FeedProgressXmlBaselineCaptureTest` passed 1/1 with 0 failures and 0 skips in 43.832s on API 36 `Custom_API_36`; it exported populated-progress and media-list rows with a local cover and avatar fallback. XML baselines: `/tmp/feed-progress-xml-progress.png` and `/tmp/feed-progress-xml-list.png`. The XML capture harness was removed after capture; the Compose capture-only test was removed after comparison.

### Compose implementation and visual comparison

- `FeedProgressRow` is a stateless composable with `@Preview`. `ProgressFeedViewHolder` and `ListFeedViewHolder` now host it in pool-safe `ComposeView`s using `DisposeOnDetachedFromWindowOrReleasedFromPool`; `FeedListAdapter` retains the canonical model, action dispatch, binding epoch, and recycling ownership.
- `FavouriteActionController` remains in the holder-owned ComposeView. `FeedDeleteAction` retains `StatusDeleteWidget` behind its existing `AndroidView` boundary. Progress/list actions remain routed through the adapter callbacks.
- `AniTrendImage` uses Coil for nonblank models and emits no image for absent models. The cover's surface background is its empty state, so a null-media rebind clears the prior cover. `AniTrendAvatarImage` preserves the `avatar_placeholder` color while loading and the `ic_emoji_sweat` error drawable for missing or failed avatars.
- `FeedProgressRowComposeTest` covers progress/list semantics and actions, pending favorite/delete disablement, the missing-avatar error image, and cover clearing after rebind. All 4 retained instrumentation tests passed on API 36 `Custom_API_36`.
- Both rows were visually compared with the XML baselines on API 36 `Custom_API_36`. XML images contain transparent outer corners; the Compose rows were captured from the activity window because Coil hardware bitmaps cannot be drawn with `View.drawToBitmap`. After compositing XML transparency over the Compose window background, the screenshots are 1032x862:

| State | Size | Mean absolute RGB delta/channel | Pixels with any channel delta over 20/255 |
|---|---:|---:|---:|
| Progress | 1032x862 | 11.58/255 | 14.49% |
| Media list | 1032x862 | 11.00/255 | 14.04% |

- Visual inspection shows matching card/context geometry, avatar fallback, cover crop, title wrapping, and footer states. Remaining pixel deltas are concentrated in image and glyph rasterization across the View and Compose capture paths.

### XML removal and next candidate

- `adapter_feed_progress.xml` was removed after recursive source searches found no `adapter_feed_progress`, `AdapterFeedProgressBinding`, `FeedProgressBinding`, `R.layout.adapter_feed_progress`, or `@layout/adapter_feed_progress` consumers. The progress/list holders no longer use ViewBinding; `AvatarImageView` and `AspectImageView` remain because other production rows still use them.
- The next ranked XML candidate is `adapter_feed_message.xml`; the scheduled fortieth slice performs the remaining Compose image cutover first.

### Full verification gate

| Check | Result |
|---|---|
| `:app:spotlessCheck` | Passed. |
| `:app:testAppDebugUnitTest` | 1,285 tests, 0 failures, 3 skipped; 19.473s. |
| `:app:testGithubDebugUnitTest` | 1,285 tests, 0 failures, 3 skipped; 19.435s. |
| `:app:connectedAppDebugAndroidTest` | 206 tests, 0 failures, 0 skipped; 15m39.72s on API 36 `Custom_API_36` (`emulator-5554`). |
| `:app:assembleAppDebug` and `:app:assembleGithubDebug` | Both APKs exist. |
| `validate-changelogs.sh` | Passed. All six store changelog files are within 500 characters (136, 491, 468, 476, 287, and 428). |

Debug APKs: `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.

- The combined shell invocation hit its 1,800-second limit; the Gradle daemon reported `BuildActionResult.Success`. Changelog validation passed in a separate run.



## Fortieth Compose slice: Coil image loading cutover

### Pre-cutover Compose image surface inventory

- Coil 3.6.3 is pinned in `gradle/libs.versions.toml:28,106-108` and applied by `app/buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt:53-57`: `coil-compose`, `coil-network-okhttp`, and `coil-gif`. The [Coil network guide](https://coil-kt.github.io/coil/network/) says the OkHttp module enables HTTP models automatically; the [Coil 3.6.3 GIF guide](https://github.com/coil-kt/coil/blob/3.6.3/coil-gif/README.md) says GIF decoding is auto-detected. The table below records the legacy View behavior found before cutover. The shared `AniTrendImage` now handles nonblank models with the `ic_emoji_sweat` error drawable and a 350ms default crossfade; `AniTrendAvatarImage` retains the circular crop and `colorPrimaryDark` background. No app-specific Coil `ImageLoader` or OkHttp client is configured.

| Surface and source | Model input and current image behavior | Owner, lifecycle, target, and coverage |
|---|---|---|
| Feed status avatar: `ui/feed/FeedStatusRow.kt:284-337` | `FeedItemUiModel.userAvatarUrl`; `AvatarImageView` uses `avatar_placeholder`, global `ic_emoji_sweat` error, 150ms crossfade and circle crop. Preview shows an initial. | `FeedListAdapter.StatusFeedViewHolder`; profile action currently stores and passes the actual `AvatarImageView`. `FeedStatusRowComposeTest` asserts its type/id and fallback pixels. `RichMarkdownTextView` body remains Markwon/Glide. |
| Comment reply avatar: `ui/feed/CommentReplyRow.kt:243-311` | `CommentReplyUiModel.userAvatar`; keyed `AvatarImageView`, same placeholder/error, 150ms circle crop. Preview shows an initial. | `CommentListAdapter.CommentViewHolder` rebinds and clears the image; profile callback passes the avatar View, but `CommentFragment.openProfile` ignores it. `CommentListAdapterComposeTest`; Markwon body remains View-owned. |
| Latest media cover: `ui/media/MediaLatestRow.kt:67-95,155-174` | `coverImage.extraLarge ?: large`; `AspectImageView`, center crop, 350ms, width 104dp and `KeyUtil.AspectRatio`; null/blank no-op. | `MediaLatestAdapter` anime/manga holders use a request-key guard and clear on release; clicks target the ComposeView. `MediaLatestRowComposeTest` currently inspects the ImageView drawable/release. |
| Character media header cover: `ui/character/CharacterMediaHeaderRow.kt:50-100` | `media.coverImage` (`extraLarge ?: large`); `AspectImageView`, center crop, `avatar_size_xl` width and `KeyUtil.AspectRatio`. | `CharacterAdapters.CharacterMediaHeaderViewHolder` owns card click/long-click and releases the ImageView; target is ComposeView. `MediaHeaderComposeTest` inspects `AspectImageView`. |
| Character cover: `ui/character/CharacterRow.kt:40-82` | `imageUrl` is `extraLarge ?: large`; `AspectImageView`, center crop, full-width measure. | `MediaCharacterAdapter` and `GroupCharacterAdapter`; key guard, Glide clear on change/release; row click targets ComposeView. `CharacterRowComposeTest`. |
| Staff cover: `ui/staff/StaffRow.kt:47-103` | `StaffRowUiState.image` (`extraLarge ?: large`); `AspectImageView`, center crop, full-width measure. | `StaffAdapter`, `MediaStaffRoleAdapter`, and `CharacterAdapters` host it. `UnboundStaffImageTag` is used; a changed key does not clear the prior Glide request before replacement. `StaffAdapterComposeTest`. |
| Staff character-role cover: `ui/staff/StaffCharacterRoleRow.kt:37-73` | Character `imageUrl` (`extraLarge ?: large`); `AspectImageView`, center crop, full-width measure. | `StaffAdapters.CharacterViewHolder`; request-key guard and Glide clear on update/release. Card action targets ComposeView. `StaffCharacterRolesComposeTest`. |
| User avatar: `ui/user/UserRow.kt:49-101` | `UserRowUiState.avatar`; `AvatarImageView.setImage(ImageBase)` uses `.large`, 150ms circle crop, `avatar_placeholder`, and global error icon. | `UserAdapter.UserViewHolder`; identity sentinel and clear on release; row click targets ComposeView; `FollowStateWidget` remains AndroidView. `UserAdapterComposeTest` currently inspects the avatar View/drawable. |
| Episode thumbnail: `ui/episode/EpisodeRow.kt:43-113` | `episode.thumbnail.firstOrNull()?.url`; `WideImageView`, center crop, 350ms; null URL loads `reg_bg`. Its measure is `((heightSpec - sm_margin) * KeyUtil.WideAspectRatio)`, using `app_bar_height` when unbounded. | `EpisodeAdapter.EpisodeViewHolder` maps click/long-click through `findViewById(R.id.series_image)`; `FragmentChannelBase` branches on that id. Keep this target contract. `EpisodeAdapterComposeTest` checks target id and row actions. |
| Giphy result image: `ui/giphy/GiphyResultRow.kt:29-77` | `GIPHY_PREVIEW ?: GIPHY_ORIGINAL_ANIMATED`; inline Glide, center crop, 250ms; null clears. Aspect is 4:3.3; `BrandImageView` draws the top-right GIPHY badge. | `GiphyAdapter.GiphyViewHolder` owns tap/long-press on ComposeView and nulls row state on recycle. `GiphyAdapterComposeTest` currently checks labels, ratio, and actions with a static local image. |
| Feed slide image: `ui/media/FeedSlideRow.kt:43-92` | `FeedSlideState.imageUrl`; inline Glide, 250ms, `CenterCrop` or `CenterInside`, optional `RoundedCorners(md_margin)`; blank clears. | `ImagePreviewAdapter.PreviewHolder` sets transition names on both host ComposeView and inner WideImageView; keep the host transition name. The image-preview launcher/callback seams are dormant but source-pinned. `FeedSlideComposeTest` currently inspects WideImageView/dimensions. |
| Review banner: `ui/review/ReviewBannerRow.kt:61-151` | `review.media.bannerImage`; `WideImageView`, center crop, 350ms; null loads `reg_bg`. Preview uses `reg_bg`. | `ReviewAdapter.ReviewBanner` forwards the actual `series_image` View to `BrowseReviewFragment`/`MediaFragment` for image click/long-click. Read-more and `CustomRatingBar` are separate View leaves. `ReviewAdapterVoteComposeTest`. |
| Series review avatar and cover: `ui/review/ReviewSeriesRow.kt:71-170,335-371` | Avatar `review.user.avatar` uses `.large`, 150ms circle crop, `avatar_placeholder`/global error; cover `review.media.coverImage` uses `extraLarge ?: large`, center crop, 350ms, 88dp width and `KeyUtil.AspectRatio`; blank cover is explicitly cleared. | `ReviewAdapter.ReviewDefault` forwards `user_avatar` and `series_image` Views to `MediaFragment.openReview/longPressReview`, which dispatch by ID. Read-more, rating, and Markdown remain View-owned. `ReviewAdapterVoteComposeTest` currently inspects the cover View/drawable. |
| Drawer avatar: `ui/profile/AvatarIndicator.kt:52-120` | `AvatarIndicatorState.avatarUrl`; initializes with `R.mipmap.ic_launcher_round`, requests avatar only when `hasRenderedAvatar`, then uses 150ms circle-crop Glide helper/global error. | `MainActivity` owns navigation and click behavior; no image View target is consumed. `AvatarIndicatorComposeTest` covers badge and click semantics. |

### Retained View-owned paths and action contracts

- `EpisodeAdapter` retains `R.id.series_image` on its `ComposeView` because `FragmentChannelBase` locates that action target. `ImagePreviewAdapter` sets and clears the shared transition name on the host `ComposeView`; no inner `WideImageView` remains. `ReviewAdapter` forwards image actions using the bound review record and keeps vote and read-more ownership in the adapter.
- Feed and comment profile/media callbacks remain in their existing adapter and Fragment owners. The remaining `AndroidView` leaves in migrated rows are non-image components such as Markwon, ratings, status/follow controls, delete confirmation, and read-more.
- Markwon images remain Glide-owned. `RichMarkdownTextView` stays hosted through `AndroidView` in feed, comment, review, ranking, and changelog surfaces; `GlideImagePlugin` retains Markwon's Glide integration and User-Agent headers. No Markwon image request moved to Coil.
- `AvatarImageView`, `AspectImageView`, `HeaderImageView`, and `WideImageView` remain for active View/XML consumers in feed-message, media/detail/profile screens, and the Giphy preview activity. `BrandImageView.kt` was removed after a source-wide `app/src` search found no consumer; `GiphyResultRow` now draws its badge in Compose.
- The migrated Compose row tests now assert rendered pixels, fallbacks, semantics, and rebind/recycle behavior rather than `ImageView` drawables or Glide targets. The source audit found no image `ImageView` or Glide usage in the migrated Compose rows.

### Coil implementation and regression coverage

- Every listed Compose-owned remote or resource image now uses `AniTrendImage` or `AniTrendAvatarImage`. XML/View-only Glide requests and Markwon image requests remain unchanged. Missing and blank models follow each row's explicit null fallback or clear behavior; failed nonblank requests use the shared `ic_emoji_sweat` error painter.
- `FeedSlideRow` supplies a painter content shape. `AniTrendImage` observes Coil painter intrinsic size and clips the `ContentScale`-fitted painter bounds, so a letterboxed `Inside` image receives rounded corners instead of only rounding the full-width container. `FeedSlideComposeTest` covers fitted inside/crop pixels, a model rebind between scale modes, the host transition name, playback overlay, dimensions, and click semantics.
- `ReviewSeriesRow` no longer passes `errorResource = null`. `ReviewAdapterVoteComposeTest` now rebinds a review to an invalid local resource and verifies the shared error drawable before checking that a missing-media rebind clears it; the focused class passed 10 tests on `emulator-5554`.
- `EpisodeRow` retains `reg_bg` for a null thumbnail and uses the shared error icon for blank thumbnails, preserving the legacy measured image height. `EpisodeAdapterComposeTest` covers null and whitespace cases.
- `ReviewBannerRow` retains `reg_bg` for missing or blank banners and uses the shared error painter for failed nonblank requests. `ReviewAdapterVoteComposeTest` checks the banner error icon through its vertical overlay.
- `StaffAdapters` compares the rendered character-role fields for content changes and projects models into immutable `CharacterRoleRowState` values. This avoids `CharacterStaffBase.equals`'s id-only equality suppressing Compose updates. `StaffCharacterRolesComposeTest` covers same-id favorite changes, cover clearing/restoration, titles, and removal; the focused class passed 3 tests on `emulator-5554`.
- `GiphyAdapterComposeTest` uses a deterministic two-frame GIF, waits for the alternate frame, verifies preview-over-original selection and an Android resource model, and preserves the 4:3.3 image ratio and row actions.

### Saved XML/Compose visual comparisons

- Means below are average absolute RGB-channel differences on a 0 to 255 scale after compositing XML transparency over the Compose background.

| Surface/state | Saved XML / Compose captures | Size | Mean difference |
|---|---|---:|---:|
| Latest media anime | `/tmp/latest-media-anime-xml-formatted.png` / `/tmp/latest-media-anime-compose-final.png` | 1032x427 | 1.60/255 |
| Latest media manga | `/tmp/latest-media-manga-xml-formatted.png` / `/tmp/latest-media-manga-compose-final.png` | 1032x427 | 1.06/255 |
| Staff standard/favorite | `/tmp/staff-row-xml-standard-latest.png` / `/tmp/staff-row-compose-standard.png`; `/tmp/staff-row-xml-favorite-stable.png` / `/tmp/staff-row-compose-favorite.png` | 360x580; 360x635 | 3.63 to 3.89/255 |
| User self/following/follow | `/tmp/user-row-xml-self-final.png` / `/tmp/user-row-compose-self-final.png`; `/tmp/user-row-xml-following-final.png` / `/tmp/user-row-compose-following-final.png`; `/tmp/user-row-xml-follow-final.png` / `/tmp/user-row-compose-follow-final.png` | 1080x168; 1080x204 | 1.98 to 2.16/255 |
| Feed status other-user/owner/empty | `/tmp/feed-status-xml-other-user.png` / `/tmp/feed-status-compose-other-user.png`; `/tmp/feed-status-xml-owner.png` / `/tmp/feed-status-compose-owner.png`; `/tmp/feed-status-xml-owner-empty.png` / `/tmp/feed-status-compose-owner-empty.png` | 1032x526 | 1.77 to 1.82/255 |
| Review banner missing media | `/tmp/review-banner-xml-missing-media.png` / `/tmp/review-banner-compose-missing-media.png` | 1032x865 | 0.39/255 |
| Series review populated/missing content | `/tmp/review-series-xml-populated.png` / `/tmp/review-series-compose-populated.png`; `/tmp/review-series-xml-missing-content.png` / `/tmp/review-series-compose-missing-content.png` | 1032x601 | 0.11 to 0.65/255 |
| Character media header | `/tmp/character-media-header-xml-final.png` / `/tmp/character-media-header-compose-final.png` | 360x335 | 2.37/255 |
| FeedSlide carousel/video | `/tmp/feed-slide-current-xml-carousel-image-latest.png` / `/tmp/feed-slide-compose-carousel-image-fixed.png`; `/tmp/feed-slide-xml-youtube-latest.png` / `/tmp/feed-slide-compose-youtube-fixed.png` | 1032x2231; 1080x2276 | 0.00/255 |

- Review-banner populated captures remain 4px different in height (XML 1032x985, Compose 1032x989); the earlier review-banner record documents the visual inspection and text-dominated delta. Episode and Giphy have no retained standalone screenshot pairs; their connected contracts verify the rendered image pixels, aspect/target behavior, resource fallback, and GIF frame advancement.

### Final verification gate

- The final gate invoked `:app:spotlessCheck`, `:app:testAppDebugUnitTest`, `:app:testGithubDebugUnitTest`, `:app:connectedAppDebugAndroidTest`, `:app:assembleAppDebug`, and `:app:assembleGithubDebug` with the low-worker Gradle profile. Spotless passed; each unit report records 1,285 tests, 0 failures, and 3 skipped.
- Connected instrumentation on Custom_API_36 (AVD), serial `emulator-5554`, records 211 tests, 0 failures, and 0 skipped (19m41.60s).
- Debug APKs were generated at `app/build/outputs/apk/app/debug/app-debug.apk` and `app/build/outputs/apk/github/debug/github-debug.apk`.
- `bash .github/scripts/validate-changelogs.sh` passed; all six store changelog files remain below 500 characters, with the longest at 491.

## Forty-first Compose slice: search and favourites section grids

### Candidate audit and contracts

- `SearchFragment` and `FavouriteFragment` swap `RecyclerSectionAdapter` delegates per tab (`select(createAdapter(...))`): `MediaAdapter` for the media tabs, `GroupCharacterAdapter`/`StaffAdapter`/`StudioAdapter` for the CHARACTERS/STAFF/STUDIO(S) tabs, and `UserAdapter` for the search USERS tab. The three delegate adapters hosted rows that earlier slices already migrated to Compose (`CharacterRow`, `StaffRow`, `StudioRow`) inside pooled `ComposeView` holders with per-item Compose state (the character and staff holders used `neverEqualPolicy()`, the studio holder plain `mutableStateOf`).
- `GroupingUtil.wrapInGroup` was an identity copy (`ArrayList(data)`), so the character tabs have always rendered flat lists; the real grouping (`groupCharactersByRole` and siblings) belongs to the detail sections and remains unchanged.
- Paging ran through `RecyclerScrollListener` on the shared RecyclerView (visible threshold 9, first-visible formula) with `mScrollListener.currentPage` feeding each section ViewModel's `load(...)`; section clicks route by model type (`CharacterBase`/`StaffBase`/`StudioBase` → typed detail screens).

### Compose implementation

- New keyed lazy grids beside their rows: `StudioGrid` (`grid_list_x2`), `StaffGrid` and `CharacterGrid` (`grid_giphy_x3`), each rendering projected immutable row states (`StudioRowUiState`, `StaffRowUiState` plus its `id`, `CharacterRowUiState`) in `Card` surfaces with `colorSurfaceContainerLow` fill, `lg_margin` corners and item spacing, `sm_margin` elevation, and `xl_margin` content padding. Row composables own clicks (`StudioRow` gained the `onClick` seam for parity with `CharacterRow`/`StaffRow`); navigation remains in the fragments via `navigateToCharacter`/`navigateToStaff`/`navigateToStudio`.
- `LazyGridLoadMoreEffect` centralizes the legacy first-visible load-more formula (threshold 9) in `ui/media/LazyGridLoadMore.kt`; `LatestMediaList` now shares it.
- Both fragments host one `ComposeView` as a sibling of the `FragmentListBinding` root inside each screen's `*_list_container` FrameLayout, because `CustomSwipeRefreshLayout` measures only its single target child and would lay out a second content child at zero size. `section` is observable state; the section switch toggles the RecyclerView and Compose surfaces.
- Compose sections carry their own paging state (`composePage`, `composeHasMore`, and a `composeLoading` in-flight guard that a blank search query cannot wedge) and merge pages into `referentialEqualityPolicy()` lists of projected states; `commitComposePage` reproduces the `onPostProcessed` empty/limit semantics. The query-change, section-switch, and swipe-refresh paths reset paging and items exactly as the adapter paths did.
- No intended UX delta: the tabs render the same rows, column counts, and click targets through the grids.

### Removals

- Deleted `GroupCharacterAdapter`, `StaffAdapter`, `StudioAdapter`, their `createAdapter` branches in both fragments, and `GroupingUtil.wrapInGroup` after zero-consumer audits. The search USERS tab and both media tabs keep `UserAdapter`/`MediaAdapter` on the shared `RecyclerSectionAdapter` shell for later slices.

### Verification

| Task | Tests | Failures | Skipped |
|---|---:|---:|---:|
| `:app:testAppDebugUnitTest` | 1,288 | 0 | 3 |
| `:app:testGithubDebugUnitTest` | 1,288 | 0 | 3 |
| `:app:connectedAppDebugAndroidTest` | 211 | 1 (environment flake) | 0 |

- Targeted runs on `emulator-5554` (`Custom_API_36`): `StudioGridComposeTest` 2/2, `StaffGridComposeTest` 3/3, `CharacterGridComposeTest` 2/2. Spotless, both debug assembles, and `validate-changelogs.sh` passed.
- Each full connected run's single failure was environmental rather than behavioral, and both classes passed targeted reruns: `LoginShortcutIntentTest.myAnimeShortcutIntentCarriesTypedIdentityRouteAndPushedOrigin` (45s cold-start intent timeout while the per-test logcat recorded an emulator wifi drop, a 37s FocusEvent stall, and HWUI/JNI main-thread contention; rerun 2/2) and `LoggingFragmentTest.activityLaunches_withComposableAndParsedEntries` (Compose-Espresso idling timeout under a 25s HWUI Davey, wifi disconnect, and a startup 401; rerun 7/7). Both classes cover surfaces untouched by this slice and were green in the preceding full gate.
- Live pass on the installed `app-debug.apk` (`emulator-5554`) verified the search screen end-to-end: query-driven loading, the section chips switching surfaces (studio chip checked state observed), `StudioGrid` two-column rows (WIT STUDIO, Studio Ghibli, Kyoto Animation, MADHOUSE, MAPPA and others), `StaffGrid` rows with language captions (Kana Hanazawa, Kana Ichinose and others), `CharacterGrid` rows (Kana Arima, Tanjirou Kamado and others), the empty state ("No data to display" with retry) for a non-matching query, and a studio row tap navigating to the WIT STUDIO media list. The favourites screen requires AniList sign-in on this fresh install and was not exercised live; it shares this slice's wiring shape and the same three grid test suites.

## Forty-second Compose slice: episodes feed list and refresh-target host

### Candidate audit and contracts

- `WatchListFragment` (the sole `FragmentChannelBase` subclass) hosted `EpisodeAdapter` rows: the row was already Compose (`EpisodeRow(title, duration, imageUrl, onClick, onLongClick)`) inside a bare `ComposeView` holder carrying `R.id.series_image` (the `FragmentChannelBase` episode action target) with `lg_margin` item margins; `formatDuration` lived adapter-locally (seconds to `m:ss`, `00:00` fallback) and deliberately outside the presenter lane.
- The lane is feed-only from the live entries (`navigateToWatchList` writes `ARG_FEED_ROUTE` + `arg_popular`; AiringFragment's feed chip, HubFragment's button, and the drawer item); the `newInstance(externalLinks, popular)` variant has zero call sites since the pager hosts were deleted. Clicks route through the base's `ItemClickListener` (the `R.id.series_image` dialog with Watch/Dismiss/Search), and `onChanged(Rss)`/`updateUI`/`onStart` drive the load state.
- `Episode` has no stable domain id (RSS input), so position is the identity fallback for list keys.

### Compose implementation

- New `ui/episode/EpisodeList.kt`: `EpisodeRowUiState(id, title, duration, imageUrl)` (position keys; `formatDuration` moved into the projection) and `EpisodeList` as a `LazyVerticalGrid(GridCells.Fixed(single_list_x1))` host with `lg_margin` row padding matching the retired adapter's item margins.
- `WatchListFragment` hosts the `ComposeView` (id `R.id.series_image`, `DisposeOnViewTreeLifecycleDestroyed`) and overrides `onChanged(Rss)` (projects rows into state), `updateUI()`, and `hasContent()` (extracted in `FragmentChannelBase` so Compose hosts stop consulting the dead `mAdapter`), and forwards row clicks by index to the base's `ItemClickListener` with the id-carrying host as target.

### Live-found defects fixed in this slice

- The feed route could never reach `getLatestFeed`/`getPopularFeed`: `makeRequest`'s gate tested `externalLinks != null`, which the nav entry never sets, so every feed navigation fell into `getMediaEpisodes(id = 0)` (HTTP 404 "Not Found.") whose failure path only logs, leaving the screen loading forever. The gate is now `externalLinks != null || targetLink != null`, preserving the media-entry link resolution path and its missing-links empty state.
- `CustomSwipeRefreshLayout` measures only its single cached target, and `ensureTarget()` resolved (and pinned) the RecyclerView before the fragment could insert the Compose host, leaving the host at 0x0 (confirmed in `dumpsys activity top`). `ensureTarget()` now drops a cached target that left the container, `onMeasure`/`onLayout` resolve it unconditionally instead of only when `target == null`, and `WatchListFragment` removes the legacy RecyclerView from the refresh container so the Compose host owns the target slot.

### Removals

- Deleted `adapter/recycler/index/EpisodeAdapter.kt` after zero-consumer checks and migrated `EpisodeAdapterComposeTest` to `EpisodeListComposeTest`, which hosts the grid directly: title/duration projection ("1421" to "23:41", invalid to "00:00"), click and long-click routing by index, null-thumbnail resource fallback, blank-thumbnail shared error drawable, and the legacy image height contract.

### Verification

- `EpisodeListComposeTest` 3/3 and `WatchListComposeTest` 1/1 on `emulator-5554` (`Custom_API_36`); the watch-list smoke drives the real airing entry (feed chip) and settles on rows or the shared empty/error state.
- The manual live pass on this emulator was environmentally blocked (cold-start ANR "failed to complete startup" after dexopt, persistent 30-40-frame Choreographer skips, and adb shell churn). It did confirm the feed request reaching `https://feeds.feedburner.com/crunchyroll/rss` (HTTP 200) and exposed both defects fixed above before the instrumented smoke took over as the behavioral proof.
- Full gate: `spotlessCheck` and both unit variants green (1,288 tests, 0 failures, 3 skipped each), connected instrumentation on `emulator-5554` green (213 tests, 0 failures, 0 skipped), both debug assembles built, and `validate-changelogs.sh` passed (longest file 491 characters).

## Forty-third Compose slice: notifications list

### Candidate audit and contracts

- `NotificationListAdapter` was a plain `ListAdapter` (DiffUtil by stable server notification id) with three view types: SOCIAL and FORUM shared `item_notification_person.xml` (only the fallback drawable differed: account-circle vs comment icon) and MEDIA used `item_notification_media.xml`. Both rows are `Widget.AniTrend.ManageSheet.SectionCard` cards (surfaceContainer, 0dp elevation, 16dp content padding, lg_margin margins) with an unread indicator bar (md_margin primary), subject/time/header/content text stack (bold header when unread), and an image: leading 100dp aspect cover for media rows, trailing 48dp circular avatar for person rows.
- `NotificationFragment` (the sole consumer) is a `FragmentBaseList` shell fed by `NotificationViewModel` StateFlow pages, projecting `NotificationRecord` into immutable `NotificationItemUiModel(id, record, isRead)` with append-on-pagination semantics and ObjectBox `NotificationHistory` read-state persistence. Row clicks route by type (comment/profile/media/forum intents) with an avatar-click split (`R.id.notification_img` target id), and long-click is AIRING-only (series manage action).

### Compose implementation

- New `ui/notification/NotificationList.kt`: `NotificationRowUiState` reproduces the adapter's bindings verbatim (the 15 type-to-subject mappings, per-type header/content rules, PrettyTime relative dates, unread description suffix, and per-view-type image fallbacks), `NotificationRow` renders the person/media card layouts with the `SectionCard` chrome, and `NotificationList` is a keyed `LazyVerticalGrid(single_list_x1)` host with the shared `LazyGridLoadMoreEffect` paging trigger.
- `NotificationFragment` hosts the ComposeView in the refresh container's target slot (the self-healing `CustomSwipeRefreshLayout` from the episodes slice serves it), keeps every side effect (read-state writes, mark-all menu, forum intents, MediaActionUtil), splits row/avatar/long dispatch explicitly, and pages through `composePage`/`composeHasMore` state feeding `notificationViewModel.load(page)`.

### Defect found and fixed during migration

- `AniTrendImage` renders nothing for null models, but the retired Glide bindings always painted a fallback drawable and kept the avatar tap target alive. The row now renders `painterResource(imageErrorResource)` with the same size/shape/tag/click chain whenever the URL is null or blank (common for notifications without avatars), restoring that parity.

### Removals

- Deleted `adapter/recycler/detail/NotificationListAdapter.kt` and `NotificationListAdapterDiffTest` (its DiffUtil contract died with the adapter). `NotificationItemUiModelProjectionTest`, `NotificationRecordNavigationTest`, `NotificationViewModelTest`, and `UserRepositoryNotificationsTest` remain green unchanged; the row/routing contracts moved to `NotificationListComposeTest` (per-type projection and read chrome, row/avatar/long-click routing split, stable-id keys).

### Verification

- `NotificationListComposeTest` 3/3 on `emulator-5554` (`Custom_API_36`). Full gate: `spotlessCheck` and both unit variants green (1,284 tests, 0 failures, 3 skipped each; the -4 versus the prior gate is the deleted `NotificationListAdapterDiffTest`), connected instrumentation green (216 tests, 0 failures, 0 skipped), both debug assembles built, and `validate-changelogs.sh` passed.

## Forty-fourth Compose slice: media search browse list (Paging3)

### Candidate audit and contracts

- `MediaSearchAdapter` was already a Paging3 `PagingDataAdapter` over immutable `MediaSearchItemUiModel` (stable server id) with a single view type, the shared `adapter_series.xml` card (aspect cover, coloured status strip, title, year/type caption, and a `RatingTextView` score/favourite overlay). Its sole consumer was SearchFragment's ANIME/MANGA tabs; FavouriteFragment uses `MediaAdapter` and keeps `adapter_series.xml` alive with the other shared consumers.
- Pages flow through `MediaSearchViewModel.pagingDataFlow` (query-keyed `Pager`, `cachedIn`), with query submits and pull-to-refresh calling `adapter.refresh()`; `PagingLoadStateRenderer` (item-count-gated) drives the shared ProgressLayout surfaces and `LoadStateFooter` handles append states.

### Compose implementation

- Added `androidx.paging:paging-compose` 3.5.3 (catalog + `ProjectDependencies`), the first paging-compose adoption in the repo. New `ui/mediasearch/MediaSearchList.kt`: `MediaSeriesCardUiState` reproduces the adapter's bindings (status colours, the year/type caption rules incl. TBA and episode/chapter suffixes), `MediaSeriesCard` renders the card with the `RatingTextView` overlay kept through `AndroidView` interop for score-format parity, and `MediaSearchPagingList` is a keyed 3-column `LazyVerticalGrid` over `LazyPagingItems` with the shared `LoadStateFooter` append state (indexed `items(count)` bridge; the paging-compose `items` extension does not exist in 3.5.3).
- SearchFragment's media tabs host the list in the refresh container's target slot via `swapRefreshTarget`; the Compose-backed USERS list also uses that target slot and swaps back to media. Media refresh requests hoist through a `mediaRefreshSignal` state consumed by `LaunchedEffect`, and media load states flow into the existing `PagingLoadStateRenderer` through fragment state. The unreachable `createAdapter` media arm and the `mediaAdapterWithFooter` concat are deleted.

### Defects found and fixed during migration

- Coverless cards collapsed to captions because `AniTrendImage` emits no node for null models; the cover now reserves its `AspectImageView` bounds in an unconditional box (the `MediaLatestRow` pattern) with the image filling it.
- Scroll gating: `CustomSwipeRefreshLayout` gates its refresh/load gestures on `target.canScrollVertically`, which ComposeView cannot answer (it does not delegate to its internal scrolling child; it is also a final class, so a subclass bridge is impossible). Mid-list downward drags were therefore hijacked as refresh gestures on every Compose host placed in the container. The container now accepts an optional `scrollBoundary` oracle (direction -1/1) with the target's own answer as fallback, and the episodes, notifications, and search hosts bridge it from their `LazyGridState`; `CustomSwipeRefreshBoundaryTest` covers both the oracle and the fallback.

### Removals

- Deleted `adapter/recycler/search/MediaSearchAdapter.kt` (zero-consumer verified). The paging source, ViewModel, mapper, renderer, and footer unit tests remain green unchanged; the row contracts moved to `MediaSearchListComposeTest` (title/year-type caption rules incl. TBA, cover bounds reservation, card/long-click routing).

### Verification

- `MediaSearchListComposeTest` 2/2, `CustomSwipeRefreshBoundaryTest` 2/2, and `SearchMediaTabsComposeTest` 1/1 on `emulator-5554` (`Custom_API_36`); the integration smoke drives the real fragment entry (search route + query), asserts the mid-list downward drag scrolls the grid while refresh stays inactive, and exercises the tab round-trip whose cached load state must dismiss the selection loading surface. The three host smokes (`WatchListComposeTest`, `FavouriteSectionsComposeTest`, `NotificationListComposeTest`) stay green.
- Test-environment note: fresh installs raise the notification-permission dialog and the first-run "What's New" sheet, both of which prevent the routed destination from composing; the smoke grants the permission deterministically (verified via `checkSelfPermission`) and dismisses the sheet when present. Full gate: `spotlessCheck` and both unit variants green (1,284 tests, 0 failures, 3 skipped each), connected instrumentation green (221 tests, 0 failures, 0 skipped), both debug assembles built, and `validate-changelogs.sh` passed.





## Forty-fifth Compose slice: media and airing lists

### Candidate audit and contracts

- `MediaListAdapter` was the RecyclerView row host for media-list and airing-list screens. `MediaListFragment` and `AiringListFragment` keep their existing StateFlow-backed success payloads, progress mutations, manage sheet, navigation, filter settings, and unfiltered empty-state semantics; only their row presentation moves to Compose. `AiringFragment` independently builds the same refresh/list container and inherits the shared host swap through `AiringListFragment`.
- Search filtering now belongs to each screen's ViewModel and matches the preferred, English, or original media title. A search with no matching rows leaves the unfiltered `UiState.Success` payload intact, so it does not become an empty-list or reload condition.

### Compose implementation

- `MediaListFragment` replaces the RecyclerView target with a lifecycle-disposed `ComposeView` hosting the existing stateless `MediaListGrid`; `LazyGridState` supplies the refresh-boundary oracle. `AiringListFragment` selects `AiringListViewModel.display` through the same protected list-state seams, so its own state and filtering remain authoritative.
- Mutation restoration preserves the existing target/revision gate and predecessor search. It captures the first visible lazy-grid item and `firstVisibleItemScrollOffset`, then restores to the same item, nearest predecessor, or item zero after the new rows have committed.

### Removals

- Deleted `MediaListAdapter`, its adapter-backed DiffUtil and progress tests, and moved the row contracts to `MediaListGridComposeTest`: expanded and compact X1/X2 projection, nonzero compact status-stripe height, hidden X2 progress pill, title/year-type/airing metadata, click and long-click routing, and increment callback routing.

### Verification

- `MediaListGridComposeTest` 2/2 on `emulator-5554` (`Custom_API_36`): expanded and compact X1/X2 projection (title, year-type, airing captions asserted through the native interop view tree), nonzero compact status-stripe height, hidden X2 progress pill, click/long-click routing, increment routing. The ViewModel suites cover query filtering (including the no-match case that keeps the unfiltered payload) and the atomic display snapshot contract: a reorder or removal response pairs rows and entries from one success, which is what keeps viewport-anchor restoration from consuming a revision against stale row order.
- Post-cutover hardening: search filtering lives in each ViewModel (`MediaListViewModel`/`AiringListViewModel`); rows and entries are emitted as one `MediaListDisplayState` snapshot instead of two independently scheduled flows; compact status stripe sizes against the completed row via `matchParentSize`; caption assertions target the native interop views (Compose semantics do not see `SeriesYearTypeTextView`/`AiringTextView` text).
- Full gate: `spotlessCheck` and both unit variants green (1,284 tests, 0 failures, 3 skipped each), connected instrumentation green (220 tests, 0 failures, 0 skipped, exit 0), both debug assembles built, and `validate-changelogs.sh` passed.

## Forty-sixth Compose slice: user lists

### Candidate audit and contracts

- `UserAdapter` hosted the existing Compose `UserRow` through RecyclerView and was used by SearchFragment's USERS tab and both user sheets. The row projection must snapshot mutable `UserBase` presentation data because entity equality is id-only; follow convergence replaces projections, including the `isFollowing` value used by data-class equality.
- `BottomSheetUsers` receives `UserSheetModel`, not parcelled `UserBase`; its mapper and boundary contract remain intact. The shared `bottom_sheet_list.xml` also served `BottomSheetGiphy`, so Giphy retains its RecyclerView host through the new dedicated `bottom_sheet_giphy.xml`.

### Compose implementation

- Added `ui/user/UserList.kt`, a responsive `LazyVerticalGrid` using `single_list_x1`, stable user-id keys, `md_margin` bottom spacing, and the existing `UserRow` with the current user passed through.
- SearchFragment now swaps a dedicated users ComposeView into the refresh target, routes row/follow callbacks, and uses `LazyGridState` near-end observation for the existing manual page requests. StateFlow page success runs through `onPostProcessed` into fresh row projections; the list's scroll boundary and follow-state convergence are bridged without adapter notifications.
- Both user sheets host `UserList` through the shared ComposeView binding. BottomSheetListUsers retains manual pages, retry surfaces, and UserStore convergence; refresh and retry re-request page 1 and replace the rows (including an empty result) so keyed rows cannot duplicate. BottomSheetUsers forwards name-query changes to its ViewModel-owned filter and keeps its supplied-list parcel boundary.

### Removals

- Deleted `UserAdapter.kt` after confirming zero remaining consumers and migrated the adapter-era row contract to `UserListComposeTest`, which hosts the lazy grid directly. RecyclerView base classes and the separate Giphy RecyclerView host remain.

### Verification

- `UserListComposeTest` 3/3 and `BottomSheetListUsersDialogStateTest` 5/5 on `emulator-5554` (`Custom_API_36`); `SearchMediaTabsComposeTest` 1/1 re-verified after the surface-dismiss fix. Full gate: `spotlessCheck` and both unit variants green (1,288 tests, 0 failures, 3 skipped each), connected instrumentation green (223 tests, 0 failures, 0 skipped), both debug assembles built, and `validate-changelogs.sh` passed.
- Duplicate-key hardening (keyed lazy grids turn legacy row duplication into a crash, so accumulation is now idempotent): StateFlow collectors apply each Success emission instance exactly once (STOPPED to STARTED replays), page 1 replaces rows even when the refreshed result is empty (next-page failure followed by retry no longer appends a second page 1), and page appends filter out already-present domain ids. `BottomSheetListUsersDialogStateTest` carries regressions for the replay, retry, and nonempty-then-empty cases.
- Search surface hardening: returning to a media tab re-renders the cached paging snapshot synchronously (the query-keyed Pager emits no new load state, which left the selection loading surface up and the composed cards hidden behind it), and load state plus item count travel through one observed pair so count-only changes re-trigger rendering.

## Forty-seventh Compose slice: studio and favourites media grids

### Candidate audit and contracts

- StudioFragment and FavouriteFragment were the final targets in this slice to construct `MediaAdapter(context, true)` for their media sections, so both rendered `adapter_series.xml`: cover, status stripe, preferred title, year/type caption, and the RatingTextView overlay at top-start, including the conditional `rating_list_status` icon for list entries. The existing `MediaSeriesCard` in `ui/mediasearch/` already renders that projection; the new `MediaBase` projection snapshots list-entry presence/status and delegates status colour and year/type formatting to the same helpers as the legacy XML bindings. No extra row fields or actions were added.
- The RecyclerView destinations retain their FragmentBaseList refresh/loading/error shell, manual page requests, existing sort/order and section request wiring, and route-specific click/long-click behavior. Keyed page append is domain-id unique, and each success StateFlow instance is applied once. A shared append gate prevents automatic page advancement after errors or empty/duplicate results until a retry returns new rows; page-1 empty results replace stale rows.

### Compose implementation

- Added `ui/media/MediaSeriesGrid.kt`, a keyed lazy grid reusing `MediaSeriesCard`, resource-driven columns, and `LazyGridLoadMoreEffect` with the legacy visible-threshold formula. StudioFragment swaps a lifecycle-bound ComposeView into the refresh target, bridges its `LazyGridState` for scroll-boundary checks, and preserves stored media sort/order requests.
- FavouriteFragment now hosts every section through one ComposeView and `FavouriteGridHost`: ANIME/MANGA render the shared media grid; CHARACTERS/STAFF/STUDIOS keep their existing Compose grids. Tab selection clears and reloads rows while each tab keeps its own grid state and scroll-boundary bridge.

### Removals

- Deleted `RecyclerSectionAdapter.kt`, whose final construction site was FavouriteFragment, and removed its obsolete architecture-test file reference. No adapter-era behavior tests remained to migrate; `MediaSeriesGridComposeTest` directly hosts the grids and checks adapter-series projection and list-status visibility, media click routing, and Favourite media/Compose-grid surface swaps. `ManualGridAppendGateTest` covers failed/empty appends blocking page advancement until a retry contributes rows.
- Kept `MediaAdapter.kt` and its five row layouts. The remaining constructor is `MediaBrowseFragment.kt:163`, which passes `browseUtil.isCompactType`; the browse lane still consumes the adapter and may use either compatibility mode.

### Verification

- `MediaSeriesGridComposeTest` 2/2 and `FavouriteSectionsComposeTest` 2/2 on `emulator-5554`. The deferred-anime-error regression fails before the media-tab guard and passes afterward; Characters stays visible when the old anime request fails.
- `LazyGridLoadMoreComposeTest` fails before and passes after observing layout samples rather than a Boolean near-end result. Short appends can request another page while remaining near the end.
- The full entry-point smoke exposed duplicate media ids within Studio page one. `appendMediaSeriesPage` now deduplicates first pages as well as appends; the append gate counts only newly inserted rows. Both authenticated and unauthenticated entry-point suites passed afterward. `MediaSeriesPageTest` covers duplicate page-one ids and overlapping/replayed pages.

## Forty-eighth Compose slice: media browse grids

### Cutover and contracts

- `MediaBrowseFragment` and its Anime, Manga, Suggestion, and Hub consumers render a direct `LazyVerticalGrid` with the existing refresh shell and scroll-boundary bridge. Compatibility-series, expanded anime/manga, and compact X1/X2 layouts retain their airing caption, rating visibility, status stripe, and click/long-click behavior.
- Browse successes replace the complete ViewModel snapshot, including store recombinations. Suggestion page payloads append once by success identity and deduplicate by media id. Existing filter arguments, pending filter result identities, settings, and request semantics remain unchanged.
- Removed `MediaAdapter` and its four exclusive anime/manga XML rows. Shared series XML remained until the grouped detail and recommendation cutovers below.
- Removed `remember(media)` from the card projection: legacy media equality is id-only, so it could preserve stale title/caption data after a same-id replacement. The row test now changes media type and title under the same id.

### Verification

- `MediaBrowseGridComposeTest` 1/1 verifies all row variants and actions. `MediaBrowseRouteComposeTest` 1/1 launches the existing browse ingress and verifies media navigation.

## Forty-ninth Compose slice: grouped detail lists

### Cutover and contracts

- Media relations, characters, and staff; Character anime/manga/actor roles; and Staff anime/media/staff roles now render immutable `DetailGridRow` snapshots through `DetailListGrid`. Group titles and actor-media headers span the grid; content rows reuse the existing Compose cards.
- `DetailListSection` owns view lifecycle, refresh/retry presentation, manual page gating, and viewport restoration instead of an adapter. Full snapshots replace rows; page payloads merge beneath their original group headers. Stable keys include group identity so the same entity can appear in different role groups.
- Removed the six character/staff detail adapters. After zero-consumer checks, also removed `GroupSeriesAdapter`, `GroupTitleViewHolder`, the unused Recycler loading/unresolved holder classes, shared series/airing row XMLs, and the unused loading/footer XMLs.
- Extended the grouped append regression to a third page: it failed with count 2 instead of 3. Header counts now add unique new rows to the committed header count rather than the incoming page's count.

### Verification

- `DetailListGridRowsTest` 3/3. `CharacterRowComposeTest` 1/1, `MediaHeaderComposeTest` 2/2, `StaffCharacterRolesComposeTest` 3/3, and `StaffRowHostComposeTest` 2/2 exercise the direct lazy grid, row state changes, image semantics, and action targets.
- Live emulator pass: Cowboy Bebop relations display full-width Adaptation/Side Story headers and media cards; selecting Shooting Star Bebop opens the manga destination. Spike Spiegel's anime and manga role groups render, the anime grid scrolls, and actor roles show media headers and voice actors. Selecting Kouichi Yamadera opens Staff; year-grouped anime roles, empty manga roles, and role-grouped staff credits all settle visibly. Screenshot: `/tmp/anitrend-compose-relations.png` is a local verification artifact, not a committed asset.

## Fiftieth Compose slice: Giphy picker

### Cutover and contracts

- `BottomSheetGiphy` hosts `GiphyGrid` directly and retains selection, preview URL behavior, search submission, and loading/error/retry presentation. Removed `GiphyAdapter` and the single-consumer `BottomSheetGiphyList` base.
- `GiphyViewModel` owns query identity, offset paging, accumulated unique-id snapshots, cancellation, and terminal-page gating. Refresh replaces results even when empty; failed or unproductive appends cannot advance automatically.
- Collection uses the fragment lifecycle because this dialog installs content in `onCreateDialog` and does not create a fragment view. Reattaching the dialog does not restart an existing query.

### Verification

- `GiphyViewModelTest` 3/3 covers overlapping pages, terminal metadata, failed append gating, empty refresh, and query replacement. `GiphyGridComposeTest` 1/1 retains local animated-image/thumbnail, aspect, badge, selection, and preview checks. `BottomSheetGiphyDialogStateTest` 1/1 exercises deferred loading, failure, retry, visible rows, and selection through the actual dialog.

## Fifty-first Compose slice: recommendations

### Cutover and contracts

- `MediaRecommendationsSection` now renders Paging 3 `LazyPagingItems` in `RecommendationGrid`, reusing `MediaSeriesCard`. Keys use recommendation ids, not media ids, so separate recommendations of the same media remain distinct.
- Load state and item count travel as one snapshot into the existing renderer. Cached state re-renders on section selection, refresh requests are consumed once, the grid supplies swipe boundaries, and append retry uses the existing Compose footer.
- Removed `RecommendationAdapter`, its obsolete DiffUtil tests, and `LoadStateFooterAdapter`. The footer state projection now lives with the direct Compose footer; its behavior tests no longer create a RecyclerView.

### Verification and integrated checkpoint

- `RecommendationSectionComposeTest` 1/1 renders two recommendations for the same media and verifies independent click/long-click actions and cached section selection. `LoadStateFooterComposeTest` 1/1 retains loading/error/retry/settled checks.
- Spotless passed, both unit variants passed (1,288 tests, 0 failures, 3 skipped each), and both debug APKs assembled. The latest full connected run executed 232 tests with two incidental assertion failures; after removing those assertions, the affected browse and media-header suites passed 1/1 and 2/2 respectively. This is not a claim of a subsequent full connected green run. Final app-wide verification remains required after the remaining hosts are migrated.

## Fifty-second Compose slice: feed and comment threads

### Cutover and contracts

- Home/message feeds, media/profile activity sections, and comment threads render direct keyed `LazyColumn` content. `CommentFragment` places the origin and replies in one list above the existing composer instead of nesting two RecyclerViews.
- Removed `FeedListAdapter`, `CommentListAdapter`, the placeholder reply adapter, and the single-consumer `FragmentBaseComment`. Rows render immutable canonical-store projections and forward actions; loading/page metadata and current-user ownership remain in ViewModels.
- Full store snapshots replace rows; productive-page gating uses unique new feed ids. MESSAGE rows retain Markdown spans, sender/recipient identity, likes, comments, and edit controls. A missed MESSAGE likes callback was caught on-device and wired to the screen callback.
- A failed initial detail request previously marked missing data as deletion and popped external activity links back to Anime. `CommentViewModel` now distinguishes a missing initial entity from removal of an observed canonical feed. Its regression failed before and passed after the correction, including later store-driven deletion.

### Verification

- `FeedPageSnapshotTest` 1/1, `CommentViewModelTest` 13/13. Device checks: `FeedProgressRowComposeTest` 5/5, `FeedStatusRowComposeTest` 4/4, `FeedCommentFavouriteComposeTest` 2/2, `CommentThreadLazyListComposeTest` 6/6, and external ingress 11/11.
- Live feed scrolling crossed the page boundary; process-scoped HTTP logs contained page 1 and page 2 requests and no fatal-exception marker. Opening a visible feed's comments showed the origin above the composer; first back dismissed input UI without leaving, and second back returned to the retained feed viewport. No remote mutation was submitted during this manual pass.

## Fifty-third Compose slice: review lists and vote ownership

### Cutover and contracts

- Browse and media review sections use `ReviewList`, preserving banner/default variants and the actual hosted `MaterialTextView` reader action. Removed `ReviewAdapter`, holder registries, and adapter-only DiffUtil tests.
- ViewModels own per-review, per-direction pending vote tickets through `ReviewVoteStateOwner`. Active votes toggle to `NO_VOTE`; counts remain server-authoritative. Timeout and late completion clear only the matching ticket, including when a row leaves the viewport. Login gating, stale-refresh feedback, failure handling, sorting, and manual paging remain intact.

### Verification

- `ReviewVoteStateOwnerTest` 4/4 uses fake time for timeout and late-result boundaries; `ReviewListProjectionTest` 1/1 and both rate ViewModel suites passed in the unit gate. `ReviewListComposeTest` 2/2 verifies both variants, image/profile actions, vote dispatch, and the real reader target.
- Live review browse displayed server cards and settled after pull-to-refresh. Read More opened the complete matching review, author, media title, and body in the existing reader sheet.

## Fifty-fourth Compose slice: status attachment carousel

### Cutover and contracts

- `StatusContentWidget` renders immutable typed attachments with `FeedAttachmentCarousel` and native `HorizontalPager`. Removed `ImagePreviewAdapter` and its now-unreferenced `CenterSnapUtil`.
- Image, WebM, and YouTube routing; case-insensitive markup; fit/center-crop choices; wide-image height; playback badges; current-page indicator; and selection callbacks remain. Null text and recycling clear previous attachment content; unchanged status rebinds retain the current page.

### Verification

- `FeedSlideComposeTest` 6/6 covers local image pixels, fit/fill placement, height, playback affordance, clearing on reuse, and real swipe/selection/indicator behavior. No RecyclerView or holder is created by these tests.

## Fifty-fifth Compose slice: onboarding pager

### Cutover and contracts

- `WelcomeActivity` uses native `HorizontalPager` instead of a RecyclerView adapter behind `ViewPager2`. Existing non-list hero artwork remains View interop; the native Continue/Get Started button, page indicators, completion action, and window handling remain.
- Pager state is saveable. Each Continue request is a distinct UI action, so swiping back does not suppress the next button press.

### Verification and pre-retirement checkpoint

- `OnboardingPagerComposeTest` 1/1 verifies swipes, Continue after swiping back, page restoration after recreation, and the final button state.
- Current App unit gate: 1,283 tests, 0 failures, 3 skipped. The preceding Github gate passed 1,284 tests before removing two projection-only checks and adding the comment failure/deletion regression. Both debug APKs assembled before those final feed/comment corrections.
- The full device run executed 226 tests with four failures. After corrections, the affected ingress/progress/status/review suites passed 11/11, 5/5, 4/4, and 2/2. A fresh full matrix is still required after removing shared Recycler infrastructure and the Recycler-backed navigation drawer.

## Fifty-sixth Compose slice: retire shared Recycler infrastructure

- `FragmentBaseList` retains only native refresh/error presentation and `ManualPageState` bookkeeping. It no longer owns a RecyclerView, adapter, layout manager, or scroll listener. Direct section controllers mount Compose content into the empty refresh target.
- `CommonPresenter` uses the same framework-independent page bookkeeping without a Recycler dependency. This does not expand presenter usage or replace the ViewModel-first direction.
- `FragmentChannelBase` is removed. `WatchListViewModel` owns RSS loading, cached content, title filtering, request freshness, and source episode identity; the screen renders state and forwards selections.
- Unused adapter/holder/scroll-listener bases, stateful Recycler hosts, selection callbacks, animation infrastructure, Recycler row XML, and obsolete recycling/wiring tests are removed after their consumers were migrated.
- Paging load-state rendering and footer code live in `ui.paging`. The direct Paging runtime dependency and catalog alias are removed; Compose paging retains the required common API.
- Unused grouped-row `contentType` metadata is removed. ObjectBox generation reported its schema JSON unchanged, so this renderer retirement adds no persistence migration.

## Fifty-seventh Compose slice: native collection drawer

- `MainNavigationDrawer` replaces the Recycler-backed Material `NavigationView`, including its header and update row. `DrawerLayout`, the toolbar toggle, and Navigation 2 remain.
- Drawer constraints are applied once: the lazy list respects the native child width with a `320.dp` maximum. The header participates in the same lazy list rather than remaining pinned.
- Selected labels/icons use `onPrimary`; unselected labels/icons explicitly use `onSurface`. Authentication-dependent entries, unread count, header/avatar actions, destination callbacks, and app/github update behavior remain explicit immutable render state.
- `MainNavigationDrawerComposeTest` covers signed-in/out visibility, row selection, update badges, native-parent width, dark/black label contrast, constrained-height header scrolling, and keyboard traversal/activation.
- Live API 36 screenshots exercised light, dark, and black app themes with the real drawer toggle and destination selection. Captures are local artifacts at `/tmp/anitrend-compose-drawer-light.png`, `/tmp/anitrend-compose-drawer-dark.png`, and `/tmp/anitrend-compose-drawer-black.png`.
- The final Github APK also rendered the public media grid and exposed its drawer Update row. A real row tap opened the update confirmation sheet; choosing No dismissed it without downloading or installing a release. Local capture: `/tmp/anitrend-compose-github-update.png`.

## Fifty-eighth Compose slice: lifecycle and native-container boundaries

- Notifications publish page-keyed, ID-deduplicated full snapshots. Resume and view recreation re-render those snapshots rather than appending the same page again. The lifecycle regression loads two pages, replays state, recreates the view, and reaches earlier rows without requesting page three.
- Manual character, staff, studio, and user search retain unique full snapshots with submitted query/page identity. Saved counters are reconciled before the first request, and fresh-process state reloads page one rather than skipping a missing page.
- Editable search drafts are separate from submitted query identity. Draft edits do not discard pending responses; explicit submission or tab selection commits the query. Consumed media refresh signals do not refresh again on detail/back.
- Lazy viewport detection owns manual appends. Search and direct detail grids disable competing native bottom-load gestures, retain same-page retry, stop both native indicators on success, and stop after terminal or duplicate-only pages.
- `FollowStateWidget.setUserState(userId, isFollowing)` renders the committed flag without retaining or mutating the target entity. User rows pass snapshot values; profile rendering no longer allocates a copied user merely to carry follow state.
- Giphy, supplied-user/likes, and fetched followers/following sheets connect their lazy content to native nested scrolling. Real downward mid-list drag checks verify earlier content becomes visible while the dialog stays expanded at the same position.
- Gesture fixtures position content semantically before the native drag. This avoids unrelated fast-fling edge/idle races while preserving the real touch interception assertion. Native retry interaction waits for the snackbar action to finish entering before clicking it.
- Final live RSS smoke exposed an unreachable toolbar bridge: the activity fragment list contained the NavHost rather than its active list destination. Toolbar text changes and closing now reach the NavHost's primary media-list or episode destination. Episode screens expose title search instead of unrelated media API filters.
- `ToolbarEpisodeSearchNavigationTest` failed before that correction because the episode search action was absent, then passed native typing, visible row filtering, and cached-row restoration on close. Live RSS filtering for `Attack` also opened the correct nonzero-source episode preview, without starting playback.
- The comment action regression uses an image-independent fixture instead of waiting for an unrelated avatar center pixel. It verifies mention, profile, and likes callbacks both before and after replacing the reply snapshot, including the rebound reply/user IDs.

### Removal boundary and rollback

- Production source scans cover `app/src/main`, `app/src/app`, `app/src/github`, and `app/src/debug`; catalog/build scans cover `gradle/libs.versions.toml`, `buildSrc`, and `app/build.gradle.kts`.
- No app-owned RecyclerView, NavigationView, ViewPager2, retired adapter/holder/scroll helper, or direct Recycler-backed dependency reference remains in those scopes. Third-party Material/debug artifacts may still contain Recycler bytecode; this is not a claim that all transitive library classes were stripped from APKs.
- Roll back renderer/state bookkeeping changes together if necessary. Do not preserve a parallel legacy adapter path or reintroduce an event-based entity synchronization shim. No database schema migration is required by the final infrastructure retirement.

## Final collection-cutover verification

All commands below completed successfully against the final production code. The only subsequent code change was the image-independent comment-action test fixture; the complete connected suite was rerun after that change.

| Suite | Tests | Failures | Skipped |
| --- | ---: | ---: | ---: |
| App debug unit tests | 1,281 | 0 | 1 |
| Github debug unit tests | 1,281 | 0 | 1 |
| App debug connected tests, API 36 | 245 | 0 | 0 |

The unit skip is pre-existing. Spotless and both debug APK assemblies passed:

```sh
jenv exec ./gradlew :app:spotlessCheck :app:testAppDebugUnitTest :app:testGithubDebugUnitTest :app:assembleAppDebug :app:assembleGithubDebug --no-configuration-cache --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet
```

The final unfiltered connected run passed all cases, including native drawer sizing/focus/themes, search paging/retry/recreation/drafts, RSS toolbar filtering, notification lifecycle replay, sheet touch interception, comment snapshot actions, and existing entry-point regressions:

```sh
jenv exec ./gradlew :app:connectedAppDebugAndroidTest --no-configuration-cache --no-daemon --max-workers=1 -Dorg.gradle.parallel=false --quiet
```

`bash .github/scripts/validate-changelogs.sh` passed. Final production and catalog/build scans returned zero references to the retired Recycler hosts/helpers and direct dependencies in the scopes above.

Live checks across the migration exercised public feed scrolling/paging and comments/back behavior, review opening and refresh, grouped media/character/staff navigation, live search drafts and detail/back, RSS filtering and correct episode preview identity, light/dark/black drawer rendering, and the Github update confirmation. No remote social mutation or playback was triggered. The Github update confirmation was dismissed without downloading or installing a release.

Runtime evidence is from the API 36 emulator. Release/R8 builds and a physical-device/API-level matrix were not run. Integrated paging/state, drawer/pager, search-lifecycle, and native-sheet reviews were completed; their identified cutover regressions were corrected and exercised by the final suite.
