package com.github.wnder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.scoreboard.ScoreboardActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class GuessLocationActivityInstrumentedTest  {
    //Intent with extras that the activity will start with
    private static Intent intent;
    private static Map<String, Double> dummyMap;

    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(intent));

    @BindValue
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    //For activities that we did ourself otherwise need to use mockito
    @Before //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown(){
        Intents.release();
    }

    @BeforeClass
    public static void beforeAll(){
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Location dummyLoc = new Location("");
        dummyLoc.setLatitude(0.);
        dummyLoc.setLongitude(0.);
        dummyMap = new HashMap<>();
        dummyMap.put("User0", 32.);
        dummyMap.put("User1", 44.);
        when(picturesDatabase.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDatabase.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLoc));
        when(picturesDatabase.getScoreboard(any())).thenReturn(CompletableFuture.completedFuture(dummyMap));
        when(picturesDatabase.updateKarma(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(null));

        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, "");
    }

    @Test
    public void goodThingsAreVisibleWhenZooming(){
        //Before zooming
        onView(withId(R.id.imageToGuessCard)).check(matches(isDisplayed()));
        onView(withId(R.id.imageToGuessCardZoomedIn)).check(matches(not(isDisplayed())));

        //Try zooming in
        onView(withId(R.id.imageToGuessCard)).perform(click());
        onView(withId(R.id.imageToGuessCard)).check(matches(not(isDisplayed())));
        onView(withId(R.id.imageToGuessCardZoomedIn)).check(matches(isDisplayed()));
        onView(withId(R.id.compassMode)).check(matches(not(isDisplayed())));
        onView(withId(R.id.confirmButton)).check(matches(not(isDisplayed())));

        //Zoom out again
        onView(withId(R.id.imageToGuessCardZoomedIn)).perform(click());
        onView(withId(R.id.imageToGuessCard)).check(matches(isDisplayed()));
        onView(withId(R.id.imageToGuessCardZoomedIn)).check(matches(not(isDisplayed())));
        onView(withId(R.id.compassMode)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmButton)).check(matches(isDisplayed()));
    }

    @Test
    public void nextGuessButtonLeadsToGuessPreview(){
        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.backToGuessPreview)).perform(click());

        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));
    }

    @Test
    public void testConfirmButtonPressAndLittleImageUpdatesStatus() {
        onView(withId(R.id.imageToGuessCard)).check(matches(isDisplayed()));

        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.imageToGuessCard)).check(matches(not(isDisplayed())));

        Intents.assertNoUnverifiedIntents();

        onView(withId(R.id.confirmButton)).perform(click());

        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));
    }

    @Test
    public void testConfirmInCompassMode(){
        onView(withId(R.id.mapView)).perform(click());
        onView(withId(R.id.compassMode)).perform(click());
        onView(withId(R.id.confirmButton)).perform(click());

        Intents.assertNoUnverifiedIntents();

        onView(withId(R.id.confirmButton)).perform(click());

        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));
    }

    @Test
    public void testSwitchMode(){
        onView(withId(R.id.compassMode)).perform(click());
        onView(withId(R.id.compassMode)).perform(click());

        onView(withId(R.id.confirmButton)).perform(click());
        onView(withId(R.id.confirmButton)).perform(click());

        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));
    }

    @Test
    public void nextGuessButtonIsDisplayedAtRightTime(){
        onView(withId(R.id.backToGuessPreview)).check(matches(not(isDisplayed())));

        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.backToGuessPreview)).check(matches(isDisplayed()));
    }
}

