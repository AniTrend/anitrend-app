package co.anitrend.support.markdown.video

import co.anitrend.support.markdown.ICoreRegexTest
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class WebMPluginTest : ICoreRegexTest {

    override val plugin: IMarkdownPlugin by lazy {
        WebMPlugin.create()
    }

    @Test
    override fun `defined regex pattern detect elements`() {
        val testCase = """
            __Blue Day Done Right &#128525; ♥️ &#128527;__

            img270(https://cdn.discordapp.com/attachments/458389398782869524/541236388897751061/WhatsApp_Image_2019-02-02_at_11.45.24.jpeg) img250(https://cdn.discordapp.com/attachments/458389398782869524/541236389522571264/WhatsApp_Image_2019-02-02_at_11.45.28.jpeg) 

            ~!img250(https://cdn.discordapp.com/attachments/458389398782869524/541236396401360906/WhatsApp_Image_2019-02-02_at_11.45.26.jpeg) img250(https://cdn.discordapp.com/attachments/458389398782869524/541236404471070730/WhatsApp_Image_2019-02-02_at_11.45.36_1.jpeg)!~

            __We're not done yet..__

            webm(https://cdn.discordapp.com/attachments/458389398782869524/541236487275151360/WhatsApp_Video_2019-02-02_at_11.45.32.mp4)
        """.trimIndent()

        assertTrue(plugin.regex.containsMatchIn(testCase))

        val matchResultSet = plugin.regex.findAll(testCase, 0)

        val actual = matchResultSet.count()
        assertEquals(1, actual)
    }
}