package com.github.wnder;

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
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class PictureHistoryTest {
    @Rule //launches a given activity before the test starts and closes after the test
    public ActivityScenarioRule<PictureHistoryActivity> activityRule = new ActivityScenarioRule(PictureHistoryActivity.class);

    //For activities that we did ourself otherwise need to use mockito
    @Before
    //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        Intents.init();
    }

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testScoreboardButton(){
        //placeholder test. Change this later
        onView(withId(R.id.pictureHistoryToScoreboardButton)).perform(click());
        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));
    }
}
