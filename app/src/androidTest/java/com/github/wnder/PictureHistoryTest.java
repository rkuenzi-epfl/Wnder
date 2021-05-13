package com.github.wnder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class PictureHistoryTest {

    private static Map<String, Double> dummyMap;
    private static Intent intent;

    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(intent));

    // We do this statically to have the mock available before creating the intent
    @BindValue
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    @BeforeClass
    public static void classSetUp(){
        dummyMap = new HashMap<>();
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Location dummyLocation = new Location("");
        dummyLocation.setLatitude(32);
        dummyLocation.setLongitude(2);
        when(picturesDatabase.getScoreboard(anyString())).thenReturn(CompletableFuture.completedFuture(dummyMap));
        when(picturesDatabase.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDatabase.getMapSnapshot(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDatabase.getUserGuess(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLocation));
        when(picturesDatabase.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLocation));
        intent = new Intent(ApplicationProvider.getApplicationContext(), PictureHistoryActivity.class);
        intent.putExtra(PictureHistoryActivity.EXTRA_PICTURE_ID, "picture1");
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
    public void testScoreboardButton() {
        SignedInUser u = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(u);
        onView(withId(R.id.pictureHistoryToScoreboardButton)).perform(click());
        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));
        GlobalUser.resetUser();
    }
}
