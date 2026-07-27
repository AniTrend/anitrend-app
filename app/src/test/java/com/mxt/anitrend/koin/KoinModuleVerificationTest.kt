package com.mxt.anitrend.koin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.annotation.KoinInternalApi

/**
 * Structural verification of Koin module definitions.
 *
 * Checks that [appModules] has the expected number of modules in the
 * correct order and that each module contains the expected number
 * of unique dependency definitions.
 *
 * Full resolution tests with Android context live in
 * androidTest: [com.mxt.anitrend.koin.KoinModuleResolutionTest].
 */
class KoinModuleVerificationTest {

    /**
     * Expected unique definition counts per module, in [appModules] order.
     *
     * Counts use [Module.mappings] distinct factory instances, which match
     * the number of `single`, `factory`, `viewModel`, and `worker` calls.
     * Secondary `bind` mappings are excluded via [kotlin.collections.distinct].
     *
     * When adding/removing a definition, update the corresponding entry.
     */
    private val expectedDefinitionCounts = mapOf(
        0 to 8, // coreModule (DatabaseHelper now wraps BoxStore, bound to BoxQuery)
        1 to 2, // widgetModule
        2 to 5, // workerModule
        3 to 1, // presenterModule
        4 to 6, // networkModule
        5 to 10, // retrofitModule (OkHttpClient x4 + Retrofit x5 + Gson)
        6 to 13, // serviceModule (9 AniList + BaseModel + RepositoryModel + Crunchyroll x2)
        7 to 11, // repositoryModule (+ CrunchyrollRepository + WidgetMutationCoordinator)
        8 to 17, // mediaFeatureModule
        9 to 10, // userFeatureModule
        10 to 5, // characterFeatureModule
        11 to 7, // staffFeatureModule
        12 to 4, // studioFeatureModule
        13 to 5, // utilityFeatureModule (GiphyVM + LoginAuthVM + LoggingVM + logFile + metadata)
    )

    @Test
    fun `appModules has exactly 14 entries`() {
        assertEquals(
            "appModules should contain exactly 14 modules:\n" +
                "  core, widget, worker, presenter, network, retrofit,\n" +
                "  service, repository,\n" +
                "  mediaFeature, userFeature, characterFeature, staffFeature,\n" +
                "  studioFeature, utilityFeature",
            14,
            appModules.size,
        )
    }

    @Test
    fun `each module is non-null and at expected position`() {
        val modules = appModules
        for (i in 0 until 14) {
            assertNotNull("Module at index $i must not be null", modules[i])
        }
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun `each module contains expected number of definitions`() {
        expectedDefinitionCounts.forEach { (index, expected) ->
            val module = appModules[index]
            val actual = module.mappings.values.distinct().size
            assertEquals(
                "Module #$index has $actual definitions, expected $expected. " +
                    "Did you add or remove a definition without updating the test?",
                expected,
                actual,
            )
        }
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun `appModules combined has 104 distinct definitions`() {
        val total = appModules.sumOf { it.mappings.values.distinct().size }
        assertEquals(
            "Combined distinct definition count drifted. Expected 104, got $total.",
            104,
            total,
        )
    }
}
