package com.github.wnder;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.InternalCachePictureDatabase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JUnit4.class)
public class InternalCachePictureDatabaseConnectionTest {
    private static Context context = ApplicationProvider.getApplicationContext();

    private static InternalCachePictureDatabase ICPD;

    @BeforeClass
    public static void setup(){
        ICPD = new InternalCachePictureDatabase(context);
    }

    @Test
    public void isOnlineRetsSameAsNetworkInfo(){
        assertThat(ICPD.isOnline(), is(NetworkInformation.isNetworkAvailable(context)));
    }

}
