package com.github.wnder;

import android.view.View;
import android.widget.SeekBar;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        onView(withText(R.string.no_connection)).perform(pressBack());
        Intents.release();
    }

    @Test
    public void testUploadPictureButton(){
        Intents.init();
        onView(withId(R.id.uploadPictureButton)).perform(click());


        Intents.intended(hasComponent(TakePictureActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testUploadButtonWhenNoInternet(){
        Intents.init();


        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(false);
        onView(withId(R.id.uploadPictureButton)).perform(click());
        onView(withText(R.string.no_connection)).check(matches(isDisplayed()));
        onView(withText(R.string.no_connection)).perform(pressBack());

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
