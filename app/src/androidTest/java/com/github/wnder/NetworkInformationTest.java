package com.github.wnder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NetworkInformationTest {
    @Test
    public void isNetworkAvailableWorksWhenThereIsConnection(){
        Context context = ApplicationProvider.getApplicationContext();

        assertThat(NetworkInformation.isNetworkAvailable(context), Matchers.is(true));
    }
}
