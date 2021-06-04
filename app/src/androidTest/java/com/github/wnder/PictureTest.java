package com.github.wnder;

import android.os.Parcel;

import com.github.wnder.picture.Picture;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class PictureTest {
    private static String uniqueId;
    private static double picLat;
    private static double picLng;

    private static Picture pic;

    @BeforeClass
    public static void setup() throws ExecutionException, InterruptedException {
        uniqueId = "testPic";
        picLat = 10;
        picLng = 20;

        pic = new Picture(uniqueId, picLat, picLng);
    }

    @Test
    public void getUniqueIdWorks(){
        assertThat(pic.getUniqueId(), is(uniqueId));
    }

    @Test
    public void getPicLatWorks() {
        assertThat(pic.getPicLat(), is(picLat));
    }

    @Test
    public void getPicLngWorks() {
        assertThat(pic.getPicLng(), is(picLng));
    }

    @Test
    public void parcelablePicTest() {
        Parcel parcel = Parcel.obtain();
        pic.writeToParcel(parcel, pic.describeContents());

        parcel.setDataPosition(0);

        Picture createdFromParcel = Picture.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel.getUniqueId(), is(uniqueId));
        assertThat(createdFromParcel.getPicLat(), is(picLat));
        assertThat(createdFromParcel.getPicLng(), is(picLng));
    }
}