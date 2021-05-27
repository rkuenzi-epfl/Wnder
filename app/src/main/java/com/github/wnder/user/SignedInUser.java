package com.github.wnder.user;

import android.net.Uri;

/**
 * Defines a signed in user
 */
public class SignedInUser extends User{

    /**
     * Constructor for SignedInUser
     * @param name name of the user
     * @param profilePicture profile picture of the user
     */
    public SignedInUser(String name, Uri profilePicture){
        super(name, profilePicture);
    }
}
