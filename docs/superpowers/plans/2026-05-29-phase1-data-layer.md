# Phase 1: Core Data Layer (Apollo Kotlin + Room) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Set up Apollo Kotlin client and Room database alongside the existing app, migrate Glide from kapt to KSP.

**Architecture:** Add KSP plugin, create `app-compose/` Gradle module, configure Apollo client with normalized SQLite cache, create Room database with initial entity, wire both into Koin modules. The existing `app/` module runs unchanged. Glide moves to KSP; ObjectBox stays on kapt.

**Tech Stack:** Apollo Kotlin (runtime + normalized cache + annotations), Room (runtime + ktx + compiler via KSP), KSP, Kotlin 2.3.21, AGP 8.13.2

---

### Task 1: Add Apollo Kotlin, KSP, and Room to Version Catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add version keys to `[versions]` section**

Insert after the `hamcrest` line (or wherever makes sense alphabetically):

```toml
apollo = "4.1.0"
ksp = "2.3.21-1.0.XX"  # Verify exact version from https://github.com/google/ksp/releases — must match Kotlin 2.3.21
room = "2.7.1"
```

- [ ] **Step 2: Add library entries to `[libraries]` section**

Insert a new `[libraries]` section entry group:

```toml
apollo-runtime = { module = "com.apollographql.apollo:apollo-runtime", version.ref = "apollo" }
apollo-normalized-cache = { module = "com.apollographql.apollo:apollo-normalized-cache-api", version.ref = "apollo" }
apollo-normalized-cache-sqlite = { module = "com.apollographql.apollo:apollo-normalized-cache-sqlite", version.ref = "apollo" }
apollo-annotations = { module = "com.apollographql.apollo:apollo-runtime-annotations", version.ref = "apollo" }

ksp-gradle-plugin = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }

room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

- [ ] **Step 3: Verify the catalog**

Run: `./gradlew :app:dependencies > /dev/null 2>&1` and confirm the TOML parses without error. Expected output: no exceptions, exits with BUILD SUCCESSFUL.

---

### Task 2: Add KSP Plugin to buildSrc

**Files:**
- Modify: `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/extensions/DependencyHandlerExtensions.kt`
- Modify: `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/PluginComponents.kt`

- [ ] **Step 1: Add `ksp` dependency type to `DependencyHandlerExtensions.kt`**

Add `KSP("ksp")` to the `DependencyType` enum:

```kotlin
private enum class DependencyType(val configurationName: String) {
    API("api"),
    COMPILE("compileOnly"),
    DEBUG("debugOnly"),
    KAPT("kapt"),
    KSP("ksp"),
    IMPLEMENTATION("implementation"),
    DEBUG_IMPLEMENTATION("debugImplementation"),
    RELEASE_IMPLEMENTATION("releaseImplementation"),
    RUNTIME("runtimeOnly"),
    TEST("testImplementation"),
    ANDROID_TEST("androidTestImplementation")
}
```

- [ ] **Step 2: Add `ksp` extension function to `DependencyHandlerExtensions.kt`**

Insert after the `kapt` function:

```kotlin
internal fun DependencyHandler.ksp(
    dependencyNotation: Any,
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.KSP, dependencyConfiguration)
```

- [ ] **Step 3: Add KSP plugin to `PluginComponents.kt`**

In `configurePlugins()`, add `kotlin-ksp` after `kotlin-kapt`:

```kotlin
internal fun Project.configurePlugins() {
    plugins.apply("com.android.application")
    plugins.apply("kotlin-android")
    plugins.apply("kotlinx-serialization")
    plugins.apply("kotlin-parcelize")
    plugins.apply("kotlin-kapt")
    plugins.apply("com.google.devtools.ksp")
    plugins.apply("io.objectbox")
    // ...
}
```

- [ ] **Step 4: Add KSP classpath dependency to root `build.gradle.kts`**

In `buildscript { dependencies { } }`, add:

```kotlin
classpath(libs.ksp.gradle.plugin)
```

- [ ] **Step 5: Verify the build**

Run: `./gradlew :app:assembleAppDebug > /dev/null 2>&1`
Expected: `BUILD SUCCESSFUL`. The KSP plugin is applied but has no processors yet, so it's a no-op.

---

### Task 3: Migrate Glide from kapt to KSP

**Files:**
- Modify: `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt`

- [ ] **Step 1: Replace `kapt` with `ksp` for Glide compiler**

In `ProjectDependencies.kt`, change:

```kotlin
// Old:
dependencies.kapt(libs.glide.compiler)

// New:
dependencies.ksp(libs.glide.compiler)
```

- [ ] **Step 2: Verify the build**

Run: `./gradlew :app:assembleAppDebug`
Expected: `BUILD SUCCESSFUL`. Glide annotation processing now runs via KSP instead of kapt. ObjectBox still uses `kapt` (no change).

---

### Task 4: Create `app-compose` Module Structure

**Files:**
- Create: `app-compose/build.gradle.kts`
- Create: `app-compose/src/main/AndroidManifest.xml`
- Create: `app-compose/src/main/java/com/mxt/anitrend/AniTrendApp.kt`
- Create: `app-compose/src/main/res/values/strings.xml`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Create `app-compose/build.gradle.kts`**

```kotlin
plugins {
    id("com.mxt.anitrend.plugin")
}

android {
    namespace = "com.mxt.anitrend.compose"
}
```

This delegates all plugin/dependency configuration to the existing `CorePlugin` from buildSrc — same as `app/`.

- [ ] **Step 2: Create `app-compose/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".AniTrendApp"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.AniTrend" />

</manifest>
```

The theme reference will use the existing `AppThemeLight` from the `app/` module's resources for now. In Phase 2, Compose theming replaces this.

- [ ] **Step 3: Create `app-compose/src/main/java/com/mxt/anitrend/AniTrendApp.kt`**

```kotlin
package com.mxt.anitrend

import android.app.Application

class AniTrendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Phase 2: initialize Koin, Apollo, Room here
    }
}
```

- [ ] **Step 4: Create `app-compose/src/main/res/values/strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">AniTrend (Compose)</string>
</resources>
```

- [ ] **Step 5: Add module to `settings.gradle.kts`**

```kotlin
rootProject.name = "anitrend-app"
include(":app")
include(":app-compose")
```

- [ ] **Step 6: Create module directories**

```bash
mkdir -p app-compose/src/main/java/com/mxt/anitrend
mkdir -p app-compose/src/main/res/values
```

- [ ] **Step 7: Verify the build**

Run: `./gradlew :app-compose:assembleAppDebug`
Expected: `BUILD SUCCESSFUL`. The module compiles and produces a placeholder APK.

---

### Task 5: Configure Apollo Kotlin Client

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/api/ApiClient.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/api/ApiModule.kt`

- [ ] **Step 1: Create `ApiClient.kt`**

```kotlin
package com.mxt.anitrend.data.api

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .okHttpClient(okHttpClient)
            .build()
    }
}
```

- [ ] **Step 2: Create `ApiModule.kt` (Koin module)**

```kotlin
package com.mxt.anitrend.data.api

import org.koin.dsl.module

val apiModule = module {
    single { ApiClient.apolloClient }
}
```

- [ ] **Step 3: Verify the build**

Run: `./gradlew :app-compose:compileAppDebugKotlin`
Expected: `BUILD SUCCESSFUL`. Apollo client compiles.

---

### Task 6: Set Up Room Database with Initial Schema

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/local/AppDatabase.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/local/entity/UserPreferences.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/local/dao/UserPreferencesDao.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/data/local/LocalModule.kt`

- [ ] **Step 1: Create `AppDatabase.kt`**

```kotlin
package com.mxt.anitrend.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import com.mxt.anitrend.data.local.entity.UserPreferencesEntity

@Database(
    entities = [UserPreferencesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        fun create(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "anitrend-compose.db")
                .build()
    }
}
```

- [ ] **Step 2: Create `UserPreferencesEntity.kt`**

```kotlin
package com.mxt.anitrend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val selectedTheme: String = "light",
    val showAdultContent: Boolean = false,
    val currentUserId: Long? = null
)
```

- [ ] **Step 3: Create `UserPreferencesDao.kt`**

```kotlin
package com.mxt.anitrend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mxt.anitrend.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun observe(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preferences: UserPreferencesEntity)
}
```

- [ ] **Step 4: Create `LocalModule.kt`**

```kotlin
package com.mxt.anitrend.data.local

import android.content.Context
import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { AppDatabase.create(androidContext()) }
    single<UserPreferencesDao> { get<AppDatabase>().userPreferencesDao() }
}
```

- [ ] **Step 5: Verify the build**

Run: `./gradlew :app-compose:compileAppDebugKotlin`
Expected: `BUILD SUCCESSFUL`. Room entities and DAOs compile via KSP codegen.

---

### Task 7: Wire Core Data Koin Modules

**Files:**
- Modify: `app-compose/src/main/java/com/mxt/anitrend/AniTrendApp.kt`

- [ ] **Step 1: Update `AniTrendApp.kt` to initialize Koin**

```kotlin
package com.mxt.anitrend

import android.app.Application
import com.mxt.anitrend.data.api.apiModule
import com.mxt.anitrend.data.local.localModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AniTrendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AniTrendApp)
            modules(listOf(apiModule, localModule))
        }
    }
}
```

- [ ] **Step 2: Verify the build**

Run: `./gradlew :app-compose:assembleAppDebug`
Expected: `BUILD SUCCESSFUL`. Full APK produced with Apollo + Room + Koin wired.

---

### Task 8: Add Room + KSP deps to `buildSrc` dependencies for `app-compose`

**Files:**
- Modify: `buildSrc/src/main/java/com/mxt/anitrend/buildsrc/components/ProjectDependencies.kt`

Since `ProjectDependencies.kt` applies all dependencies to every module that uses `CorePlugin`, we need to add Room and Apollo deps there so `app-compose` picks them up. We keep them scoped to `app-compose` only by using conditional logic.

- [ ] **Step 1: Add Room + Apollo dependencies to `ProjectDependencies.kt`**

Insert after the `/** Testing-only dependencies */` block:

```kotlin
    /** Apollo GraphQL */
    dependencies.implementation(libs.apollo.runtime)
    dependencies.implementation(libs.apollo.normalized.cache)
    dependencies.implementation(libs.apollo.normalized.cache.sqlite)
    dependencies.implementation(libs.apollo.annotations)

    /** Room Database */
    dependencies.implementation(libs.room.runtime)
    dependencies.implementation(libs.room.ktx)
    dependencies.ksp(libs.room.compiler)
```

- [ ] **Step 2: Verify both modules build**

Run: `./gradlew :app:assembleAppDebug :app-compose:assembleAppDebug`
Expected: `BUILD SUCCESSFUL`. Both modules compile. The old `app/` module gains Apollo + Room on its classpath (harmless — unused until Phase 3 wiring) and the new `app-compose` module has them ready.

---

### Task 9: Full Integration Verification

- [ ] **Step 1: Clean build**

```bash
./gradlew clean :app:assembleAppDebug :app-compose:assembleAppDebug
```

Expected: `BUILD SUCCESSFUL`. Both APKs are produced.

- [ ] **Step 2: Verify old app tests still pass**

```bash
./gradlew :app:testAppDebugUnitTest
```

Expected: All existing unit tests pass.

- [ ] **Step 3: Verify APKs exist**

```bash
ls -la app/build/outputs/apk/app/debug/*.apk app-compose/build/outputs/apk/app/debug/*.apk
```

Expected: Two APK files exist (old `app` and new `app-compose` placeholder).
