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
import java.util.function.Consumer;

/**
 * Defines a guest user
 */
public class GuestUser extends User{

    /**
     * get the name
     * @return "Guest"
     */
    @Override
    public String getName(){
        return "Guest";
    }

    /**
     * get the profile picture
     * @return default profile picture
     */
    @Override
    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }

    /**
     * Get a new picture id that the user can guess
     * @param pictureIdAvailable function to apply
     */
    @Override
    public void onNewPictureAvailable(Consumer<String> pictureIdAvailable){
        //Get the ids of all the uploaded pictures
        Storage.onIdsAndLocAvailable((allIdsAndLocs) -> {
            Set<String> allIds = keepOnlyInRadius(allIdsAndLocs);
            //If no image fits, return empty string
            if(0 == allIds.size()){
                pictureIdAvailable.accept("");
            }
            //else, return randomly chosen string
            else{
                List<String> idList = new ArrayList<>();
                idList.addAll(allIds);
                Random random = new Random();
                int index = random.nextInt(allIds.size());
                pictureIdAvailable.accept(idList.get(index));
            }
        });
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
     * @param rad new radius
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
