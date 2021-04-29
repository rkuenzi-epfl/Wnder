package com.github.wnder;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GuessPreviewActivityTest {
    static int distanceTest = 1000;

    //Intent with extra that the activity with start with
    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessPreviewActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_DISTANCE, distanceTest);
    }

    @Rule
    public ActivityScenarioRule<GuessPreviewActivity> testRule = new ActivityScenarioRule<>(intent);

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

    @Before
    //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        Intents.init();
    }

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGuessLocationButton(){
        User user = GlobalUser.getUser();
        user.setLocation(new Location("defaultLocation"));

        onView(withId(R.id.guessButton)).perform(click());

        Intents.intended(hasComponent(GuessLocationActivity.class.getName()));

        //Test of one of the extras
        int distanceDiameter = Intents.getIntents().get(0).getExtras().getInt(GuessLocationActivity.EXTRA_DISTANCE);
        assertEquals(distanceDiameter, distanceTest);

        GlobalUser.resetUser();
    }

    @Test
    public void testSkipButton(){
        SignedInUser u = new SignedInUser("allGuessedUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(u);

        onView(withId(R.id.skipButton)).perform(click());
        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();

        GlobalUser.resetUser();

        Intents.init();

        onView(withId(R.id.skipButton)).perform(click());
        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));
    }

    @Test
    public void testReportButton(){
        onView(withId(R.id.reportButton)).perform(click());

        onView(withText("Confirm")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }
}
