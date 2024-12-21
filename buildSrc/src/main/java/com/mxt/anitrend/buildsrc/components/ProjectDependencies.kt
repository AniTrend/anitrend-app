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

package com.mxt.anitrend.buildsrc.components

import org.gradle.api.Project
import com.mxt.anitrend.buildsrc.extensions.*

internal fun Project.configureDependencies() {
    dependencies.add(
        "implementation",
        fileTree("libs") {
            include("*.jar")
        }
    )

    dependencies.implementation(libs.jetbrains.kotlin.stdlib.jdk8)
    dependencies.implementation(libs.jetbrains.kotlin.reflect)
    dependencies.implementation(libs.jetbrains.kotlinx.coroutines.core)
    dependencies.implementation(libs.jetbrains.kotlinx.coroutines.android)
    dependencies.implementation(libs.jetbrains.kotlinx.serialization.json)
    dependencies.testImplementation(libs.jetbrains.kotlinx.coroutines.test)

    /** Material Design */
    dependencies.implementation(libs.google.android.material)
    dependencies.implementation(libs.google.android.flexbox)
    dependencies.implementation(libs.google.firebase.analytics)
    dependencies.implementation(libs.google.firebase.crashlytics)

    /** Architecture Components */
    dependencies.implementation(libs.androidx.constraintLayout)
    dependencies.implementation(libs.androidx.collectionKtx)

    dependencies.implementation(libs.androidx.coreKtx)
    dependencies.implementation(libs.androidx.appcompat)
    dependencies.implementation(libs.androidx.appcompat.resources)
    dependencies.implementation(libs.androidx.activityKtx)
    dependencies.implementation(libs.androidx.fragmentKtx)
    dependencies.implementation(libs.androidx.preferenceKtx)

    dependencies.implementation(libs.androidx.work.runtimeKtx)
    dependencies.implementation(libs.androidx.startupRuntime)

    dependencies.implementation(libs.androidx.lifecycle.extensions)
    dependencies.implementation(libs.androidx.lifecycle.runTimeKtx)
    dependencies.implementation(libs.androidx.lifecycle.liveDataKtx)
    dependencies.implementation(libs.androidx.lifecycle.viewModelKtx)
    dependencies.implementation(libs.androidx.lifecycle.liveDataCoreKtx)

    /** Koin AndroidX Dependency Injection */
    dependencies.implementation(libs.koin.core)
    dependencies.implementation(libs.koin.android)
    dependencies.implementation(libs.koin.workManager)
    dependencies.testImplementation(libs.koin.test)
    dependencies.testImplementation(libs.koin.test.jUnit4)

    /** Glide Libraries */
    dependencies.implementation(libs.glide)
    dependencies.kapt(libs.glide.compiler)

    /** Retrofit Libraries */
    dependencies.implementation(libs.square.logging)
    dependencies.implementation(libs.square.retrofit)
    dependencies.implementation(libs.square.converter.gson)

    /** Rich Text Markdown Parser */
    dependencies.implementation(libs.markwon.core)
    dependencies.implementation(libs.markwon.editor)
    dependencies.implementation(libs.markwon.html)
    dependencies.implementation(libs.markwon.image)
    dependencies.implementation(libs.markwon.glide)
    dependencies.implementation(libs.markwon.parser)
    dependencies.implementation(libs.markwon.linkify)
    dependencies.implementation(libs.markwon.simpleExt)
    dependencies.implementation(libs.markwon.syntaxHighlight)
    dependencies.implementation(libs.markwon.ext.taskList)
    dependencies.implementation(libs.markwon.ext.strikeThrough)
    dependencies.implementation(libs.markwon.ext.tables)
    dependencies.implementation(libs.markwon.ext.latex)
    dependencies.implementation(libs.betterLinkMovement)

    /** Object Box */
    dependencies.implementation(libs.objectbox.android)
    dependencies.kapt(libs.objectbox.processor)

    /** Logging */
    dependencies.implementation(libs.timber)
    dependencies.implementation(libs.treessence)

    /** AniTrend */
    dependencies.implementation(libs.anitrend.android.emoji)
    dependencies.implementation(libs.anitrend.android.emoji.contract)
    dependencies.implementation(libs.anitrend.android.emoji.gson)
    dependencies.implementation(libs.anitrend.support.markwon)
    dependencies.implementation(libs.square.converter.simplexml.get()) {
        exclude("xpp3", "xpp3")
        exclude("stax", "stax-api")
        exclude("stax", "stax")
    }
    dependencies.implementation(libs.anitrend.retrofit.graphql)

    /** Smart Tab Layout */
    dependencies.implementation(libs.smarttablayout.library)
    dependencies.implementation(libs.smarttablayout.utils)

    /** Testing-only dependencies */
    dependencies.testImplementation(libs.junit)
    dependencies.testImplementation(libs.hamcrest)
    dependencies.testImplementation(libs.mockito.core)

    dependencies.androidTestImplementation(libs.cash.turbine)
    dependencies.androidTestImplementation(libs.androidx.test.coreKtx)
    dependencies.androidTestImplementation(libs.androidx.test.runner)
    dependencies.androidTestImplementation(libs.androidx.test.rules)
    dependencies.androidTestImplementation(libs.androidx.fragment.test)
    dependencies.androidTestImplementation(libs.androidx.test.espresso.core)
    dependencies.androidTestImplementation(libs.androidx.junitKtx)
    dependencies.androidTestImplementation(libs.mockito.core)
    dependencies.androidTestImplementation(libs.mockk.android)
    dependencies.androidTestImplementation(libs.mockk)

    dependencies.debugImplementation(libs.chuncker.debug)
    dependencies.releaseImplementation(libs.chuncker.release)
}
