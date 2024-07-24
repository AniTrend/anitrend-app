package com.mxt.anitrend.buildsrc.components

import com.mxt.anitrend.buildsrc.extensions.androidComponents
import org.gradle.api.Project

internal fun Project.configurePlugins() {
    plugins.apply("com.android.application")
    plugins.apply("kotlin-android")
    plugins.apply("kotlinx-serialization")
    plugins.apply("kotlin-parcelize")
    plugins.apply("kotlin-kapt")
    plugins.apply("io.objectbox")
}

internal fun Project.configureAdditionalPlugins() {
    androidComponents().beforeVariants {
        logger.lifecycle("VariantFilter { name: ${it.name}, flavor: ${it.flavorName}, module: $name }")
        if (it.flavorName == "google") {
            logger.lifecycle("Applying additional google plugins on -> module: $name | type: ${it.name}")
            if (file("google-services.json").exists()) {
                plugins.apply("com.google.gms.google-services")
                plugins.apply("com.google.firebase.crashlytics")
            } else logger.lifecycle("google-services.json cannot be found and will not be using any of the google plugins")
        }
    }
}