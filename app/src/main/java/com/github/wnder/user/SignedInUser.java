package com.github.wnder.user;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.wnder.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SignedInUser implements User{

    private String name;
    private Uri profilePicture;
    // These are guesses on future fields for a user
    //private int GlobalScore;
    //private History history;
    
    public SignedInUser(String name, Uri profilePicture){

        this.name = name;
        this.profilePicture = profilePicture;
    }

    public String getName(){
        return name;
    }

    public Uri getProfilePicture(){
        return profilePicture;
    }

    @Override
    public void onNewPictureAvailable(Consumer<String> pictureIdAvailable){
        //Get the ids of all the uploaded pictures
        Set<String> allIds = new HashSet<>();

        Storage.downloadFromFirestore("pictures").addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //Get the ids of all the pictures linked with the user (guessed or uploaded)
                CompletableFuture<Set<String>> upAndGuessedPicsFuture = getUploadedAndGuessedPictures();
                Set<String> upAndGuessedPics = null;
                try {
                    upAndGuessedPics = upAndGuessedPicsFuture.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Get only the ones we need
                allIds.removeAll(upAndGuessedPics);

                //If no image fits, return empty string
                if(allIds.size() == 0){
                    pictureIdAvailable.accept("");
                }
                //else, return randomly chosen string
                else{
                    Random rn = new Random();
                    int index = rn.nextInt(allIds.size());
                    List<String> ids = new ArrayList<>();
                    ids.addAll(allIds);
                    pictureIdAvailable.accept(ids.get(index));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pictureIdAvailable.accept("");
            }
        });
    }

    /**
     * Returns the ids of all the uploaded and guessed pictures of a user
     * @return a future that holds the ids of all the uploaded and guessed pictures of a user
     */
    public CompletableFuture<Set<String>> getUploadedAndGuessedPictures(){
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
                picturesToReturn.complete(allPictures);
            }
        });

        return picturesToReturn;
    }

}
