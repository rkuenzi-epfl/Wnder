package com.github.wnder;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    //Open the activity as a test because there is nothing in the activity yet
    @Test
    public void testEmptyMain(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        scenario.close();
    }
}
