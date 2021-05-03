package com.github.wnder;

import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.PicturesModule;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@HiltAndroidTest
@UninstallModules({NetworkModule.class})
public class MainActivityTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(MainActivity.class));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @Before
    public void before(){
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void testGetPictureButton(){
        Intents.init();
        onView(withId(R.id.getPictureButton)).perform(click());

        GlobalUser.resetUser();
        intended(hasComponent(GuessPreviewActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testGetPictureButtonWhenNoInternet(){
        Intents.init();


        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(false);
        onView(withId(R.id.getPictureButton)).perform(click());
        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));

        Intents.release();
    }

    @Test
    public void testUploadPictureButton(){
        Intents.init();
        onView(withId(R.id.uploadPictureButton)).perform(click());


        intended(hasComponent(TakePictureActivity.class.getName()));
        assertTrue(GlobalUser.getUser().getLocation() != null);

        Intents.release();
    }

    @Test
    public void testUploadButtonWhenNoInternet(){
        Intents.init();


        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(false);
        onView(withId(R.id.uploadPictureButton)).perform(click());
        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));

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

    @Test
    public void testHistoryButton(){
        Intents.init();
        onView(withId(R.id.menuToHistoryButton)).perform(click());

        intended(hasComponent(HistoryActivity.class.getName()));

        Intents.release();
    }
}
