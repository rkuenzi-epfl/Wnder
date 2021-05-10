package com.github.wnder;

import android.app.ActivityGroup;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.InternalCachePictureDatabase;
import com.github.wnder.picture.LocalPicture;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.internal.cache.InternalCache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


//Those tests are essentially the same as the ones in LocalPictureDatabaseTest, maybe deleting it would be better
//TODO delete this later
@RunWith(JUnit4.class)
public class InternalCachePictureDatabaseOfflineTest {
    private static FirebasePicturesDatabase fdb = new FirebasePicturesDatabase();
    private static Context context = ApplicationProvider.getApplicationContext();

    private static String uniqueId;
    private static Bitmap bmp;
    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    private static LocalPicture picture;

    private static File iDirectory;
    private static File mDirectory;

    private static InternalCachePictureDatabase ICPD;

    @BeforeClass
    public static void setup() throws ExecutionException, InterruptedException {
        //Mock offline check
        ICPD = Mockito.spy(new InternalCachePictureDatabase(context));
        Mockito.doReturn(false).when(ICPD).isOnline();

        //SETUP TEST IMAGE
        uniqueId = "testPicDontRm";
        bmp = fdb.getBitmap("testPicDontRm").get();
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

        picture = new LocalPicture(uniqueId, bmp, realLoc, guessLoc, scoreboard);

        ICPD.storePictureLocally(picture);
        //asserts ensuring both files were correctly created
        assertThat(iDirectory.listFiles().length, is(currILength + 1));
        assertThat(mDirectory.listFiles().length, is(currDLength + 1));
    }

    @AfterClass
    public static void teardown(){
        int currILength = iDirectory.listFiles().length;
        int currDLength = mDirectory.listFiles().length;
        ICPD.deleteLocalPicture(uniqueId);
        //Asserts ensuring both files were correctly deleted
        assertThat(iDirectory.listFiles().length, is(currILength - 1));
        assertThat(mDirectory.listFiles().length, is(currDLength - 1));
    }

    @Test
    public void updateAndGetScoreboardWork() throws ExecutionException, InterruptedException {
        Map<String, Double> scoreboard = ICPD.getScoreboard(uniqueId).get();
        assertThat(scoreboard.get("testUser"), is(200.));

        scoreboard.put("testUser", 150.);
        ICPD.updateLocalScoreboard(uniqueId, scoreboard);

        Map<String, Double> newScoreboard = ICPD.getScoreboard(uniqueId).get();
        assertThat(newScoreboard.get("testUser"), is(150.));
    }

    @Test
    public void getLocationWorks() throws ExecutionException, InterruptedException {
        Location storedLoc = ICPD.getLocation(uniqueId).get();
        assertThat(storedLoc.getLongitude(), is(realLoc.getLongitude()));
        assertThat(storedLoc.getLatitude(), is(realLoc.getLatitude()));
    }

    @Test
    public void getGuessedLocationWorks(){
        Location storedLoc = ICPD.getLocalGuessedLocation(uniqueId);
        assertThat(storedLoc.getLongitude(), is(guessLoc.getLongitude()));
        assertThat(storedLoc.getLatitude(), is(guessLoc.getLatitude()));
    }

    @Test
    public void getPictureWorks() throws ExecutionException, InterruptedException {
        Bitmap readFile = ICPD.getBitmap(uniqueId).get();
        int w1 = bmp.getWidth();
        int w2 = readFile.getWidth();
        assertThat(w1, is(w2));
    }

    @Test
    public void getApproximateLocationThrows(){

        assertTrue(ICPD.getApproximateLocation(uniqueId).isCompletedExceptionally());
    }

    @Test
    public void getUserGuessesThrows(){
        assertTrue(ICPD.getUserGuesses(uniqueId).isCompletedExceptionally());
    }

    @Test
    public void sendUserGuessesThrows(){
        assertTrue(ICPD.sendUserGuess(uniqueId, "testUser", realLoc).isCompletedExceptionally());

    }

    @Test
    public void uploadPictureThrows(){
        assertTrue(ICPD.uploadPicture(uniqueId, "testUser", realLoc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)).isCompletedExceptionally());

    }

    @Test
    public void getKarmaThrows(){
        try {
            ICPD.getKarma(uniqueId);
            fail();
        }
        catch(IllegalStateException e){
            assertTrue(true);
        }
    }

    @Test
    public void updateKarmaThrows(){

        assertTrue(ICPD.updateKarma(uniqueId, 0).isCompletedExceptionally());

    }
}
