package com.github.wnder;

import java.util.HashMap;
import java.util.Map;

public class ExistingPicture {

    //Image unique ID
    private String uniqueId;

    //Image location
    private double longitude;
    private double latitude;

    //User data for the image: global scoreboard + all guesses
    private Map<String, Integer> scoreboard;
    //Map<[user ID], Map<[longitude/latitude], [value]>>
    private Map<String, Map<String, Double>> guesses;

    //Constructor for the image
    public ExistingPicture(String uniqueId){
    }

    /**
     * Send a user's guess and score to the database
     * @param user: user id
     * @param score: user score
     * @param longitude: user longitude guess
     * @param latitude: user latitude guess
     */
    private void sendUserData(String user, int score, double longitude, double latitude) {

    }

    /**
     * Compute user score given his guess + upload his guess and his score to the database
     * @param guessedLongitude: user longitude guess
     * @param guessedLatitude: user latitude guess
     * @return: user score
     */
    public int computeScore(double guessedLongitude, double guessedLatitude){
        return -1;
    }

    /**
     * Get a user's score
     * @param user: user ID
     * @return: user score if it exists, -1 else
     */
    public int getUserScore(String user){
        return -1;
    }

    /**
     * Get a user's guess
     * @param user: user ID
     * @return: user guess if it exists, empty map else
     */
    public Map<String, Double> getUserGuess(String user){
        return new HashMap<>();
    }

}
