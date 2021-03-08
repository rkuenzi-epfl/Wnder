package com.github.wnder;

import android.net.Uri;

public class GuestUser implements User{

    public String getName(){
        return "Guest";
    }

    public Uri getProfilePicture(){
        return Uri.parse("android.resource://raw/LaDiag.jpg");
    }
}
