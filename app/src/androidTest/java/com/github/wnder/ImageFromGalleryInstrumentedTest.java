package com.github.wnder;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ImageFromGalleryInstrumentedTest  {
    @Rule //launches a given activity before the test starts and closes after the test
    public ActivityScenarioRule<ImageFromGallery> activityRule = new ActivityScenarioRule(ImageFromGallery.class);

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
        onView(withId(R.id.getGalleryImage)).perform(click());
    }

    @Test
    public void testGalleryOpens() {
        onView(withId(R.id.getGalleryImage)).perform(click());
        intended(hasAction(Intent.ACTION_PICK));
    }

    @Test
    public void testReceiveFromGallery() {
        Intent resultData = new Intent();
        resultData.putExtra("imageReturnedName", "imageReturnedData");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        onView(withId(R.id.getGalleryImage)).perform(click());

        onView(withId(R.id.textView)).check(matches(withText("imageReturnedData")));
    }
}

