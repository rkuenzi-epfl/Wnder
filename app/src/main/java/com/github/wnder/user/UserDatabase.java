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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UserDatabase {
    private NetworkInformation networkInfo;
    private final InternalCachePictureDatabase ICPD;

    private List<String> guessedPics;
    private double totalScore;

    public UserDatabase(Context context){
        networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        ICPD = new InternalCachePictureDatabase(context);

        totalScore = 0;
        guessedPics = new ArrayList<>();

        if(networkInfo.isNetworkAvailable()){
            getAllGuessedPictures().thenAccept(pics -> guessedPics = pics);
            getAllScores();
        }
    }

    public double getTotalScore(){
        return totalScore;
    }

    public double getNbrOfGuessedPictures(){
            return getGuessedPics().size();
    }

    public double getAverageScore(){
        return (double) totalScore/getNbrOfGuessedPictures();
    }

    public List<String> getGuessedPics(){
        return guessedPics;
    }

    private void addScoreToTotal(Double score){
        totalScore = totalScore + score;
    }

    private CompletableFuture<List<Double>> getAllScores(){
        CompletableFuture<List<Double>> allScoresFuture = new CompletableFuture<>();

        for(String uniqueId: guessedPics){
            ICPD.getScoreboard(uniqueId).thenAccept(scoreboard -> addScoreToTotal(scoreboard.get(GlobalUser.getUser().getName())));
        }

        return allScoresFuture;
    }

    private CompletableFuture<List<String>> getAllGuessedPictures(){
        CompletableFuture<List<String>> guessedPicsFuture = new CompletableFuture<>();
        Task<DocumentSnapshot> guessedPics = Storage.downloadFromFirestore("users", GlobalUser.getUser().getName());
        guessedPics.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                guessedPicsFuture.complete((ArrayList) snapshot.get("guessedPics"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                guessedPicsFuture.complete(new ArrayList<>());
            }
        });
        return guessedPicsFuture;
    }

}
