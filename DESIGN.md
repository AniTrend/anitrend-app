# AniTrend Material 3 Design System

## 1. Overview

AniTrend is a content-dense anime and manga tracking app for AniList. This document is the
binding design system for its Jetpack Compose UI. It replaces the previous XML/View rules;
Compose `MaterialTheme` tokens are the source of truth for every new and refactored surface.

The legacy XML themes (`AppThemeLightBase`, `AppThemeDarkBase`, `AppThemeBlack`) remain only for
the activity window chrome (manifest themes and `CommonActivity` theming) and mirror the same
token values in
`values/m3_color.xml`. When the two disagree, the Compose schemes in
`ui/designsystem/AniTrendTheme.kt` are authoritative.

### Product intent

AniTrend supports discovery, tracking and social context at high information throughput.
Media identity (title, artwork, status, score, progress, availability) comes before decoration.

### Decision rule

When trade-offs are unclear, evaluate in this order:

1. **Comprehension speed** — can users parse state and actions quickly?
2. **Task efficiency** — can users finish common actions with low effort?
3. **Consistency** — does this match existing AniTrend interaction language?
4. **Expression** — does the UI still feel premium and intentional?

Expression never wins at the expense of comprehension or efficiency. Dense is valid; crowded is
not. Group related metadata into clear bands instead of unstructured stacks, and never collapse
hierarchy into uniform visual weight.

## 2. Theme and color

- Wrap every screen in `AniTrendTheme(theme, content)` where `theme` is the observed Settings
  value: `light`, `dark` or `black` (AMOLED). Previews pass the value explicitly.
- The three schemes are the brand palettes: brand-blue primary, blue-grey secondary, warm-brown
  tertiary, standard M3 error roles, and the full `surfaceContainer*` hierarchy with paired
  on-colors. `black` is the dark scheme with every surface/bottom layer forced to `#000000`.
- Never hard-code colors or read live XML theme attributes in Compose code. Use
  `MaterialTheme.colorScheme` and `MaterialTheme.typography` roles.
- Use tonal layering (`surfaceContainerLow` → `surfaceContainer` → `surfaceContainerHigh`) to
  separate groups. No ornamental shadows, no nested cards around every row.
- Hero artwork may carry white overlay text only on a contrast-protected scrim or a stable
  surface; text must survive light, dark and AMOLED schemes.

## 3. Typography

The family is condensed (`aniTrendCondensedFontFamily`, bold `aniTrendCondensedBoldFontFamily`)
with the platform font-padded text style. Role usage:

| Role | Use |
|---|---|
| `headlineSmall` | Screen headings |
| `titleMedium` | Section headings |
| `bodyMedium` | Content |
| `labelLarge` | Controls (buttons, chips, action bars) |

Supporting rows use `titleSmall`, `bodySmall` and `labelMedium` as already mapped in
`AniTrendTheme`. Do not set raw font sizes or invent per-screen type styles.

## 4. Shape, spacing and width

- `Shapes(extraSmall = 4.dp, small = 8.dp, medium = 16.dp, large = 24.dp, extraLarge = 28.dp)`,
  all `RoundedCornerShape`.
- Chips and buttons use fully rounded component shapes, posters `medium`, grouped cards `large`,
  sheets `extraLarge`.
- Screen and card insets: 16dp. Intra-group gaps: 8/12dp. Between major sections: 24dp.
- Content stays within 1040dp overall; prose is constrained to 720dp. Cover grids are adaptive
  with a 148dp minimum tile. On wide windows keep the same drawer and bottom-action model; no
  speculative navigation rails or multi-pane layouts.

## 5. Screen chrome

### Scaffold

Every destination uses `AniTrendScaffold`: one bottom action bar, one snackbar host, safe
drawing insets. Content consumes the Scaffold padding exactly once. The bar stays visible while
content scrolls. A scrollable heading or artwork hero supplies top identity — never a second
toolbar.

### Bottom action bars

These are **action bars**, not icon-only primary-destination navigation bars.

- One bar per screen. Leading Menu at roots, Back below roots.
- At most three other frequent actions; rare actions go in a localized overflow.
- Never move row-specific actions (Like, Reply, progress steppers) into a global bar, never
  duplicate an action at top and bottom, never stack a FAB over a composer.
- While a sheet is open, the screen bar is replaced by the sheet's task-specific footer.

### Route drawer

One M3 `ModalBottomSheet` opened by the root Menu button: profile/sign-in header, static
labeled route rows, selected state. Tonal grouping and rounded sheet language only. Back, scrim
and drag dismiss without navigating. Selecting the current root just dismisses; selecting
another root restores its saved screen state.

### Chips are in-screen controls

Chips are presets, section selection or filters — never disguised global tabs.

- `FilterChip` with a checkmark and selected semantics for single selection.
- `LazyRow` for a compact preset strip; `FlowRow` for small multi-select sets; an advanced
  filter sheet for large option sets.
- No TabLayout, TabRow, pager-backed tab strip, and no raw ordinal or enum `.name` as UI copy.
  All categorical values render through `ui/format/EnumLabels.kt`.
- Swipe paging exists only for onboarding and image browsing.

## 6. Components

- **Buttons**: filled for primary, outlined for equally reachable alternatives, text for
  tertiary. Minimum 48dp targets.
- **Text fields**: Compose `OutlinedTextField`; editors share `ui/composer/MarkdownComposer`
  with a formatting toolbar and a read-only preview.
- **Switches and sliders**: `Switch` for booleans with label plus supporting text; `Slider` with
  a visible value label for numeric ranges.
- **Rating indicators**: display-only average scores use the five-star
  `ui/review/ReviewSeriesRow` `ReviewScoreStars` pattern (fractional fill over the base star,
  localized content description); interactive rating lives in the review reader sheet.
- **Date controls**: staged list dates use `ui/medialist/FuzzyDateField` opening an M3
  `DatePickerDialog` seeded from the staged value; the TBA action clears the field.
- **Lists**: keyed `LazyColumn`/`LazyRow`/`LazyVerticalGrid` with immutable item models and
  domain-prefixed keys (`media:123`). Refresh/append states are handled separately and stay
  inline with prior content retained.
- **Rows**: adapt the existing media/feed/person/settings row families; identity on the
  artwork/title rail, actions in stable positions.
- **Custom views** remain view-only: no ViewModel, no data loading; they receive state and emit
  actions.
- **Markdown**: the only `AndroidView` in the app is `ui/markdown/MarkdownText`, a TextView
  renderer with route-owned link dispatch.

## 7. State, feedback and motion

- Every screen defines loading, empty, populated and error states (`ui/state/ScreenState.kt`).
  Initial loading uses geometry-matched placeholders; empty states explain and offer the
  relevant inline action; initial failure offers Retry. Refresh and append errors stay inline.
- A failing optional section never replaces a loaded screen; retry only the failed request and
  retain drafts and filters.
- Selection, disabled and loading states are explicit — never color alone.
- Actions give immediate visual response. Validation and status errors appear next to their
  field. Prefer inline feedback over toasts; use snackbars for transient, non-blocking notices.
- Motion is restrained and respects the system reduced-motion setting; no looping spectacle.

## 8. Accessibility and adaptation

- 48dp minimum touch targets; keyboard focus visible; start/end layout for RTL.
- Headings use semantic roles; icon-only controls carry labels; selected/disabled/loading are
  exposed as state semantics.
- Respect system font scaling. At large text or short height, body scrolls and decorative
  infographic space shrinks before text or actions are hidden.
- Light/dark/AMOLED contrast must hold for text on artwork and tonal surfaces.

## 9. Do's and don'ts

- Do use `AniTrendTheme` and `MaterialTheme` tokens; don't create per-screen themes or a
  competing theme wrapper.
- Do use `surfaceContainer*` roles for grouped backgrounds; don't use raw hex values.
- Do localize every control and category label through `EnumLabels`/string resources in all ten
  supported locales; don't display enum names, ordinals or capitalization as copy.
- Do keep one bottom action bar per screen; don't add top toolbars, tab strips or stacked
  composers.
- Do ship only working settings entries; don't add "Coming soon" rows or unimplemented feature
  promises.

## 10. Change management

When a design decision changes (new component pattern, revised token usage, updated spacing
convention), update this document in the same change so it stays the source of truth.
`ui/manage/ManageListSheet.kt` is the reference implementation of this language.
