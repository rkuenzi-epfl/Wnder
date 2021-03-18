package com.github.wnder;

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
public class NewPictureTesting {

    public NewPicture createPic() throws InterruptedException {
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        sleep(3000);
        return pic;
    }

    @Test
    public void sendPictureToDbWorks() throws InterruptedException {
        NewPicture pic = createPic();
        assertEquals(true, pic.sendPictureToDb());
    }

    @Test
    public void uniqueIdHasGoodFormat() throws InterruptedException {
        NewPicture pic = createPic();
        assertTrue(pic.getUniqueId().matches("testUser\\d+"));
    }

    @Test
    public void getUriWorks() throws InterruptedException {
        NewPicture pic = createPic();
        assertEquals(Uri.parse("android.resource://raw/ladiag.jpg"), pic.getUri());
    }

    @Test
    public void getLongitudeAndGetLatitudeWork() throws InterruptedException {
        NewPicture pic = createPic();
        assertTrue(pic.getLongitude() == 0);
        assertTrue(pic.getLatitude() == 0);
    }

    @Test
    public void scoreboardCorrectlyInitialized() throws InterruptedException {
        NewPicture pic = createPic();
        Map<String, Object> scoreboard = pic.getScoreboard();
        assertTrue(scoreboard.size() == 1);
        assertTrue(scoreboard.containsKey("default"));
        assertTrue((Double)scoreboard.get("default") == -1);
    }

    @Test
    public void guessesCorrectlyInitialized() throws InterruptedException {
        NewPicture pic = createPic();
        Map<String, Object> guesses = pic.getGuesses();
        assertTrue(guesses.size() == 1);
        assertTrue(guesses.containsKey("default"));
        Object guess = guesses.get("default");
        assertTrue(((ArrayList<Integer>) guess).get(0) == -1);
        assertTrue(((ArrayList<Integer>) guess).get(1) == -1);
    }
}
