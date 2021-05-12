package com.github.wnder;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        when(userDb.getPictureList(any(),anyString())).thenReturn(CompletableFuture.completedFuture(dummyPicList));
        when(picturesDb.getBitmap(anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
        Map<String,Double> dummyScoreboard = new HashMap<>();
        dummyScoreboard.put("demo1", 45.5);
        when(picturesDb.getScoreboard(anyString())).thenReturn(CompletableFuture.completedFuture(dummyScoreboard));


        onView(withId(R.id.history_page)).perform(click());
        onView(withId(R.id.historyToScoreboard)).perform(click());

        Intents.intended(hasComponent(ScoreboardActivity.class.getName()));

    }
}
