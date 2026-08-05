# AniTrend Material 3 Design System

## 1. Overview

AniTrend is an anime and manga tracking app for AniList. This is the app-wide design system for its View-based Android UI. The current implementation uses Material 3 XML components. Compose is the future direction, but every new and refactored screen must follow these M3 rules today.

The app ships three themes: `AppThemeLightBase` (`Theme.Material3.Light.NoActionBar`), `AppThemeDarkBase` (`Theme.Material3.Dark.NoActionBar`), and `AppThemeBlack` (`Theme.Material3.Dark.NoActionBar` with AMOLED black surfaces). All three share the same token structure, color roles, surface container hierarchy, shape theming, and typography overrides.

The manage list editor sheet is the reference implementation of this language. Its choices for cards, spacing, typography, and action anchoring are the default for all future screens, sheets, dialogs, and lists.

## 2. Colors

Use Material 3 tokens only. Never use raw hex values or legacy platform colors in new code.

Core roles: `colorPrimary`, `colorOnPrimary`, `colorPrimaryContainer`, `colorOnPrimaryContainer`; `colorSecondary`, `colorOnSecondary`, `colorSecondaryContainer`, `colorOnSecondaryContainer`; `colorTertiary`, `colorOnTertiary`, `colorTertiaryContainer`, `colorOnTertiaryContainer`; `colorError`, `colorOnError`, `colorErrorContainer`, `colorOnErrorContainer`.

Surface roles: `colorSurface`, `colorOnSurface`, `colorSurfaceVariant`, `colorOnSurfaceVariant`, `colorOutline`, `colorOutlineVariant`, `colorPrimaryInverse`, `colorSurfaceInverse`, `colorOnSurfaceInverse`.

Surface container hierarchy:

- `colorSurfaceContainerLowest`: lowest emphasis, closest to background.
- `colorSurfaceContainerLow`: subtle grouping, secondary backgrounds.
- `colorSurfaceContainer`: grouped section cards (the default card fill).
- `colorSurfaceContainerHigh`: sheet backgrounds and raised panels.
- `colorSurfaceContainerHighest`: top app bars, toolbars, and cards that need to sit above a sheet.

Custom attrs are mapped to M3 tokens:

- `cardColor` -> `colorSurfaceContainerHighest`
- `rootColor` -> `colorSurface`
- `titleColor` -> `colorOnSurface`
- `subtitleColor` -> `colorOnSurfaceVariant`
- `contentColor` -> `colorOnSurfaceVariant`

Legacy compat: `colorAccent` resolves to `colorPrimary`, `colorPrimaryDark` resolves to `colorPrimaryContainer`. Treat these as legacy bridges only. New code uses the M3 token directly.

Dark and black themes keep the same role names. `AppThemeBlack` flattens surfaces to black, but containers still provide grouping. Do not invent new black-only color names.

### Image overlay text

Profile and media hero images may use white overlay text when a dark scrim is
present beneath the text and the image cannot provide a stable surface token.
The scrim is responsible for contrast across dynamic artwork and all three
themes. Keep this exception limited to image-overlay content; regular screen
text must continue using the M3 `colorOnSurface*` roles.

## 3. Typography

All text uses the `TextAppearance.AniTrend.*` overrides. The family is condensed. Reference tokens through `?attr/textAppearance*` and never set raw `android:textSize` or `android:textColor`.

Tokens:

- `textAppearanceHeadlineMedium`, `textAppearanceHeadlineSmall`
- `textAppearanceTitleLarge`, `textAppearanceTitleMedium`, `textAppearanceTitleSmall`
- `textAppearanceBodyLarge`, `textAppearanceBodyMedium`, `textAppearanceBodySmall`
- `textAppearanceLabelLarge`, `textAppearanceLabelMedium`, `textAppearanceLabelSmall`

Usage:

- Screen titles: `?attr/textAppearanceTitleLarge`
- Section headers: `?attr/textAppearanceTitleSmall` (style `Widget.AniTrend.ManageSheet.SectionLabel`)
- Field labels: `?attr/textAppearanceLabelMedium` (style `Widget.AniTrend.ManageSheet.FieldLabel`)
- Body values and list item titles: `?attr/textAppearanceBodyMedium`
- Helper text and captions: `?attr/textAppearanceBodySmall`
- Button labels: `?attr/textAppearanceLabelLarge`

Use the same styles for sheet, dialog, and list labels. Do not create one-off text appearances.

## 4. Layout & Spacing

The app-wide spacing scale is:

- `xl_margin` = 16dp
- `mg_margin` = 12dp
- `lg_margin` = 8dp
- `md_margin` = 4dp
- `sm_margin` = 2dp
- `xs_margin` = 1dp

Structural spacing:

- `spacing_xl` = 48dp
- `spacing_lg` = 40dp
- `spacing_md` = 32dp
- `spacing_sm` = 24dp
- `spacing_xs` = 16dp

Screen padding: `activity_horizontal_margin` = 16dp, `activity_vertical_margin` = 16dp. Treat `xl_margin` as the default horizontal screen inset.

Group related controls in filled `MaterialCardView` sections. Use `MaterialDivider` for major breaks. Do not use empty `View` spacers.

Sheet spacing pattern:

- `sheet_section_margin` = 16dp between section cards
- `sheet_card_padding` = 16dp inside a section card
- `sheet_field_margin` = 12dp between a header and its first field
- `sheet_inline_spacing` = 4dp between a label and its control

Sheets are edge-to-edge. The content scrolls, but the action bar and app bar stay anchored.

## 5. Components

### Bottom sheets
- Use `BottomSheetDragHandleView` at the top.
- Header: title + optional subtitle, both centered.
- Body goes inside `NestedScrollView`.
- Action bar is sticky at the bottom, separated by `MaterialDivider`.
- Open expanded and allow drag-to-dismiss. Use `skipCollapsed` for forms.

### Dialogs
- Use `MaterialAlertDialogBuilder` for confirmations.
- Title: `?attr/textAppearanceHeadlineSmall`.
- Body: `?attr/textAppearanceBodyMedium`.
- Positive action: filled button. Negative action: text button. Neutral action: text button.
- Destructive confirmations use a text button with `?attr/colorError`.

### Cards
- Use filled `MaterialCardView` for grouped sections.
- Default style: `Widget.AniTrend.ManageSheet.SectionCard` extends `Widget.Material3.CardView.Filled`, with `colorSurfaceContainer` fill, `0dp` elevation, and `0dp` stroke.
- Elevated cards are reserved for items that need to float above a list.

### Buttons
- Primary action: `Widget.Material3.Button` (filled).
- Secondary action: `Widget.Material3.Button.TextButton`.
- Tertiary action: `Widget.Material3.Button.OutlinedButton`.
- Icon-only buttons: `?attr/materialIconButtonStyle`.
- Destructive action: text button with `?attr/colorError`.
- Keep button labels short and action-oriented.

### Text fields
- Use `TextInputLayout` with `Widget.Material3.TextInputLayout.OutlinedBox`.
- Dropdowns use `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu`.
- Show hints, helper text, and error text through the layout.
- Use start icons for semantic affordance (status, date, score, progress).
- Never use a raw `EditText`.

### Switches
- Use `MaterialSwitch` for boolean options.
- Pair with a label and supporting text below.
- Supporting text uses `?attr/textAppearanceBodySmall` and `?attr/colorOnSurfaceVariant`.

### Sliders
- Use `Slider` with a visible value label.
- Show the label and current value below the track.
- Set a clear `stepSize` and value range.
- Update the value label in real time.

### Chips
- Use `ChipGroup` with filter chips for multi-select.
- Keep chip labels short.
- Use `?attr/textAppearanceLabelMedium` for chip text.

### Lists
- Use `RecyclerView` with consistent item layouts.
- Item title: `?attr/textAppearanceBodyMedium`, `?attr/colorOnSurface`.
- Item subtitle: `?attr/textAppearanceBodySmall`, `?attr/colorOnSurfaceVariant`.
- Trailing actions use `?attr/materialIconButtonStyle`.
- Avoid arbitrary item heights. Use the same spacing tokens as the rest of the app.

### Navigation
- Top app bars use `LightToolbarTheme`, `DarkToolbarTheme`, or `BlackToolbarTheme`.
- Overflow menus use `PopupThemeLight` or `PopupThemeDark`, extending `Widget.Material3.PopupMenu.Overflow`.
- Bottom navigation follows the M3 component and the active icon color is `colorPrimary`.

### Custom views
- Custom views must read M3 tokens and never hardcode colors or sizes.
- `ProgressWidget`, `ScoreWidget`, and `FuzzyDateWidget` are the existing pattern: a horizontal layout that uses `?attr/materialIconButtonStyle` and outlined `TextInputLayout`.
- Custom views must not own their own ViewModel or theme. They receive values and state from the parent.

### Loading
- Use `ProgressLayout` with `Widget.AniTrend.ProgressLayout.LoadingIndicator`, which extends `Widget.Material3.LoadingIndicator.Contained`.
- Show loading inline inside a container, not as a full-screen overlay unless the whole screen is waiting.
- Lifecycle-aware: dismiss progress indicators when the host stops or the request is cancelled.

## 6. Motion & Feedback

Prefer inline feedback over toasts. Status conflicts, validation errors, and helper updates appear next to the field that caused them.

Use `Snackbar` for undoable actions or global confirmations. Use `Toast` only for transient background events that do not need a user decision.

Update values in real time: status icons, score labels, priority values, and progress counters should reflect the latest input immediately.

Bottom sheets open expanded. Do not let forms start in a collapsed state. The action bar stays visible while the body scrolls.

Progress dialogs must be lifecycle-aware. Tie them to the request lifecycle and cancel them on configuration change or sheet dismissal.

## 7. Do's and Don'ts

- Do use the three M3 themes (`AppThemeLightBase`, `AppThemeDarkBase`, `AppThemeBlack`) for all screens. Do not create screen-specific themes.
- Do use `?attr/colorSurfaceContainer*` for grouped backgrounds. Do not use raw color values or legacy `?android:colorBackground`.
- Do use `MaterialCardView` filled sections for grouping. Do not leave long forms as flat vertical lists.
- Do use `MaterialDivider` for major breaks. Do not use empty `View` spacers.
- Do anchor primary and secondary actions at the bottom of sheets and dialogs. Do not let actions scroll away with the content.
- Do use `?attr/textAppearance*` tokens. Do not set raw text sizes or colors.
- Do use `TextInputLayout` with outlined style for inputs and dropdowns. Do not use raw `EditText` widgets.
- Do use `MaterialSwitch` for booleans. Do not use `CheckBox` or `ToggleButton` for app settings.
- Do show slider values inline. Do not show a bare slider without a value label.
- Do use text buttons with `colorError` for destructive actions. Do not give destructive actions the same visual weight as save actions.
- Do use `?attr/materialIconButtonStyle` for icon buttons. Do not tint icon buttons with `?colorAccent`.
- Do use `ChipGroup` with filter chips for multi-select. Do not use nested `CheckBox` grids for lists of choices.
- Do use `NestedScrollView` in bottom sheets. Do not use `ScrollView`.
- Do keep custom views view-only. Do not let custom views initiate loading or own ViewModels.
- Do keep the manage list editor as the reference implementation. Future sheets, dialogs, and lists should reuse its spacing, card grouping, and typography choices.
