package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import com.github.wnder.picture.NewPicture;
import com.github.wnder.picture.ReportedPictures;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ReportedPicturesTest {

    private static NewPicture pic;
    private static String userName = "testUser";

    @BeforeClass
    public static void createPic() throws InterruptedException {
        Location loc = new Location("");
        loc.setLatitude(10d);
        loc.setLongitude(5d);
        pic = new NewPicture(userName, loc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        CompletableFuture uploadStatus = pic.sendPictureToDb();
        try{
            // Make sure the picture finishes to upload before proceeding
            uploadStatus.get();
        } catch (Exception e){

        }
        try {
            ReportedPictures.addToReportedPictures(pic.getUniqueId()).get();
        } catch (Exception e){

        }
    }

    @Test
    public void successfullyAddToReportedListAndRetrieve(){
        ReportedPictures.onAllReportedPicturesAvailable((reportedSet) ->{
            assertTrue(reportedSet.contains(pic.getUniqueId()));
        });
    }
}