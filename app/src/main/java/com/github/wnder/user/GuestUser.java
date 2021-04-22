package com.github.wnder.user;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.github.wnder.R;
import com.github.wnder.Storage;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class GuestUser extends User{

    @Override
    public String getName(){
        return "Guest";
    }

    @Override
    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }

    /**
     * Returns the id of a picture existing in the db
     * @return the id of the picture, an empty string if non is eligible
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void onNewPictureAvailable(LocationManager manager, Context context, Consumer<String> pictureIdAvailable){
        //Get the ids of all the uploaded pictures
        Storage.onIdsAndLocAvailable((allIdsAndLocs) -> {
            //Keep only ids in desired radius
            Set<String> allIds = keepOnlyInRadius(manager, context, allIdsAndLocs);

            //Retrieve the karma of all pictures
            Storage.onIdsAndKarmaAvailable((allIdsAndKarma) -> {
                pictureIdAvailable.accept(selectImageBasedOnKarma(allIdsAndKarma, allIds));
            });
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
