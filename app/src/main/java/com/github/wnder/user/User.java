package com.github.wnder.user;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

/**
 * abstract class defining a user
 */
public abstract class User {

    //Radius: the images will be taken into this radius around the user's location, in kilometers
    protected int radius = 5;

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
        Location loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(loc == null){ //To avoid unexpected result from the GPS, we set it to 0, 0.
            loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLongitude(0);
            loc.setLatitude(0);
        }
        return loc;
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

}
