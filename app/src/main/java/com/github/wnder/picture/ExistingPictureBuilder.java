package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Map;

/**
 * This class build step by step an existing with all it's necessary data
 */
public class ExistingPictureBuilder {

    private String uniqueId;
    private Location location;
    private Bitmap bmp;
    private Map<String, Double> scoreboard;

    /**
     * Constructor needs at least a unique ID
     * @param uniqueId picture unique ID
     */
    public ExistingPictureBuilder(String uniqueId){
        this.uniqueId = uniqueId;
    }

    /**
     * Get the unique ID of the built picture
     * @return picture unique ID
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Add the location of the picture
     * @param location location to add
     * @return This builder
     */
    public ExistingPictureBuilder withLocation(Location location){
        this.location = location;
        return this;
    }

    /**
     * Get the location of the built picture
     * Can return Null
     * @return location of the picture
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Add the bitmap of the picture
     * @param bitmap bitmap to add
     * @return This builder
     */
    public ExistingPictureBuilder withBitmap(Bitmap bitmap){
        this.bmp = bitmap;
        return this;
    }

    /**
     * Get the bitmap of the built picture
     * Can return Null
     * @return bitmap of the picture
     */
    public Bitmap getBitmap() {
        return bmp;
    }

    /**
     * Add the scoreboard of the picture
     * @param scoreboard scoreboard to add
     * @return This builder
     */
    public ExistingPictureBuilder withScoreboard(Map<String, Double> scoreboard){
        this.scoreboard = scoreboard;
        return this;
    }

    /**
     * Get the scoreboard of the built picture
     * Can return Null
     * @return scoreboard of the picture
     */
    public Map<String, Double> getScoreboard() {
        return scoreboard;
    }



}
