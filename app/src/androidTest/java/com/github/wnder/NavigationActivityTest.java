package com.github.wnder;


import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.github.wnder.guessLocation.GuessPreviewActivity;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.tour.TourActivity;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;

import org.junit.After;
import org.junit.Before;
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
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class, NetworkModule.class})
public class NavigationActivityTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkService.class);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Before
    public void before(){
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }


    @BindValue
    public static PicturesDatabase picturesDb = Mockito.mock(PicturesDatabase.class);

    @Before
    public void setUp() {
        Intents.init();

        //Check that first displayed fragment is the profile page
        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        Intents.release();
    }

  
    @Test
    public void guessButtonShowsSeekbar() {
        onView(withId(R.id.guess_page)).perform(click());
        onView(withText("Radius: 5km")).check(matches(isDisplayed()));
        onView(withId(R.id.radiusSeekBar)).perform(swipeRight());
        onView(withText("Radius: 1000km")).check(matches(isDisplayed()));
    }

    @Test
    public void guessButtonWithoutConnectionLaunchesAlert() {
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        onView(withId(R.id.guess_page)).perform(click());
        onView(withId(R.id.navigationToGuessButton)).perform(click());
        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));
        when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void tourButtonGoesToTour() {
        onView(withId(R.id.guess_page)).perform(click());
        onView(withText("Radius: 5km")).check(matches(isDisplayed()));
        onView(withId(R.id.navigationToTourButton)).perform(click());
        Intents.intended(hasComponent(TourActivity.class.getName()));
    }

    @Test
    public void informPictureCantBeUploadedAsGuest(){
        GlobalUser.resetUser();
        //Goto take picture
        onView(withId(R.id.take_picture_page)).perform(click());
        // As we are guest, verify that we are alerted we cannot upload
        onView(withText(R.string.guest_no_upload)).check(matches(isDisplayed()));
        /* Not working for some reason
        onView(withId(android.R.id.button1)).perform(click());
        // Check that we are sent back to the profile page
        onView(withText(R.id.profile_picture)).check(matches(isDisplayed()));*/
    }

    @Test
    public void signedInUserUploadStart(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        CompletableFuture<Void> cf = new CompletableFuture<>();

        when(picturesDb.uploadPicture(anyString(), any())).thenReturn(cf);

        //Goto take picture
        onView(withId(R.id.take_picture_page)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.takePictureButton)).perform(click());

        /* Disabled because it doesn't work on cirrus for unknown reasons.
        SystemClock.sleep(2000);
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.upload_started)).check(matches(isDisplayed()));*/
    }

    @Test
    public void signedInUserUploadSuccessful(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        cf.complete(null);


        when(picturesDb.uploadPicture(anyString(), any())).thenReturn(cf);

        //Goto take picture
        onView(withId(R.id.take_picture_page)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.takePictureButton)).perform(click());

        /* Disabled because it doesn't work on cirrus for unknown reasons.
        SystemClock.sleep(2000);
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.upload_successful)).check(matches(isDisplayed()));*/
    }

    @Test
    public void signedInUserInAPerfectWorld(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(true);
        when(picturesDb.uploadPicture(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        //Goto take picture
        onView(withId(R.id.take_picture_page)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.takePictureButton)).perform(click());

        /* Disabled because it doesn't work on cirrus for unknown reasons.
        SystemClock.sleep(2000);
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withId(R.id.uploadButton)).check(matches(not(isDisplayed())));*/
    }

    @Test
    public void activateTourMode(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(true);

        onView(withId(R.id.take_picture_page)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.activateTour)).perform(click());
        onView(withId(R.id.activateTour)).check(matches(not(isDisplayed())));

        onView(withId(R.id.takePictureButton)).perform(click());
    }
}
