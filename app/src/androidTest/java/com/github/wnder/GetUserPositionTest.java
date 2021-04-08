package com.github.wnder;


import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.maps.model.LatLng;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

import static androidx.core.content.ContextCompat.startActivity;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class GetUserPositionTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule1 = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setup(){
        /*Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(InstrumentationRegistry.getInstrumentation().getTargetContext(), i, null);*/
        //Settings.Secure.getString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //with(getInstrumentation().uiAutomation) {
            //...
                InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("appops set " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() + " android:mock_location allow");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //...
            //}
        }
    }

    @Test
    public void PositionIsWithinRange(){

        LocationManager locationManager = (LocationManager) InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        User user = new SignedInUser("test", null);

        Looper.prepare();

        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, false, false, 1, 1);
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(50);
        location.setLongitude(30);
        location.setAltitude(3f);
        location.setTime(System.currentTimeMillis());
        location.setSpeed(0.01f);
        location.setBearing(1f);
        location.setAccuracy(3f);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);



        Location l1 = user.getPositionFromGPS(locationManager, InstrumentationRegistry.getInstrumentation().getTargetContext());

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        location.setLatitude(60);
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
        Location l2 = user.getPositionFromGPS(locationManager, InstrumentationRegistry.getInstrumentation().getTargetContext());

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //Looper.loop();

        //Looper.myLooper().quitSafely();

        assertThat(l1.getLatitude(), Matchers.is(50.0));
        assertThat(l2.getLatitude(), Matchers.is(60.0));
        //assertThat(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), Matchers.is(60.0));
    }
}
