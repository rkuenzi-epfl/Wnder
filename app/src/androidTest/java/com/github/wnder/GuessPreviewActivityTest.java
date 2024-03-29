package com.github.wnder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.guessLocation.GuessPreviewActivity;
import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.picture.UserModule;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.UserDatabase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({NetworkModule.class, PicturesModule.class, UserModule.class})
public class GuessPreviewActivityTest {

    //Intent with extra that the activity with start with
    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), GuessPreviewActivity.class);
    }

    private final static SignedInUser user = new SignedInUser("allGuessedUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));

    private final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(GuessPreviewActivity.class));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @BindValue
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    @BindValue
    public static UserDatabase userDatabase = Mockito.mock(UserDatabase.class);

    @BeforeClass
    public static void setup(){
        GlobalUser.setUser(user);
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Location loc =  new Location("");
        loc.setLatitude(15.);
        loc.setLongitude(5.);
        when(picturesDatabase.updateKarma(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(null));
        when(picturesDatabase.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDatabase.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(loc));
        when(userDatabase.getNewPictureForUser(user)).thenReturn(CompletableFuture.completedFuture("testPicDontRm")); //This string will never really be used by the tests, but in case the test are not robust, it's here
    }

    @AfterClass
    public static void end(){
        GlobalUser.resetUser();
    }

    @Before
    //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        Intents.init();
        when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGuessLocationButton(){
        onView(withId(R.id.guessButton)).perform(click());
        Intents.intended(hasComponent(GuessLocationActivity.class.getName()));
    }

    @Test
    public void testGuessLocationButtonWhenNoInternet(){
        when(networkInfo.isNetworkAvailable()).thenReturn(false);

        onView(withId(R.id.guessButton)).perform(click());

        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));
        onView(withText(R.string.no_connection)).perform(pressBack());
    }

    @Test
    public void testSwipeRight(){
        onView(withId(R.id.imagePreview)).perform(swipeRight());
        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));
    }

    @Test
    public void testSwipeLeft(){
        onView(withId(R.id.imagePreview)).perform(swipeLeft());
        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));
    }

    @Test
    public void testReportButton(){
        onView(withId(R.id.helperButton)).perform(click());

        onView(withText("Report")).perform(click());

        onView(withText("Confirm")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    @Test
    public void testHelpButton(){
        onView(withId(R.id.helperButton)).perform(click());

        onView(withText("Help")).perform(click());

        onView(withText("Ok")).check(matches(isDisplayed()));
    }

    @Test
    public void testSaveButtonIsClickable(){
        onView(withId(R.id.helperButton)).perform(click());

        onView(withText("Save to gallery")).perform(click());
    }
}
