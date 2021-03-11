package com.github.wnder;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SignedInUser implements User{

    //private GoogleSignInAccount account;
    private String name;
    private Uri profilePicture;
    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;
    
    public SignedInUser(String name, Uri profilePicture){
        //this.account = account;
        this.name = name;
        this.profilePicture = profilePicture;

    }

    public String getName(){
        return name;
    }

    public Uri getProfilePicture(){
        return profilePicture;
    }

}
