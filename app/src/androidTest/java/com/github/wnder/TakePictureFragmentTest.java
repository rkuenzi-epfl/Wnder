package com.github.wnder;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;

import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class, NetworkModule.class})
public class TakePictureFragmentTest {

    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @BindValue
    public static PicturesDatabase picturesDb = Mockito.mock(PicturesDatabase.class);

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkService.class);

    @Test
    public void guestUserInformedTheyCannotUpload(){
        GlobalUser.resetUser();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TakePictureFragmentFakeActivity.class);;
        intent.putExtra(TakePictureFragmentFakeActivity.EXPECTED_RESULT, true);
        when(networkInfo.isNetworkAvailable()).thenReturn(true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.takePictureButton)).perform(click());
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(R.string.guest_no_upload)).check(matches(isDisplayed()));

    }

//    @Test
//    public void signedInUserInformedNoConnection(){
//        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
//        when(networkInfo.isNetworkAvailable()).thenReturn(false);
//        CompletableFuture<Void> cf = new CompletableFuture<>();
//        cf.completeExceptionally(new Exception());
//        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(cf);
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TakePictureFragmentFakeActivity.class);;
//        intent.putExtra(TakePictureFragmentFakeActivity.EXPECTED_RESULT, true);
//        ActivityScenario.launch(intent);
//
//        onView(withId(R.id.takePictureButton)).perform(click());
//        onView(withId(R.id.uploadButton)).perform(click());
//        onView(withText(R.string.upload_failed)).check(matches(isDisplayed()));
//
//    }

//    @Test
//    public void signedInUserDidNotGetPicture(){
//        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
//        CompletableFuture<Void> cf = new CompletableFuture<>();
//        cf.complete(null);
//        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(cf);
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TakePictureFragmentFakeActivity.class);;
//        intent.putExtra(TakePictureFragmentFakeActivity.EXPECTED_RESULT, false);
//        ActivityScenario.launch(intent);
//
//        onView(withId(R.id.takePictureButton)).perform(click());
//        onView(withText(R.string.no_picture_from_camera)).check(matches(isDisplayed()));
//        onView(withId(R.id.uploadButton)).check(matches(not(isDisplayed())));
//
//
//    }

//    @Test
//    public void signedInUserInAPerfectWorld(){
//        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
//        CompletableFuture<Void> cf = new CompletableFuture<>();
//        cf.complete(null);
//        when(picturesDb.uploadPicture(anyString(), anyString(), any(), any())).thenReturn(cf);
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TakePictureFragmentFakeActivity.class);;
//        intent.putExtra(TakePictureFragmentFakeActivity.EXPECTED_RESULT, true);
//        ActivityScenario.launch(intent);
//
//        onView(withId(R.id.takePictureButton)).perform(click());
//        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
//        onView(withId(R.id.uploadButton)).perform(click());
//        onView(withId(R.id.uploadButton)).check(matches(not(isDisplayed())));
//
//    }


}
