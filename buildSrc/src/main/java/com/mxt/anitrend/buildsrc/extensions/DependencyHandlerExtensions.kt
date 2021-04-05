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

import groovy.lang.Closure
import org.gradle.api.artifacts.dsl.DependencyHandler

private enum class DependencyType(val configurationName: String) {
    API("api"),
    COMPILE("compileOnly"),
    DEBUG("debugOnly"),
    KAPT("kapt"),
    IMPLEMENTATION("implementation"),
    DEBUG_IMPLEMENTATION("debugImplementation"),
    RELEASE_IMPLEMENTATION("releaseImplementation"),
    RUNTIME("runtimeOnly"),
    TEST("testImplementation"),
    ANDROID_TEST("androidTestImplementation")
}

private fun DependencyHandler.addDependency(
    dependencyNotation: Any,
    dependencyType: DependencyType,
    configureClosure: Closure<*>? = null
) = when (configureClosure) {
    null -> add(dependencyType.configurationName, dependencyNotation)
    else -> add(dependencyType.configurationName, dependencyNotation, configureClosure)
}

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.kapt(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.KAPT, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.api(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.API, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.compile(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.COMPILE, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.debug(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.DEBUG, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.implementation(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.IMPLEMENTATION, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.debugImplementation(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.DEBUG_IMPLEMENTATION, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.releaseImplementation(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.RELEASE_IMPLEMENTATION, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.runtime(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.RUNTIME, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.testImplementation(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.TEST, configureClosure)

/**
 * Adds a dependency to the given configuration, and configures the dependency using the given closure.
 *
 * @param dependencyNotation The dependency notation, in one of the notations described above.
 * @param configureClosure The closure to use to configure the dependency.
 *
 * @return The dependency.
 */
internal fun DependencyHandler.androidTestImplementation(
    dependencyNotation: Any,
    configureClosure: Closure<*>? = null
) = addDependency(dependencyNotation, DependencyType.ANDROID_TEST, configureClosure)