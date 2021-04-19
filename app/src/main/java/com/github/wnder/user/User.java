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

public abstract class User {

    //Radius: the images will be taken into this radius around the user's location, in kilometers
    protected int radius = 5;

    //location, null if non-valid
    protected Location location;

    protected String name;
    protected Uri profilePicture;

    public abstract String getName();

    public abstract Uri getProfilePicture();

    public Location getPositionFromGPS(LocationManager manager, Context context){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //TODO:
        //throw new IllegalStateException();
        }

        return manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public abstract int getRadius();

    public abstract void setRadius(int rad);

    public abstract void onNewPictureAvailable(LocationManager manager, Context context, Consumer<String> pictureIdAvailable);

    public abstract Location getLocation();

    public abstract void setLocation(Location location);

    /**
     * returns a map with only the ids of the photos in the radius of the user
     * @param idsAndLocs the ids associated with the locations of the pictures
     * @return a set of string with the ids of the pictures respecting the criteria
     */
    public Set<String> keepOnlyInRadius(LocationManager manager, Context context, Map<String, Location> idsAndLocs){
        Set<String> correctIds = new HashSet<>();
        for(Map.Entry<String, Location> entry : idsAndLocs.entrySet()){
            float[] res = new float[1];
            Location loc = getPositionFromGPS(manager, context);
            Location.distanceBetween(entry.getValue().getLatitude(), entry.getValue().getLongitude(), loc.getLatitude(), loc.getLongitude(), res);
            if(res[0] < radius*1000){
                correctIds.add(entry.getKey());
            }
        }
        return correctIds;
    }
}
