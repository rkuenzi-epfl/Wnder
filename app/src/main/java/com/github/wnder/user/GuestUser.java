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
     * @param rad new radius
     */
    public void setRadius(int rad){
        this.radius = rad;
    }

}
