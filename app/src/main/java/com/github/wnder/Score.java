package com.github.wnder;

import android.location.Location;
import android.util.Log;

import com.github.wnder.user.GlobalUser;

/**
 * Class to manage scores
 */
public final class Score {

    //Max score and max distance
    private final static double MAX_SCORE = 200;

    /**
     * Calculate a score
     * @param distance Distance between real location and guess location
     * @param distanceReference radius, "permitted error" in KM
     * @param radiusCircle radius selected by the user
     * @return score
     */
    public static double calculationScore(double distance, double distanceReference, int radiusCircle){

        if(distance > distanceReference){
            return 0; //The given distance is bigger than what we expected with the reference
        }

        double normalized = distance / distanceReference; //a value between 1 and 0
        double sqrtDistribution = 1 - Math.sqrt(normalized);
        double difficultyBonus = Math.pow(radiusCircle, 0.25);

        return Math.floor(MAX_SCORE * sqrtDistribution * difficultyBonus);
    }

    /**
     * Computes a user's score given the real position of the location and the position of the user's guess
     * @param realPos image real location
     * @param guessedPos user guessed location
     * @return score of the user depending on the distance between his guess and the real location
     */
    public static double computeScore(Location realPos, Location guessedPos){
        double distance = guessedPos.distanceTo(realPos);
        return calculationScore(distance, GlobalUser.getUser().getRadius() * 1000, GlobalUser.getUser().getRadius());
    }
}
