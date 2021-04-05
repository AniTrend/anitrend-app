// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions")
}

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(com.mxt.anitrend.buildsrc.Libraries.Android.Tools.buildGradle)
        classpath(com.mxt.anitrend.buildsrc.Libraries.JetBrains.Kotlin.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.JetBrains.Kotlin.Serialization.serialization)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Koin.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Google.Services.googleServices)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Google.Firebase.Crashlytics.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.ObjectBox.Gradle.plugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url = java.net.URI(com.mxt.anitrend.buildsrc.Libraries.Repositories.jitPack)
        }
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
}

tasks.named(
    "dependencyUpdates",
    DependencyUpdatesTask::class.java
).configure {
    checkForGradleUpdate = false
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
    resolutionStrategy {
        componentSelection {
            all {
                val reject = listOf("preview", "alpha", "beta", "m")
                    .map { qualifier ->
                        val pattern = "(?i).*[.-]$qualifier[.\\d-]*"
                        Regex(pattern, RegexOption.IGNORE_CASE)
                    }
                    .any { it.matches(candidate.version) }
                if (reject)
                    reject("Preview releases not wanted")
            }
        }
    }
}