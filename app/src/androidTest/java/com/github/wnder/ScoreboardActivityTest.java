package com.github.wnder;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ScoreboardActivityTest {
    // TODO: Create fake image with a full scoreboard, then test if we can still finish activity

    @Rule
    public ActivityScenarioRule<ScoreboardActivity> activityRule = new ActivityScenarioRule(ScoreboardActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void leaveButtonFinishesActivity(){
        onView(withId(R.id.leaveScoreboardButton)).perform(click());
        assertThat(activityRule.getScenario().getState(), is(Lifecycle.State.DESTROYED));
    }

}
