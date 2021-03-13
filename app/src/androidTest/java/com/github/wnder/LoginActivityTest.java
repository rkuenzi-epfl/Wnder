package com.github.wnder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Before
    public void setup(){
    }

    @Test
    public void testNoLogin(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        onView(withId(R.id.textLogin)).check(matches(withText("Please sign in.")));
        scenario.close();
    }

    @Test
    public void testButtonPress(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        onView(withId(R.id.sign_in_button)).perform(click());
        scenario.close();
    }

}
