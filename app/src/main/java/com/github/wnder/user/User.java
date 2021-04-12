package com.github.wnder.user;

import android.net.Uri;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface User {

    public String getName();

    public Uri getProfilePicture();

    public void onNewPictureAvailable(Consumer<String> pictureIdAvailable);
}
