package co.anitrend.support.markdown.text

import co.anitrend.support.markdown.ICoreRegexTest
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class SpoilerPluginTest : ICoreRegexTest {

    override val plugin: IMarkdownPlugin by lazy {
        SpoilerPlugin.create(0xFFF, 0xFF0000)
    }

    //@Test
    override fun `defined regex pattern detect elements`() {
        val testCase = """
            ~!youtube(ZVJ3Ho83Ksg)!~

            Watch "**京都橘高校吹奏楽部**　大手筋商店街パレード　_Kyoto Tachibana SHS Band_" ~~on YouTube~~

            ~!**Just enjoy &#x1f642;** !~
        """.trimIndent()

        //assertTrue(plugin.regex.containsMatchIn(testCase))

        val matchResultSet = plugin.regex.findAll(testCase, 0)

        val actual = matchResultSet.count()
        //assertEquals(2, actual)
    }
}