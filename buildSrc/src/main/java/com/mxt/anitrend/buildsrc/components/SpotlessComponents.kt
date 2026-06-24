package com.mxt.anitrend.buildsrc.components

import com.mxt.anitrend.buildsrc.extensions.spotlessExtension
import org.gradle.api.Project

internal fun Project.configureSpotless() = spotlessExtension().run {
    val ktlintRules = mapOf(
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_standard_property-naming" to "disabled",
        "ktlint_standard_class-naming" to "disabled",
        "ktlint_standard_max-line-length" to "disabled",
        "ktlint_standard_no-consecutive-comments" to "disabled",
        "ktlint_standard_parameter-list-wrapping" to "disabled",
        "ktlint_standard_backing-property-naming" to "disabled",
        "ktlint_standard_comment-wrapping" to "disabled",
        "max_line_length" to "off",
    )

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintRules)
    }

    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintRules)
    }
}
