package com.github.wnder;

import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.*;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);


    // Open the activity as a test because there is nothing in the activity yet
    @Test
    public void testEmptyMain(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        scenario.close();
    }

    @Test
    public void testGetPictureButton(){
        Intents.init();
        onView(withId(R.id.getPictureButton)).perform(click());

        assertTrue(GlobalUser.getUser().getLocation() != null);
        GlobalUser.resetUser();
        Intents.intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testUploadPictureButton(){
        Intents.init();
        onView(withId(R.id.uploadPictureButton)).perform(click());

        Intents.intended(hasComponent(ImageFromGalleryActivity.class.getName()));
        assertTrue(GlobalUser.getUser().getLocation() != null);

        Intents.release();
    }

    private static ViewAction setProgressAndAssert(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }

            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    @Test
    public void testRadius(){
        
        Intents.init();
        User u = GlobalUser.getUser();
        onView(withId(R.id.radiusSeekBar)).perform(setProgressAndAssert(0));
        assertThat(u.getRadius(), is(5));

        onView(withId(R.id.radiusTextView)).check(matches(withText("Radius: 5km")));

        GlobalUser.resetUser();
        Intents.release();
    }
}
