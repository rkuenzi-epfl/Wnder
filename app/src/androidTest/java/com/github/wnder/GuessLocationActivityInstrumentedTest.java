package com.github.wnder;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

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
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class GuessLocationActivityInstrumentedTest  {
    //Intent with extras that the activity will start with
    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, "");
    }
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

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

    @BeforeClass
    public static void beforeAll(){
        dummyMap = new HashMap<>();
        dummyMap.put("User0", 32.);
        dummyMap.put("User1", 44.);
        when(picturesDatabase.getScoreboard(any())).thenReturn(CompletableFuture.completedFuture(dummyMap));
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 10.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, "");
    }

    @Test
    public void testConfirmButtonPress() {
        onView(withId(R.id.confirmButton)).perform(click());

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
}

