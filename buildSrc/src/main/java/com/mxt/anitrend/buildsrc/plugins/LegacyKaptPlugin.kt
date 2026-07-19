package com.mxt.anitrend.buildsrc.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * No-op plugin matching the [com.android.legacy-kapt] plugin ID that AGP 9.x
 * expects when built-in Kotlin is enabled and KSP migration is not possible.
 *
 * AGP's [BuiltInKotlinServicesKt.initBuiltInKaptSupportIfRequired] registers a
 * [PluginManager.withPlugin("com.android.legacy-kapt")] listener. When this
 * plugin is applied, AGP calls [initBuiltInKaptSupport], which registers the
 * [kapt] extension (and its Gradle configuration) from the already-initialised
 * [KotlinBaseApiPlugin].
 *
 * The plugin body is intentionally empty; all kapt wiring is done by AGP's
 * built-in Kotlin support upon detecting this plugin ID.
 *
 * Remove this descriptor and class once AGP ships its own
 * `com.android.legacy-kapt` plugin descriptor, otherwise a duplicate plugin ID
 * conflict will occur during dependency resolution.
 */
class LegacyKaptPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // No-op: AGP's withPlugin callback handles all kapt setup.
    }
}
