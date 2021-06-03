package com.github.wnder;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.UploadInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UploadInfoTest {

    @Test
    public void storeLoadDelete(){
        File infoDir = ApplicationProvider.getApplicationContext().getDir("active_uploads_tests", Context.MODE_PRIVATE);
        File file = new File(infoDir, "someTestId");
        Location location = new Location("");
        location.setLatitude(10);
        location.setLongitude(15);
        try{

            UploadInfo.storeUploadInfo(file, new UploadInfo("randomName", location, Uri.parse("some/String/probably")));
        } catch (Exception e){

        }
        UploadInfo info = UploadInfo.loadUploadInfo(file);
        assertThat(info.getUserUid(), is("randomName"));
        Location loc = info.getLocation();
        assertThat(loc.getLatitude(), is(location.getLatitude()));
        assertThat(loc.getLongitude(), is(location.getLongitude()));
        assertThat(info.getPictureUri(), is(Uri.parse("some/String/probably")));

        assertTrue(UploadInfo.deleteUploadInfo(file));
    }
}
