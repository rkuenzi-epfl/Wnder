package com.github.wnder;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.scoreboard.ScoreboardActivity;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class ScoreboardActivityTest {

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
    public static void setUp(){
        dummyMap = new HashMap<>();
        dummyMap.put("User0", 32.);
        dummyMap.put("User1", 44.);
        when(picturesDatabase.getScoreboard("toaster")).thenReturn(CompletableFuture.completedFuture(dummyMap));
        intent = new Intent(ApplicationProvider.getApplicationContext(), ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, "toaster");
    }

    @Test
    public void hasCorrectInitialContent(){


        Intents.init();
        onView(withText("User0")).check(matches(isDisplayed()));
        onView(withText("User1")).check(matches(isDisplayed()));
        onView(withText(Double.toString(32))).check(matches(isDisplayed()));
        onView(withText(Double.toString(44))).check(matches(isDisplayed()));
        Intents.release();
    }
}

