package com.github.wnder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.LocalPicture;
import com.github.wnder.picture.LocalPictureDatabase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JUnit4.class)
public class LocalPictureDatabaseTest {
    private static Context context = ApplicationProvider.getApplicationContext();
    private static LocalPictureDatabase LPD = new LocalPictureDatabase(context);

    private static String uniqueId;
    private static Bitmap bmp;
    private static Bitmap mapSnapshot;
    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    private static LocalPicture picture;

    private static File iDirectory;
    private static File mDirectory;

    @BeforeClass
    public static void setup() throws ExecutionException, InterruptedException {
        // Setup test picture
        uniqueId = "testPic";
        bmp = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.ladiag);
        mapSnapshot = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.picture1);
        realLoc = new Location("");
        realLoc.setLongitude(1);
        realLoc.setLatitude(0);
        guessLoc = new Location("");
        guessLoc.setLongitude(11);
        guessLoc.setLatitude(10);
        scoreboard = new HashMap<>();
        scoreboard.put("testUser", 200.);
        iDirectory = context.getDir("images", Context.MODE_PRIVATE);
        mDirectory = context.getDir("metadata", Context.MODE_PRIVATE);

        int currILength = iDirectory.listFiles().length;
        int currDLength = mDirectory.listFiles().length;

        picture = new LocalPicture(uniqueId, bmp, mapSnapshot, realLoc, guessLoc, scoreboard);
        LPD.storePicture(picture);
        //asserts ensuring both files were correctly created
        assertThat(iDirectory.listFiles().length, is(currILength + 1));
        assertThat(mDirectory.listFiles().length, is(currDLength + 1));

    }

    @AfterClass
    public static void delete(){
        int currILength = iDirectory.listFiles().length;
        int currDLength = mDirectory.listFiles().length;
        LPD.deletePicture(uniqueId);
        //Asserts ensuring both files were correctly deleted
        assertThat(iDirectory.listFiles().length, is(currILength - 1));
        assertThat(mDirectory.listFiles().length, is(currDLength - 1));
    }

    @Test
    public void updateAndGetScoreboardWorks(){
        Map<String, Double> scoreboard = LPD.getScoreboard(uniqueId);
        assertThat(scoreboard.get("testUser"), is(200.));

        scoreboard.put("testUser", 150.);
        LPD.updateScoreboard(uniqueId, scoreboard);

        Map<String, Double> newScoreboard = LPD.getScoreboard(uniqueId);
        assertThat(newScoreboard.get("testUser"), is(150.));
    }

    @Test
    public void getLocationWorks(){
        Location storedLoc = LPD.getLocation(uniqueId);
        assertThat(storedLoc.getLongitude(), is(realLoc.getLongitude()));
        assertThat(storedLoc.getLatitude(), is(realLoc.getLatitude()));
    }

    @Test
    public void getGuessedLocationWorks(){
        Location storedLoc = LPD.getGuessedLocation(uniqueId);
        assertThat(storedLoc.getLongitude(), is(guessLoc.getLongitude()));
        assertThat(storedLoc.getLatitude(), is(guessLoc.getLatitude()));
    }

    @Test
    public void getBitmapWorks() throws FileNotFoundException {
        Bitmap storedBmp = LPD.getBitmap(uniqueId);
        assert(storedBmp.sameAs(bmp));
    }

    @Test
    public void getMapSnapshotWorks() throws FileNotFoundException {
        Bitmap storedMapSnapshot = LPD.getMapSnapshot(uniqueId);
        assert(storedMapSnapshot.sameAs(mapSnapshot));
    }
}
