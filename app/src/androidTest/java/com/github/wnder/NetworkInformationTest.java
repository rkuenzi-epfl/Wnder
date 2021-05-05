package com.github.wnder;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.networkService.NetworkInformation;

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
        NetworkInformation networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        assertThat(networkInfo.isNetworkAvailable(), Matchers.is(true));
    }
}
