package com.github.wnder.user;

import android.net.Uri;

/**
 * Defines a signed in user
 */
public class SignedInUser extends User{

    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;

    /**
     * Constructor for SignedInUser
     * @param name name of the user
     * @param profilePicture profile picture of the user
     */
    public SignedInUser(String name, Uri profilePicture){

        this.name = name;
        this.profilePicture = profilePicture;
        //By default, radius is set at 5 km
        this.radius = 5;
    }

    /**
     * get name of user
     * @return name of user
     */
    @Override
    public String getName(){
        return name;
    }

    /**
     * get profile picture of user
     * @return profile picture of user
     */
    @Override
    public Uri getProfilePicture(){
        return profilePicture;
    }

    /**
     * return radius for current user
     * @return radius, in kilometers
     */
    public int getRadius(){
        return radius;
    }

    /**
     * set radius for current user
     * @param rad, in kilometers
     */
    public void setRadius(int rad){
        this.radius = rad;
    }

}
