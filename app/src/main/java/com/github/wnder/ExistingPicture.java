package com.github.wnder;

import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class ExistingPicture implements Picture{

    //Image unique ID
    private String uniqueId;
    private Uri uri;

    //Image location
    private double longitude;
    private double latitude;

    //User data for the image: global scoreboard + all guesses
    private Map<String, Double> scoreboard;
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
    private void sendUserData(String user, Double score, double longitude, double latitude) {

    }

    /**
     * Compute user score given his guess + upload his guess and his score to the database
     * @param guessedLongitude: user longitude guess
     * @param guessedLatitude: user latitude guess
     * @return: user score
     */
    public Double computeScoreAndSendToDb(String user, double guessedLongitude, double guessedLatitude){
        double score = 100 - Math.abs(guessedLongitude - longitude) - Math.abs(guessedLatitude - latitude);
        sendUserData(user, score, guessedLongitude, guessedLatitude);
        return score;
    }

    /**
     * Get a user's score
     * @param user: user ID
     * @return: user score if it exists, -1 else
     */
    public Double getUserScore(String user){
        if(scoreboard.containsKey(user)){
            return scoreboard.get(user);
        }
        else{
            return -1.;
        }
    }

    /**
     * Get a user's guess
     * @param user: user ID
     * @return: user guess if it exists, empty map else
     */
    public Map<String, Double> getUserGuess(String user){
        if(guesses.containsKey(user)){
            return guesses.get(user);
        }
        else{
            return new HashMap<>();
        }
    }

    public String getUniqueId(){
        return uniqueId;
    }

    public Uri getUri(){
        return uri;
    }

    public Double getLongitude(){
        return longitude;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Map<String, Double> getScoreboard(){
        return scoreboard;
    }

    public Map<String, Map<String, Double>> getGuesses(){
        return guesses;
    }
}
