package com.github.wnder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;

import org.junit.After;
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({PicturesModule.class})
public class HistoryMapActivityTest {
    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), HistoryMapActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, "");
    }

    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(intent));

    @BindValue
    public static PicturesDatabase picturesDatabase = Mockito.mock(PicturesDatabase.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown(){
        Intents.release();
    }

    @BeforeClass
    public static void beforeAll() {
        Bitmap dummyPic = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        when(picturesDatabase.getMapSnapshot(any(), anyString())).thenReturn(CompletableFuture.completedFuture(dummyPic));
    }

    @Test
    public void mapSnapshotIsDisplayed() {
        onView(withId(R.id.mapSnapshot)).check(matches(isDisplayed()));
    }
}
