package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Map;

/**
 * Class describing a locally stored picture
 */
public class LocalPicture extends Picture {
    private final Bitmap mapSnapshot;
    private final Location location;
    private final Bitmap bmp;
    private final Location guessLocation;
    private final Map<String, Double> scoreboard;

    /**
     * Constructor for a locally stored picture
     * @param uniqueId unique ID
     * @param bmp picture
     * @param picLocation picture location
     * @param guessLocation local user guess
     * @param scoreboard global scoreboard
     */
    public LocalPicture(String uniqueId, Bitmap bmp, Bitmap mapSnapshot, Location picLocation, Location guessLocation, Map<String, Double> scoreboard) {
        super(uniqueId, picLocation.getLatitude(), picLocation.getLongitude());

        this.mapSnapshot = mapSnapshot;
        this.location = picLocation;
        this.bmp = bmp;
        this.guessLocation = guessLocation;
        this.scoreboard = scoreboard;
    }

    /**
     * Get map snapshot of local picture
     * @return map
     */
    public Bitmap getMapSnapshot(){
        return this.mapSnapshot;
    }

    /**
     * Get the location of the picture
     * @return the location
     */
    public Location getPicLocation(){
        return this.location;
    }

    /**
     * Get bitmap of local picture
     * @return bitmap
     */
    public Bitmap getBitmap(){
        return this.bmp;
    }

    /**
     * Get user guess for location
     * @return guessed location
     */
    public Location getGuessLocation(){
        return this.guessLocation;
    }

    /**
     * Get scoreboard
     * @return scoreboard
     */
    public Map<String, Double> getScoreboard(){
        return this.scoreboard;
    }
}
