package com.github.wnder.user;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.github.wnder.R;
import com.github.wnder.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GuestUser extends User{

    //Radius: the images will be taken into this radius around the user's location, in kilometers
    private int radius = 5;
    //location, null if non-valid
    private Location location;

    @Override
    public String getName(){
        return "Guest";
    }

    @Override
    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }

    /**
     * returns a map with only the ids of the photos in the radius of the user
     * @param idsAndLocs the ids associated with the locations of the pictures
     * @return a set of string with the ids of the pictures respecting the criteria
     */
    private Set<String> keepOnlyInRadius(Map<String, Location> idsAndLocs){

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

    /**
     * Returns the id of a picture existing in the db
     * @return the id of the picture, an empty string if non is eligible
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public String getNewPicture() throws ExecutionException, InterruptedException{
        //Get the ids of all the uploaded pictures
        CompletableFuture<Map<String, Location>> allIdsAndLocsFuture = Storage.getIdsAndLocationOfAllUploadedPictures();
        Map<String, Location> allIdsAndLocs = allIdsAndLocsFuture.get();

        Set<String> allIds = keepOnlyInRadius(allIdsAndLocs);
        //If no image fits, return empty string
        if(0 == allIds.size()){
            return new String();
        }
        //else, return randomly chosen string
        else{
            List<String> idList = new ArrayList<>();
            idList.addAll(allIds);
            Random random = new Random();
            int index = random.nextInt(allIds.size());
            return idList.get(index);
        }
    }

    /**
     * returns radius for current user
     * @return radius
     */
    public int getRadius(){
        return radius;
    }

    /**
     * set radius for current user
     * @param rad
     */
    public void setRadius(int rad){
        this.radius = rad;
    }

    /**
     * get user location
     * @return last known location
     */
    public Location getLocation(){
        return location;
    }

    /**
     * set user location
     * @param loc location, null if non-valid
     */
    public void setLocation(Location loc){
        this.location = loc;
    }
}
