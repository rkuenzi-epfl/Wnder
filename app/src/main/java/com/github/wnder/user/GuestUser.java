package com.github.wnder.user;

import android.net.Uri;

import com.github.wnder.R;

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
