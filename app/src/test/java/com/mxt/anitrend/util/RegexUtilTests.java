package com.mxt.anitrend.util;
import org.junit.Test;

import java.util.regex.Matcher;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class RegexUtilTests {

    @Test
    public void findMedia() {
        String testStatus = "MAL is back\\n\\nand i no longer give a shit\\n\\nimg220(https:\\/\\/static1.fjcdn.com\\/thumbnails\\/comments\\/Go+talk+to+our+friendly+fellows+on+the+anime+board+_496f62c2f231bc1c8a9b77a449bf628f.gif)\\nThis place is nice, i like it.";
        Matcher matcher = RegexUtil.findMedia(testStatus);
        assertNotNull(matcher);
        int expectedCount = 1, current = 0;
        while (matcher.find()) {
            int gc = matcher.groupCount();
            String tag = matcher.group(gc - 1);
            assertEquals(RegexUtil.KEY_IMG, tag);
            String media = matcher.group(gc);
            assertEquals("(https:\\/\\/static1.fjcdn.com\\/thumbnails\\/comments\\/Go+talk+to+our+friendly+fellows+on+the+anime+board+_496f62c2f231bc1c8a9b77a449bf628f.gif)", media);
            current += 1;
        }
        assertEquals(expectedCount, current);
    }

    @Test
    public void findIntentKeys() {
        Matcher matcher = RegexUtil.findIntentKeys("https://anitrend.gitbook.io/project/architecture");
        assertNull(matcher);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/anime/100483/Yuragisou-no-Yuunasan/");
        assertNotNull(matcher);
        String type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_ANIME, type);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/manga/87213/Yuragisou-no-Yuunasan/");
        assertNotNull(matcher);
        type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_MANGA, type);

        /* This is deprecated in the new front end */
        matcher = RegexUtil.findIntentKeys("https://anilist.co/actor/102263/Youko-Hikasa");
        assertNull(matcher);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/character/88573/Subaru-Natsuki");
        assertNotNull(matcher);
        type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_CHARACTER, type);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/staff/102263/Youko-Hikasa");
        assertNotNull(matcher);
        type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_STAFF, type);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/studio/18/Toei-Animation");
        assertNotNull(matcher);
        type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_STUDIO, type);

        matcher = RegexUtil.findIntentKeys("https://anilist.co/user/wax911/");
        assertNotNull(matcher);
        type = matcher.group(1);
        assertEquals(KeyUtil.DEEP_LINK_USER, type);
    }

    @Test
    public void buildYoutube() {
        String expected = "https://www.youtube.com/watch?v=8a0gn8mmnaY";
        String result = RegexUtil.buildYoutube("https://www.youtube.com/watch?v=8a0gn8mmnaY");
        assertEquals(expected, result);
        result = RegexUtil.buildYoutube("https://youtu.be/8a0gn8mmnaY");
        assertEquals(expected, result);
        result = RegexUtil.buildYoutube("8a0gn8mmnaY");
        assertEquals(expected, result);
    }

    @Test
    public void createYoutubeStandard() {
        String expected = "youtube(8a0gn8mmnaY)";
        String result = RegexUtil.createYoutubeStandard("https://youtu.be/8a0gn8mmnaY");
        assertEquals(expected, result);
    }

    @Test
    public void getYoutubeThumb() {
        String expected = "https://img.youtube.com/vi/8a0gn8mmnaY/hqdefault.jpg";
        String result = RegexUtil.getYoutubeThumb("https://www.youtube.com/watch?v=8a0gn8mmnaY");
        assertEquals(expected, result);

        result = RegexUtil.getYoutubeThumb("https://data.whicdn.com/images/107659661/original.gif");
        assertEquals(RegexUtil.NO_THUMBNAIL, result);
    }
}