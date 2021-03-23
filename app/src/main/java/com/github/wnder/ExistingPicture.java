package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExistingPicture implements Picture{
    //Storage
    private Storage storage;

    //Image unique ID
    private String uniqueId;
    private Bitmap bmp;

    //Image location
    private long longitude;
    private long latitude;

    //User data for the image: global scoreboard + all guesses
    private Map<String, Object> scoreboard;
    //Map<[user ID], Map<[longitude/latitude], [value]>>
    private Map<String, Object> guesses;


    /**
     * Start loading a picture with a given ID
     *
     * @param uniqueId
     * @return a task that succeed when all subtask succeed (fail otherwise)
     */
    public static CompletableFuture<ExistingPicture> loadExistingPicture(String uniqueId){

        CompletableFuture<ExistingPicture> pictureFuture = new CompletableFuture<ExistingPicture>();
        ExistingPicture picture = new ExistingPicture(uniqueId);
        if(PictureCache.isInCache(uniqueId)){
            pictureFuture.complete((ExistingPicture) PictureCache.getPicture(uniqueId));
            return pictureFuture;
        }

        picture.storage = new Storage();
        Task<DocumentSnapshot> coorTask = picture.storage.downloadFromFirestore("pictures", picture.uniqueId);
        coorTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long longitude = (long)documentSnapshot.get("longitude");
                long latitude = (long)documentSnapshot.get("latitude");
                picture.setLocation(longitude, latitude);
            }
        });

        String[] path1 = {"pictures", picture.uniqueId, "userData", "userGuesses"};
        Task<DocumentSnapshot> guessTask = picture.storage.downloadFromFirestore(path1);
        guessTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                picture.setGuesses(documentSnapshot.getData());
            }
        });

        //retrieve scores
        String[] path2 = {"pictures", picture.uniqueId, "userData", "userScores"};
        Task<DocumentSnapshot> scoreTask = picture.storage.downloadFromFirestore(path2);
        scoreTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                picture.setScoreboard(documentSnapshot.getData());
            }
        });

        //retrieve picture
        Task<byte[]> pictureTask = picture.storage.downloadFromCloudStorage("pictures/"+picture.uniqueId+".jpg");
        pictureTask.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                picture.setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        });

        Tasks.whenAllSuccess(coorTask, guessTask, scoreTask, pictureTask).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> resultsList) {
                PictureCache.addPicture(uniqueId, picture);
                pictureFuture.complete(picture);
            }
        });

        return pictureFuture;
    }

    private ExistingPicture(String uniqueId){
        this.uniqueId = uniqueId;
    }

    private void setBitmap(Bitmap bmp){
        this.bmp = bmp;
    }

    private void setLocation(long longitude, long latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    private void setScoreboard(Map<String, Object> scoreboard){
        this.scoreboard = scoreboard;
    }

    private void setGuesses(Map<String, Object> guesses){
        this.guesses = guesses;
    }


    /**
     * Constructor for a picture already existing on db
     * @param uniqueId id of the image
     */
   /* public ExistingPicture(String uniqueId){
        //put dummy values in case the instantiation doesn't work
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
        String[] path1 = {"pictures", this.uniqueId, "userData", "userGuesses"};
        Task<DocumentSnapshot> guessTask = storage.downloadFromFirestore(path1);
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
        String[] path2 = {"pictures", this.uniqueId, "userData", "userScores"};
        Task<DocumentSnapshot> scoreTask = storage.downloadFromFirestore(path2);
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
        Task<byte[]> pictureTask = storage.downloadFromCloudStorage("pictures/"+this.uniqueId+".jpg");
        pictureTask.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //set to dummy values
                bmp = null;
            }
        });
    }*/

    /**
     * Send a user's score and guess to the database
     * @param user the user
     * @param score the user's score
     */
    private void sendUserGuess(String user, Double score){
        //userGuesses
        ArrayList<Object> userCoor = new ArrayList<>();
        userCoor.add(longitude);
        userCoor.add(latitude);
        this.guesses.put(user, userCoor);
        String[] path1 = {"pictures", this.uniqueId, "userData", "userGuesses"};
        storage.uploadToFirestore(this.guesses, path1);

        //userScores
        this.scoreboard.put(user, score);
        String[] path2 = {"pictures", this.uniqueId, "userData", "userScores"};
        storage.uploadToFirestore(this.scoreboard, path2);
    }

    /**
     * Add a picture to the guessed pictures of a user on the database
     * @param user the user
     */
    private void addToUserGuessedPictures(String user) {
        //user guessed pictures
        storage.downloadFromFirestore("users", user).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                if (!guessedPictures.contains(uniqueId)) {
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

    /**
     * Compute user score given his guess + upload his guess and his score to the database
     * @param guessedLongitude: user longitude guess
     * @param guessedLatitude: user latitude guess
     * @return: user score
     */
    public Double computeScoreAndSendToDb(String user, double guessedLongitude, double guessedLatitude) throws IllegalStateException{
        double score = Score.computeScore(this.latitude, this.longitude, guessedLatitude, guessedLongitude);

        sendUserGuess(user, score);
        addToUserGuessedPictures(user);

        return score;
    }

    /**
     * Get a user's score
     * @param user: user ID
     * @return: user score if it exists, -1 else
     * @throws IllegalStateException if the image is not correctly initialized
     */
    public Object getUserScore(String user) throws IllegalStateException{
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
     * * @throws IllegalStateException if the image is not correctly initialized
     */
    public Object getUserGuess(String user) throws IllegalStateException{
        if(guesses.containsKey(user)){
            return guesses.get(user);
        }
        else{
            return new ArrayList<>();
        }
    }

    public String getUniqueId() throws IllegalStateException{
        return uniqueId;
    }

    public Bitmap getBmp() throws IllegalStateException{
        return bmp;
    }

    public LatLng getLatLng() throws IllegalStateException{
        LatLng latlng = new LatLng(latitude, longitude);
        return latlng;
    }

    public Map<String, Object> getScoreboard() throws IllegalStateException{
        return scoreboard;
    }

    public Map<String, Object> getGuesses() throws IllegalStateException{
        return guesses;
    }
}
