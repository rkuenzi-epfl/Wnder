package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Map;

/**
 * Class describing a locally stored picture
 */
public class Picture {
    private final String uniqueId;
    private final Bitmap bmp;
    private final Location picLocation;

    /**
     * Constructor for a locally stored picture
     * @param uniqueId unique ID
     * @param bmp picture
     */
    public Picture(String uniqueId, Bitmap bmp, Location loc) {
        this.uniqueId = uniqueId;
        this.bmp = bmp;
        this.picLocation = loc;
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
     * Get real location of picture
     * @return real location
     */
    public Location getPicLocation(){
        return this.picLocation;
    }
}
