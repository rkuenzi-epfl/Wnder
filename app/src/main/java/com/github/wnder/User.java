package com.github.wnder;

import android.net.Uri;

import java.util.concurrent.ExecutionException;

public interface User {

    public String getName();

    public Uri getProfilePicture();

    public String getNewPicture() throws ExecutionException, InterruptedException;
}
