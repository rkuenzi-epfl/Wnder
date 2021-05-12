package com.github.wnder;

import android.view.View;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

@HiltAndroidTest
@UninstallModules({NetworkModule.class})
public class NavigationActivityTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkService.class);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @Before
    public void before(){
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void guessButtonShowsSeekbar(){
        onView(withId(R.id.guess_page)).perform(ViewActions.click());
        onView(withText("Radius: 5km")).check(matches(isDisplayed()));
    }

}
