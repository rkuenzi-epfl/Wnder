package com.github.wnder.user;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import com.github.wnder.Storage;
import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.picture.InternalCachePictureDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This class brings in useful methods to get user information
 */
public class UserDatabase {
    private NetworkInformation networkInfo;
    private final InternalCachePictureDatabase ICPD;

    private CompletableFuture<List<String>> guessedPics;

    /**
     * Constructor for a userdb
     * @param context app context
     */
    public UserDatabase(Context context){
        //setup network info
        networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        ICPD = new InternalCachePictureDatabase(context);

        guessedPics = new CompletableFuture<>();

        //only available if there's an internet connection, else every return will be empty
        if(networkInfo.isNetworkAvailable()){
            guessedPics = getAllGuessedPictures();
        }
        else{
            List<String> emptyList = new ArrayList<>();
            guessedPics = new CompletableFuture<>();
            guessedPics.complete(emptyList);
        }
    }

    /**
     * get all the scores of a user
     * @return a set of all the scores a user achieved
     */
    public CompletableFuture<Set<Double>> getAllScores(){
        CompletableFuture<Set<Double>> allScoresFuture = new CompletableFuture<>();
        Set<Double> allScores = new HashSet<>();

        //get all the guessed pics
        guessedPics.thenAccept(pics ->{
            //create an array to store all the score futures
            CompletableFuture[] futureScores = new CompletableFuture[pics.size()];

            //for each guessed pic, get its score and store this future into the array
            for(String uniqueId: pics){
                futureScores[pics.indexOf(uniqueId)] = (ICPD.getScoreboard(uniqueId).thenApply(s -> s.get(GlobalUser.getUser().getName())));
            }

            //Once a score is completed, complete add it to all the scores already completed
            for(CompletableFuture<Double> futureScore: futureScores){
                futureScore.thenAccept(score -> allScores.add(score));
            }

            //once all scores have been completed, complete the future
            CompletableFuture<Void> allScoresReceived = CompletableFuture.allOf(futureScores);
            allScoresReceived.thenAccept(empty -> allScoresFuture.complete(allScores));
        });

        return allScoresFuture;
    }

    /**
     * Get all the guessed pictures of a user directly from the database
     * @return a list of all the uniqueIds of the pictures a user guessed
     */
    public CompletableFuture<List<String>> getAllGuessedPictures(){
        CompletableFuture<List<String>> guessedPicsFuture = new CompletableFuture<>();

        //download the guessed pics from firestore
        Task<DocumentSnapshot> guessedPics = Storage.downloadFromFirestore("users", GlobalUser.getUser().getName());
        guessedPics.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            //if success, complete future with it
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                guessedPicsFuture.complete((ArrayList) snapshot.get("guessedPics"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            //else, complete with empty list
            @Override
            public void onFailure(@NonNull Exception e) {
                guessedPicsFuture.complete(new ArrayList<>());
            }
        });
        return guessedPicsFuture;
    }

}
