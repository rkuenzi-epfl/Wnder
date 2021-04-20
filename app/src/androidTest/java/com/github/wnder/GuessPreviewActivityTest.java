package com.github.wnder;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.picture.ExistingPicture;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GuessPreviewActivityTest {

    private int isDone = 0;

    @Rule
    public ActivityScenarioRule<GuessPreviewActivity> testRule = new ActivityScenarioRule<>(GuessPreviewActivity.class);

    @Before
    public void setup(){
    }

    @Test
    public void testSkipAndGuessButton(){
        Intents.init();
        //TODO: replace once PR 90 is merged
        ExistingPicture pic = new ExistingPicture("testPicDontRm");

        pic.onKarmaAvailable((k1) -> {
            onView(withId(R.id.skipButton)).perform(click());
            pic.onKarmaAvailable((k2) -> {
                assertThat(k2, is(k1-1));

                Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

                //Check guess location button
                onView(withId(R.id.guessButton)).perform(click());

                // TODO: Check openGuessActivity() correct execution, probably that the activity to make a guess is actually launched and maybe that it sends the image identifier with it
                Intents.intended(hasComponent(GuessLocationActivity.class.getName()));

                Intents.release();
            });


        });
    }

    /* We test it in the skip button test for intents releasing problems purpose.
    @Test
    public void testGuessLocationButton(){
        Intents.init();
        onView(withId(R.id.guessButton)).perform(click());

        // TODO: Check openGuessActivity() correct execution, probably that the activity to make a guess is actually launched and maybe that it sends the image identifier with it
        Intents.intended(hasComponent(GuessLocationActivity.class.getName()));

        Intents.release();
    }*/



}
