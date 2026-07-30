package com.mxt.anitrend.architecture

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class ArchitectureEnforcementTest {

    private val projectRoot: File = findProjectRoot()

    @Test
    fun `adapters do not inject repositories`() {
        val adapterFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/adapter")
        val explicitRepoInjection = adapterFiles.matching(
            Regex("inject<.*Repository>|koinOf<.*Repository>"),
        )
        val inferredRepoInjection = adapterFiles.matching(
            Regex("(?i).*koinOf\\(\\).*Repository.*|.*Repository.*koinOf\\(\\).*"),
        )
        // NotificationAdapter is a pre-existing violation in the non-migrated notifications domain.
        // Track its removal as follow-up infrastructure debt.
        val knownExceptions = setOf(
            "app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/NotificationAdapter.kt",
        )
        val violations = (explicitRepoInjection + inferredRepoInjection)
            .filterNot { violation -> knownExceptions.any { violation.startsWith(it) } }

        assertFalse(
            "Adapters must not inject or resolve repositories via Koin. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `adapter and widget files do not reference WidgetMutationCoordinator`() {
        val targetFiles =
            kotlinFiles("app/src/main/java/com/mxt/anitrend/adapter") +
                kotlinFiles("app/src/main/java/com/mxt/anitrend/base/custom/view/widget")
        val violations = targetFiles.matching(Regex("WidgetMutationCoordinator"))

        assertFalse(
            "WidgetMutationCoordinator must not be referenced from adapters or widgets. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

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
    fun `widgets do not create ad hoc CoroutineScope instances`() {
        val widgetFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/base/custom/view/widget")
        val violations = widgetFiles.matching(Regex("CoroutineScope\\("))

        assertFalse(
            "Widgets must not create ad hoc CoroutineScope instances. Violations:\n${violations.joinToString("\n")}",
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

    private fun kotlinFiles(relativePath: String): List<File> =
        projectRoot.resolve(relativePath)
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

    private fun List<File>.matching(pattern: Regex): List<String> =
        flatMap { file ->
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
