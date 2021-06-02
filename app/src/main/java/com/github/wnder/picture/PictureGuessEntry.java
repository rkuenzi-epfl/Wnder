package com.github.wnder.picture;

import com.github.wnder.Score;
import com.google.firebase.firestore.GeoPoint;

/**
 * Represent a guess entry for the picture database
 */
public class PictureGuessEntry {

    private String userName;
    private GeoPoint location;
    private double score;

    // For firestore
    public PictureGuessEntry(){}

    // Actual constructor
    public PictureGuessEntry(String userName, GeoPoint location, double score){
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

    public double getScore() {
        return score;
    }
}
