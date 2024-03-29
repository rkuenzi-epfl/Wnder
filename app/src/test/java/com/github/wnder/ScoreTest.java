package com.github.wnder;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ScoreTest {

    @Test
    public void scoreCalculatedIsWithinRange(){
        double dist1 = 100;
        double dist2 = 1000;
        int radius = 1;

        assertEquals(135, Score.calculationScore(dist1, dist2, radius), 5);
    }

    @Test
    public void computeScoreCalculatedIsWithinRange(){
        Location realLocation = new Location("");
        realLocation.setLongitude(46.46256);
        realLocation.setLatitude(6.34004);
        Location guessedLocation = new Location("");
        guessedLocation.setLongitude(46.46250);
        guessedLocation.setLatitude(6.34003);

        assertEquals(192, Score.computeScore(realLocation, guessedLocation), 2);
    }
}
