package com.github.wnder.user;

import android.net.Uri;

/**
 * Defines a signed in user
 */
public class SignedInUser extends User{


    private String uniqueId;

    /**
     * Get a higher resolution version of the google profile picture uri
     * @param profilePicture original uri
     * @return higher resolution uri
     */
    private static Uri higherResolutionUri(Uri profilePicture){
        String uriAsString = profilePicture.toString();
        if(uriAsString.contains("96")){

            String[] splUrl = uriAsString.split("96");
            return Uri.parse(splUrl[0] + "400" + splUrl[1]);
        } else {
            return Uri.parse(uriAsString);
        }
    }

    /**
     * Constructor for SignedInUser
     * @param name name of the user
     * @param profilePicture profile picture of the user
     */
    public SignedInUser(String name, Uri profilePicture, String uid){
        super(name, higherResolutionUri(profilePicture));
        this.uniqueId = uid;
    }

    public String getUniqueId(){
        return uniqueId;
    }

}
