package co.anitrend.support.markdown.style

import co.anitrend.support.markdown.ICoreRegexTest
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class CenterPluginTest : ICoreRegexTest {

    override val plugin: IMarkdownPlugin by lazy {
        CenterPlugin.create()
    }

    @Test
    override fun `defined regex pattern detect elements`() {

        val testCase = """
            # ~~~__Creator of [AniTrend](https://anitrend.co), check it out :p__~~~

            ~~~<a href="https://discordapp.com/invite/2wzTqnF"><img src="https://img.shields.io/discord/314442908478472203.svg?color=%237289da&label=Join%20Anitrend!&logo=discord&logoColor=%23fff" alt="best anitrend"/>~~~

            ~~~__I can make stuff with my mind, how cool is that?? :p!__~~~
            ~~~img250(https://media.giphy.com/media/gZq7GstcdqVXi/giphy.gif)~~~

            ~~~__[Kitsu](https://kitsu.io/users/wax911)__ | __[Instagram](https://www.instagram.com/nekosenpaic/)__ | __[GitHub](https://github.com/wax911)__~~~

            ~~~__Can I Be Your Senpai Now??__~~~
            ~~~img250(https://media.giphy.com/media/VnNdJolKFyg7e/giphy.gif)~~~

            ~~~<a href="https://www.patreon.com/bePatron?u=7968843" data-patreon-widget-type="become-patron-button">Support Me On Patron!</a>~~~
        """.trimIndent()

        assertTrue(plugin.regex.containsMatchIn(testCase))

        val matchResultSet = plugin.regex.findAll(testCase, 0)

        val actual = matchResultSet.count()
        assertEquals(8, actual)
    }
}