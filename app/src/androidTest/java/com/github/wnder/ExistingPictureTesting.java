package com.github.wnder;

import android.graphics.Bitmap;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ExistingPictureTesting {

    private ExistingPicture getTestPic() throws InterruptedException {
        ExistingPicture pic = new ExistingPicture("picture1");
        sleep(1000);
        return pic;
    }

    @Test
    public void computeScoreAndSendToDbWorks() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(1000);
        assertEquals(200, score, 10);
    }

    @Test
    public void getUserScoreAndGuessWork() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        assertEquals(200, (Double)pic.getUserScore("testUser"), 10);
        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(0) == 10.);
        assertTrue(((ArrayList<Double>)pic.getUserGuess("testUser")).get(1) == 10.);
    }

    @Test
    public void getUniqueIdWorks() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        assertTrue(pic.getUniqueId().equals("picture1"));
    }

    @Test
    public void getBmpReturnsBitmap() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        sleep(3000);
        assertTrue(pic.getBmp() instanceof Bitmap);
    }

    @Test
    public void getLongitudeAndLatitudeWork() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        assertTrue(pic.getLongitude() == 10);
        assertTrue(pic.getLatitude() == 10);
    }

    @Test
    public void getScoreboardWorks() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(5000);
        Map<String, Object> scoreboard = pic.getScoreboard();
        Double userScore = (double)scoreboard.getOrDefault("testUser", -1);
        assertEquals(userScore, score);
    }

    @Test
    public void getGuessesWorks() throws InterruptedException {
        ExistingPicture pic = getTestPic();
        Double score = pic.computeScoreAndSendToDb("testUser", 10, 10);
        sleep(5000);
        Map<String, Object> guesses = pic.getGuesses();
        Object guess = guesses.get("testUser");
        assertTrue(((ArrayList<Double>) guess).get(0) == 10.);
        assertTrue(((ArrayList<Double>) guess).get(1) == 10.);
    }
}
