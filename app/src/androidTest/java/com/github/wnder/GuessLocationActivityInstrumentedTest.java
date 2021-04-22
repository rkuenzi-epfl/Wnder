package com.github.wnder;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
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
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class GuessLocationActivityInstrumentedTest  {
    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, "");
    }

    @Rule //launches a given activity before the test starts and closes after the test
    public ActivityScenarioRule<ImageFromGalleryActivity> activityRule = new ActivityScenarioRule(intent);

    //For activities that we did ourself otherwise need to use mockito
    @Before //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        Intents.init();
    }

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testButtonPress() {
        onView(withId(R.id.confirmButton)).perform(click());
    }

    @Test
    public void testMapPress() {
        onView(withId(R.id.mapView)).perform(click());
    }
}

