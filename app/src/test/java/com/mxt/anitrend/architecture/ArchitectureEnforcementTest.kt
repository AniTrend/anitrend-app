package com.mxt.anitrend.architecture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        val violations = explicitRepoInjection + inferredRepoInjection

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
    fun `avatar indicator is render only and emits clicks`() {
        val avatar = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/base/custom/view/image/AvatarIndicatorView.kt",
        )
        val source = avatar.readText()
        val forbidden = listOf(
            "KoinComponent",
            "BoxQuery",
            "Settings",
            "startActivity",
            "MainActivity",
            "LoginActivity",
        ).filter(source::contains)
        assertTrue("AvatarIndicatorView must not own state or navigation: $forbidden", forbidden.isEmpty())
        assertTrue("AvatarIndicatorView must expose render state.", source.contains("fun render("))
        assertTrue("AvatarIndicatorView must expose a UI action callback.", source.contains("onAvatarClick"))
    }

    @Test
    fun `status content widget emits preview actions instead of navigating`() {
        val status = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/base/custom/view/widget/StatusContentWidget.kt",
        )
        val source = status.readText()
        val forbidden = listOf(
            "startActivity",
            "ImagePreviewActivity",
            "VideoPlayerActivity",
            "NavController",
        ).filter(source::contains)
        assertTrue("StatusContentWidget must not own navigation: $forbidden", forbidden.isEmpty())
        assertTrue("StatusContentWidget must expose image preview actions.", source.contains("onImagePreviewRequested"))
        assertTrue("StatusContentWidget must expose video preview actions.", source.contains("onVideoPreviewRequested"))
        assertTrue("StatusContentWidget must expose YouTube actions.", source.contains("onYoutubeRequested"))
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

    @Test
    fun `migrated detail destinations do not reintroduce pager infrastructure`() {
        val migratedFiles =
            listOf(
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/CharacterFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/StaffFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/ProfileFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/CharacterOverviewSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/StaffOverviewSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/ProfileOverviewSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/ProfileFeedSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/favourite/FavouriteFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/search/SearchFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/FeedFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/TrendingFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/AnimeFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/MangaFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/AiringFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/list/HubFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/ReviewBrowseFragment.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaOverviewSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaStatsSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaRelationSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaStaffSection.kt",
                "app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/MediaStaffRoleAdapter.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaCharacterSection.kt",
                "app/src/main/java/com/mxt/anitrend/adapter/recycler/detail/MediaCharacterAdapter.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaRecommendationsSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaReviewSection.kt",
                "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaFeedSection.kt",
                "app/src/main/java/com/mxt/anitrend/base/custom/recycler/RecyclerSectionAdapter.kt",
            ).map(projectRoot::resolve)
        val violations = migratedFiles.matching(
            Regex("BaseStatePageAdapter|FragmentStateAdapter|ViewPager2?|childFragmentManager"),
        )

        assertFalse(
            "Migrated detail destinations must use local section state, not a pager or child fragments. " +
                "Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `main shell no longer owns the index pager bridge`() {
        val files = listOf(
            projectRoot.resolve("app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt"),
            projectRoot.resolve("app/src/main/res/navigation/nav_root.xml"),
        )
        val violations = files.matching(
            Regex("LegacyMainFragment|legacyMainFragment|BaseStatePageAdapter|ViewPager2?|AiringPageAdapter|HubPageAdapter"),
        )

        assertFalse(
            "MainActivity must be a Navigation 2 shell without the legacy index pager bridge. " +
                "Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `media destination no longer has an activity or pager adapter`() {
        val obsolete = listOf(
            "app/src/main/java/com/mxt/anitrend/view/activity/detail/MediaActivity.kt",
            "app/src/main/java/com/mxt/anitrend/adapter/pager/detail/AnimePageAdapter.kt",
            "app/src/main/java/com/mxt/anitrend/adapter/pager/detail/MangaPageAdapter.kt",
            "app/src/main/java/com/mxt/anitrend/base/custom/pager/BaseStatePageAdapter.kt",
        ).map(projectRoot::resolve)
        assertFalse(
            "The unified media destination must be the only production media navigation surface.",
            obsolete.any(File::exists),
        )
    }

    @Test
    fun `unified media fragment contains no pager implementation`() {
        val mediaFragment = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/view/fragment/detail/MediaFragment.kt",
        )
        val violations = mediaFragment.readLines().mapIndexedNotNull { index, line ->
            if (Regex("BaseStatePageAdapter|FragmentStateAdapter|ViewPager2?|TabLayoutMediator").containsMatchIn(line)) {
                "${mediaFragment.relativeTo(projectRoot).path}:${index + 1}: ${line.trim()}"
            } else {
                null
            }
        }
        assertFalse("MediaFragment must use local section state, not a pager.\n${violations.joinToString("\n")}", violations.isNotEmpty())
    }

    @Test
    fun `share ingress no longer has a dedicated activity`() {
        val obsolete = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/view/activity/base/SharedContentActivity.kt",
        )
        val manifest = projectRoot.resolve("app/src/main/AndroidManifest.xml").readText()
        val rootGraph = projectRoot.resolve("app/src/main/res/navigation/nav_root.xml").readText()
        assertFalse("Shared content must be hosted by the root Navigation 2 graph.", obsolete.exists())
        assertFalse("Shared content must not retain a manifest activity boundary.", manifest.contains("SharedContentActivity"))
        assertTrue("Shared content must be registered as a Navigation 2 destination.", rootGraph.contains("sharedContentFragment"))
    }

    @Test
    fun `migrated destinations remain direct children of the primary graph`() {
        val navigationRoot = projectRoot.resolve("app/src/main/res/navigation")
        val graphFiles = navigationRoot.listFiles()
            ?.filter { it.isFile && it.extension == "xml" }
            ?.map { it.name }
            ?.toSet()
            .orEmpty()
        val rootGraph = navigationRoot.resolve("nav_root.xml").readText()
        val requiredDestinations = listOf(
            "animeFragment",
            "mediaFragment",
            "profileFragment",
            "characterFragment",
            "staffFragment",
            "studioFragment",
            "commentFragment",
            "sharedContentFragment",
            "settingsHubFragment",
        )

        assertEquals("The primary host must have one authoritative graph registry.", setOf("nav_root.xml"), graphFiles)
        requiredDestinations.forEach { destination ->
            assertTrue("$destination must be registered in nav_root.xml", rootGraph.contains("@+id/$destination"))
        }
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
    fun `fragment route helpers do not create a second main activity`() {
        val routes = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/navigation/extension/NavigationDestinations.kt",
        )
        val violations = routes.readLines().mapIndexedNotNull { index, line ->
            if (line.contains("startActivity") || line.contains("MainActivity")) {
                "${routes.relativeTo(projectRoot).path}:${index + 1}: ${line.trim()}"
            } else {
                null
            }
        }
        assertFalse(
            "Fragment route helpers must delegate to the owning NavController, not start another Activity. " +
                "Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `migrated UI components do not relaunch the main activity`() {
        val targetFiles =
            kotlinFiles("app/src/main/java/com/mxt/anitrend/view/fragment") +
                kotlinFiles("app/src/main/java/com/mxt/anitrend/view/sheet") +
                kotlinFiles("app/src/main/java/com/mxt/anitrend/base/custom/view")
        val violations = targetFiles.matching(
            Regex("Intent\\([^\\n]*MainActivity|MainActivity\\.(EXTRA_ROUTE|ROUTE_)|startActivity\\([^\\n]*MainActivity"),
        )
        assertFalse(
            "Fragments, sheets, and custom views must route through the owning NavController " +
                "or emit an explicit callback, not relaunch MainActivity. Violations:\n${violations.joinToString("\n")}",
            violations.isNotEmpty(),
        )
    }

    @Test
    fun `main shell does not retain an obsolete voice result bridge`() {
        val mainActivity = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/view/activity/index/MainActivity.kt",
        ).readText()
        val searchView = projectRoot.resolve(
            "app/src/main/java/com/mxt/anitrend/base/custom/view/search/MaterialSearchView.kt",
        ).readText()
        assertFalse("MainActivity must not retain the removed voice result callback.", mainActivity.contains("onActivityResult"))
        assertFalse("MaterialSearchView must not expose the removed voice request contract.", searchView.contains("REQUEST_VOICE"))
    }

    @Test
    fun `ViewPager is limited to onboarding boundary`() {
        val productionFiles = kotlinFiles("app/src/main/java") +
            projectRoot.resolve("app/src/main/res/layout/activity_welcome.xml")
                .let { listOf(it) }
        val violations = productionFiles.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                if (Regex("ViewPager|PageAdapter|BaseStatePageAdapter").containsMatchIn(line) &&
                    file.name != "WelcomeActivity.kt" &&
                    file.name != "activity_welcome.xml"
                ) {
                    "${file.relativeTo(projectRoot).path}:${index + 1}: ${line.trim()}"
                } else {
                    null
                }
            }
        }
        assertFalse(
            "Pager infrastructure is allowed only for the retained onboarding boundary. Violations:\n${violations.joinToString("\n")}",
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
