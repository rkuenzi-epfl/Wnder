package com.github.wnder.picture;

import android.location.Location;
import android.net.Uri;

import com.github.wnder.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class NewPicture extends Picture{
    //User id
    private String user;

    //Image content (as uri)
    private Uri uri;

    // Empty user data to create firebase documents
    private Map<String, Object> emptyScoreboard;
    private Map<String, Object> emptyGuesses;

    /**
     * Constructor for a new picture that doesn't exist in the db already
     * @param user user's id
     * @param location the image location
     * @param uri uri of the image
     */
    public NewPicture(String user, Location location, Uri uri){
        //Unique id of the picture, depends on the user + the time
        super(user + Calendar.getInstance().getTimeInMillis(), location);

        //instantiate parameters
        this.user = user;
        this.uri = uri;

        // Default creation value use the user as field name

        //default instantiation for the scoreboard
        //necessary to have the correct documents created in firestore
        emptyScoreboard= new HashMap<>();
        emptyScoreboard.put(user, -1.);

        //default instantiation for the guesses ()
        //necessary to have the correct documents created in firestore
        emptyGuesses = new HashMap<>();
        GeoPoint defaultGuess = new GeoPoint(location.getLatitude(),location.getLongitude());
        emptyGuesses.put(user, defaultGuess);
    }

    private void addPhotoToUploadedUserPhoto(){
        //upload specific user data
        Task<DocumentSnapshot> userUploaded = Storage.downloadFromFirestore("users", user);
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
                if (!uploadedPictures.contains(getUniqueId())){
                    uploadedPictures.add(getUniqueId());
                }
                Map<String, Object> toUpload = new HashMap<>();
                toUpload.put("guessedPics", guessedPictures);
                toUpload.put("uploadedPics", uploadedPictures);
                Storage.uploadToFirestore(toUpload, "users", user);
            }
        });
    }

    /**
     * Send this picture to the database
     * @return true if the tasks were successfully created
     */
    public CompletableFuture<Void> sendPictureToDb(){
        CompletableFuture<Void> updateStatus= new CompletableFuture();

        //coordinates
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("latitude", this.getLocation().getLatitude());
        attributes.put("longitude", this.getLocation().getLongitude());
        attributes.put("karma", 0);
        Task<Void> locationTask = Storage.uploadToFirestore(attributes, "pictures", getUniqueId());

        //userGuesses
        String[] path1 = {"pictures", getUniqueId(), "userData", "userGuesses"};
        Task<Void> guessesTask = Storage.uploadToFirestore(emptyGuesses, path1);

        //userScores
        String[] path2 = {"pictures", getUniqueId(), "userData", "userScores"};
        Task<Void> scoreTask = Storage.uploadToFirestore(emptyScoreboard, path2);

        //Send picture to Cloud Storage
        Task<UploadTask.TaskSnapshot> pictureTask = Storage.uploadToCloudStorage(this.uri, "pictures/"+getUniqueId()+".jpg");
        this.addPhotoToUploadedUserPhoto();

        Tasks.whenAllSuccess(locationTask, guessesTask, scoreTask, pictureTask).addOnSuccessListener((results)->{
            updateStatus.complete(null);
        });
        return updateStatus;
    }

    /**
     * Returns the uri of the image
     * @return uri of the picture
     */
    public Uri getUri(){
        return uri;
    }

    /**
     * Returns the location of the image
     * @return location of the picture
     */
    public Location getLocation(){
        return new Location(location);
    }
}
