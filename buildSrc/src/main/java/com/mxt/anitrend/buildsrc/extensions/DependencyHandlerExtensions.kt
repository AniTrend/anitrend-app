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

import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude

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


/**
 * Adds a dependency to the given configuration, and configures the dependency using the given expression.
 *
 * @param configuration The name of the configuration.
 * @param dependency The dependency.
 * @param dependencyConfiguration The expression to use to configure the dependency.
 * @return The dependency.
 */
private inline fun <T : ModuleDependency> DependencyHandler.add(
    configuration: String,
    dependency: T,
    dependencyConfiguration: T.() -> Unit
): T = dependency.apply {
    dependencyConfiguration()
    add(configuration, this)
}


/**
 * Adds a dependency to the given configuration, and configures the dependency using the given expression.
 *
 * @param configuration The name of the configuration.
 * @param dependencyNotation The dependency notation.
 * @param dependencyConfiguration The expression to use to configure the dependency.
 * @return The dependency.
 */
private inline fun DependencyHandler.add(
    configuration: String,
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): Dependency = add(
    configuration,
    create(dependencyNotation) as ExternalModuleDependency,
    dependencyConfiguration
)


/**
 * Adds an exclude rule to exclude transitive dependencies of this dependency.
 *
 * Excluding a particular transitive dependency does not guarantee that it does not show up
 * in the dependencies of a given configuration.
 * For example, some other dependency, which does not have any exclude rules,
 * might pull in exactly the same transitive dependency.
 * To guarantee that the transitive dependency is excluded from the entire configuration
 * please use per-configuration exclude rules: [Configuration.getExcludeRules].
 * In fact, in majority of cases the actual intention of configuring per-dependency exclusions
 * is really excluding a dependency from the entire configuration (or classpath).
 *
 * If your intention is to exclude a particular transitive dependency
 * because you don't like the version it pulls in to the configuration
 * then consider using the forced versions feature: [ResolutionStrategy.force].
 *
 * @param group the optional group identifying the dependencies to be excluded.
 * @param module the optional module name identifying the dependencies to be excluded.
 * @return this
 *
 * @see [ModuleDependency.exclude]
 */
@Suppress("UNCHECKED_CAST")
fun <T : ModuleDependency> T.exclude(group: String? = null, module: String? = null): T =
    exclude(mapOf("group" to group, "module" to module)) as T

private fun DependencyHandler.addDependency(
    dependencyNotation: Any,
    dependencyType: DependencyType,
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = when (dependencyConfiguration) {
    null -> add(dependencyType.configurationName, dependencyNotation)
    else -> add(
        configuration = dependencyType.configurationName,
        dependencyNotation = dependencyNotation.toString(),
        dependencyConfiguration = dependencyConfiguration
    )
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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.KAPT, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.API, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.COMPILE, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.DEBUG, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.IMPLEMENTATION, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.DEBUG_IMPLEMENTATION, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.RELEASE_IMPLEMENTATION, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.RUNTIME, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.TEST, dependencyConfiguration)

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
    dependencyConfiguration: (ExternalModuleDependency.() -> Unit)? = null
) = addDependency(dependencyNotation, DependencyType.ANDROID_TEST, dependencyConfiguration)