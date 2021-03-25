package com.github.wnder;

import android.graphics.Bitmap;
import android.location.Location;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ExistingPictureTesting {

    private static ExistingPicture testPic;

    @BeforeClass
    public static void getTestPic(){
        try{
            // Usually you'd use "thenApply()" to run another function when the future complete
            // Here with get() we want to wait for it to complete then test the class
            testPic = ExistingPicture.loadExistingPicture("picture1").get();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void computeScoreAndSendToDbWorks() throws InterruptedException {
        ExistingPicture pic = testPic;
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(1000);
        assertEquals(200, score, 10);
    }

    @Test
    public void getUserScoreAndGuessWork() throws InterruptedException {
        ExistingPicture pic = testPic;
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        assertEquals(200, (Double)pic.getUserScore("testUser"), 10);
        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(0) == 10.);
        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(1) == 10.);
    }

    @Test
    public void getUniqueIdWorks() throws InterruptedException {
        ExistingPicture pic = testPic;
        assertTrue(pic.getUniqueId().equals("picture1"));
    }

    @Test
    public void getBmpReturnsBitmap() throws InterruptedException {
        ExistingPicture pic = testPic;
        sleep(3000);
        assertTrue(pic.getBmp() instanceof Bitmap);
    }

    @Test
    public void getLatLng() throws InterruptedException {
        ExistingPicture pic = testPic;
        Location latlng = pic.getLocation();
        assertEquals(10, latlng.getLatitude(), 0);
        assertEquals(10, latlng.getLongitude(), 0);
    }

    @Test
    public void getScoreboardWorks() throws InterruptedException {
        ExistingPicture pic = testPic;
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(5000);
        Map<String, Object> scoreboard = pic.getScoreboard();
        Double userScore = (double)scoreboard.getOrDefault("testUser", -1);
        assertEquals(userScore, score);
    }

    @Test
    public void getGuessesWorks() throws InterruptedException {
        ExistingPicture pic = testPic;
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(5000);
        Map<String, Object> guesses = pic.getGuesses();
        Object guess = guesses.get("testUser");
        assertTrue(((ArrayList<Double>) guess).get(0) == 10.);
        assertTrue(((ArrayList<Double>) guess).get(1) == 10.);
    }

    @Test
    public void loadingCachedPictureSuccessfullyReturnCompletedFuture(){
        assertTrue(ExistingPicture.loadExistingPicture(testPic.getUniqueId()).isDone());
    }

    @Test
    public void pictureAreSuccessfullyRemovedFromCache(){
        ExistingPicture pic = testPic;
        PictureCache.removePicture(testPic.getUniqueId());
        assertFalse(PictureCache.isInCache(testPic.getUniqueId()));
    }
}
