package com.github.wnder.user;

import android.net.Uri;

import com.github.wnder.R;
import com.github.wnder.Storage;

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
        //Get the ids of all the uploaded pictures
        CompletableFuture<Set<String>> allIdsFuture = Storage.getIdsOfAllUploadedPictures();
        Set<String> allIds = allIdsFuture.get();

        //If no image fits, return empty string
        if(0 == allIds.size()){
            return new String();
        }
        //else, return randomly chosen string
        else{
            List<String> idList = new ArrayList<>();
            idList.addAll(allIds);
            Random random = new Random();
            int index = random.nextInt(allIds.size());
            return idList.get(index);
        }
    }
}
