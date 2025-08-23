---
applyTo: **
description: This file describes the Kotlin code style for the project.
---
# AniTrend Android Client - AI Coding Guidelines

## Architecture Overview

AniTrend uses a custom **MVVMP architecture** (Model-View-ViewModel-Presenter) designed for component independence and state self-management. The architecture promotes decoupling with EventBus for cross-component communication.

### Core Architecture Patterns

- **Generic Base Classes**: Activities/Fragments extend `ActivityBase<M, P>` or `FragmentBase<M, P, VM>` where:
  - `M` = data model type (e.g., `VersionBase`, `MediaBase`)  
  - `P` = presenter extending `CommonPresenter` (usually `BasePresenter` or `MediaPresenter`)
  - `VM` = view model data for fragments

- **Strict Component Lifecycle**: All components follow these mandatory patterns:
  - Call `setPresenter(new SomePresenter(this))` and `setViewModel(true)` in `onCreate()`
  - Override `makeRequest()` for API calls - triggered after `onActivityReady()` 
  - Override `updateUI()` for view updates - called when data is ready
  - Override `onChanged(@Nullable M model)` for observing model state changes
  - Use `onActivityReady()` for post-creation setup (permissions, background tasks)

- **ViewModelBase Pattern**: ViewModels extend `ViewModelBase<T>` and implement `RetroCallback<T>` for Retrofit integration. They handle network request dispatching and use Bundle parameters instead of plain objects.

### GraphQL Integration

- **Schema-First**: GraphQL queries defined in `app/src/main/assets/` using `@GraphQuery` annotations
- **Query Building**: Use `GraphUtil.getDefaultQuery(includePaging)` then add variables with `QueryContainerBuilder.putVariable()`
- **API Models**: Retrofit interfaces in `model/api/retro/anilist/` map to GraphQL operations
- **Request Pattern**: 
  ```kotlin
  val queryContainer = GraphUtil.getDefaultQuery(false)
      .putVariable(KeyUtil.arg_id, id)
  viewModel.params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
  viewModel.requestData(KeyUtil.MEDIA_BASE_REQ, context)
  ```

### Build System & Dependencies

- **Custom Gradle Plugin**: Core functionality in `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/plugin/CorePlugin.kt`
- **Flavor Configuration**: Two product flavors - `app` (Play Store) and `github` (GitHub releases)  
- **Dependency Management**: Centralized in `gradle/libs.versions.toml` using Gradle version catalogs
- **Configuration Files**: Requires `.config/secrets.properties` and `.config/configuration.properties` (use `.github/scripts/setup-config.sh` for setup)

### Data Layer & Persistence

- **ObjectBox Database**: 10x faster than alternatives using key-value storage instead of column storage
- **Database Helper**: Access via `BoxQuery` interface through presenter's `getDatabase()` method
- **Custom Type Converters**: Complex objects stored as JSON using `@Convert` annotations (e.g., `MediaListOptionsConverter`)  
- **Direct BoxStore Access**: For custom queries use `getBoxStore(Class<S> classType)` where S has `@Entity` annotation
- **Preferences**: Use `ApplicationPref` utility class for simple settings, accessed through CommonPresenter
- **Authentication**: OAuth tokens stored securely, managed by `WebTokenRequest` and auth interceptors

### Communication Patterns

- **Network Layer**: Retrofit with Gson and custom GraphQL converter for AniList API
- **Cross-Component Communication**: EventBus publisher-subscriber pattern for non-attached classes
- **Request Handling**: Multi-threaded `RequestHandler` manages network requests using Bundle parameters
- **Error States**: Implement `ResponseCallback` interface with `showError()` and `showEmpty()` methods

### Development Workflows

- **Required Setup**: 
  - Copy `.travis-ci/google-services.json` to `app/google-services.json`
  - Run `bash .github/scripts/setup-config.sh` to create `.config/secrets.properties`
  - Get AniList API credentials from https://anilist.co/settings/developer
  - Set redirect URL to `intent://com.mxt.anitrend` for OAuth

- **Build Commands**:
  - `./gradlew clean` - Clean build artifacts
  - `./gradlew assembleRelease` - Build release APK  
  - `./gradlew test` - Run unit tests
  - `bundle exec fastlane build` - Fastlane build process

- **CI/CD**: GitHub Actions workflows in `.github/workflows/` for testing, building, and releases

- **Version & Changelog Management**:
  - Create a TODO list for these actions first before you begin any changes.
  - Check next version from `gradle/version.properties` but always check for automated PR from `platform/update-version-meta-data` branch for this REPO, this should be our source of truth as the version might be bumped between releases/changes.
  - When making user-facing changes, create/update changelog file in `fastlane/metadata/android/en-GB/changelogs/{versionCode}.txt`
  - Use version code format (e.g., `1011009000.txt` for version 1.11.9) matching the `code` field in in the check version step.
  - Focus only on user-visible changes (features, bug fixes, improvements) - exclude internal/technical changes
  - Follow existing changelog format with emojis: 🚀 What's New, 📈 Improvements, 🐛 Bug Fixes
  - Also assure that `app/src/main/assets/changelog.md` is updated accordingly, but keep the format for this file as as close to what already exists within it as much as possible

### Key Conventions

- **Dependency Injection**: Koin modules defined in `app/src/main/java/com/mxt/anitrend/koin/Modules.kt`
- **View Binding**: ButterKnife with `@BindView` annotations for view references
- **Presenter Responsibilities**: Handle SharedPreferences listeners, database access, and RecyclerView scroll listeners
- **Application Class**: `App.kt` configures database, analytics, and exception handlers
- **Type Safety**: Use `@StringDef` annotations for constants (e.g., `@KeyUtil.ApplicationTheme`)
- **Worker Tasks**: Background sync via AndroidX WorkManager (notification sync, genre/tag updates)
- **Navigation**: Intent-based with deep link support for AniList URLs via `IntentBundleUtil`

### Project-Specific Patterns

- **Activity Example**: See `SplashActivity` extending `ActivityBase<VersionBase, BasePresenter>`
- **Component Self-Management**: Each component manages its own state and lifecycle independently  
- **Presenter Lifecycle**: Always call `presenter.onResume()` / `presenter.onPause()` with SharedPreference listeners
- **Request Keys**: Use `KeyUtil` constants for request types and parameter keys
- **Multi-Module Requests**: Use `RequestHandler` for async GraphQL operations with Bundle parameters
- **Preference Keys**: Store in `strings.xml` for settings screens, otherwise as class constants

When working with this codebase, always check existing patterns in similar activities/fragments before implementing new features. The architecture is consistent but custom, so following established conventions is crucial for maintainability.