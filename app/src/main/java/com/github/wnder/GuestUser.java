package com.github.wnder;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GuestUser implements User{

    public String getName(){
        return "Guest";
    }

    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }

    /**
     * Returns the id of a picture existing in the db
     * @return the id of the picture, an empty string if non is eligible
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String getNewPicture() throws ExecutionException, InterruptedException{
        Storage storage = new Storage();

        //Get the ids of all the uploaded pictures
        CompletableFuture<Set<String>> allIdsFuture = storage.getIdsOfAllUploadedPictures();
        Set<String> allIds = allIdsFuture.get();

        //If no image fits, return empty string
        if(allIds.size() == 0){
            return "";
        }
        //else, return randomly chosen string
        else{
            Random rn = new Random();
            int index = rn.nextInt(allIds.size());
            List<String> ids = new ArrayList<>();
            ids.addAll(allIds);
            return ids.get(index);
        }
    }
}
