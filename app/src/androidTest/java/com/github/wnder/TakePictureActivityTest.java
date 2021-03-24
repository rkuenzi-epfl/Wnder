package com.github.wnder;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class TakePictureActivityTest {
    @Rule
    public ActivityScenarioRule<ImageFromGalleryActivity> activityRule = new ActivityScenarioRule(TakePictureActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testTakePhotoButton(){
        onView(withId(R.id.takePictureButton)).perform(click());
        intended(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE));
    }

    @Test
    public void testActivityResult() {
        Intent resultData = new Intent();

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        onView(withId(R.id.takePictureButton)).perform(click());
        onView(withId(R.id.pictureConfirmButton)).perform(click());
    }
}
