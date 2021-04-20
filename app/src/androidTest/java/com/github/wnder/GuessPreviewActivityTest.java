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

    @Rule
    public ActivityScenarioRule<GuessPreviewActivity> testRule = new ActivityScenarioRule<>(GuessPreviewActivity.class);

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
    public void testGuessLocationButton(){
        onView(withId(R.id.guessButton)).perform(click());

        // TODO: Check openGuessActivity() correct execution, probably that the activity to make a guess is actually launched and maybe that it sends the image identifier with it
        Intents.intended(hasComponent(GuessLocationActivity.class.getName()));

    }



}
