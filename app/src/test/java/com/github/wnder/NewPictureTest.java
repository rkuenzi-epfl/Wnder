package com.github.wnder;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NewPictureTest {

    @Test
    public void sendPictureToDbWorks(){
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        assertEquals(true, pic.sendPictureToDb());
    }

    @Test
    public void uniqueIdHasGoodFormat(){
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        assertTrue(pic.getUniqueId().matches("testUser\\d+"));
    }

    @Test
    public void getUriWorks(){
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        assertEquals(Uri.parse("android.resource://raw/ladiag.jpg"), pic.getUri());
    }

    @Test
    public void getLongitudeAndGetLatitudeWork(){
        NewPicture pic = new NewPicture("testUser", 10., 100., Uri.parse("android.resource://raw/ladiag.jpg"));
        assertTrue(pic.getLongitude() == 10.);
        assertTrue(pic.getLatitude() == 100.);
    }

    @Test
    public void scoreboardCorrectlyInitialized(){
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        Map<String, Double> scoreboard = pic.getScoreboard();
        assertTrue(scoreboard.size() == 1);
        assertTrue(scoreboard.containsKey("default"));
        assertTrue(scoreboard.get("default") == -1);
    }

    @Test
    public void guessesCorrectlyInitialized(){
        NewPicture pic = new NewPicture("testUser", 0, 0, Uri.parse("android.resource://raw/ladiag.jpg"));
        Map<String, Map<String, Double>> guesses = pic.getGuesses();
        assertTrue(guesses.size() == 1);
        assertTrue(guesses.containsKey("default"));
        Map<String, Double> guess = guesses.get("default");
        assertTrue(guess.size() == 2);
        assertTrue(guess.containsKey("latitude"));
        assertTrue(guess.containsKey("longitude"));
        assertTrue(guess.get("longitude") == -1);
        assertTrue(guess.get("latitude") == -1);
    }
}
