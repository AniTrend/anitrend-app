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

package com.mxt.anitrend.buildsrc.componets

import org.gradle.api.Project
import com.mxt.anitrend.buildsrc.Libraries
import com.mxt.anitrend.buildsrc.extensions.*

internal fun Project.configureDependencies() {
    dependencies.add(
        "implementation",
        fileTree("libs") {
            include("*.jar")
        }
    )

    dependencies.implementation(Libraries.JetBrains.Kotlin.stdlib)
    dependencies.implementation(Libraries.JetBrains.KotlinX.Serialization.json)
    dependencies.implementation(Libraries.JetBrains.KotlinX.Coroutines.android)
    dependencies.implementation(Libraries.JetBrains.KotlinX.Coroutines.core)
    dependencies.testImplementation(Libraries.JetBrains.KotlinX.Coroutines.test)

    /** Material Design */
    dependencies.implementation(Libraries.Google.Material.material)
    dependencies.implementation(Libraries.Google.FlexBox.flexBox)
    dependencies.implementation(Libraries.Google.Firebase.Analytics.analyticsKtx)
    dependencies.implementation(Libraries.Google.Firebase.Crashlytics.crashlytics)

    /** Architecture Components */
    dependencies.implementation(Libraries.AndroidX.ConstraintLayout.constraintLayout)
    dependencies.implementation(Libraries.AndroidX.Collection.collectionKtx)

    dependencies.implementation(Libraries.AndroidX.Core.coreKtx)
    dependencies.implementation(Libraries.AndroidX.Activity.activityKtx)
    dependencies.implementation(Libraries.AndroidX.Fragment.fragmentKtx)
    dependencies.implementation(Libraries.AndroidX.Preference.preferenceKtx)

    dependencies.implementation(Libraries.AndroidX.Work.runtimeKtx)
    dependencies.implementation(Libraries.AndroidX.Work.multiProcess)
    dependencies.implementation(Libraries.AndroidX.StartUp.startUpRuntime)

    dependencies.implementation(Libraries.AndroidX.Lifecycle.liveDataCoreKtx)
    dependencies.implementation(Libraries.AndroidX.Lifecycle.runTimeKtx)
    dependencies.implementation(Libraries.AndroidX.Lifecycle.liveDataKtx)
    dependencies.implementation(Libraries.AndroidX.Lifecycle.extensions)

    /** Koin AndroidX Dependency Injection */
    dependencies.implementation (Libraries.Koin.core)
    dependencies.implementation (Libraries.Koin.extension)
    dependencies.implementation (Libraries.Koin.AndroidX.scope)
    dependencies.implementation (Libraries.Koin.AndroidX.viewModel)
    dependencies.implementation (Libraries.Koin.AndroidX.workManager)
    dependencies.androidTestImplementation(Libraries.Koin.test)

    /** Glide Libraries */
    dependencies.implementation(Libraries.Glide.glide)
    dependencies.kapt(Libraries.Glide.compiler)

    /** Retrofit Libraries */
    dependencies.implementation(Libraries.Square.OkHttp.logging)
    dependencies.implementation(Libraries.Square.Retrofit.retrofit)
    dependencies.implementation(Libraries.Square.Retrofit.gsonConverter)

    /** Rich Text Markdown Parser */
    dependencies.implementation(Libraries.Markwon.core)
    dependencies.implementation(Libraries.Markwon.editor)
    dependencies.implementation(Libraries.Markwon.html)
    dependencies.implementation(Libraries.Markwon.image)
    dependencies.implementation(Libraries.Markwon.glide)
    dependencies.implementation(Libraries.Markwon.parser)
    dependencies.implementation(Libraries.Markwon.linkify)
    dependencies.implementation(Libraries.Markwon.simpleExt)
    dependencies.implementation(Libraries.Markwon.syntaxHighlight)
    dependencies.implementation(Libraries.Markwon.Extension.taskList)
    dependencies.implementation(Libraries.Markwon.Extension.strikeThrough)
    dependencies.implementation(Libraries.Markwon.Extension.tables)
    dependencies.implementation(Libraries.Markwon.Extension.latex)
    dependencies.implementation(Libraries.betterLinkMovement)

    /** Object Box */
    dependencies.implementation(Libraries.ObjectBox.android)
    dependencies.kapt(Libraries.ObjectBox.processor)

    /** Logging */
    dependencies.implementation(Libraries.timber)
    dependencies.implementation(Libraries.treessence)

    /** AniTrend */
    dependencies.implementation(Libraries.AniTrend.Emojify.emojify)
    dependencies.implementation(Libraries.AniTrend.Markdown.markdown)
    dependencies.implementation(Libraries.AniTrend.Retrofit.graphQL)

    /** Smart Tab Layout */
    dependencies.implementation(Libraries.SmartTab.layout)
    dependencies.implementation(Libraries.SmartTab.utilities)

    /** Testing-only dependencies */
    dependencies.testImplementation(Libraries.junit)
    dependencies.testImplementation(Libraries.Mockito.core)

    dependencies.androidTestImplementation(Libraries.CashApp.Turbine.turbine)
    dependencies.androidTestImplementation(Libraries.AndroidX.Test.coreKtx)
    dependencies.androidTestImplementation(Libraries.AndroidX.Test.rules)
    dependencies.androidTestImplementation(Libraries.AndroidX.Test.runner)
    dependencies.androidTestImplementation(Libraries.AndroidX.Test.Espresso.core)
    dependencies.androidTestImplementation(Libraries.AndroidX.Test.Extension.junitKtx)
    dependencies.androidTestImplementation(Libraries.Mockito.android)
    dependencies.androidTestImplementation(Libraries.mockk)
}