# Phase 2: Compose App Shell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the Compose UI scaffolding — MD3 `AniTrendTheme`, `NavHost` with placeholder screens, and a `MainActivity` single-activity entry point, all compiling and running alongside the existing `app/` module.

**Architecture:** Single `MainActivity` calls `setContent { AniTrendTheme { NavHost(...) } }`. The theme wraps three color schemes (light, dark, AMOLED black). The navigation graph defines all primary routes (splash, login, feed, detail, search, settings) with **Splash -> Feed (main)** as the default placeholder path. Each screen is a minimal placeholder composable; full ViewModel wiring comes in Phase 3.

**Tech Stack:** Compose BOM `2025.05.00`, Compose Material3, Navigation Compose `2.9.0`, Activity Compose `1.10.1`, Kotlin `2.3.21` with `org.jetbrains.kotlin.plugin.compose`.

**Existing project patterns:**
- Module uses `com.mxt.anitrend.plugin` (buildSrc `CorePlugin`) which auto-applies `kotlin-android`, `kotlinx-serialization`, `kotlin-parcelize`, `kotlin-kapt`, `io.objectbox`, and a shared dependency set from `ProjectDependencies.kt`. Phase 2 does NOT modify the shared plugin — it adds compose deps on top.
- Namespace is `com.mxt.anitrend.compose`; source package is `com.mxt.anitrend`.
- Flavor dimensions: `app` (Play Store) and `github`. Sdk: compileSdk 36, minSdk 23, targetSdk 36.

---

### File Structure

**Files to create:**
```
app-compose/src/main/java/com/mxt/anitrend/
├── MainActivity.kt                          # Single-activity Compose entry point
├── navigation/
│   └── NavGraph.kt                          # Sealed routes + NavHost composable
├── theme/
│   ├── Color.kt                             # MD3 color roles (light, dark, black)
│   ├── Theme.kt                             # AniTrendTheme composable
│   ├── Type.kt                              # MD3 type scale
│   └── Shape.kt                             # MD3 shape scheme
└── ui/
    ├── splash/SplashScreen.kt               # Placeholder splash
    ├── login/LoginScreen.kt                 # Placeholder login
    ├── feed/FeedScreen.kt                   # Placeholder feed (main screen)
    ├── detail/DetailScreen.kt               # Placeholder detail with mediaId arg
    ├── search/SearchScreen.kt               # Placeholder search
    └── settings/SettingsScreen.kt           # Placeholder settings
```

**Files to modify:**
- `gradle/libs.versions.toml` — add Compose BOM, Navigation Compose, Activity Compose, lifecycle-compose deps
- `app-compose/build.gradle.kts` — add compose plugin, buildFeatures, deps
- `app-compose/src/main/AndroidManifest.xml` — add INTERNET permission, declare `MainActivity` as launcher

---

### Task 1: Add Compose dependencies to version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

The catalog already has `androix-lifecycle = "2.10.0"` and `jetbrains-kotlinx-coroutines = "1.10.2"`. Add Compose-specific entries.

- [x] **Step 1: Add compose BOM version and library entries**

Add to the `[versions]` section:

```toml
compose-bom = "2025.05.00"
navigation-compose = "2.9.0"
activity-compose = "1.10.1"
```

Add to the `[libraries]` section:

```toml
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androix-lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androix-lifecycle" }
```

Place entries in alphabetical order within each section, following the existing catalog style.

Expected: The catalog file compiles (validation happens when Gradle syncs in Task 11).

---

### Task 2: Update app-compose/build.gradle.kts

**Files:**
- Modify: `app-compose/build.gradle.kts`

- [x] **Step 1: Add kotlin-compose plugin and compose buildFeatures**

```kotlin
plugins {
    id("com.mxt.anitrend.plugin")
    id("com.google.devtools.ksp") version "2.3.7"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
}

android {
    namespace = "com.mxt.anitrend.compose"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // BOM — controls all Compose library versions
    implementation(platform(libs.compose.bom))

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Material Design 3
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Activity + Navigation
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room compiler (from Phase 1)
    ksp(libs.room.compiler)
}
```

Expected: No Gradle sync errors when run in Task 11.

---

### Task 3: Create MD3 color scheme (Color.kt)

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/theme/Color.kt`

- [x] **Step 1: Define brand palette and MD3 color roles for light, dark, and AMOLED black**

```kotlin
package com.mxt.anitrend.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// ── Brand palette ──────────────────────────────────────────────────
// Primary seed: #54A5FA (AniTrend brand blue accent)

private val BrandPrimaryLight = Color(0xFF1863C9)
private val BrandPrimaryDark = Color(0xFFAAC7FF)

// ── Light scheme ───────────────────────────────────────────────────

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1863C9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF545F71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF111C2B),
    tertiary = Color(0xFF6D5676),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6D9FF),
    onTertiaryContainer = Color(0xFF271430),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFAAC7FF),
    surfaceTint = Color(0xFF1863C9),
)

// ── Dark scheme ────────────────────────────────────────────────────

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAAC7FF),
    onPrimary = Color(0xFF003065),
    primaryContainer = Color(0xFF004A99),
    onPrimaryContainer = Color(0xFFD7E3FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF263141),
    secondaryContainer = Color(0xFF3C4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFD9BDE3),
    onTertiary = Color(0xFF3D2946),
    tertiaryContainer = Color(0xFF553F5D),
    onTertiaryContainer = Color(0xFFF6D9FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF1863C9),
    surfaceTint = Color(0xFFAAC7FF),
)

// ── AMOLED Black scheme ────────────────────────────────────────────
// Same as dark but with true black (#000) backgrounds.
// This collapses the status/nav bar area into the display for OLED panels.

val BlackColorScheme = darkColorScheme(
    primary = Color(0xFFAAC7FF),
    onPrimary = Color(0xFF003065),
    primaryContainer = Color(0xFF004A99),
    onPrimaryContainer = Color(0xFFD7E3FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF263141),
    secondaryContainer = Color(0xFF3C4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFD9BDE3),
    onTertiary = Color(0xFF3D2946),
    tertiaryContainer = Color(0xFF553F5D),
    onTertiaryContainer = Color(0xFFF6D9FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color(0xFFE3E2E6),
    surface = Color.Black,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF1863C9),
    surfaceTint = Color(0xFFAAC7FF),
)
```

Expected: File compiles when used from Theme.kt. No lint issues.

---

### Task 4: Create MD3 type scale (Type.kt)

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/theme/Type.kt`

- [x] **Step 1: Define MD3 type scale using Material3 defaults**

```kotlin
package com.mxt.anitrend.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Uses default system sans-serif family adjusted for MD3 scale.
// Override individual styles here as brand typography evolves.

val AniTrendTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
```

Expected: File compiles.

---

### Task 5: Create MD3 shape scheme (Shape.kt)

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/theme/Shape.kt`

- [x] **Step 1: Define MD3 shape categories**

```kotlin
package com.mxt.anitrend.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AniTrendShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
```

Expected: File compiles.

---

### Task 6: Create AniTrendTheme composable (Theme.kt)

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/theme/Theme.kt`

- [x] **Step 1: Define theme mode enum and AniTrendTheme composable**

```kotlin
package com.mxt.anitrend.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    BLACK,
}

private val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

@Composable
fun AniTrendTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.BLACK -> true
    }

    val colorScheme = when {
        themeMode == ThemeMode.BLACK -> BlackColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AniTrendTypography,
        shapes = AniTrendShapes,
    ) {
        CompositionLocalProvider(LocalThemeMode provides themeMode) {
            content()
        }
    }
}
```

Expected: File compiles.

Note: The static `LocalThemeMode` is declared but not used with `CompositionLocalProvider` yet — it's available for Phase 3 when theme switching UI is built.

---

### Task 7: Create navigation graph (NavGraph.kt)

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/navigation/NavGraph.kt`

- [x] **Step 1: Define sealed routes and NavHost composable**

```kotlin
package com.mxt.anitrend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mxt.anitrend.ui.detail.DetailScreen
import com.mxt.anitrend.ui.feed.FeedScreen
import com.mxt.anitrend.ui.login.LoginScreen
import com.mxt.anitrend.ui.search.SearchScreen
import com.mxt.anitrend.ui.settings.SettingsScreen
import com.mxt.anitrend.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val FEED = "feed"
    const val DETAIL = "detail/{mediaId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun detail(mediaId: Long) = "detail/$mediaId"
}

@Composable
fun AniTrendNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.FEED) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                } },
            )
        }

        composable(Routes.FEED) {
            FeedScreen(
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToDetail = { mediaId ->
                    navController.navigate(Routes.detail(mediaId))
                },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
            DetailScreen(
                mediaId = mediaId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { mediaId ->
                    navController.navigate(Routes.detail(mediaId))
                },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
```

Expected: File compiles.

---

### Task 8: Create placeholder screens (6 files)

Create one file per screen under `app-compose/src/main/java/com/mxt/anitrend/ui/`.

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/splash/SplashScreen.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/login/LoginScreen.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/feed/FeedScreen.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/detail/DetailScreen.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/search/SearchScreen.kt`
- Create: `app-compose/src/main/java/com/mxt/anitrend/ui/settings/SettingsScreen.kt`

- [x] **Step 1: Create SplashScreen**

```kotlin
package com.mxt.anitrend.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(600L)
        // Phase 3: check auth state gate here.
        // Phase 2 placeholder behavior: Splash -> Main feed.
        onNavigateToMain()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "AniTrend",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
```

- [x] **Step 2: Create LoginScreen**

```kotlin
package com.mxt.anitrend.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "AniTrend",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to sync your anime lists",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLoginSuccess) {
            Text("Log In with AniList (placeholder)")
        }
    }
}
```

- [x] **Step 3: Create FeedScreen (main content)**

```kotlin
package com.mxt.anitrend.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Feed",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Your anime & manga activity will appear here",
            style = MaterialTheme.typography.bodyLarge,
        )

        Button(
            onClick = { onNavigateToDetail(1L) },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Open Detail (placeholder)")
        }

        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Search")
        }

        Button(
            onClick = onNavigateToSettings,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Settings")
        }
    }
}
```

- [x] **Step 4: Create DetailScreen**

```kotlin
package com.mxt.anitrend.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailScreen(
    mediaId: Long,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Detail",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Media ID: $mediaId",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
```

- [x] **Step 5: Create SearchScreen**

```kotlin
package com.mxt.anitrend.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search for anime & manga",
            style = MaterialTheme.typography.bodyLarge,
        )

        Button(
            onClick = { onNavigateToDetail(1L) },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Open Detail (placeholder)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
```

- [x] **Step 6: Create SettingsScreen**

```kotlin
package com.mxt.anitrend.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App preferences and account settings",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}
```

Expected: All six files compile. No unused import warnings.

---

### Task 9: Create MainActivity.kt

**Files:**
- Create: `app-compose/src/main/java/com/mxt/anitrend/MainActivity.kt`

- [x] **Step 1: Write single-activity Compose entry point**

```kotlin
package com.mxt.anitrend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.mxt.anitrend.navigation.AniTrendNavGraph
import com.mxt.anitrend.theme.AniTrendTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AniTrendTheme {
                val navController = rememberNavController()
                AniTrendNavGraph(navController = navController)
            }
        }
    }
}
```

Expected: File compiles.

---

### Task 10: Update AndroidManifest.xml

**Files:**
- Modify: `app-compose/src/main/AndroidManifest.xml`

- [x] **Step 1: Replace current manifest content**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name="com.mxt.anitrend.AniTrendApp"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name="com.mxt.anitrend.MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

Key changes:
- Added `<uses-permission android:name="android.permission.INTERNET" />` for Apollo GraphQL calls
- Replaced generic `<application>` block with one declaring `com.mxt.anitrend.MainActivity` as the launcher activity
- Theme is `@android:style/Theme.Material.Light.NoActionBar` (platform theme, not AppCompat — no `app/` module resource dependency)

Expected: No resource resolution errors (theme is a platform `@android:style`, not AppCompat).

---

### Task 11: Verify build

- [x] **Step 1: Run full assemble for the app-compose module**

Run: `./gradlew :app-compose:assembleAppDebug`

Expected: `BUILD SUCCESSFUL` with zero compilation errors. The APK is produced at `app-compose/build/outputs/apk/app/debug/app-compose-app-debug.apk`.

If compilation fails, common issues:
- `Unresolved reference: lightColorScheme` / `darkColorScheme` — missing `import androidx.compose.material3.*` (add explicit imports to Color.kt)
- `Activity.setContent` not found — missing `activity-compose` dependency
- `enableEdgeToEdge()` not found — `enableEdgeToEdge()` requires `import androidx.activity.enableEdgeToEdge` and `activity-ktx` 1.8.0+ (already in version catalog)
- Kotlin Compose compiler plugin not applied — verify `id("org.jetbrains.kotlin.plugin.compose")` is in plugins block
- `okHttpClient()` import — if any existing file references this, ensure `import com.apollographql.apollo.network.okHttpClient` is present

- [x] **Step 2: Verify the APK exists**

```bash
ls -lh app-compose/build/outputs/apk/app/debug/*.apk
```

Expected: A non-zero-size APK file exists.

- [x] **Step 3: Run lint**

```bash
./gradlew :app-compose:lint
```

Expected: `BUILD SUCCESSFUL` (lint is configured to not abort on error in the shared plugin).

---

### Self-Review

**Spec coverage:**
- Add Compose BOM, Compose compiler, MD3, Navigation Compose to `libs.versions.toml` → **Task 1**
- Create MD3 `AniTrendTheme` composable (three color schemes) → **Tasks 3, 4, 5, 6**
- Create `NavHost` with route definitions for all major screens → **Task 7**
- Wire Apollo + Room into the new module → Already done in Phase 1 (`AniTrendApp.kt` starts Koin with `apiModule` + `localModule`); Phase 2 keeps those intact
- Create placeholder screens → **Task 8**
- Create `MainActivity.kt` as single-activity entry point → **Task 9**
- Update `AndroidManifest.xml` to use `MainActivity` as launcher → **Task 10**
- Remove deprecated theme/styles references → Manifest already uses `@android:style/Theme.Material.Light.NoActionBar` (no AppCompat dependency)
- Verify build → **Task 11**

**Placeholder scan:**
- All code blocks contain complete Kotlin source with no `TBD`, `TODO`, or `implement later` placeholders.
- Every file path is absolute and exact.
- Imports for `Color.kt` and callback signatures across `SplashScreen` and `NavGraph` are aligned.
- Manifest class names use fully-qualified names to avoid namespace/package ambiguity.

**Type consistency:**
- `Routes.detail(mediaId: Long)` → called from FeedScreen and SearchScreen with `Long` → DetailScreen receives `mediaId: Long` → consistent.
- Navigation callbacks follow consistent naming: `onNavigateToX`, `onLoginSuccess`, `onNavigateBack`.
- `NavHostController` is created once in `MainActivity` and passed to `AniTrendNavGraph`.

---

### Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-29-phase2-compose-shell.md`. Two execution options:

1. **Subagent-Driven (recommended)** — dispatch a fresh subagent per task, review between tasks, fast iteration
2. **Inline Execution** — execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
