package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.LocalPicture;
import com.github.wnder.picture.LocalPictureDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.util.HashMap;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static java.lang.Thread.sleep;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@HiltAndroidTest
@UninstallModules({NetworkModule.class})
public class HistoryActivityTest {

    private static LocalPictureDatabase localPicDb = new LocalPictureDatabase(ApplicationProvider.getApplicationContext());

    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule
    public RuleChain testRule = RuleChain.outerRule(hiltRule)
            .around(new ActivityScenarioRule<>(HistoryActivity.class));

    @BindValue
    public static NetworkService networkInfo = mock(NetworkInformation.class);

    @BeforeClass
    public static void beforeAll(){
        when(networkInfo.isNetworkAvailable()).thenReturn(false);
        Location loc = new Location("");
        loc.setLatitude(10);
        loc.setLongitude(15);
        Bitmap bmp = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        LocalPicture pic =  new LocalPicture("testPic", bmp, bmp, loc, loc, new HashMap<>());
        localPicDb.storePicture(pic);
    }

    @AfterClass
    public static void afterAll(){
        localPicDb.deletePicture("testPic");
    }

    //For activities that we did ourself otherwise need to use mockito
    @Before
    //Initializes Intents and begins recording intents, similar to MockitoAnnotations.initMocks.
    public void setUp() {
        GlobalUser.setUser(new GuestUser());
        Intents.init();
    }

    @After //Clears Intents state. Must be called after each test case.
    public void tearDown() {
        Intents.release();
    }

//    @Test
//    public void textIsDisplayed(){
//        // Verifiy that we can click the button
//        onView(withId(R.id.leftHistory)).perform(click());
//    }
}
