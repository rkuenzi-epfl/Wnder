package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.guessLocation.TemporaryActivity;
import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.tour.TourDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({NetworkModule.class, PicturesModule.class})
public class TemporaryActivityTest {
    private final static SignedInUser user = new SignedInUser("allGuessedUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @BindValue
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    private final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    public static TourDatabase tourDb = Mockito.mock(FirebaseTourDatabase.class);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(TemporaryActivity.class));

    @BeforeClass
    public static void setup(){
        GlobalUser.setUser(user);
        List<String> tourPics = new ArrayList<>();
        tourPics.add("pic1");
        Location loc =  new Location("");
        loc.setLatitude(15.);
        loc.setLongitude(5.);
        when(tourDb.getTourPics(anyString())).thenReturn(CompletableFuture.completedFuture(tourPics));
        when(picturesDatabase.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(loc));
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
    public void testGuessLocationButton() {
        onView(withId(R.id.temporary_button)).perform(click());
        Intents.intended(hasComponent(GuessLocationActivity.class.getName()));
    }

    @Test
    public void testGuessLocationButtonWhenNoInternet(){
        when(networkInfo.isNetworkAvailable()).thenReturn(false);

        onView(withId(R.id.temporary_button)).perform(click());

        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));
        onView(withText(R.string.no_connection)).perform(pressBack());
    }

}
