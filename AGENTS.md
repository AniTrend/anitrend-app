# Repository Guidelines

## Project Structure & Module Organization
- `app/` is the main Android application module.
  - `app/src/main/java/` contains Kotlin/Java sources.
  - `app/src/main/res/` holds Android resources (layouts, drawables, strings).
  - `app/src/main/assets/` includes GraphQL queries and other assets.
  - `app/src/test/java/` contains JVM unit tests.
  - Flavor sources live in `app/src/app/` and `app/src/github/` (dimension: `version`).
- `buildSrc/` hosts custom Gradle plugins and shared build logic.
- Root `build.gradle.kts`, `settings.gradle.kts`, and `gradle/libs.versions.toml` define build configuration and dependency catalog.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repo root:
- `./gradlew :app:assembleAppDebug` builds the Play Store flavor debug APK.
- `./gradlew :app:assembleGithubDebug` builds the GitHub flavor debug APK.
- `./gradlew :app:testAppDebugUnitTest` runs JVM unit tests for the app flavor.
- `./gradlew :app:lint` runs Android lint (configured to not abort on error).

## Coding Style & Naming Conventions
- Kotlin and Java are both used; follow existing file patterns and Android Studio formatting (4-space indentation).
- Use Android naming conventions for resources: `snake_case` for layouts, drawables, and IDs (for example, `activity_login.xml`, `ic_search_white_24dp.xml`).
- Prefer dependency aliases from `gradle/libs.versions.toml` when updating Gradle files.

## Testing Guidelines
- Unit tests use JUnit4, Hamcrest, Mockito, and Koin test helpers.
- Instrumented tests (if added) should live under `app/src/androidTest/java` and use AndroidX Test + Espresso.
- Name tests after the behavior under test (for example, `EpisodeUtilTests`).

## Commit & Pull Request Guidelines
- Recent history follows Conventional Commit style, e.g. `fix(deps): ...`, `chore(deps): ...`.
- Open an issue before large changes, especially new libraries or test refactors.
- PRs should target the `develop` branch, include a clear description, and add screenshots when UI changes apply.

## Security & Configuration Tips
- Keep secrets in `.config/*.properties` or `local.properties`; do not hardcode keys in source.
- `app/.config/` and signing files are environment-specific and should remain uncommitted unless explicitly requested.
