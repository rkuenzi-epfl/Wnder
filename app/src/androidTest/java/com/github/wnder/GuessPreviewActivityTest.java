package com.github.wnder;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class GuessPreviewActivityTest {

    @Rule
    public ActivityScenarioRule<GuessPreviewActivity> testRule = new ActivityScenarioRule<>(GuessPreviewActivity.class);

    @Before
    public void setup(){
    }

    @Test
    public void testGuessLocationButton(){
        Intents.init();
        onView(withId(R.id.guessButton)).perform(click());

        // TODO: Check openGuessActivity() correct execution, probably that the activity to make a guess is actually launched and maybe that it sends the image identifier with it
        // Intents.intended(hasComponent(GuessActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testSkipButton(){
        Intents.init();
        onView(withId(R.id.skipButton)).perform(click());

        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();
    }

}
