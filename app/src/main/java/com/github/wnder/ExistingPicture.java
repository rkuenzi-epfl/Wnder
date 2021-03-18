package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExistingPicture implements Picture{
    private Storage storage;

    //Image unique ID
    private String uniqueId;
    private Uri uri;

    //Image location
    private long longitude;
    private long latitude;

    //User data for the image: global scoreboard + all guesses
    private Map<String, Object> scoreboard;
    //Map<[user ID], Map<[longitude/latitude], [value]>>
    private Map<String, Object> guesses;

    //Constructor for the image
    public ExistingPicture(String uniqueId){
        this.longitude = -1;
        this.latitude = -1;

        this.storage = new Storage();
        this.uniqueId = uniqueId;

        //retrieve coordinates
        Task<DocumentSnapshot> coorTask = storage.downloadFromFirestore("pictures", this.uniqueId);
        coorTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                longitude = (long)documentSnapshot.get("longitude");
                latitude = (long)documentSnapshot.get("latitude");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to dummy values
                longitude = -1;
                latitude = -1;
            }
        });

        //retrieve guesses
        Task<DocumentSnapshot> guessTask = storage.downloadFromFirestore("pictures", "userData", this.uniqueId, "userGuesses");
        guessTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                guesses = documentSnapshot.getData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to dummy values
                guesses = new HashMap<>();
            }
        });

        //retrieve scores
        Task<DocumentSnapshot> scoreTask = storage.downloadFromFirestore("pictures", "userData", this.uniqueId, "userScores");
        scoreTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                scoreboard = documentSnapshot.getData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to dummy values
                scoreboard = new HashMap<>();
            }
        });

        //retrieve picture
        Task<byte[]> pictureTask = storage.downloadFromCloudStorage("pictures/"+this.uniqueId+"/"+this.uniqueId);
        pictureTask.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                uri = Uri.parse("android.resource://raw/ladiag.jpg");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to dummy values
                uri = Uri.parse("android.resource://raw/ladiag.jpg");
            }
        });
    }

    /**
     * Send a user's guess and score to the database
     * @param user: user id
     * @param score: user score
     * @param longitude: user longitude guess
     * @param latitude: user latitude guess
     */
    private void sendUserData(String user, Double score, double longitude, double latitude) throws IllegalStateException{
        if(this.guesses != null && this.scoreboard != null) {
            //userGuesses
            ArrayList<Object> userCoor = new ArrayList<>();
            userCoor.add(longitude);
            userCoor.add(latitude);
            this.guesses.put(user, userCoor);
            storage.uploadToFirestore(this.guesses, "pictures", "userData", this.uniqueId, "userGuesses");

            //userScores
            this.scoreboard.put(user, score);
            storage.uploadToFirestore(this.scoreboard, "pictures", "userData", this.uniqueId, "userScores");

            //user guessed pictures
            Task<DocumentSnapshot> userGuessed = storage.downloadFromFirestore("users", user);
            userGuessed.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                    if (!guessedPictures.contains(uniqueId)){
                        guessedPictures.add(uniqueId);
                    }
                    Map<String, Object> toUpload = new HashMap<>();
                    toUpload.put("guessedPics", guessedPictures);
                    toUpload.put("uploadedPics", uploadedPictures);
                    storage.uploadToFirestore(toUpload, "users", user);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //set to dummy values
                    scoreboard = new HashMap<>();
                }
            });
        }
        else{
            throw new IllegalStateException("Image not correctly initialized");
        }
    }

    /**
     * Compute user score given his guess + upload his guess and his score to the database
     * @param guessedLongitude: user longitude guess
     * @param guessedLatitude: user latitude guess
     * @return: user score
     */
    public Double computeScoreAndSendToDb(String user, double guessedLongitude, double guessedLatitude) throws IllegalStateException{
        double score = Math.max(100 - Math.abs(guessedLongitude - longitude) - Math.abs(guessedLatitude - latitude), 0);
        sendUserData(user, score, guessedLongitude, guessedLatitude);
        return score;
    }

    /**
     * Get a user's score
     * @param user: user ID
     * @return: user score if it exists, -1 else
     */
    public Object getUserScore(String user) throws IllegalStateException{
        if(scoreboard == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        if(scoreboard.containsKey(user)){
            return scoreboard.get(user);
        }
        else{
            return -1.;
        }
    }

    /**
     * Get a user's guess
     * @param user: user ID
     * @return: user guess if it exists, empty map else
     */
    public Object getUserGuess(String user){
        if(guesses == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        if(guesses.containsKey(user)){
            return guesses.get(user);
        }
        else{
            return new ArrayList<>();
        }
    }

    public String getUniqueId(){
        if(uniqueId == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return uniqueId;
    }

    public Uri getUri(){
        if(uri == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return uri;
    }

    public Long getLongitude(){
        if(longitude == -1){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return longitude;
    }

    public Long getLatitude(){
        if(latitude == -1){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return latitude;
    }

    public Map<String, Object> getScoreboard(){
        if(scoreboard == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return scoreboard;
    }

    public Map<String, Object> getGuesses(){
        if(guesses == null){
            throw new IllegalStateException("Image not correctly initialized");
        }
        return guesses;
    }
}
