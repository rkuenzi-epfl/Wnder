package com.github.wnder.user;

import android.net.Uri;

import com.github.wnder.R;

/**
 * Defines a guest user
 */
public class GuestUser extends User{


    public GuestUser(){
        super("Guest", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
    }
}
