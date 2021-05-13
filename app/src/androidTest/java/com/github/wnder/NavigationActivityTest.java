package com.github.wnder;


import android.view.View;

import androidx.test.espresso.action.ViewActions;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;


import org.hamcrest.Matcher;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
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

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.assertTrue;
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

    @Before
    public void before(){
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }


    @BindValue
    public static PicturesDatabase picturesDb = Mockito.mock(PicturesDatabase.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

  
    @Test
    public void guessButtonShowsSeekbar() {
        onView(withId(R.id.guess_page)).perform(click());
        onView(withText("Radius: 5km")).check(matches(isDisplayed()));
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
    public void informPictureCantBeUploadedAsGuest(){
        GlobalUser.resetUser();
        //Goto take picture
        onView(withId(R.id.bottom_navigation)).perform(click(1, 0));
        // As we are guest, verify that we are alerted we cannot upload
        onView(withText(R.string.guest_no_upload)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        // Build a result to return from the Camera app
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Intent resultData = new Intent();
        resultData.putExtra("data", dummyPic);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Return a sucessful result from the camera
        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        // Click the take picture button
        onView(withId(R.id.takePictureButton)).perform(click());
        // And check that we are informed that upload did not happen
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.guest_no_upload)).check(matches(isDisplayed()));

    }

    @Test
    public void signedInUserInformedNoConnection(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        cf.completeExceptionally(new Exception());
        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(cf);

        //Goto take picture
        onView(withId(R.id.bottom_navigation)).perform(click(1, 0));
        // As we have no connection, verify that we are alerted we cannot upload
        onView(withText(R.string.no_internet_upload)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        // Build a result to return from the Camera app
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Intent resultData = new Intent();
        resultData.putExtra("data", dummyPic);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Return a sucessful result from the camera
        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        onView(withId(R.id.takePictureButton)).perform(click());
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.upload_failed)).check(matches(isDisplayed()));

    }

    @Test
    public void signedInUserDidNotGetPicture(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        cf.completeExceptionally(new Exception());
        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(cf);

        //Goto take picture
        onView(withId(R.id.bottom_navigation)).perform(click(1, 0));
        // As we have no connection, verify that we are alerted we cannot upload
        onView(withText(R.string.no_internet_upload)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        // Build a result to return from the Camera app
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Intent resultData = new Intent();
        resultData.putExtra("data", dummyPic);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData);

        // Return a sucessful result from the camera
        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        onView(withId(R.id.takePictureButton)).perform(click());

        onView(withId(R.id.uploadButton)).check(matches(not(isDisplayed())));


    }

    @Test
    public void signedInUserInAPerfectWorld(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        when(networkInfo.isNetworkAvailable()).thenReturn(true);
        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        //Goto take picture
        onView(withId(R.id.bottom_navigation)).perform(click(1, 0));

        // Build a result to return from the Camera app
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        Intent resultData = new Intent();
        resultData.putExtra("data", dummyPic);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Return a sucessful result from the camera
        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        onView(withId(R.id.takePictureButton)).perform(click());
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.upload_successful)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadButton)).check(matches(not(isDisplayed())));
    }
}
