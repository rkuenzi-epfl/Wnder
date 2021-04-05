package com.github.wnder;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.getIntents;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GuessLocationActivityInstrumentedTest  {

    @Rule //launches a given activity before the test starts and closes after the test
    public ActivityScenarioRule<ImageFromGalleryActivity> activityRule = new ActivityScenarioRule(GuessLocationActivity.class);

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

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        Bundle b = new Bundle();
        b.putDouble("guessLat", 10);
        b.putDouble("guessLng", 10);
        b.putDouble("cameraLat", 10);
        b.putDouble("cameraLng", 10);
        intent.putExtras(b);
    }

    @Test
    public void testMapPressWithExtra() {
        onView(withId(R.id.mapView)).perform(click());
    }
}

