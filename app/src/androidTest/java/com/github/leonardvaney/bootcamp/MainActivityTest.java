package com.github.leonardvaney.bootcamp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static com.github.leonardvaney.bootcamp.MainActivity.EXTRA_MESSAGE;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testUI(){
        Intents.init();

        Intent resultData = new Intent();
        String nameTest = "Nametest";
        resultData.putExtra(EXTRA_MESSAGE, nameTest);
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);


        intending(toPackage("com.github.leonardvaney.bootcamp")).respondWith(result);

        Espresso.onView(ViewMatchers.withId(R.id.mainName)).perform(ViewActions.click(), ViewActions.typeText("test"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.mainGoButton)).perform(ViewActions.click());

        Intents.release();
    }
}

