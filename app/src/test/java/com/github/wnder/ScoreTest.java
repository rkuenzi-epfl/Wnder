package com.github.wnder;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScoreTest {

    @Test
    public void distanceCalculatedIsWithinRange(){

        LatLng test1 = new LatLng(46.46256, 6.34004);
        LatLng test2 = new LatLng(46.51968, 6.62706);
        double distance1 = Score.calculationDistance(test1, test2);
        assertEquals(22950, distance1, 1000);

        LatLng test3 = new LatLng(2.3618, -77.5921);
        LatLng test4 = new LatLng(-4.1566, -77.4823);
        double distance2 = Score.calculationDistance(test3, test4);
        assertEquals(719330, distance2, 10000);

        //delta is high because latitude/longitude values are taken by hand so error are expected
    }

    @Test
    public void scoreCalculatedIsWithinRange(){
        double dist1 = 100;
        double dist2 = 1000;
        double dist3 = 1000000000;

        assertEquals(0, Score.calculationScore(dist3), 1);
        assertEquals(135, Score.calculationScore(100, 1000), 5);
    }
}
