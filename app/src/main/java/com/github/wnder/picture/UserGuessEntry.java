package com.github.wnder.picture;

import com.google.firebase.firestore.GeoPoint;

public class UserGuessEntry {

    private GeoPoint location;
    private double score;

    // For firestore
    public UserGuessEntry(){}

    // Actual constructor
    public UserGuessEntry(GeoPoint location, double score){
        this.location = location;
        this.score = score;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public double getScore() {
        return score;
    }
}
