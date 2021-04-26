package com.github.wnder;

import android.location.Location;

/**
 * Class to manage scores
 */
public final class Score {

    //Max score and max distance
    private final static double MAX_SCORE = 200;
    private final static double MAX_DISTANCE = 20015000; //in meter

    //Calculate score from a distance only
    public static double calculationScore(double distance){

        return calculationScore(distance, MAX_DISTANCE);
    }

    //Calculate score from a distance and a distance reference
    public static double calculationScore(double distance, double distanceReference){

        if(distance > distanceReference){
            return 0; //The given distance is bigger than what we expected with the reference
        }

        double normalized = distance / distanceReference; //a value between 1 and 0
        double sqrtDistribution = 1 - Math.sqrt(normalized);

        return MAX_SCORE * sqrtDistribution;
    }

    /**
     * Computes a user's score given the real position of the location and the position of the user's guess
     * @param realPos image real location
     * @param guessedPos user guessed location
     * @return score of the user depending on the distance between his guess and the real location
     */
    public static double computeScore(Location realPos, Location guessedPos){
        double distance = guessedPos.distanceTo(realPos);
        return calculationScore(distance);
    }
}
