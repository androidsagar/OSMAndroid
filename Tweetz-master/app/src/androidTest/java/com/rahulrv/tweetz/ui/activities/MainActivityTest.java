package com.rahulrv.tweetz.ui.activities;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import com.rahulrv.tweetz.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

/**
 *
 *
 */
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() throws Exception {
        rule.launchActivity(null);
    }

    @Test
    public void testLaunch() {
        Espresso.onView(ViewMatchers.withId(R.id.trends)).perform(RecyclerViewActions.scrollToPosition(2));
        Espresso.onView(ViewMatchers.withText(R.string.app_name)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.menu_search)).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.search_view)).perform(typeText("y"))
                .perform(typeText("e"))
                .perform(typeText("a"))
                .perform(typeText("r"))
                .perform(typeText(" 2"))
                .perform(typeText("0"))
                .perform(typeText("16"));
       // Espresso.onView(ViewMatchers.withId(R.id.search_results)).perform(RecyclerViewActions.scrollToPosition(2));
    }

}
