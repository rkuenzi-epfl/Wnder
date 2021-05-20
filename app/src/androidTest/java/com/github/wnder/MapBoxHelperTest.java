package com.github.wnder;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.guessLocation.MapBoxHelper;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(JUnit4.class)
public class MapBoxHelperTest {
    private static Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void onMapSnapshotAvailableWorks() {
        LatLng guessLatLng = new LatLng(2, 4);
        LatLng pictureLatLng = new LatLng(3, 5);
        MapBoxHelper.onMapSnapshotAvailable(context, guessLatLng, pictureLatLng, (mapSnapshot) -> {
            assertThat(mapSnapshot, notNullValue());
        });
    }
}
