package com.github.wnder;

import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class GuessPreviewActivityTest {

    @Rule
    public ActivityScenarioRule<GuessPreviewActivity> testRule = new ActivityScenarioRule<>(GuessPreviewActivity.class);

    @BeforeClass
    public static void setup(){
        SignedInUser u = new SignedInUser("allGuessedUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(u);
        boolean[] isDone = new boolean[1];
        isDone[0] = false;
        Storage.onIdsAndLocAvailable((allIdsAndLocs) -> {
            for (String id: allIdsAndLocs.keySet()) {
                new ExistingPicture(id);
            }
            isDone[0] = true;
        });
        while(!isDone[0]);
        GlobalUser.resetUser();
    }

    @Test
    public void testGuessLocationButton(){
        Intents.init();
        onView(withId(R.id.guessButton)).perform(click());

        // TODO: Check openGuessActivity() correct execution, probably that the activity to make a guess is actually launched and maybe that it sends the image identifier with it
        // Intents.intended(hasComponent(GuessActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testSkipButton(){
        Intents.init();
        onView(withId(R.id.skipButton)).perform(click());

        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();

        SignedInUser u = new SignedInUser("allGuessedUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(u);

        Intents.init();
        onView(withId(R.id.skipButton)).perform(click());

        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();
        GlobalUser.resetUser();
    }



}
