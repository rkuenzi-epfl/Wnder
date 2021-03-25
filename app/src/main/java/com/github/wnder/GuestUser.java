package com.github.wnder;

import android.net.Uri;

public class GuestUser extends User{

    @Override
    public String getName(){
        return "Guest";
    }

    @Override
    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }
}
