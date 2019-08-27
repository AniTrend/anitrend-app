package co.anitrend.support.markdown.image

import co.anitrend.support.markdown.ICoreRegexTest
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ImagePluginTest : ICoreRegexTest {

    override val plugin: IMarkdownPlugin by lazy {
        ImagePlugin.create()
    }

    @Test
    override fun `defined regex pattern detect elements`() {
        val testCase = """
            ~!
            img180(https://www.anime-planet.com/images/characters/henrietta-168.jpg) 
            img180(https://cdn.myanimelist.net/images/characters/7/30959.jpg)!~

            img(https://something.top.find)

            img20%(https://something.top.find)

            iMg20px(https://something.top.find) img20px(https://something.img.ref)

            ImG20px(https://something.top.find)
        """.trimIndent()

        assertTrue(plugin.regex.containsMatchIn(testCase))

        val matchResultSet = plugin.regex.findAll(testCase, 0)

        val actual = matchResultSet.count()
        assertEquals(7, actual)
    }
}