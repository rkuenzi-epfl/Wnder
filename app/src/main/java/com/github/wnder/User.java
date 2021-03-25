package com.github.wnder;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public abstract class User {

    public abstract String getName();

    public abstract Uri getProfilePicture();

    public LatLng getPosition(){
        return null;
    }
}
