package com.github.wnder.user;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Utils for the user database class
 */
public class UserDatabaseUtils {
    private CompletableFuture<List<String>> guessedPics;
    private CompletableFuture<Set<Double>> scores;

    /**
     * Constructor
     * @param guessedPics future of all user guessed pics
     * @param scores future of all user scores
     */
    public UserDatabaseUtils(CompletableFuture<List<String>> guessedPics, CompletableFuture<Set<Double>> scores){
        this.guessedPics = guessedPics;
        this.scores = scores;
    }

    /**
     * Gets total score of a user
     * @return added scores of all user's guesses
     */
    public CompletableFuture<Double> getTotalScore(){
        CompletableFuture<Double> totalScoreFuture = new CompletableFuture<>();

        //Get all scores
        scores.thenAccept(scores -> {
            double totalScore = 0;
            //for each score, add it to the total
            for(Double score: scores){
                if(score != null) {
                    totalScore = totalScore + score;
                }
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
                getTotalScore().thenAccept(total -> {
                    averageScore.complete((double) total/nbr);
                });
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
}
