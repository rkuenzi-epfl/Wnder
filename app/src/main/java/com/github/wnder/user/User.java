package com.github.wnder.user;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import com.github.wnder.NavigationActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * abstract class defining a user
 */
public abstract class User {


    private String name;
    private Uri profilePicture;

    //Radius: the images will be taken into this radius around the user's location, in kilometers
    private int radius = 5;

    // List of skipped picture
    private Set<String> skippedPictures;

    public User(String name, Uri profilePicture){
        this.name = name;
        this.profilePicture = profilePicture;

        // Default radius is 5km;
        this.radius = 5;
        this.skippedPictures = new HashSet<>();
    }

    /**
     * Returns name of user
     * @return name of user
     */
    public String getName(){
        return name;
    }

    /**
     * Returns profile picture of user
     * @return profile picture of user
     */
    public Uri getProfilePicture(){
        return profilePicture;
    }

    /**
     * Returns last know location of user
     * @param manager LocationManager
     * @param context current context
     * @return last known location of user
     */
    public Location getPositionFromGPS(LocationManager manager, Context context){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(context, NavigationActivity.class);
            context.startActivity(intent);
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
     * Return radius for current user
     * @return radius, in kilometers
     */
    public int getRadius(){
        return radius;
    }

    /**
     * Set radius for current user
     * @param rad, in kilometers
     */
    public void setRadius(int rad){
        this.radius = rad;
    }


    /**
     * Skip a picture for the user so that he does not get the same picture later
     * @param pictureId the picture we want to skip
     */
    public void skipPicture(String pictureId){
        skippedPictures.add(pictureId);
    }

    /**
     * Get all the pictures skipped by the user
     * @return set of all skipped pictures
     */
    public Set<String> getSkippedPictures(){
        return skippedPictures;
    }

}
