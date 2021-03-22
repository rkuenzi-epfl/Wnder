package com.github.wnder;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPicture implements Picture{
    private Storage storage;

    private String user;

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

    public NewPicture(String user, long longitude, long latitude, Uri uri){
        this.storage = new Storage();

        this.user = user;

        this.longitude = longitude;
        this.latitude = latitude;
        this.uri = uri;

        this.scoreboard = new HashMap<String, Object>();
        this.scoreboard.put("default", -1.);

        this.guesses = new HashMap<>();
        ArrayList<Object> defaultCoordinates = new ArrayList<>();
        defaultCoordinates.add(-1);
        defaultCoordinates.add(-1);
        this.guesses.put("default", defaultCoordinates);

        this.uniqueId = this.user + Calendar.getInstance().getTimeInMillis();
    }

    public Boolean sendPictureToDb(){
        //coordinates
        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("longitude", this.longitude);
        coordinates.put("latitude", this.latitude);
        this.storage.uploadToFirestore(coordinates, "pictures", this.uniqueId);

        //userGuesses
        this.storage.uploadToFirestore(this.guesses, "pictures", "userData", this.uniqueId, "userGuesses");

        //userScores
        this.storage.uploadToFirestore(this.scoreboard, "pictures", "userData", this.uniqueId, "userScores");

        //Send picture to Cloud Storage
        this.storage.uploadToCloudStorage(this.uri, "pictures/"+this.uniqueId);

        //upload specific user data
        Task<DocumentSnapshot> userUploaded = storage.downloadFromFirestore("users", user);
        userUploaded.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> guessedPictures = (List<String>) documentSnapshot.getData().get("guessedPics");
                List<String> uploadedPictures = (List<String>) documentSnapshot.getData().get("uploadedPics");
                if (guessedPictures == null) {
                    guessedPictures = new ArrayList<>();
                }
                if (uploadedPictures == null) {
                    uploadedPictures = new ArrayList<>();
                }
                if (!uploadedPictures.contains(uniqueId)){
                    uploadedPictures.add(uniqueId);
                }
                Map<String, Object> toUpload = new HashMap<>();
                toUpload.put("guessedPics", guessedPictures);
                toUpload.put("uploadedPics", uploadedPictures);
                storage.uploadToFirestore(toUpload, "users", user);
            }
        });

        return true;
    }

    public String getUniqueId(){
        return uniqueId;
    }

    public Uri getUri(){
        return uri;
    }

    public Long getLongitude(){
        return longitude;
    }

    public Long getLatitude(){
        return latitude;
    }

    public Map<String, Object> getScoreboard(){
        return scoreboard;
    }

    public Map<String, Object> getGuesses(){
        return guesses;
    }
}
