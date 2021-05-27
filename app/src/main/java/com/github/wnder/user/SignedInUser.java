package com.github.wnder.user;

import android.net.Uri;

/**
 * Defines a signed in user
 */
public class SignedInUser extends User{


    private String uniqueId;

    /**
     * Constructor for SignedInUser
     * @param name name of the user
     * @param profilePicture profile picture of the user
     */
    public SignedInUser(String name, Uri profilePicture, String uid){
        // Get better quality profile pic
        String[] splUrl = profilePicture.toString().split("96");
        this.name = name;
        this.profilePicture = Uri.parse(splUrl[0] + "400" + splUrl[1]);
        //By default, radius is set at 5 km
        this.radius = 5;

        this.uniqueId = uid;
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
