package com.github.wnder;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SignedInUser implements User{

    //private GoogleSignInAccount account;
    private String name;
    private String givenName;
    private String familyName;
    private String email;
    private Uri profilPicture;
    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;

    public SignedInUser(GoogleSignInAccount account){
        //this.account = account;
        name = account.getDisplayName();
        givenName = account.getGivenName();
        familyName = account.getFamilyName();
        email = account.getEmail();
        profilPicture = account.getPhotoUrl();

    }

    public String getName(){
        return name;
    }

    public String getGivenName(){
        return givenName;
    }

    public String getFamilyName(){
        return familyName;
    }

    public String getEmail(){
        return email;
    }

    public Uri getProfilePicture(){
        return profilPicture;
    }

}
