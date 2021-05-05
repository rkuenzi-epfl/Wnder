package com.github.wnder;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkModule;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.InternalCachePictureDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.UninstallModules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@HiltAndroidTest
@UninstallModules({NetworkModule.class})
public class InternalCachePictureDatabaseConnectionTest {
    private HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    private static Context context = ApplicationProvider.getApplicationContext();

    private static InternalCachePictureDatabase ICPD;

    @BindValue
    public static NetworkService networkInfo = Mockito.mock(NetworkInformation.class);

    @Before
    public void before(){
        Mockito.when(networkInfo.isNetworkAvailable()).thenReturn(true);
    }

    @BeforeClass
    public static void setup(){
        ICPD = new InternalCachePictureDatabase(context);
    }

    @Test
    public void isOnlineRetsSameAsNetworkInfo(){
        assertThat(ICPD.isOnline(), is(networkInfo.isNetworkAvailable()));
    }

}
