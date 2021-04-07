package com.github.wnder;

import android.graphics.Bitmap;
import android.location.Location;

import com.github.wnder.picture.ExistingPicture;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

@RunWith(JUnit4.class)
public class ExistingPictureTesting {

    private static ExistingPicture testPic;


    @BeforeClass
    public static void getTestPic(){
        testPic = new ExistingPicture("picture1");
        Location loc = new Location("");
        loc.setLatitude(22d);
        loc.setLongitude(44d);
        CompletableFuture guessSentResult = testPic.sendUserGuess("testUser", loc);
        try{
            // Make sure the picture finishes to upload before proceeding
            guessSentResult.get();
        } catch (Exception e){

        }
    }

    @Test
    public void onLocationAvailableWorksForExistingPicture(){
        testPic.onLocationAvailable((location)->{
            assertThat(location.getLatitude(), is(10d));
            assertThat(location.getLongitude(), is(10d));
        });
    }

    @Test
    public void onBitmapAvailableWorks(){
        testPic.onBitmapAvailable((bmp)->{
           assertTrue(bmp instanceof Bitmap);
        });

        // Verify that it is properly accessible once it's stored in the object
        testPic.onBitmapAvailable((bmp)->{
            assertTrue(bmp instanceof Bitmap);
        });
    }

    @Test
    public void userGuessIsProperlyStored(){

        testPic.onUpdatedGuessesAvailable((userGuesses)->{
            assertTrue(userGuesses.containsKey("testUser"));
            assertThat(userGuesses.get("testUser").getLatitude(), is(22d));
            assertThat(userGuesses.get("testUser").getLongitude(), is(44d));
        });

        testPic.onUpdatedScoreboardAvailable((scoreboard)->{
            assertTrue(scoreboard.containsKey("testUser"));
            assertThat(scoreboard.get("testUser"), is(Score.computeScore(10d,10d,22d,44d)));

        });
    }

    @Test
    public void scoreboardCorrectlyInitialized(){
        testPic.onUpdatedScoreboardAvailable((scoreboard)->{
            assertTrue(scoreboard.containsKey("user1"));
            assertTrue(scoreboard.containsKey("user3"));
            assertThat(scoreboard.get("user2"), is(13d));
            assertThat(scoreboard.get("user4"), is(9d));

        });
    }

    @Test
    public void guessesCorrectlyInitialized(){
        testPic.onUpdatedGuessesAvailable((userGuesses)->{
            assertTrue(userGuesses.containsKey("user0"));
            assertThat(userGuesses.get("user0").getLatitude(), is(10d));
            assertThat(userGuesses.get("user0").getLongitude(), is(10d));
        });
    }
//
//    @Test
//    public void computeScoreAndSendToDbWorks() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
//        sleep(1000);
//        assertEquals(200, score, 10);
//    }
//
//    @Test
//    public void getUserScoreAndGuessWork() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
//        assertEquals(200, (Double)pic.getUserScore("testUser"), 10);
//        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(0) == 10.);
//        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(1) == 10.);
//    }
//
//    @Test
//    public void getUniqueIdWorks() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        assertTrue(pic.getUniqueId().equals("picture1"));
//    }
//
//    @Test
//    public void getBmpReturnsBitmap() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        sleep(3000);
//        assertTrue(pic.getBmp() instanceof Bitmap);
//    }
//
//    @Test
//    public void getLatLng() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        Location latlng = pic.getLocation();
//        assertEquals(10, latlng.getLatitude(), 0);
//        assertEquals(10, latlng.getLongitude(), 0);
//    }
//
//    @Test
//    public void getScoreboardWorks() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
//        sleep(5000);
//        Map<String, Object> scoreboard = pic.getScoreboard();
//        Double userScore = (double)scoreboard.getOrDefault("testUser", -1);
//        assertEquals(userScore, score);
//    }
//
//    @Test
//    public void getGuessesWorks() throws InterruptedException {
//        ExistingPicture pic = testPic;
//        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
//        sleep(5000);
//        Map<String, Object> guesses = pic.getGuesses();
//        Object guess = guesses.get("testUser");
//        assertTrue(((ArrayList<Double>) guess).get(0) == 10.);
//        assertTrue(((ArrayList<Double>) guess).get(1) == 10.);
//    }
//
//    @Test
//    public void loadingCachedPictureSuccessfullyReturnCompletedFuture(){
//        assertTrue(ExistingPicture.loadExistingPicture(testPic.getUniqueId()).isDone());
//    }

}
