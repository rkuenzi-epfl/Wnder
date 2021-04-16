package com.github.wnder.user;

import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.wnder.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SignedInUser extends User{

    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;
    
    public SignedInUser(String name, Uri profilePicture){

        this.name = name;
        this.profilePicture = profilePicture;
        this.radius = 5;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public Uri getProfilePicture(){
        return profilePicture;
    }

    /**
     * return radius for current user
     * @return radius, in meters
     */
    public int getRadius(){
        return radius;
    }

    /**
     * set radius for current user
     * @param rad, in meters
     */
    public void setRadius(int rad){
        this.radius = rad;
    }

    /**
     * Returns the ids of all the uploaded and guessed pictures of a user
     * @return a future that holds the ids of all the uploaded and guessed pictures of a user
     */
    public void onUploadedAndGuessedPicturesAvailable(Consumer<Set<String>> uAGPA){
        //Get the user data
        Task<DocumentSnapshot> userData = Storage.downloadFromFirestore("users", this.name);
        Set<String> allPictures = new HashSet<>();
        CompletableFuture<Set<String>> picturesToReturn = new CompletableFuture<>();

        //When successful, fuse the guessed and the uploaded pictures and complete the future accordingly
        userData.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
                List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");
                if (guessedPictures == null) {
                    guessedPictures = new ArrayList<>();
                }
                if (uploadedPictures == null) {
                    uploadedPictures = new ArrayList<>();
                }
                allPictures.addAll(guessedPictures);
                allPictures.addAll(uploadedPictures);
                uAGPA.accept(allPictures);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uAGPA.accept(new HashSet<>());
            }
        });
    }

    /**
     * Returns the id of a picture existing in the db that the user neither already guessed nor uploaded himself
     * @param pictureIdAvailable
     */
    @Override
    public void onNewPictureAvailable(Consumer<String> pictureIdAvailable){
        //Get the ids and locs of all the uploaded pictures

        Storage.onIdsAndLocAvailable((allIdsAndLocs) -> {
            //Get the ids of all the pictures linked with the user (guessed or uploaded)
            onUploadedAndGuessedPicturesAvailable((upAndGuessedPics) -> {

                for (String id : upAndGuessedPics) {
                    if (allIdsAndLocs.containsKey(id)) {
                        allIdsAndLocs.remove(id, allIdsAndLocs.get(id));
                    }
                }

                //Keep only ids in desired radius
                Set<String> allIds = keepOnlyInRadius(allIdsAndLocs);

                //If no image fits, return empty string
                if (allIds.size() == 0) {
                    pictureIdAvailable.accept("");
                }
                //else, return randomly chosen string
                else {
                    Random rn = new Random();
                    int index = rn.nextInt(allIds.size());
                    List<String> ids = new ArrayList<>();
                    ids.addAll(allIds);
                    pictureIdAvailable.accept(ids.get(index));
                }
            });
        });
    }

    /**
     * get user location
     * @return last known location
     */
    public Location getLocation(){
        return location;
    }

    /**
     * set user location
     * @param loc location, null if non-valid
     */
    public void setLocation(Location loc){
        this.location = loc;
    }
}
