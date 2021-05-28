package com.github.wnder;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.guessLocation.GuessPreviewActivity;
import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.picture.UploadInfo;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.wnder.TourDatabaseTest.deleteTestTour;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class GuessLocationTourModeActivityTest {
    //Intent with extras that the activity will start with
    private static Intent intent;

    @BindValue //For the creation of the tour in the database
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    private final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(intent));

    //For activities that we did ourself otherwise need to use mockito
    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown(){
        Intents.release();
    }

    @BeforeClass
    public static void beforeAll() throws ExecutionException, InterruptedException{

        FirebaseTourDatabase tdb = new FirebaseTourDatabase(ApplicationProvider.getApplicationContext());
        FirebasePicturesDatabase db = new FirebasePicturesDatabase(ApplicationProvider.getApplicationContext());

        Location defaultPicLoc = new Location("");
        defaultPicLoc.setLatitude(90);
        defaultPicLoc.setLongitude(0);

        String userName = "testUser";
        String firstUniqueId = "first" + userName + Calendar.getInstance().getTimeInMillis();

        CompletableFuture<Void> uploadFirstPic = db.uploadPicture(firstUniqueId, new UploadInfo(userName, defaultPicLoc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));

        uploadFirstPic.get();

        String tourName = "testTour";
        String tourUniqueId = tdb.generateTourUniqueId(tourName);

        List<String> pictures = new ArrayList<>();
        pictures.add(firstUniqueId);

        CompletableFuture<Void> uploadTour = tdb.uploadTour(tourUniqueId, tourName, pictures);

        try{
            uploadTour.get();
        } catch(Exception e){
            e.printStackTrace();
        }

        User user = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(user);

        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Location dummyLoc = new Location("");
        dummyLoc.setLatitude(0.);
        dummyLoc.setLongitude(0.);
        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("User0", 32.);
        dummyMap.put("User1", 44.);

        when(picturesDatabase.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDatabase.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLoc));
        when(picturesDatabase.getScoreboard(any())).thenReturn(CompletableFuture.completedFuture(dummyMap));
        when(picturesDatabase.updateKarma(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(null));

        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_GUESS_MODE, R.string.guess_tour_mode);
        Picture pictureToGuess = new Picture(firstUniqueId, defaultPicLoc.getLatitude(), defaultPicLoc.getLongitude());
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_TO_GUESS, pictureToGuess);
        intent.putExtra(GuessLocationActivity.EXTRA_TOUR_ID, tourUniqueId);
    }

    // Clean db
    @AfterClass
    public static void afterAll() {

        deleteTestTour();
    }

    @Test
    public void goodThingsAreVisibleWhenZoomingTourMode(){
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
        onView(withId(R.id.compassMode)).check(matches(not(isDisplayed())));
        onView(withId(R.id.confirmButton)).check(matches(isDisplayed()));
    }

    @Test
    public void goThroughTourBySkipping(){
        onView(withId(R.id.confirmButton)).perform(click());

        onView(withText(R.string.tour_mode_confirm_while_far_text)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.backToGuessPreview)).perform(click());

        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));
    }

    @Test
    public void testCompassModeNotAvailable(){
        onView(withId(R.id.compassMode)).check(matches(not(isDisplayed())));
    }
}