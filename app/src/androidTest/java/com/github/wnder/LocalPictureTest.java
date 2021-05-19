package com.github.wnder;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.LocalPicture;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JUnit4.class)
public class LocalPictureTest {

    private static FirebasePicturesDatabase db;

    private static String uniqueId;
    private static Bitmap bmp;
    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    private static LocalPicture pic;

    @BeforeClass
    public static void setup() throws ExecutionException, InterruptedException {
        db = new FirebasePicturesDatabase(ApplicationProvider.getApplicationContext());
        uniqueId = "testPic";
        bmp = db.getBitmap("testPicDontRm").get();
        realLoc = new Location("");
        realLoc.setLongitude(0);
        realLoc.setLatitude(1);
        guessLoc = new Location("");
        guessLoc.setLongitude(10);
        guessLoc.setLatitude(11);
        scoreboard = new HashMap<>();
        scoreboard.put("testUser", 200.);

        pic = new LocalPicture(uniqueId, bmp, realLoc, guessLoc, scoreboard);
    }

    @Test
    public void getUniqueIdWorks(){
        assertThat(pic.getUniqueId(), is("testPic"));
    }

    @Test
    public void getBitmapWorks(){
        //Compare what's comparable
        assertThat(pic.getBitmap().getWidth(), is(bmp.getWidth()));
    }

    @Test
    public void getRealLocationWorks(){
        Location rL = pic.getRealLocation();
        assertThat(rL.getLongitude(), is(realLoc.getLongitude()));
        assertThat(rL.getLatitude(), is(realLoc.getLatitude()));
    }

    @Test
    public void getGuessLocationWorks(){
        Location gL = pic.getGuessLocation();
        assertThat(gL.getLongitude(), is(guessLoc.getLongitude()));
        assertThat(gL.getLatitude(), is(guessLoc.getLatitude()));
    }

    @Test
    public void getScoreboardWorks(){
        Map<String, Double> s = pic.getScoreboard();
        assertThat(s.get("testUser"), is(scoreboard.get("testUser")));
    }
}
