package com.github.wnder;

import android.content.Intent;
import android.location.Location;
import android.widget.TableLayout;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.TreeMap;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ScoreboardActivityTest {
    private static final String TEST_PIC_ID = "picture1";

    private class FakePic implements Picture{

        @Override
        public String getUniqueId() {
            return TEST_PIC_ID;
        }

        @Override
        public Location getLocation() {
            return new Location("");
        }

        @Override
        public Map<String, Object> getScoreboard() {
            Map<String, Object> scoreboard = new TreeMap<>();
            scoreboard.put("User1", 15d);
            scoreboard.put("User3", 5d);
            scoreboard.put("User2", 50d);
            return scoreboard;
        }

        @Override
        public Map<String, Object> getGuesses() {
            return null;
        }
    }

    @Test
    public void leaveButtonFinishesActivity(){

        Intent intent = intent = new Intent(ApplicationProvider.getApplicationContext(), ScoreboardActivity.class);
        try (ActivityScenario<ScoreboardActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.leaveScoreboardButton)).perform(click());
            assertThat(scenario.getState(), is(Lifecycle.State.DESTROYED));
        }
    }

    @Test
    public void scoreboardFillsCorrectly() {
        Picture pic = new FakePic();
        PictureCache.addPicture(TEST_PIC_ID, pic);
        Intent intent = intent = new Intent(ApplicationProvider.getApplicationContext(), ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, TEST_PIC_ID);
        try (ActivityScenario<ScoreboardActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withText("User1")).check(matches(isDisplayed()));
            onView(withText("User2")).check(matches(isDisplayed()));
            onView(withText("User3")).check(matches(isDisplayed()));
        }

    }

    @Test
    public void failingGetDisplaySnackbar(){

        Intent intent = intent = new Intent(ApplicationProvider.getApplicationContext(), ScoreboardActivity.class);
        try (ActivityScenario<ScoreboardActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withText(ScoreboardActivity.LOADING_FAILED)).check(matches(isDisplayed()));

        }

    }

}
