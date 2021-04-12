package com.github.wnder;

import android.content.Context;
import android.net.Uri;


import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.user.GlobalUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.gms.tasks.Tasks.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> testRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @BeforeClass
    public static void disconnectWhenNecessary(){

        // Get application context for google sign in steps
        Context ctx = ApplicationProvider.getApplicationContext();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(ctx, gso);
        Task<Void> taskWaiter;
        if(GoogleSignIn.getLastSignedInAccount(ctx) != null){
            taskWaiter = client.signOut();
            try {
                // Wait maximum of 30 seconds, otherwise assume the user was disconnected successfully
                await(taskWaiter, 30, TimeUnit.SECONDS);
            } catch (Exception e){
                System.out.println("Assuming disconnection succeeded");
            }
        }

    }

    @Test
    public void testNoLogin(){

        onView(withId(R.id.textLogin)).check(matches(withText("Please sign in.")));
    }

    @Test
    public void testButtonPress(){
        onView(withId(R.id.signInButton)).perform(click());
    }

    @Test
    public void testGuestButton(){
        onView(withId(R.id.guestButton)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
        assertThat(GlobalUser.getUser().getName(), is("Guest"));
        assertThat(GlobalUser.getUser().getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
    }
}
