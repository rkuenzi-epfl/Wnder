package com.github.wnder.user;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
     * Gets total score of a user
     * @return added scores of all user's guesses
     */
    public CompletableFuture<Double> getTotalScore(){
        CompletableFuture<Double> totalScoreFuture = new CompletableFuture<>();

        //Get all scores
        getAllScores().thenAccept(scores -> {
            double totalScore = 0;
            //for each score, add it to the total
            for(double score: scores){
                totalScore = totalScore + score;
            }
            //Then complete with the total
            totalScoreFuture.complete(totalScore);
        });

        return totalScoreFuture;
    }

    /**
     * Get a user's number of guessed pictures
     * @return user's number of guessed pictures
     */
    public CompletableFuture<Integer> getNbrOfGuessedPictures(){
        CompletableFuture<Integer> toRet = new CompletableFuture<>();

        //get the pics
        guessedPics.thenAccept(pics -> {
            //complete with the size of the list, once the pics gotten
           toRet.complete(pics.size());
        });
        return toRet;
    }

    /**
     * Get a user's average score
     * @return user's guess average score
     */
    public CompletableFuture<Double> getAverageScore(){
        CompletableFuture<Double> averageScore = new CompletableFuture<>();

        //Get the nbr of guessed pictures
        getNbrOfGuessedPictures().thenAccept(nbr -> {
            //if not pic, then the average score returned is 0
            if(nbr == 0){
                averageScore.complete(0.);
            }
            //if pics, get the total score, then compute average and complete the future with it
            else{
                getTotalScore().thenAccept(total -> averageScore.complete((double) total/nbr));
            }
        });

        return averageScore;
    }

    /**
     * Get a user's guessed pics
     * @return list of all uniqueIds of the pictures a user guessed
     */
    public CompletableFuture<List<String>> getGuessedPics(){
        return guessedPics;
    }

    /**
     * get all the scores of a user
     * @return a set of all the scores a user achieved
     */
    private CompletableFuture<Set<Double>> getAllScores(){
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
    private CompletableFuture<List<String>> getAllGuessedPictures(){
        CompletableFuture<List<String>> guessedPicsFuture = new CompletableFuture<>();

        //download the guessed pics from firestor
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
