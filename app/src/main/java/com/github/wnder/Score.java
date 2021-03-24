package com.github.wnder;

import com.google.android.gms.maps.model.LatLng;

public final class Score {

    private final static double MAX_SCORE = 200;
    private final static double MAX_DISTANCE = 20015000; //in meter

    public static double calculationDistance(LatLng pos1, LatLng pos2){

        final int R = 6371;

        double latitude = Math.toRadians(pos2.latitude - pos1.latitude);
        double longitude = Math.toRadians(pos2.longitude - pos1.longitude);

        double a = Math.sin(latitude / 2) * Math.sin(latitude / 2)
                + Math.cos(Math.toRadians(pos1.latitude)) * Math.cos(Math.toRadians(pos2.latitude))
                * Math.sin(longitude / 2) * Math.sin(longitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

    public static double calculationScore(double distance){

        return calculationScore(distance, MAX_DISTANCE);
    }

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
    public static double computeScore(LatLng realPos, LatLng guessedPos){
        double distance = calculationDistance(realPos, guessedPos);
        return calculationScore(distance);
    }

    /**
     * Computes a user's score given the real position of the location and the position of the user's guess
     * @param realLat latitude of the location
     * @param realLong longitude of the location
     * @param guessedLat latitude of the guess
     * @param guessedLong longitude of the guess
     * @return score of the user depending on the distance between his guess and the real location
     */
    public static double computeScore(double realLat, double realLong, double guessedLat, double guessedLong){
        LatLng realPos = new LatLng(realLat, realLong);
        LatLng guessedPos = new LatLng(guessedLat, guessedLong);
        return computeScore(realPos, guessedPos);
    }
}
