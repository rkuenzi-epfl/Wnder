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

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.leonardvaney.bootcamp.MainActivity.EXTRA_MESSAGE;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testUI(){
        Intents.init();

        String nameTest = "Nametest"; //string de comparaison (sur l'émulateur on écrit "test" à côté de la string "Name" déjà écrite)

        //Permet de créer des intents mais apparement c'est pas nécessaire dans ce cas
        /*Intent resultData = new Intent();
        resultData.putExtra(EXTRA_MESSAGE, nameTest);
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);*/

        //Effectue les actions sur l'émulateur
        Espresso.onView(ViewMatchers.withId(R.id.mainName)).perform(ViewActions.click(), ViewActions.typeText("test"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.mainGoButton)).perform(ViewActions.click());

        intended(hasExtra(EXTRA_MESSAGE, nameTest)); //regarde si les intents qui passent contiennent nameTest

        Intents.release();
    }
}

