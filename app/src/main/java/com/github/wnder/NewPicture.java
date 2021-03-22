package com.github.wnder;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.tasks.Tasks.await;

public class NewPicture implements Picture{
    //Storage
    private Storage storage;

    //User id
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

    /**
     * Constructor for a new picture that doesn't exist in the db already
     * @param user user's id
     * @param longitude longitude of the image's location
     * @param latitude latitude of the image's location
     * @param uri uri of the image
     */
    public NewPicture(String user, long longitude, long latitude, Uri uri){
        //instantiate parameters
        this.storage = new Storage();

        this.user = user;

        this.longitude = longitude;
        this.latitude = latitude;
        this.uri = uri;

        //default instantiation for the scoreboard
        //necessary to have the correct documents created in firestore
        this.scoreboard = new HashMap<String, Object>();
        this.scoreboard.put("default", -1.);

        //default instantiation for the guesses
        //necessary to have the correct documents created in firestore
        this.guesses = new HashMap<>();
        ArrayList<Object> defaultCoordinates = new ArrayList<>();
        defaultCoordinates.add(-1);
        defaultCoordinates.add(-1);
        this.guesses.put("default", defaultCoordinates);

        //Unique id of the picture, depends on the user + the time
        this.uniqueId = this.user + Calendar.getInstance().getTimeInMillis();
    }

    private void addPhotoToUploadedUserPhoto(){
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
    }

    /**
     * Send this picture to the database
     * @return true if the tasks were successfully created
     */
    public Boolean sendPictureToDb(){
        //coordinates
        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("longitude", this.longitude);
        coordinates.put("latitude", this.latitude);
        this.storage.uploadToFirestore(coordinates, "pictures", this.uniqueId);

        //userGuesses
        String[] path1 = {"pictures", this.uniqueId, "userData", "userGuesses"};
        this.storage.uploadToFirestore(this.guesses, path1);

        //userScores
        String[] path2 = {"pictures", this.uniqueId, "userData", "userGuesses"};
        this.storage.uploadToFirestore(this.scoreboard, path2);

        //Send picture to Cloud Storage
        this.storage.uploadToCloudStorage(this.uri, "pictures/"+this.uniqueId+".jpg");

        return true;
    }

    public String getUniqueId(){
        return uniqueId;
    }

    /**
     * Returns the uri of the image
     * @return uri of the picture
     */
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
