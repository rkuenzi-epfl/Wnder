package com.github.wnder;


import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Test
    public void testLogin(){

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);

        ActivityScenario scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.textLogin)).check(matches(withText("Welcome milo Fegelein")));

        scenario.close();
    }
}
