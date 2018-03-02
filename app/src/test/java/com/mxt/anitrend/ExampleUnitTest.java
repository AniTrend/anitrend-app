package com.mxt.anitrend;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void milliseconds_Condition() throws Exception {
        long currentTime = 3000000;
        long result = TimeUnit.MICROSECONDS.toSeconds(currentTime - 2000000);
        assertEquals(1, result);
    }
}