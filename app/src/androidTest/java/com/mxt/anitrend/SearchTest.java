package com.mxt.anitrend;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by max on 2017/04/19.
 */
@RunWith(AndroidJUnit4.class)
public class SearchTest {

    private String mQuery;

    @Rule
    public ActivityTestRule<MainActivity> mMainRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void initTest() {
        mQuery = "One Punch Man";
    }

    @Test
    public void assumeMainTest() {

        onView(withId(com.mancj.materialsearchbar.R.id.mt_search)).perform(click());

        onView(withId(com.mancj.materialsearchbar.R.id.mt_editText)).perform(typeText(mQuery));

        closeSoftKeyboard();

        onView(withId(com.mancj.materialsearchbar.R.id.mt_editText)).perform(pressImeActionButton());
    }

    @Test
    public void secondMainTest() {

        onView(withId(R.id.search_pager)).perform(swipeUp());

        onView(withId(R.id.search_pager)).perform(swipeLeft());

        pressBack();

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeLeft());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeLeft());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

        onView(withId(R.id.page_container)).perform(swipeUp());

    }
}
