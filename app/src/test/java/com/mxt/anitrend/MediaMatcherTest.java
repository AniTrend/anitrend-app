package com.mxt.anitrend;

import com.mxt.anitrend.util.PatternMatcher;

import org.junit.Test;

import java.util.regex.Matcher;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MediaMatcherTest {

    @Test
    public void youtubeIdTest() {
        String output = PatternMatcher.buildYoutube("MuizRxZu9rg");
        assertNotNull(output);
        assertEquals("https://www.youtube.com/watch?v=MuizRxZu9rg",output);
    }

    @Test
    public void youtubeLinkTest() {
        String output = PatternMatcher.buildYoutube("https://www.youtube.com/watch?v=MuizRxZu9rg");
        assertNotNull(output);
        assertEquals("https://www.youtube.com/watch?v=MuizRxZu9rg", output);
    }

    @Test
    public void youtubeShortLinkTest() {
        String output = PatternMatcher.buildYoutube("https://youtu.be/MuizRxZu9rg");
        assertNotNull(output);
        assertEquals("https://www.youtube.com/watch?v=MuizRxZu9rg", output);
    }

    @Test
    public void isYoutube() {
        Matcher matcher = PatternMatcher.findMedia("youtube(MuizRxZu9rg)");
        assertEquals(2, matcher.groupCount());
        assertTrue(matcher.find());
    }

    @Test
    public void isImage() {
        Matcher matcher = PatternMatcher.findMedia("img(http://www.webmfiles.org/wp-content/uploads/2010/05/webm-file.jpg)");
        assertEquals(2, matcher.groupCount());
        assertTrue(matcher.find());
    }

    @Test
    public void isVideo() {
        Matcher matcher = PatternMatcher.findMedia("webm(http://dl1.webmfiles.org/big-buck-bunny_trailer.webm)");
        assertEquals(2, matcher.groupCount());
        assertTrue(matcher.find());
    }
}