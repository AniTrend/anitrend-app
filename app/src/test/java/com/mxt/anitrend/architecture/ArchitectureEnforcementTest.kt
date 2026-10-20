package com.mxt.anitrend.architecture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ArchitectureEnforcementTest {

    private val projectRoot: File = findProjectRoot()

    @Test
    fun `repositories do not declare mutationEvents`() {
        val repositoryFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/repository")
        val violations = repositoryFiles.matching(Regex("mutationEvents"))

        assertFalse(
            "Repositories must not declare or use mutationEvents. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `GlobalScope is not used`() {
        val mainFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend")
        val violations = mainFiles.matching(Regex("GlobalScope\\.launch"))

        assertFalse(
            "GlobalScope.launch is prohibited. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `remaining activities are explicit platform boundaries`() {
        val activityRoot = projectRoot.resolve("app/src/main/java/com/mxt/anitrend/view/activity")
        val actual = activityRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith("Activity.kt") }
            .map { it.relativeTo(projectRoot).path }
            .toSet()
        val allowed = setOf(
            "app/src/main/java/com/mxt/anitrend/view/activity/CommonActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/base/GiphyPreviewActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/base/ImagePreviewActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/base/VideoPlayerActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/base/WelcomeActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/index/LoginActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt",
            "app/src/main/java/com/mxt/anitrend/view/activity/index/SplashActivity.kt",
        )
        assertEquals("Every remaining Activity must be an explicitly classified boundary.", allowed, actual)
    }

    @Test
    fun `production code does not retain ViewPager infrastructure`() {
        val violations =
            kotlinFiles("app/src/main/java").flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (Regex("ViewPager|PageAdapter|BaseStatePageAdapter").containsMatchIn(line)) {
                        "${file.relativeTo(projectRoot).path}:${index + 1}: ${line.trim()}"
                    } else {
                        null
                    }
                }
            }
        assertFalse(
            "The Compose onboarding replaced the last ViewPager surface; no pager infrastructure may remain. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    private fun kotlinFiles(relativePath: String): List<File> = projectRoot.resolve(relativePath)
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()

    private fun List<File>.matching(pattern: Regex): List<String> = flatMap { file ->
        file.readLines().mapIndexedNotNull { index, line ->
            if (pattern.containsMatchIn(line)) {
                file.relativeTo(projectRoot).path + ":${index + 1}: ${line.trim()}"
            } else {
                null
            }
        }
    }

    private fun findProjectRoot(): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        var current = File(userDir).absoluteFile
        while (true) {
            if (current.resolve("settings.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile ?: error("Unable to locate project root from $userDir")
        }
    }
}
