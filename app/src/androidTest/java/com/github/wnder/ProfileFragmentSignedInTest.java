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
public class ProfileFragmentSignedInTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(NavigationActivity.class));

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @BeforeClass
    public static void setupUser(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
    }

    @Before
    public void setup(){
        Intents.init();
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
        onView(withId(R.id.profile_page)).perform(click());
    }


    @After
    public void tearDown() {
        //Check that logout works
        onView(withId(R.id.logoutButton)).perform(click());
        assertTrue(GlobalUser.getUser() instanceof GuestUser);
        Intents.release();
    }

    @Test
    public void everythingIsInPlace(){
        onView(withId(R.id.signInButton)).check(matches(not(isDisplayed())));
        onView(withId(R.id.logoutButton)).check(matches(isDisplayed()));
        onView(withId(R.id.no_connection_text)).check(matches(not(isDisplayed())));
        onView(withId(R.id.nbrOfGuessesCard)).check(matches(isDisplayed()));
        onView(withId(R.id.averageScoreCard)).check(matches(isDisplayed()));
        onView(withId(R.id.totalScoreCard)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));
        onView(withId(R.id.username)).check(matches(withText("testUser")));
    }

}
