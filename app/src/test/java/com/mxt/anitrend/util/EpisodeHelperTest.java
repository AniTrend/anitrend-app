package com.mxt.anitrend.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by max on 2018/01/27.
 */
public class EpisodeHelperTest {

    @Test
    public void episodeSupport() throws Exception {
    }

    @Test
    public void getActualTile() throws Exception {
        assertEquals("Dagashi Kashi", EpisodeHelper.getActualTile("Dagashi Kashi Season 2 - Episode 3 - Beigoma, Reminiscence, and"));
        assertEquals("Future Card Buddyfight X", EpisodeHelper.getActualTile("Future Card Buddyfight X - Episode 43 - Turbulent Warlord Dragon"));
    }

}