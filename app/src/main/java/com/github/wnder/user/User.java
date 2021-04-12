package com.github.wnder.user;

import android.net.Uri;

import java.util.concurrent.ExecutionException;

public interface User {

    public String getName();

    public Uri getProfilePicture();

    public String getNewPicture() throws ExecutionException, InterruptedException;

    public int getRadius();

    public void setRadius(int rad);
}
