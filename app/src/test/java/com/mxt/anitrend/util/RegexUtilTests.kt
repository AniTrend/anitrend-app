package com.mxt.anitrend.util

import com.mxt.anitrend.util.markdown.RegexUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RegexUtilTests {

    @Test
    fun findMedia() {
        val testStatus = "MAL is back\\n\\nand i no longer give a shit\\n\\nimg220(https:\\/\\/static1.fjcdn.com\\/thumbnails\\/comments\\/Go+talk+to+our+friendly+fellows+on+the+anime+board+_496f62c2f231bc1c8a9b77a449bf628f.gif)\\nThis place is nice, i like it."
        val matcher = RegexUtil.findMedia(testStatus)
        assertNotNull(matcher)
        val safeMatcher = matcher!!
        val expectedCount = 1
        var current = 0
        while (safeMatcher.find()) {
            val gc = safeMatcher.groupCount()
            val tag = safeMatcher.group(gc - 1)
            assertEquals(RegexUtil.KEY_IMG, tag)
            val media = safeMatcher.group(gc)
            assertEquals(
                "(https:\\/\\/static1.fjcdn.com\\/thumbnails\\/comments\\/Go+talk+to+our+friendly+fellows+on+the+anime+board+_496f62c2f231bc1c8a9b77a449bf628f.gif)",
                media,
            )
            current += 1
        }
        assertEquals(expectedCount, current)
    }

    @Test
    fun findIntentKeys() {
        var matcher = RegexUtil.findIntentKeys("https://anitrend.gitbook.io/project/architecture")
        assertNull(matcher)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/anime/100483/Yuragisou-no-Yuunasan/")
        assertNotNull(matcher)
        var type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_ANIME, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/manga/87213/Yuragisou-no-Yuunasan/")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_MANGA, type)

        /* This is deprecated in the new front end */
        matcher = RegexUtil.findIntentKeys("https://anilist.co/actor/102263/Youko-Hikasa")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_ACTOR, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/character/88573/Subaru-Natsuki")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_CHARACTER, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/staff/102263/Youko-Hikasa")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_STAFF, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/studio/18/Toei-Animation")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_STUDIO, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/activity/38932001")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_ACTIVITY, type)

        matcher = RegexUtil.findIntentKeys("https://anilist.co/user/wax911/")
        assertNotNull(matcher)
        type = matcher!!.group(1)
        assertEquals(KeyUtil.DEEP_LINK_USER, type)
    }

    @Test
    fun buildYoutube() {
        val expected = "https://www.youtube.com/watch?v=8a0gn8mmnaY"
        var result = RegexUtil.buildYoutube("https://www.youtube.com/watch?v=8a0gn8mmnaY")
        assertEquals(expected, result)
        result = RegexUtil.buildYoutube("https://youtu.be/8a0gn8mmnaY")
        assertEquals(expected, result)
        result = RegexUtil.buildYoutube("8a0gn8mmnaY")
        assertEquals(expected, result)
    }

    @Test
    fun createYoutubeStandard() {
        val expected = "youtube(8a0gn8mmnaY)"
        val result = RegexUtil.createYoutubeStandard("https://youtu.be/8a0gn8mmnaY")
        assertEquals(expected, result)
    }

    @Test
    fun getYoutubeThumb() {
        val expected = "https://img.youtube.com/vi/8a0gn8mmnaY/hqdefault.jpg"
        var result = RegexUtil.getYoutubeThumb("https://www.youtube.com/watch?v=8a0gn8mmnaY")
        assertEquals(expected, result)

        result = RegexUtil.getYoutubeThumb("https://data.whicdn.com/images/107659661/original.gif")
        assertEquals(RegexUtil.NO_THUMBNAIL, result)
    }

    @Test
    fun findUserTags() {
        val input = "img(https://cdn.discordapp.com/attachments/317768562620235776/525201025393754112/Anitrend.png)\n" +
            "\n" +
            "__The AniTrend Family__ by @Signi58\n" +
            "\n" +
            "Top Left -> @YouseffHabri | Top Right -> @Lionirdeadman \n" +
            "Middle Left -> @Swap | Middle _who else??_ | Middle Right -> @Mokacchi\n" +
            "Bottom Left -> @Signi58 | Bottom Right @Taichikuji\n" +
            "\n" +
            "Feel jealous yet <3"
        val actual = RegexUtil.findUserTags(input)

        val expected = "img(https://cdn.discordapp.com/attachments/317768562620235776/525201025393754112/Anitrend.png)\n" +
            "\n" +
            "__The AniTrend Family__ by __[@Signi58](https://anilist.co/user/Signi58)__\n" +
            "\n" +
            "Top Left -> __[@YouseffHabri](https://anilist.co/user/YouseffHabri)__ | Top Right -> __[@Lionirdeadman](https://anilist.co/user/Lionirdeadman)__ \n" +
            "Middle Left -> __[@Swap](https://anilist.co/user/Swap)__ | Middle _who else??_ | Middle Right -> __[@Mokacchi](https://anilist.co/user/Mokacchi)__\n" +
            "Bottom Left -> __[@Signi58](https://anilist.co/user/Signi58)__ | Bottom Right __[@Taichikuji](https://anilist.co/user/Taichikuji)__\n" +
            "\n" +
            "Feel jealous yet <3"
        assertEquals(expected, actual)
    }
}
