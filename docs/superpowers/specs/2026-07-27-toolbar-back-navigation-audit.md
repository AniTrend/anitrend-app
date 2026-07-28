# Toolbar Back Navigation Audit

Date: 2026-07-27
Project: AniTrend Android app
Branch: `refactor/retrofit-graphql-response-types`
Scope: Audit and codemap investigation for non-function toolbar back or up navigation across app screens.

## 1. Executive summary

Toolbar back handling is inconsistent because the old activity shell has already been inlined away, but the replacement navigation behavior was not normalized.

The highest-confidence broken cluster is the set of activities that:
- call `setDisplayHomeAsUpEnabled(true)`
- do not override `onOptionsItemSelected(...)`
- do not override `onSupportNavigateUp()`
- do not declare `parentActivityName`

For `AppCompatActivity`, the default home or up path delegates to `onSupportNavigateUp()`. When there is no parent activity intent, `onSupportNavigateUp()` returns `false`, so the toolbar back press becomes a no-op instead of finishing the screen.

That means the non-function toolbar back cluster is not speculative. It is directly supported by the app code plus the AppCompat 1.7.1 default behavior used by this repo.

## 2. Key evidence

### 2.1 AppCompat version in this repo

- `gradle/libs.versions.toml:7` pins `androidx-appcompat = "1.7.1"`.

### 2.2 Default AppCompat home or up behavior

The current AndroidX `AppCompatActivity` implementation handles home or up like this:

```java
@Override
public final boolean onMenuItemSelected(int featureId, MenuItem item) {
    if (super.onMenuItemSelected(featureId, item)) {
        return true;
    }

    final ActionBar ab = getSupportActionBar();
    if (item.getItemId() == android.R.id.home && ab != null
            && (ab.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0) {
        return onSupportNavigateUp();
    }
    return false;
}

public boolean onSupportNavigateUp() {
    Intent upIntent = getSupportParentActivityIntent();
    if (upIntent != null) {
        ...
        return true;
    }
    return false;
}
```

Implication for this repo:
- home or up does **not** fall back to `finish()`
- home or up does **not** fall back to `onBackPressedDispatcher.onBackPressed()`
- if no parent activity is declared and no local handler exists, toolbar back does nothing

No activity in this codebase overrides `getSupportParentActivityIntent()` or any `supportNavigateUp*` method, so the default `NavUtils.getParentActivityIntent(this)` path, which reads `parentActivityName` from the manifest, is the only one in play.

The default `onSupportNavigateUp()` path in `onMenuItemSelected(...)` only fires when `DISPLAY_HOME_AS_UP` is set in the action bar display options. Every audited activity sets that bit through `setDisplayHomeAsUpEnabled(true)`, so the fallback gate is satisfied for the default-reliant buckets. Bucket A screens are unaffected by this gate because their explicit `onOptionsItemSelected(...)` handler runs through the `super.onMenuItemSelected(...)` call that happens before the AppCompat fallback.

## 3. Architectural codemap

### 3.1 Shared toolbar surfaces

- `app/src/main/res/layout/custom_toolbar.xml`
  - standard toolbar include used by most activity shells
- `app/src/main/res/layout/activity_frame_generic.xml`
  - generic single-fragment host with `custom_toolbar`
- `app/src/main/res/layout/activity_pager_generic.xml`
  - generic tab or pager host with `custom_toolbar`
- `app/src/main/res/layout/activity_series.xml`
  - collapsing toolbar path for `MediaActivity`
- `app/src/main/res/layout/activity_profile.xml`
  - collapsing toolbar path for `ProfileActivity`

### 3.2 Shared navigation reality

- There is no shared `ActivityBase` navigation shell in the current tree.
- Activities now extend `AppCompatActivity` directly and repeat their own toolbar setup.
- `MainActivity` is a separate drawer-managed case through `ActionBarDrawerToggle` at `app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt:115-123`.
- No Kotlin activity overrides `onSupportNavigateUp()`.

### 3.3 Fragment interaction model

- `app/src/main/java/com/mxt/anitrend/base/custom/fragment/FragmentBase.kt:84-89` enables options menus for most fragments through `setHasOptionsMenu(true)`.
- Several fragments override `onOptionsItemSelected(...)`, but they handle action items only.
- No fragment override in the audited set handles `android.R.id.home`.
- `FragmentBase.onBackPress()` at `app/src/main/java/com/mxt/anitrend/base/custom/fragment/FragmentBase.kt:127-133` is an orphaned hook. It clears action-mode selection but has no live caller in the current tree.

## 4. Activity buckets

### 4.1 Bucket A, explicit local home handling, consistent

These activities enable home or up and explicitly route `android.R.id.home` through `onBackPressedDispatcher.onBackPressed()`:

| Activity | Evidence |
| --- | --- |
| `LoggingActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/base/LoggingActivity.kt:62-64,140-145` |
| `MediaActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/MediaActivity.kt:80-82,191-195` |
| `CharacterActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/CharacterActivity.kt:59-60,134-138` |
| `StaffActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/StaffActivity.kt:68-69,153-157` |
| `StudioActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/StudioActivity.kt:71-72,151-155` |
| `ProfileActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/ProfileActivity.kt:71-73,145-149` |

This is the only bucket with a reliable, explicit toolbar back path.

### 4.2 Bucket B, drawer-managed, separate concern

| Activity | Evidence |
| --- | --- |
| `MainActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt:115-123` |

This is not an up-navigation screen and should stay outside the failing toolbar-back cluster.

### 4.3 Bucket C, default-reliant, audit focus

These screens enable home or up but do not locally handle `android.R.id.home`.

#### C1. Lowest-risk default path, parent declared

| Activity | Evidence |
| --- | --- |
| `SettingsActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/base/SettingsActivity.kt:41-42`, `app/src/main/AndroidManifest.xml:33-41` |

`SettingsActivity` is the only audited screen where default AppCompat up navigation has a declared parent target.

#### C2. High-confidence broken cluster, no parent and no local home handler

| Activity | Home enabled | Local menu handler | Parent activity | Entry shape |
| --- | --- | --- | --- | --- |
| `CommentActivity` | `CommentActivity.kt:33` | absent | none | deep-link capable, `AndroidManifest.xml:200-216` |
| `MessageActivity` | `MessageActivity.kt:32` | absent | none | activity shell only |
| `FavouriteActivity` | `FavouriteActivity.kt:29` | absent | none | activity shell only |
| `MediaListActivity` | `MediaListActivity.kt:31` | absent | none | activity shell only |
| `MediaBrowseActivity` | `MediaBrowseActivity.kt:29` | absent | none | activity shell only |
| `SearchActivity` | `SearchActivity.kt:29` | absent | none | exported activity shell, `AndroidManifest.xml:196-199` |
| `AboutActivity` | `AboutActivity.kt:31` | absent | none | exported activity shell, `AndroidManifest.xml:186-189` |

These seven screens rely entirely on the AppCompat default path that returns `false` when no parent activity exists. Based on the code alone, toolbar back is expected to do nothing on these screens.

#### C3. High-confidence broken plus divergent behavior

| Activity | Evidence |
| --- | --- |
| `NotificationActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/detail/NotificationActivity.kt:27-30,39-45`, `app/src/main/AndroidManifest.xml:60-64` |

`NotificationActivity` is worse than the C2 group:
- toolbar back still relies on the failing default AppCompat path
- system back has a custom `isTaskRoot` redirect to `MainActivity`

So toolbar back and system back are provably divergent on this screen.

### 4.4 Bucket D, toolbar present but no home or up affordance

| Activity | Evidence |
| --- | --- |
| `ImagePreviewActivity` | `app/src/main/java/com/mxt/anitrend/view/activity/base/ImagePreviewActivity.kt:74-75` |

This screen has toolbar actions but never enables home or up, so it is not part of the broken up-navigation path. It is a separate UX decision.

## 5. Hypothesis, why the inconsistency likely exists

The strongest code-level hypothesis is that the old activity shell was removed without a unified replacement.

Evidence:
- activities now inline theme and toolbar setup themselves
- `MainActivity` still carries comments such as `Fields carried over from ActivityBase shell`
- repo guidance still describes legacy `ActivityBase` coexistence, but the current codebase no longer has that class in the navigation path

This left the codebase with three incompatible toolbar-back strategies:
- explicit `onBackPressedDispatcher` handling
- default AppCompat up-navigation reliance
- custom system-back logic without matching toolbar-home handling

## 6. Findings

1. **Six screens are explicitly wired and consistent.**
   - They all use the same local `android.R.id.home` handling pattern.

2. **Eight screens have non-function toolbar back with high confidence.**
   - Seven screens have no parent activity and no local home handling.
   - `NotificationActivity` adds a custom system-back path but still leaves toolbar back unhandled.

3. **One screen works by manifest parent rather than local code.**
   - `SettingsActivity` is the only default-reliant screen with a declared parent.

4. **Fragment menu code is not the root cause of the failure.**
   - Fragments participate in options menus, but none in this audit handle `android.R.id.home`.

5. **`FragmentBase.onBackPress()` is misleading dead surface for this problem.**
   - It suggests a shared back abstraction that is not actually connected to either toolbar back or system back.

## 7. Suggested normalization path

The smallest consistent navigation policy would be:
- keep `MainActivity` as a drawer-managed exception
- keep explicit no-up screens like `ImagePreviewActivity` only where intentional
- normalize every up-enabled activity onto one strategy

The most direct strategy is to match the working bucket:
- explicitly handle `android.R.id.home`
- call `onBackPressedDispatcher.onBackPressed()`

That would align toolbar back with the already working screens and avoid depending on manifest parents or implicit AppCompat behavior.

## 8. Follow-up validation targets

- device or emulator smoke-check one representative screen from each bucket:
  - explicit handler: `ProfileActivity`
  - parent-driven default: `SettingsActivity`
  - broken default path: `CommentActivity`
  - divergent path: `NotificationActivity`
- update repo guidance that still references legacy `ActivityBase` coexistence if this audit is used to drive follow-up cleanup work
