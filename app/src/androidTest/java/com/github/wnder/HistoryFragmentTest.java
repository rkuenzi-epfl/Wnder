package com.github.wnder;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.picture.UserModule;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.UserDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class, UserModule.class, NetworkModule.class})
public class HistoryFragmentTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkService.class);

    @BindValue
    public static PicturesDatabase picturesDb = Mockito.mock(PicturesDatabase.class);

    @BindValue
    public static UserDatabase userDb = Mockito.mock(UserDatabase.class);


    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void historyScoreboardIsClickable(){

        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        List<String> dummyPicList = new ArrayList<>();
        dummyPicList.add("demo1");
        Map<String,Double> dummyScoreboard = new HashMap<>();
        dummyScoreboard.put("user", 45.5);
        Location dummyLocation = new Location("");
        dummyLocation.setLatitude(32);
        dummyLocation.setLongitude(2);
        Map<String,Location> dummyLocationList = new HashMap<>();
        Location dummyLocation2 = new Location("");
        dummyLocation2.setLatitude(32);
        dummyLocation2.setLongitude(4);
        dummyLocationList.put("user", dummyLocation2);

        when(userDb.getPictureList(any(),anyString())).thenReturn(CompletableFuture.completedFuture(dummyPicList));
        when(picturesDb.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        when(picturesDb.getScoreboard(anyString())).thenReturn(CompletableFuture.completedFuture(dummyScoreboard));
        when(picturesDb.getLocation(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLocation));
        when(picturesDb.getUserGuesses(anyString())).thenReturn(CompletableFuture.completedFuture(dummyLocationList));



        onView(withId(R.id.history_page)).perform(click());
        onView(withId(R.id.historyToScoreboard)).perform(click());

        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));

    }
}
