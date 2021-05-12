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

public class UserDatabase {
    private NetworkInformation networkInfo;
    private final InternalCachePictureDatabase ICPD;

    private CompletableFuture<List<String>> guessedPics;

    public UserDatabase(Context context){
        networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        ICPD = new InternalCachePictureDatabase(context);

        guessedPics = new CompletableFuture<>();

        if(networkInfo.isNetworkAvailable()){
            guessedPics = getAllGuessedPictures();
        }
    }

    public CompletableFuture<Double> getTotalScore(){
        CompletableFuture<Double> totalScoreFuture = new CompletableFuture<>();

        getAllScores().thenAccept(scores -> {
            double totalScore = 0;
            for(double score: scores){
                totalScore = totalScore + score;
            }
            totalScoreFuture.complete(totalScore);
        });

        return totalScoreFuture;
    }

    public CompletableFuture<Integer> getNbrOfGuessedPictures(){
        CompletableFuture<Integer> toRet = new CompletableFuture<>();
        guessedPics.thenAccept(pics -> {
           toRet.complete(pics.size());
        });
        return toRet;
    }

    public CompletableFuture<Double> getAverageScore(){
        CompletableFuture<Double> averageScore = new CompletableFuture<>();
        getNbrOfGuessedPictures().thenAccept(nbr -> {
            if(nbr == 0){
                averageScore.complete(0.);
            }
            else{
                getTotalScore().thenAccept(total -> averageScore.complete((double) total/nbr));
            }
        });

        return averageScore;
    }

    public CompletableFuture<List<String>> getGuessedPics(){
        return guessedPics;
    }


    private CompletableFuture<Set<Double>> getAllScores(){
        CompletableFuture<Set<Double>> allScoresFuture = new CompletableFuture<>();
        //Set<CompletableFuture<Double>> futureScores = new HashSet<>();
        Set<Double> allScores = new HashSet<>();

        guessedPics.thenAccept(pics ->{
            CompletableFuture[] futureScores = new CompletableFuture[pics.size()];
            for(String uniqueId: pics){
                futureScores[pics.indexOf(uniqueId)] = (ICPD.getScoreboard(uniqueId).thenApply(s -> s.get(GlobalUser.getUser().getName())));
            }
            for(CompletableFuture<Double> futureScore: futureScores){
                futureScore.thenAccept(score -> allScores.add(score));
            }
            CompletableFuture<Void> allScoresReceived = CompletableFuture.allOf(futureScores);
            allScoresReceived.thenAccept(empty -> allScoresFuture.complete(allScores));
        });

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
