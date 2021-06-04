package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.picture.UserModule;
import com.github.wnder.tour.TourActivity;
import com.github.wnder.tour.TourDatabase;
import com.github.wnder.tour.TourModule;
import com.github.wnder.user.UserDatabase;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class, UserModule.class, TourModule.class})
public class TourActivityTest {

    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(TourActivity.class));

    @BindValue
    public static PicturesDatabase picturesDb = Mockito.mock(PicturesDatabase.class);

    @BindValue
    public static UserDatabase userDb = Mockito.mock(UserDatabase.class);

    @BindValue
    public static TourDatabase tourDb = Mockito.mock(TourDatabase.class);

    @BeforeClass
    public static void setUp() {
        List<String> tours = Arrays.asList("tour1", "tour2");
        when(userDb.getTourListForUser(any())).thenReturn(CompletableFuture.completedFuture(tours));

        List<String> tour1Pics = Arrays.asList("pic1", "pic2");
        when(tourDb.getTourPics(eq("tour1"))).thenReturn(CompletableFuture.completedFuture(tour1Pics));

        Bitmap pic1Bmp = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        when(picturesDb.getBitmap(eq("pic1"))).thenReturn(CompletableFuture.completedFuture(pic1Bmp));

        List<String> tour2Pics = Arrays.asList("pic3", "pic4", "pic5");
        when(tourDb.getTourPics(eq("tour2"))).thenReturn(CompletableFuture.completedFuture(tour2Pics));

        Bitmap pic3Bmp = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.picture1);
        when(picturesDb.getBitmap(eq("pic3"))).thenReturn(CompletableFuture.completedFuture(pic3Bmp));

        when(tourDb.getTourName(eq("tour1"))).thenReturn(CompletableFuture.completedFuture("Tour 1"));
        when(tourDb.getTourName(eq("tour2"))).thenReturn(CompletableFuture.completedFuture("Tour 2"));

        when(tourDb.getTourDistance(eq("tour1"), any())).thenReturn(CompletableFuture.completedFuture(24000.0));
        when(tourDb.getTourDistance(eq("tour2"), any())).thenReturn(CompletableFuture.completedFuture(42000.0));

        when(tourDb.getTourLength(eq("tour1"))).thenReturn(CompletableFuture.completedFuture(25000.0));
        when(tourDb.getTourLength(eq("tour2"))).thenReturn(CompletableFuture.completedFuture(52000.0));

        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void hasCorrectContent() {
        onView(withText("Tour 1")).check(matches(isDisplayed()));
        onView(withText("Tour 2")).check(matches(isDisplayed()));

        /* Disabled because it doesn't work on cirrus
        // Number of pictures
        onView(withText("2")).check(matches(isDisplayed()));
        onView(withText("3")).check(matches(isDisplayed()));

        // Distance to first picture
        onView(withText("24 km")).check(matches(isDisplayed()));
        onView(withText("42 km")).check(matches(isDisplayed()));

        // Length of tour
        onView(withText("25 km")).check(matches(isDisplayed()));
        onView(withText("52 km")).check(matches(isDisplayed()));*/
    }
}
