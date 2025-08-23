---
applyTo: **/*.gradle, **/*.kts, **/*.kt, **/*.java
description: Copilot instructions for AniTrend Android app — focus on Gradle build system, buildSrc conventions, and version catalog usage. Keep stability first; prefer hints over sweeping changes.
---

# 🎯 Purpose
You are assisting on **AniTrend’s Android-only app**. Your primary goal is to work *with* the existing Gradle setup — especially logic in **`buildSrc`** and the **Gradle Version Catalog** (`gradle/libs.versions.toml`) — without breaking stability. Favor small, surgical edits and well‑commented suggestions. When proposing bigger changes, output a concise “migration note” and **suggest opening a GitHub issue** rather than applying it inline.

---

## 🔎 Project awareness & discovery rules
Before suggesting code:
1. **Scan `buildSrc/`**: Look for custom Gradle plugins, convention plugins, extension functions, and helper utilities (e.g., `configureAndroid`, `configureKotlin`, `configureDependencies`, variant hooks, etc.).  
   - If a **custom plugin** is applied in the app module, **do not** duplicate standard Android/Kotlin plugins in `app/build.gradle.kts`. Assume the custom plugin already wires them.
2. **Check the Version Catalog** (`gradle/libs.versions.toml`):  
   - Prefer `libs.<alias>` over raw coordinates.  
   - If a lib isn’t present, add it to the catalog first (with clear `versions`, `libraries`, and optional `bundles`).
3. **Look for properties & secrets**: Respect any `.properties` files (e.g., `version.properties`, `secrets.properties`, `configuration.properties`) and `.config/` conventions. If present, assume the build converts keys into `BuildConfig` fields — don't hardcode secrets in source.
4. **Identify flavors/variants**: If product flavors exist, **preserve them**. Never add or remove flavors/variants unless explicitly requested.
5. **KAPT/KSP**: If the project uses KAPT or KSP, **keep the current choice per library**. Don’t auto‑migrate processors.

---

## ✅ Guardrails (do this)
- **Use catalog aliases**: `implementation(libs.androidx.core.ktx)` not raw Maven coords.
- **Keep AGP/Kotlin/JVM levels as-is**: Only change when explicitly asked.
- **Prefer existing convention functions** from `buildSrc` (e.g., `android {}`, `kotlin {}`, `packaging {}`, `lint {}`) wired via custom plugins.
- **Add config via properties** files → `BuildConfig` when possible (don’t inline keys).
- **Be variant-aware**: If code/config is flavor‑specific, gate it by variant (e.g., via `androidComponents` hooks, `buildFeatures`, or flavor-specific source sets).

## ⛔️ Anti‑patterns (avoid this)
- Don’t add `com.android.application` / `org.jetbrains.kotlin.android` if a **custom plugin** already applies them.
- Don’t switch KAPT ↔ KSP, enable R8/minify, or change `compileSdk`, `minSdk`, `targetSdk` without instruction.
- Don’t hardcode versions in `dependencies {}` — use the **version catalog**.
- Don’t add Google Services / Firebase plugins globally; if present, they’re usually **conditional** on a flavor or `google-services.json`.
- Don’t remove legacy libs (e.g., view-binding vs older injection utilities) opportunistically. Suggest a migration issue instead.

---

## 🧩 Version Catalog usage patterns
**Add a new library**
```toml
# gradle/libs.versions.toml
[versions]
coil = "2.7.0"

[libraries]
coil = { module = "io.coil-kt:coil", version.ref = "coil" }
````

```kts
// app/build.gradle.kts
dependencies {
  implementation(libs.coil)
}
```

**Create a bundle**

```toml
[bundles]
markwon = ["markwon", "markwon.ext.tables", "markwon.ext.tasklist"]
```

```kts
dependencies {
  implementation(libs.bundles.markwon)
}
```

---

## 🧱 Typical module structure (conservative defaults)

When no custom plugin covers it, use the following patterns **but prefer existing project conventions** if found:

```kts
plugins {
  // Prefer applying the project’s custom plugin if present:
  // id("com.example.anitrend.conventions") 
}

android {
  namespace = "co.anitrend.app" // keep existing if already defined
  defaultConfig {
    // Respect versions injected from properties if the project uses them
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }

  buildFeatures {
    viewBinding = true
    // dataBinding = true // only if the project already uses it
  }

  // If flavors exist, do not alter their names or dimensions
  // flavorDimensions += "version"
  // productFlavors { create("github") { versionNameSuffix = "-github" } }
}

dependencies {
  // Always prefer libs.* from the catalog
  // implementation(libs.androidx.core.ktx)
}
```

---

## 🔧 Variant- and file-based configuration

**Conditional plugin/config on presence of files (safe pattern):**

```kts
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.kotlin.dsl.the

plugins { /* custom plugin likely already applied */ }

val hasGoogleServices = rootProject.file("app/google-services.json").exists()

the<ApplicationAndroidComponentsExtension>().beforeVariants {
  if (hasGoogleServices) {
    // Only then apply or enable Google Services/Crashlytics behavior
    // Do not apply plugins here if a custom plugin already does it.
  }
}
```

**Add `BuildConfig` fields from properties**

```kts
// Read once; prefer existing utilities in buildSrc if available.
val config = java.util.Properties().apply {
  val file = rootProject.file(".config/configuration.properties")
  if (file.exists()) file.inputStream().use(::load)
}

android {
  defaultConfig {
    config.forEach { (k, v) ->
      buildConfigField("String", k.toString(), "\"${v.toString()}\"")
    }
  }
}
```

> If the project already has a `buildSrc` helper for this, **use it instead** of re-implementing.

---

## 🧪 Testing & Lint (do not tighten by default)

* Keep `lint` behavior and `abortOnError` as currently configured.
* Keep unit test defaults (e.g., `returnDefaultValues = true`) unless instructed to change.

---

## 🚦 CI & tasks (conservative suggestions)

* Prefer existing tasks and workflows.
* Offer scripts/commands, but don’t rename tasks:

  * `./gradlew :app:assembleDebug`
  * `./gradlew test`
  * `./gradlew lint`

---

## 📝 Migration notes — suggest as GitHub Issues (don’t apply inline)

When you detect opportunities, **output a short note and propose an issue** instead of changing the build:

* **Migrate legacy view injection → ViewBinding/DataBinding**
  *Risk*: large refactor touching many files.
  *Issue template*: “Track migration off legacy injection; phase by screen; ensure parity; remove kapt processors when done.”

* **Upgrade heavy dependencies (DB, DI, networking)**
  *Risk*: runtime and codegen changes.
  *Issue template*: “Spike branch to upgrade X → Y; run full regression; update ProGuard/R8 rules if necessary.”

* **Split build logic from `buildSrc` to convention plugins**
  *Risk*: tooling changes, Gradle plugin mgmt tweaks.
  *Issue template*: “Extract `android-app-conventions` plugin; keep behavior identical; add publishing to `build-logic`.”

---

## 🧭 Copilot behavior summary

* **Be conservative**. Reuse `buildSrc` conventions and `libs.versions.toml`.
* **Never** duplicate plugins already applied by the project’s custom plugin.
* **Add dependencies via the catalog**, not raw strings.
* **Gate flavor-specific behavior** by variant or presence of config files.
* **Prefer suggestions + issues** for big refactors instead of inline edits.
