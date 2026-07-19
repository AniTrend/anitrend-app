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

package com.mxt.anitrend.buildsrc.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.mxt.anitrend.buildsrc.components.PropertiesReader
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.props: PropertiesReader
    get() = PropertiesReader(this)

internal val Project.libs: LibrariesForLibs get() =
    extensions.getByType<LibrariesForLibs>()

internal fun Project.baseExtension() = extensions.getByType<ApplicationExtension>()

internal fun Project.androidComponents() = extensions.getByType<ApplicationAndroidComponentsExtension>()

internal fun Project.containsAndroidPlugin(): Boolean =
    project.extensions.findByType(ApplicationExtension::class.java) != null

internal fun Project.versionCatalogExtension() = extensions.getByType<VersionCatalogsExtension>()

internal fun Project.spotlessExtension() = extensions.getByType<SpotlessExtension>()

internal fun Project.runIfAppModule(body: ApplicationExtension.() -> Unit) {
    if (containsAndroidPlugin()) {
        body(baseExtension())
    }
}
