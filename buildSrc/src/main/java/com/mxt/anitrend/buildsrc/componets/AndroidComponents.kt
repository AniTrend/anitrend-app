/*
 * Copyright (C) 2020  AniTrend
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.buildsrc.componets

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.mxt.anitrend.buildsrc.common.Versions
import com.mxt.anitrend.buildsrc.extensions.*
import com.mxt.anitrend.buildsrc.Libraries
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.FileWriter
import java.util.*

private fun Properties.applyToBuildConfigForBuild(buildType: BuildType) {
    forEach { propEntry ->
        val key = propEntry.key as String
        val value = propEntry.value as String
        buildType.buildConfigField("String", key, value)
    }
}

private fun Project.createSigningConfiguration(extension: BaseAppModuleExtension) {
    var properties: Properties? = null
    val keyStoreFile = project.file(".config/keystore.properties")
    if (keyStoreFile.exists())
        keyStoreFile.inputStream().use { fis ->
            Properties().run {
                load(fis);
                properties = this
            }
        }
    else println("${keyStoreFile.absolutePath} could not be found, release may fail")
    properties?.also {
        extension.signingConfigs {
            create("release") {
                storeFile(file(it["STORE_FILE"] as String))
                storePassword(it["STORE_PASSWORD"] as String)
                keyAlias(it["STORE_KEY_ALIAS"] as String)
                keyPassword(it["STORE_KEY_PASSWORD"] as String)
                isV2SigningEnabled = true
            }
        }
    }
}

private fun NamedDomainObjectContainer<BuildType>.applyConfiguration(project: Project) {
    asMap.forEach { buildTypeEntry ->
        println("Configuring build type -> ${buildTypeEntry.key}")
        val buildType = buildTypeEntry.value

        val secretsFile = project.file(".config/secrets.properties")
        if (secretsFile.exists())
            secretsFile.inputStream().use { fis ->
                Properties().run {
                    load(fis); applyToBuildConfigForBuild(buildType)
                }
            }
        else println("${secretsFile.absolutePath} could not be found, build may fail")

        val configurationFile = project.file(".config/configuration.properties")
        if (configurationFile.exists())
            configurationFile.inputStream().use { fis ->
                Properties().run {
                    load(fis); applyToBuildConfigForBuild(buildType)
                }
            }
        else println("${configurationFile.absolutePath} could not be found, build may fail")
    }
}

private fun BaseExtension.setUpWith() {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        applicationId = "com.mxt.anitrend"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }
}

private fun BaseAppModuleExtension.configureBuildFlavours() {
    flavorDimensions("version")

    productFlavors {
        create("app") {
            dimension = "version"
            resValue("string", "flavor_description", "Playstore")
        }
        create("github") {
            dimension = "version"
            versionNameSuffix = "-github"
            resValue("string", "flavor_description", "Github")
        }
    }

    applicationVariants.all {
        outputs.map { it as BaseVariantOutputImpl }.forEach { output ->
            val original = output.outputFileName
            val destination = if (output.name != "github-release")
                original.substring(4)
            else original
            println("Configuring build output build -> name: ${output.name} | output: $destination")
            output.outputFileName = destination
        }
    }
}

private fun BaseAppModuleExtension.setUpWith(project: Project) {
    project.createSigningConfiguration(this)
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isTestCoverageEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
            isTestCoverageEnabled = false
        }

        applyConfiguration(project)

        configureBuildFlavours()
    }

    packagingOptions {
        excludes.add("META-INF/NOTICE.txt")
        excludes.add("META-INF/LICENSE")
        excludes.add("META-INF/LICENSE.txt")
        // Exclude potential duplicate kotlin_module files
        excludes.add("META-INF/*kotlin_module")
        // Exclude consumer proguard files
        excludes.add("META-INF/proguard/*")
        // Exclude AndroidX version files
        excludes.add("META-INF/*.version")
        // Exclude the Firebase/Fabric/other random properties files
        excludes.add("META-INF/*.properties")
        excludes.add("/*.properties")
        excludes.add("fabric/*.properties")
    }

    sourceSets {
        map { androidSourceSet ->
            androidSourceSet.java.srcDir(
                "src/${androidSourceSet.name}/kotlin"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lintOptions {
        isAbortOnError = false
        isIgnoreWarnings = false
        isIgnoreTestSources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

internal fun Project.applyAndroidConfiguration() {
    val baseExtension = baseExtension()
    val appExtension = baseAppExtension()
    val androidExtension = androidExtensionsExtension()

    baseExtension.setUpWith()
    appExtension.setUpWith(this)

    baseExtension.dexOptions {
        jumboMode = true
    }
    androidExtension.isExperimental = true

    configurations.all {
        exclude("org.jetbrains", "annotations-java5")
        resolutionStrategy.force(
            "com.google.code.findbugs:jsr305:3.0.2",
            Libraries.Square.OkHttp.logging,
            Libraries.Square.OkHttp.mockServer,
            Libraries.Square.OkHttp.okhttp
        )
    }

    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions {
            jvmTarget = "1.8"
            // https://blog.jetbrains.com/kotlin/2021/02/the-jvm-backend-is-in-beta-let-s-make-it-stable-together/
            useIR
        }
    }

    tasks.withType(KotlinCompile::class.java) {
        val compilerArgumentOptions = mutableListOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-Xopt-in=kotlin.Experimental",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.FlowPreview",
            "-Xopt-in=org.koin.core.component.KoinExperimentalAPI",
            "-Xopt-in=org.koin.core.component.KoinApiExtension",
            "-Xopt-in=org.koin.core.KoinExperimentalAPI"
        )

        kotlinOptions {
            allWarningsAsErrors = false
            // Filter out modules that won't be using coroutines
            freeCompilerArgs = compilerArgumentOptions
        }
    }

    tasks.register("generateVersions") {
        println("Generate versions for $name -> $projectDir")
        val versionMeta = File(projectDir, ".meta/version.json")
        if (!versionMeta.exists()) {
            println("Creating versions meta file in ${versionMeta.absolutePath}")
            versionMeta.mkdirs()
        }
        println("Writing version information to ${versionMeta.absolutePath}")
        FileWriter(versionMeta).use { writer ->
            writer.write(
                """
                    {
                        "code": ${Versions.versionCode},
                        "migration": false,
                        "minSdk": ${Versions.minSdk},
                        "releaseNotes": "",
                        "version": "${Versions.versionName}",
                        "appId": "com.mxt.anitrend"
                    }
                """.trimIndent()
            )
        }
    }
}