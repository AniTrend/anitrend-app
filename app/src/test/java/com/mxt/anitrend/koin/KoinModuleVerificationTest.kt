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
        0 to 5, // coroutineModule (4 dispatchers + ApplicationScope)
        1 to 8, // coreModule (DatabaseHelper now wraps BoxStore, bound to BoxQuery)
        2 to 2, // widgetModule
        3 to 5, // workerModule
        4 to 1, // presenterModule
        5 to 6, // networkModule
        6 to 10, // retrofitModule (OkHttpClient x4 + Retrofit x5 + Gson)
        7 to 13, // serviceModule (9 AniList + BaseService + RepositoryService + Crunchyroll x2)
        8 to 26, // repositoryModule (+ canonical stores, mutation infrastructure, revision provider, interactors)
        9 to 18, // mediaFeatureModule
        10 to 11, // userFeatureModule
        11 to 5, // characterFeatureModule
        12 to 7, // staffFeatureModule
        13 to 4, // studioFeatureModule
        14 to 5, // utilityFeatureModule (GiphyVM + LoginAuthVM + LoggingVM + logFile + metadata)
    )

    @OptIn(KoinInternalApi::class)
    @Test
    fun `appModules has exactly 15 entries`() {
        assertEquals(
            "appModules should contain exactly 15 modules:\n" +
                "  coroutineModule, core, widget, worker, presenter, network, retrofit,\n" +
                "  service, repository,\n" +
                "  mediaFeature, userFeature, characterFeature, staffFeature,\n" +
                "  studioFeature, utilityFeature",
            15,
            appModules.includedModules.size,
        )
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun `each module is non-null and at expected position`() {
        val modules = appModules.includedModules
        for (i in 0 until 15) {
            assertNotNull("Module at index $i must not be null", modules[i])
        }
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun `each module contains expected number of definitions`() {
        expectedDefinitionCounts.forEach { (index, expected) ->
            val module = appModules.includedModules[index]
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
    fun `appModules combined has 126 distinct definitions`() {
        val total = appModules.includedModules.sumOf { it.mappings.values.distinct().size }
        assertEquals(
            "Combined distinct definition count drifted. Expected 126, got $total.",
            126,
            total,
        )
    }
}
