# TODO: Post-Migration Next Steps

> Branch: `refactor/kotlin-migration`
> Status: Java→Kotlin migration **complete** (0 `.java` files remain), build passing, 57/57 tests passing.

---

## 1. 👁️ Visual / UI Verification

The `ProgressLayout` replacement (external library → local widget) should be visually smoke-tested:

- [ ] Launch app and verify loading spinners appear on list fragments
- [ ] Trigger error states (airplane mode) and verify error overlay with retry button
- [ ] Verify rating bars display correctly (SmallRating, StandardRating styles)
- [ ] Check shortcut icons use correct background color (`grey_light`)
- [ ] Walk through: Feed → Anime Detail → Character/Staff Detail → User Profile

## 2. 🧪 Expand Test Coverage

Currently only utility-layer unit tests exist:

- [ ] Add unit tests for the new `ProgressLayout` widget (Kotlin + render tests)
- [ ] Add unit tests for migrated ViewBinding base classes
- [ ] Add Espresso / Compose UI tests for critical flows (login, feed, detail screens)
- [ ] Add screenshot tests for key fragments

## 3. ⬆️ Dependency Upgrades

Several dependencies have newer versions available:

- [ ] `junit:junit:4.13.2` → 4.13.3+
- [ ] `eventbus:3.3.1` → latest
- [ ] `constraintlayout:2.2.1` → 2.3.x
- [ ] `chuncker:4.2.0` → 4.3.1+ (update version ref in `libs.versions.toml`)
- [ ] `koin:4.1.1` → latest
- [ ] `smarttablayout:2.0.0` → check for newer
- [ ] `squareup-okhttp:5.3.2` → latest
- [ ] `squareup-retrofit:3.0.0` → latest 3.x
- [ ] `anitrend-retrofit-graphql:0.11.13` → latest

## 4. 🏗️ Build System

- [ ] **Fix Gradle deprecated features** — run `./gradlew build --warning-mode all` and address warnings (incompatible with Gradle 10)
- [ ] **Enable configuration cache** — add `org.gradle.configuration-cache=true` to `gradle.properties`
- [ ] **Update Gradle wrapper** — currently on 9.3.1, check for newer
- [ ] **Update AGP** — currently on 8.13.2, check for 8.14.x or 9.x
- [ ] **Remove unused dependencies** — `butterknife` references fully removed, verify `constraintlayout-solver`, `lifecycle-extensions`, `multidex` are still needed

## 5. 🧹 Code Quality

- [ ] Run `./gradlew :app:lint` and address issues
- [ ] Run `./gradlew :app:ktlintCheck` if configured
- [ ] Remove `app/build/generated/` from version tracking if accidentally committed
- [ ] Review `@Suppress("UNCHECKED_CAST")` usages — check if they can be eliminated

## 6. 🚀 Release Prep

- [ ] Build GitHub flavor: `./gradlew :app:assembleGithubDebug`
- [ ] Run full test suite on both flavors
- [ ] Verify release-drafter autolabeler config picks up `feat(progresslayout):` prefix
- [ ] Update `AGENTS.md` with current project state
