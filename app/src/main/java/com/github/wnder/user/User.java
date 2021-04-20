package com.github.wnder.user;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * abstract class defining a user
 */
public abstract class User {

    //Radius: the images will be taken into this radius around the user's location, in kilometers
    protected int radius = 5;

    //location, null if non-valid
    protected Location location;

    protected String name;
    protected Uri profilePicture;

    /**
     * Returns name of user
     * @return name of user
     */
    public abstract String getName();

    /**
     * Returns profile picture of user
     * @return profile picture of user
     */
    public abstract Uri getProfilePicture();

    /**
     * Returns last know location of user
     * @param manager LocationManager
     * @param context current context
     * @return last known location of user
     */
    public Location getPositionFromGPS(LocationManager manager, Context context){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //TODO:
        //throw new IllegalStateException();
        }

        return manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    /**
     * Returns radius of the user
     * @return radius of the user
     */
    public abstract int getRadius();

    /**
     * Sets radius of the user
     * @param rad new radius
     */
    public abstract void setRadius(int rad);

    /**
     * Apply a function once a new pic to guess is available
     * @param pictureIdAvailable function to apply
     */
    public abstract void onNewPictureAvailable(Consumer<String> pictureIdAvailable);

    /**
     * Get location of user
     * @return location of user
     */
    public abstract Location getLocation();

    /**
     * Set location of user
     * @param location location of user
     */
    public abstract void setLocation(Location location);

    /**
     * returns a map with only the ids of the photos in the radius of the user
     * @param idsAndLocs the ids associated with the locations of the pictures
     * @return a set of string with the ids of the pictures respecting the criteria
     */
    protected Set<String> keepOnlyInRadius(Map<String, Location> idsAndLocs){
        Set<String> correctIds = new HashSet<>();
        for(Map.Entry<String, Location> entry : idsAndLocs.entrySet()){
            float[] res = new float[1];
            //TODO: replace with location getter from leonard
            Location.distanceBetween(entry.getValue().getLatitude(), entry.getValue().getLongitude(), location.getLatitude(), location.getLongitude(), res);
            if(res[0] < radius*1000){
                correctIds.add(entry.getKey());
            }
        }
        return correctIds;
    }
}
