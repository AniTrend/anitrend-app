package com.mxt.anitrend.util

import com.mxt.anitrend.util.collection.EpisodeUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class EpisodeUtil_GetActualTitleTest(
    private val inputTitle: String,
    private val actualTitle: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<String>> = listOf(
            arrayOf("Boku no Hero Academia - Episode 23", "Boku no Hero Academia"),
            arrayOf("Haikyuu Season 3", "Haikyuu"),
            arrayOf("Boku no Hero Academia Season 2 - Episode 19", "Boku no Hero Academia")
        )
    }

    @Test
    fun getActualTile() {
        assertThat(EpisodeUtil.getActualTile(inputTitle), equalTo(actualTitle))
    }
}
