package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExistingPicture implements Picture{

    //Image unique ID
    private String uniqueId;
    private Bitmap bmp;

    //Image location
    private Location location;

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

        TaskAggregator.aggregate(picture, pictureFuture);

        return pictureFuture;
    }

    private ExistingPicture(String uniqueId){
        this.uniqueId = uniqueId;
    }

    private void setBitmap(Bitmap bmp){
        this.bmp = bmp;
    }

    private void setLocation(double longitude, double latitude){
        location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    private void setScoreboard(Map<String, Object> scoreboard){
        this.scoreboard = scoreboard;
    }

    private void setGuesses(Map<String, Object> guesses){
        this.guesses = guesses;
    }

    /**
     * Send a user's score and guess to the database
     * @param user the user
     * @param score the user's score
     */
    private void sendUserGuess(String user, Double score){
        //userGuesses
        ArrayList<Object> userCoor = new ArrayList<>();
        userCoor.add(location.getLatitude());
        userCoor.add(location.getLongitude());
        this.guesses.put(user, userCoor);
        String[] path1 = {"pictures", this.uniqueId, "userData", "userGuesses"};
        Storage.uploadToFirestore(this.guesses, path1);

        //userScores
        this.scoreboard.put(user, score);
        String[] path2 = {"pictures", this.uniqueId, "userData", "userScores"};
        Storage.uploadToFirestore(this.scoreboard, path2);
    }

    /**
     * Add a picture to the guessed pictures of a user on the database
     * @param user the user
     */
    private void addToUserGuessedPictures(String user) {
        //user guessed pictures
        Storage.downloadFromFirestore("users", user).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                Storage.uploadToFirestore(toUpload, "users", user);
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
        double score = Score.computeScore(this.location.getLatitude(), this.location.getLongitude(), guessedLatitude, guessedLongitude);

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
    public Object getUserGuess(String user){
        if(guesses.containsKey(user)){
            return guesses.get(user);
        }
        else{
            return new ArrayList<>();
        }
    }

    public String getUniqueId(){
        return uniqueId;
    }

    public Bitmap getBmp(){
        return bmp;
    }

    public Location getLocation(){
        return location;
    }

    public Map<String, Object> getScoreboard(){
        return scoreboard;
    }

    public Map<String, Object> getGuesses(){
        return guesses;
    }

    private static class TaskAggregator{

        public static Task<DocumentSnapshot> getCoorTask(ExistingPicture picture){
            Task<DocumentSnapshot> coorTask = Storage.downloadFromFirestore("pictures", picture.uniqueId);
            coorTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    double longitude = (double)documentSnapshot.getDouble("longitude");
                    double latitude = (double)documentSnapshot.getDouble("latitude");
                    picture.setLocation(longitude, latitude);
                }
            });
            return coorTask;

        }

        public static Task<DocumentSnapshot> getSimpleSetterTask(ExistingPicture picture, String setter){
            String[] path = new String[]{"pictures", picture.uniqueId, "userData", setter};
            OnSuccessListener<DocumentSnapshot> successFunction = new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {};};

            if(setter.equals("userGuesses")){
                successFunction = ((DocumentSnapshot documentSnapshot) -> {
                    picture.setGuesses(documentSnapshot.getData());
                });
            } else if(setter.equals("userScores")){
                successFunction = ((DocumentSnapshot documentSnapshot) -> {
                    picture.setScoreboard(documentSnapshot.getData());
                });
            }

            Task<DocumentSnapshot> guessTask = Storage.downloadFromFirestore(path);
            guessTask.addOnSuccessListener(successFunction);
            return guessTask;
        }

        public static Task<byte[]> getPictureTask(ExistingPicture picture){
            //retrieve picture
            Task<byte[]> pictureTask = Storage.downloadFromCloudStorage("pictures/"+picture.uniqueId+".jpg");
            pictureTask.addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    picture.setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }
            });
            return pictureTask;
        }

        public static void aggregate(ExistingPicture picture, CompletableFuture<ExistingPicture> pictureFuture){
            String uniqueId = picture.getUniqueId();

            Task<DocumentSnapshot> coorTask = getCoorTask(picture);

            Task<DocumentSnapshot> guessTask = getSimpleSetterTask(picture, "userGuesses");

            Task<DocumentSnapshot> scoreTask = getSimpleSetterTask(picture, "userScores");

            Task<byte[]> pictureTask = getPictureTask(picture);

            Tasks.whenAllSuccess(coorTask, guessTask, scoreTask, pictureTask).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                @Override
                public void onSuccess(List<Object> resultsList) {
                    PictureCache.addPicture(uniqueId, picture);
                    pictureFuture.complete(picture);
                }
            });
        }
    }
}
