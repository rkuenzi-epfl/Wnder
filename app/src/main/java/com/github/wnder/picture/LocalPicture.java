package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Map;

/**
 * Class describing a locally stored picture
 */
public class LocalPicture {
    private final String uniqueId;
    private final Bitmap bmp;
    private final Bitmap mapSnapshot;
    private final Location realLocation;
    private final Location guessLocation;
    private final Map<String, Double> scoreboard;

    /**
     * Constructor for a locally stored picture
     * @param uniqueId unique ID
     * @param bmp picture
     * @param realLocation picture location
     * @param guessLocation local user guess
     * @param scoreboard global scoreboard
     */
    public LocalPicture(String uniqueId, Bitmap bmp, Bitmap mapSnapshot, Location realLocation, Location guessLocation, Map<String, Double> scoreboard) {
        this.uniqueId = uniqueId;
        this.bmp = bmp;
        this.mapSnapshot = mapSnapshot;
        this.realLocation = realLocation;
        this.guessLocation = guessLocation;
        this.scoreboard = scoreboard;
    }

    /**
     * Get id of local picture
     * @return unique ID
     */
    public String getUniqueId(){
        return this.uniqueId;
    }

    /**
     * Get bitmap of local picture
     * @return bitmap
     */
    public Bitmap getBitmap(){
        return this.bmp;
    }

    /**
     * Get map snapshot of local picture
     * @return map
     */
    public Bitmap getMapSnapshot(){
        return this.mapSnapshot;
    }

    /**
     * Get real location of picture
     * @return real location
     */
    public Location getRealLocation(){
        return this.realLocation;
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
