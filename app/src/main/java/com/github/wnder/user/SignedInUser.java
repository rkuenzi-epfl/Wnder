package com.github.wnder.user;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.wnder.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Defines a signed in user
 */
public class SignedInUser extends User{

    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;

    /**
     * Constructor for SignedInUser
     * @param name name of the user
     * @param profilePicture profile picture of the user
     */
    public SignedInUser(String name, Uri profilePicture){

        this.name = name;
        this.profilePicture = profilePicture;
        //By default, radius is set at 5 km
        this.radius = 5;
    }

    /**
     * get name of user
     * @return name of user
     */
    @Override
    public String getName(){
        return name;
    }

    /**
     * get profile picture of user
     * @return profile picture of user
     */
    @Override
    public Uri getProfilePicture(){
        return profilePicture;
    }

    /**
     * return radius for current user
     * @return radius, in kilometers
     */
    public int getRadius(){
        return radius;
    }

    /**
     * set radius for current user
     * @param rad, in kilometers
     */
    public void setRadius(int rad){
        this.radius = rad;
    }

    /**
     * Apply a function once the uploaded pictures of the user have been retrieved
     * @param UPA Function to apply
     */
    public void onUploadedPicturesAvailable(Consumer<List<String>> UPA){
        Task<DocumentSnapshot> userData = Storage.downloadFromFirestore("users", this.name);

        userData.addOnSuccessListener(documentSnapshot -> {
            List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");
            if(uploadedPictures == null) {
                uploadedPictures = new ArrayList<>();
            }
            UPA.accept(uploadedPictures);
        });
    }

    /**
     * Apply a function once the guessed pictures of the user have been retrieved
     * @param GPA Function to apply
     */
    public void onGuessedPicturesAvailable(Consumer<List<String>> GPA){
        Task<DocumentSnapshot> userData = Storage.downloadFromFirestore("users", this.name);

        userData.addOnSuccessListener(documentSnapshot -> {
            List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
            if(guessedPictures == null) {
                guessedPictures = new ArrayList<>();
            }
            GPA.accept(guessedPictures);
        });
    }


    /**
     * Apply a function once the uploaded and the guessed pictures of the user have been retrieved
     * @param uAGPA Function to apply
     */
    void onUploadedAndGuessedPicturesAvailable(Consumer<Set<String>> uAGPA){
        Set<String> allPictures = new HashSet<>();

        onGuessedPicturesAvailable(guessedPics -> {
            allPictures.addAll(guessedPics);
        });
        onUploadedPicturesAvailable(uploadedPics -> {
            allPictures.addAll(uploadedPics);
        });
        uAGPA.accept(allPictures);
    }

    /**
     * apply a function on a picture in the db that the user neither already guessed nor uploaded himself
     * @param pictureIdAvailable function to apply
     */
    @Override
    public void onNewPictureAvailable(LocationManager manager, Context context, Consumer<String> pictureIdAvailable){
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
                Set<String> allIds = keepOnlyInRadius(manager, context, allIdsAndLocs);
                Storage.onIdsAndKarmaAvailable((allIdsAndKarma) -> {
                    pictureIdAvailable.accept(selectImageBasedOnKarma(allIdsAndKarma, allIds));
                });

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
