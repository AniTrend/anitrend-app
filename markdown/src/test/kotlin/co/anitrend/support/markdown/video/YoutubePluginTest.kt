package co.anitrend.support.markdown.video

import co.anitrend.support.markdown.ICoreRegexTest
import co.anitrend.support.markdown.core.contract.IMarkdownPlugin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class YoutubePluginTest : ICoreRegexTest {

    override val plugin: IMarkdownPlugin by lazy {
        YouTubePlugin.create()
    }

    //@Test
    override fun `defined regex pattern detect elements`() {
        val testCase = """
            youtube(HbVaFnx-uAw) 
            
            ~!youtube(https://www.youtube.com/watch?v=HbVaFnx-uAw)!~
            
            ~!youtube(https://youtu.be/HbVaFnx-uAw)!~

            [Manaria Friends](https://anilist.co/anime/21322) "TVアニメ『マナリアフレンズ』第2弾PV" on YouTube

            New Waifu??? __#t h i c c  t h i g h s__ =_=
        """.trimIndent()

        //assertTrue(plugin.regex.containsMatchIn(testCase))

        val matchResultSet = plugin.regex.findAll(testCase, 0)

        val actual = matchResultSet.count()
        //assertEquals(3, actual)
    }
}