package com.github.wnder.user;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
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

    public abstract void onNewPictureAvailable(Consumer<String> pictureIdAvailable);

    public abstract Location getLocation();

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

    private Map<String, Long> computeIntersectionBetweenMapAndSet(Map<String, Long> idsAndKarma, Set<String> acceptedIds){
        Map<String, Long> intersectionMap = new HashMap<>();
        //Takes only the entries inside the set of accepted IDs
        for(Map.Entry<String, Long> entry : idsAndKarma.entrySet()){
            if(acceptedIds.contains(entry.getKey())){
                intersectionMap.put(entry.getKey(), entry.getValue());
            }
        }
        return intersectionMap;
    }

    private String findAssociatedRandomId(int randomNumber, Map<String, Long> map){
        //Select a random image
        int counter = 0;
        for(Map.Entry<String, Long> entry : map.entrySet()){
            counter += entry.getValue();
            if(counter >= randomNumber){
                return entry.getKey();
            }
        }

        //If there is no image, return this
        return "";
    }

    /**
     * Returns the id of a picture from the parameter selected randomly. The more karma a picture have, the more chances the image has to get selected
     * @param idsAndKarma the ids associated with the karma of the pictures
     * @param acceptedIds the ids that we want to take from the entire db
     * @return
     */
    protected String selectImageBasedOnKarma(Map<String, Long> idsAndKarma, Set<String> acceptedIds){
        Map<String, Long> intersectionMap = computeIntersectionBetweenMapAndSet(idsAndKarma, acceptedIds);
        //Compute the minimum karma of the pictures
        long minKarma = Collections.min(intersectionMap.values());
        Map<String, Long> correctedMap = new HashMap<>();
        int sumKarma = 0;

        //Compute the sum of all images' karma and change linearly the karma of all pictures so that the karma of the picture that has the least karma is one
        for(Map.Entry<String, Long> entry : intersectionMap.entrySet()){
            long newKarma = entry.getValue() - minKarma + 1L;
            sumKarma += newKarma;
            correctedMap.put(entry.getKey(), newKarma);
        }

        Random rand = new Random();
        int randomNumber = rand.nextInt(sumKarma);

        return findAssociatedRandomId(randomNumber, correctedMap);
    }
}
