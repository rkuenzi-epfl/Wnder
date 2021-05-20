package com.github.wnder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.InternalCachePictureDatabase;
import com.github.wnder.picture.LocalPicture;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


//Those tests are essentially the same as the ones in LocalPictureDatabaseTest, maybe deleting it would be better
//TODO delete this later
@RunWith(JUnit4.class)
public class InternalCachePictureDatabaseOfflineTest {
    private static Context context = ApplicationProvider.getApplicationContext();

    private static String uniqueId;
    private static Bitmap bmp;
    private static Bitmap mapSnapshot;
    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    private static LocalPicture picture;

    private static File iDirectory;
    private static File mDirectory;

    private static InternalCachePictureDatabase ICPD;

    @BeforeClass
    public static void setup() {
        // Mock offline check
        ICPD = Mockito.spy(new InternalCachePictureDatabase(context));
        Mockito.doReturn(false).when(ICPD).isOnline();

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
    public void getUserGuessWorks() throws ExecutionException, InterruptedException {
        Location storedLoc = ICPD.getUserGuess(uniqueId).get();
        assertThat(storedLoc.getLongitude(), is(guessLoc.getLongitude()));
        assertThat(storedLoc.getLatitude(), is(guessLoc.getLatitude()));
    }

    @Test
    public void getBitmapWorks() throws ExecutionException, InterruptedException {
        Bitmap storedBmp = ICPD.getBitmap(uniqueId).get();
        assert(storedBmp.sameAs(bmp));
    }

    @Test
    public void getMapSnapshotWorks() throws ExecutionException, InterruptedException {
        Bitmap storedMapSnapshot = ICPD.getMapSnapshot(null, uniqueId).get();
        assert(storedMapSnapshot.sameAs(mapSnapshot));
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
        assertTrue(ICPD.sendUserGuess(uniqueId, "testUser", realLoc, mapSnapshot).isCompletedExceptionally());
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
