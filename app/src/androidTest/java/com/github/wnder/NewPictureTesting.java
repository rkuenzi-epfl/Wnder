package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import org.junit.BeforeClass;
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

    private static NewPicture pic;
    private static Picture picAsPicture;


    @BeforeClass
    public static void createPic() throws InterruptedException {
        Location loc = new Location("");
        loc.setLatitude(0d);
        loc.setLongitude(0d);
        pic = new NewPicture("testUser", loc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        picAsPicture = (Picture) pic;
        sleep(3000);
    }

    @Test
    public void sendPictureToDbWorks(){
        assertEquals(true, pic.sendPictureToDb());
    }

    @Test
    public void uniqueIdHasGoodFormat(){
        assertTrue(picAsPicture.getUniqueId().matches("testUser\\d+"));
    }

    @Test
    public void getUriWorks(){
        assertEquals(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag), pic.getUri());
    }

    @Test
    public void getLongitudeAndGetLatitudeWork(){
        Location latlng = picAsPicture.getLocation();
        assertEquals(0, latlng.getLatitude(), 0);
        assertEquals(0, latlng.getLongitude(), 0);
    }

    @Test
    public void scoreboardCorrectlyInitialized(){
        Map<String, Object> scoreboard = picAsPicture.getScoreboard();
        assertTrue(scoreboard.size() == 1);
        assertTrue(scoreboard.containsKey("default"));
        assertTrue((Double)scoreboard.get("default") == -1);
    }

    @Test
    public void guessesCorrectlyInitialized(){
        Map<String, Object> guesses = picAsPicture.getGuesses();
        assertTrue(guesses.size() == 1);
        assertTrue(guesses.containsKey("default"));
        Object guess = guesses.get("default");
        assertTrue(((ArrayList<Integer>) guess).get(0) == -1);
        assertTrue(((ArrayList<Integer>) guess).get(1) == -1);
    }
}
