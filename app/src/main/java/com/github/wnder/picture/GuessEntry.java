package com.github.wnder.picture;

import com.github.wnder.Score;
import com.google.firebase.firestore.GeoPoint;

/**
 * Represent a guess entry for the database
 */
public class GuessEntry {

    private String userName;
    private GeoPoint location;
    private long score;

    // For firestore
    public GuessEntry(){}

    // Actual constructor
    public GuessEntry(String userName, GeoPoint location, long score){
        this.userName = userName;
        this.location = location;
        this.score = score;
    }

    public String getUserName() {
        return userName;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public long getScore() {
        return score;
    }
}
