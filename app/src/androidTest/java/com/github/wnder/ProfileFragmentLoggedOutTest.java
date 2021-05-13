package com.github.wnder;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.SignedInUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.gms.tasks.Tasks.await;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@HiltAndroidTest
@UninstallModules({NetworkModule.class})
public class ProfileFragmentLoggedOutTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @Before
    public void setup(){
        GlobalUser.resetUser();
        Intents.init();
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
        onView(withId(R.id.profile_page)).perform(click());
    }


    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void signInButtonShows(){
        onView(withId(R.id.signInButton)).check(matches(isDisplayed()));
    }

    @Test
    public void logoutButtonDoesntShow(){
        onView(withId(R.id.logoutButton)).check(matches(not(isDisplayed())));
    }

    @Test
    public void noConnectionTextShows(){
        onView(withId(R.id.no_connection_text)).check(matches(isDisplayed()));
    }

    @Test
    public void statsDoesntShow(){
        onView(withId(R.id.nbrOfGuessesCard)).check(matches(not(isDisplayed())));
        onView(withId(R.id.averageScoreCard)).check(matches(not(isDisplayed())));
        onView(withId(R.id.totalScoreCard)).check(matches(not(isDisplayed())));
    }

    @Test
    public void profilePicShow(){
        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));
    }

    @Test
    public void GuestNameIsDisplayed(){
        onView(withId(R.id.username)).check(matches(withText("Guest")));
    }
}
