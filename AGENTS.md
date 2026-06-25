# AniTrend Agent Notes

## Architecture and module boundaries
- Real Gradle project is single-module `:app` (`settings.gradle.kts`); `app-compose/` exists in repo but is not included in the build.
- App build logic is centralized in `buildSrc` via plugin `com.mxt.anitrend.plugin` (`app/build.gradle.kts`); prefer changing `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/*` over adding ad-hoc module config.
- Product flavors are `app` and `github` (`app/src/app/`, `app/src/github/`); preserve flavor behavior when editing build logic.

## GraphQL migration state
- GraphQL is codegen-first now: schema + operations live in `app/src/main/graphql/**`.
- Generated operation API package is `com.mxt.anitrend.graphql.generated` (configured in `GraphQLComponents.kt`).
- Use typed generated requests (`SomeOperation.request(...)` / `GraphQLRequest<...>`); do not reintroduce legacy `@GraphQuery` / `QueryContainerBuilder` patterns.

## Build and verification commands
- Java target is 21 (`.java-version` = `21.0.11`); CI also uses JDK 21.
- Fast local verification after code changes:
  - `./gradlew :app:compileAppDebugKotlin :app:assembleAppDebug --no-daemon`
- Flavor APK builds:
  - `./gradlew :app:assembleAppDebug`
  - `./gradlew :app:assembleGithubDebug`
- Unit tests used in CI:
  - `bash .github/scripts/setup-config.sh` (creates placeholder `app/.config/secrets.properties`)
  - `./gradlew test --stacktrace`

## CI and release-specific gotchas
- CI always runs `bash .github/scripts/validate-changelogs.sh`; any file in `fastlane/metadata/android/en-GB/changelogs/*.txt` must be `<= 500` chars.
- Version metadata is automated by workflow `version-updater.yml` on branch `platform/update-version-meta-data`; check that branch/PR before manual version-file edits.

## Dependency and build-edit conventions
- Use version catalog aliases from `gradle/libs.versions.toml`; do not hardcode dependency coordinates in module build files.
- Keep current toolchain choices unless asked explicitly: AGP/Kotlin/JVM levels, KAPT (do not opportunistically migrate to KSP), ObjectBox/DataBinding compatibility settings.
- Do not hardcode secrets; keep environment-specific values in `app/.config/*.properties` / local files and avoid committing them.
