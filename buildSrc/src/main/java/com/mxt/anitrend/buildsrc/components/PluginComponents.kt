package com.mxt.anitrend.buildsrc.components

import com.mxt.anitrend.buildsrc.extensions.androidComponents
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configurePlugins() {
    plugins.apply("com.android.application")
    plugins.apply("com.diffplug.spotless")
    plugins.apply("co.anitrend.retrofit.graphql.codegen")
    plugins.apply("kotlinx-serialization")
    plugins.apply("kotlin-parcelize")
    plugins.apply("com.android.legacy-kapt")
    plugins.apply("io.objectbox")
    plugins.apply("io.github.takahirom.roborazzi")

    // Screenshot regression outputs: reviewed references live in source
    // control, comparison output stays in build outputs. Separate output
    // directories keep variant tasks from racing over the same files.
    extensions.configure<RoborazziExtension> {
        outputDir.set(file("src/test/screenshots"))
        compare {
            outputDir.set(file("build/outputs/screenshots-comparison"))
        }
        separateOutputDirs.set(true)
    }

    // Stable visual-proof aliases for the appDebug variant only. These run
    // the screenshot tests once and never touch APK or device tasks.
    val screenshotsDir = file("src/test/screenshots")
    val roborazziResultsDir = file("build/test-results/roborazzi")
    tasks.register("recordUiScreenshots") { dependsOn("recordRoborazziAppDebug") }
    tasks.register("verifyUiScreenshots") {
        dependsOn("verifyRoborazziAppDebug")
        doLast {
            // Roborazzi's verification covers changed and missing references;
            // references without a capture in this run are rejected here so a
            // deleted or renamed capture cannot silently retire its baseline.
            // Paths are resolved at configuration time to stay
            // configuration-cache safe.
            val summary = roborazziResultsDir
                .walkTopDown()
                .firstOrNull { it.name == "results-summary.json" }
                ?: throw GradleException("Roborazzi results summary missing; verification did not run")
            val referenced = Regex("\"golden_file_path\"\\s*:\\s*\"([^\"]+)\"")
                .findAll(summary.readText())
                .map { it.groupValues[1].substringAfterLast('/') }
                .toSet()
            val references = screenshotsDir
                .walkTopDown()
                .filter { it.isFile && it.extension == "png" }
                .map { it.name }
                .toSet()
            val missingCaptures = references - referenced
            if (missingCaptures.isNotEmpty()) {
                throw GradleException(
                    "Screenshot references without a matching capture: ${missingCaptures.sorted().joinToString()}",
                )
            }
        }
    }
    tasks.register("compareUiScreenshots") { dependsOn("compareRoborazziAppDebug") }

    tasks.matching { it.name.startsWith("objectbox") }.configureEach {
        notCompatibleWithConfigurationCache("ObjectBox PrepareTask cannot serialize Project reference")
    }

    tasks.matching { it.name.startsWith("dataBinding") }.configureEach {
        notCompatibleWithConfigurationCache("AGP DataBinding task cannot serialize ResolutionBackedFileCollection")
    }
}

internal fun Project.configureAdditionalPlugins() {
    androidComponents().beforeVariants {
        logger.lifecycle("VariantFilter { name: ${it.name}, flavor: ${it.flavorName}, module: $name }")
        if (it.flavorName == "google") {
            logger.lifecycle("Applying additional google plugins on -> module: $name | type: ${it.name}")
            if (file("google-services.json").exists()) {
                plugins.apply("com.google.gms.google-services")
                plugins.apply("com.google.firebase.crashlytics")
            } else {
                logger.lifecycle("google-services.json cannot be found and will not be using any of the google plugins")
            }
        }
    }
}
