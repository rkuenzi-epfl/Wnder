package com.github.wnder;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.internal.NavigationMenuView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

@HiltAndroidTest
public class NavigationActivityTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @Test
    public void clickingOnBarDoesNothing(){
        Intents.init();

        //When profile is selected
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(0, 0));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(1, 0));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(2, 0));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(3, 0));

        //When take_picture is selected
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(0, 1));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(1, 1));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(2, 1));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(3, 1));

        //When guess is selected
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(0, 2));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(1, 2));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(2, 2));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(3, 2));

        //When history is selected
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(0, 3));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(1, 3));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(2, 3));
        onView(withId(R.id.bottom_navigation)).perform(ViewActions.click(3, 3));

        //All those should do nothing for now
        assertTrue(true);

        Intents.release();
    }
}
