package com.mxt.anitrend.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class EpisodeUtil_GetActualTitleTest {

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Boku no Hero Academia - Episode 23", "Boku no Hero Academia"},
                {"Haikyuu Season 3", "Haikyuu"},
                {"Boku no Hero Academia Season 2 - Episode 19", "Boku no Hero Academia"}
        });
    }

    @Parameterized.Parameter(0)
    public String inputTitle;

    @Parameterized.Parameter(1)
    public String actualTitle;


    @Test
    public void getActualTile() {
        assertThat(EpisodeUtil.getActualTile(inputTitle), equalTo(actualTitle));
    }

}
