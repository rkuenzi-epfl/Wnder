package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.github.wnder.Score;
import com.github.wnder.Storage;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Class defining a picture already existing on the database
 */
public class ExistingPicture extends Picture{

    //Image content (as bitmap)
    private Bitmap bmp;

    /**
     * Constructor for ExistingPicture
     * @param uniqueId the unique id of the picture
     */
    public ExistingPicture(String uniqueId){
        super(uniqueId);
        User user = GlobalUser.getUser();
        if(user instanceof SignedInUser){

            addToUserGuessedPictures(user.getUniqueId());
        }
    }

    /**
     * Set the bitmap of the picture
     * @param bmp bitmap to set
     */
    private void setBitmap(Bitmap bmp){
        this.bmp = bmp;
    }

    /**
     * Apply consumer function when image bitmap is available
     * @param bitmapAvailable consumer function to call when the location is available
     */
    public void onBitmapAvailable(Consumer<Bitmap> bitmapAvailable){
        //Check for errors
        if(bmp != null){

            bitmapAvailable.accept(bmp);
        } else {
            //Download bmp from database
            Task<byte[]> bitmapTask = Storage.downloadFromCloudStorage("pictures/"+getUniqueId()+".jpg");
            //If success, accept it
            bitmapTask.addOnSuccessListener((bytes) -> {
                setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                bitmapAvailable.accept(bmp);
            }).addOnFailureListener((e) ->{
                //TODO: do something if it fails
            });
        }
    }

    private void onUserLocationAvailable(String user, String variant, Consumer<Location> onUserLocationAvailable) {
        String[] path = {"pictures", getUniqueId(), "userData", variant};
        Storage.downloadFromFirestore(path).addOnSuccessListener((documentSnapshot) -> {
            GeoPoint geoPoint = documentSnapshot.getGeoPoint(user);
            Location location = new Location("");
            location.setLatitude(geoPoint.getLatitude());
            location.setLongitude(geoPoint.getLongitude());
            onUserLocationAvailable.accept(location);
        });
    }

    /**
     * Apply consumer function when the user's position during the guess is available
     * @param user unique identifier of the user
     * @param onUserPositionAvailable consumer function to call when the user's position during the guess is available
     */
    public void onUserPositionAvailable(String user, Consumer<Location> onUserPositionAvailable) {
        onUserLocationAvailable(user, "userLocations", onUserPositionAvailable);
    }

    /**
     * Apply consumer function when the user's guess is available
     * @param user unique identifier of the user
     * @param onUserGuessAvailable consumer function to call when the user's guess is available
     */
    public void onUserGuessAvailable(String user, Consumer<Location> onUserGuessAvailable) {
        onUserLocationAvailable(user, "userGuesses", onUserGuessAvailable);
    }

    /**
     * Apply consumer function when the user's radius setting is available
     * @param user unique identifier of the user
     * @param onRadiusAvailable consumer function to call when the user's radius setting is available
     */
    public void onUserRadiusAvailable(String user, Consumer<Integer> onRadiusAvailable) {
        String[] path = {"pictures", getUniqueId(), "userData", "userRadii"};
        Storage.downloadFromFirestore(path).addOnSuccessListener((documentSnapshot) -> {
            Integer radius = documentSnapshot.getLong(user).intValue();
            onRadiusAvailable.accept(radius);
        });
    }

    /**
     * Send a user's guess to the database
     * @param user the user guessing
     * @param guessedLocation the user's guessed location
     */
    public CompletableFuture<Void> sendUserGuess(String user, Location guessedLocation){
        CompletableFuture guessSent = new CompletableFuture();
        CompletableFuture scoreSent = new CompletableFuture();

        //Retrieve real location of picture
        onLocationAvailable((location)-> {

            //Add user to user who guessed the picture
            onUpdatedGuessesAvailable((userGuesses)-> {
                userGuesses.put(user, guessedLocation);

                //Convert it to GeoPoint to fit Firestore (because Location doesn't work)
                Map<String, Object> convertedGuesses = new TreeMap<>();
                for(Map.Entry<String, Location> e : userGuesses.entrySet()){
                    GeoPoint guess = new GeoPoint(e.getValue().getLatitude(),e.getValue().getLongitude());
                    convertedGuesses.put(e.getKey(), guess);
                }

                //Upload it back to Firestore
                String[] path = {"pictures", getUniqueId(), "userData", "userGuesses"};
                Storage.uploadToFirestore(convertedGuesses, path).addOnSuccessListener((result)-> guessSent.complete(null));
            });

            // Send corresponding score
            onUpdatedScoreboardAvailable((scoreboard)-> {

                //Compute score
                Double score = Score.computeScore(location, guessedLocation);
                scoreboard.put(user, score);

                //Convert the whole to fit into firestore
                Map<String, Object> convertedScoreboard = new TreeMap<>();
                for(Map.Entry<String, Double> e : scoreboard.entrySet()){
                    convertedScoreboard.put(e.getKey(), e.getValue());
                }

                //upload to firestore
                String[] path = {"pictures", getUniqueId(), "userData", "userScores"};
                Storage.uploadToFirestore(convertedScoreboard, path).addOnSuccessListener((result)-> scoreSent.complete(null));
            });
        });

        return CompletableFuture.allOf(guessSent, scoreSent);
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
                //Get uploaded and guessed pics
                List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
                List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");

                //create the lists if they don't exist
                if (guessedPictures == null) {
                    guessedPictures = new ArrayList<>();
                }
                if (uploadedPictures == null) {
                    uploadedPictures = new ArrayList<>();
                }

                //add picture to list if it isn't already in
                if (!guessedPictures.contains(getUniqueId())) {
                    guessedPictures.add(getUniqueId());
                }

                //Upload everything back to Firestore
                Map<String, Object> toUpload = new HashMap<>();
                toUpload.put("guessedPics", guessedPictures);
                toUpload.put("uploadedPics", uploadedPictures);
                Storage.uploadToFirestore(toUpload, "users", user);
            }
        });
    }

    public void skipPicture(){
        updateKarma(-1);
    }

    /**
     * Apply consumer function when the karma is available
     * @param karmaAvailable consumer function to call when the karma is available
     */
    public void onKarmaUpdated(Consumer<Map<String, Object>> karmaAvailable){
        Task<DocumentSnapshot> karmaTask = Storage.downloadFromFirestore("pictures", getUniqueId());
        karmaTask.addOnSuccessListener((documentSnapshot) -> {
            Map<String, Object> map = new HashMap<>();
            //We put these attributes back because if we don't, they disappear from the db
            map.put("latitude", documentSnapshot.getDouble("latitude"));
            map.put("longitude", documentSnapshot.getDouble("longitude"));
            map.put("karma", documentSnapshot.getLong("karma"));
            karmaAvailable.accept(map);
        });
    }

    /**
     * Modify the karma of a picture
     * @param delta the karma to add to the picture
     */
    public CompletableFuture<Void> updateKarma(int delta) {
        CompletableFuture toReturn = new CompletableFuture<>();
        //Get current karma
        onKarmaUpdated((pictureAttributes) -> {
            //Prepare new karma
            long newKarma = (long)pictureAttributes.get("karma") + delta;

            //Upload everything back to firestore
            Map<String, Object> toUpload = new HashMap<>();
            toUpload.put("latitude", pictureAttributes.get("latitude"));
            toUpload.put("longitude", pictureAttributes.get("longitude"));
            toUpload.put("karma", newKarma);
            Storage.uploadToFirestore(toUpload, "pictures", getUniqueId()).addOnSuccessListener((result) -> toReturn.complete(null));
        });
        return toReturn;
    }

    @Override
    public void onKarmaAvailable(Consumer<Long> karmaAvailable){
        onKarmaUpdated((attributesAvailable) -> {
            karmaAvailable.accept((long) attributesAvailable.get("karma"));
        });
    }

    /**
     * Apply consumer function on picture's location with purposely reduced precision
     * @param approximateLocationAvailable consumer function to call when the location is available
     */
    public void onApproximateLocationAvailable(Consumer<Location> approximateLocationAvailable) {
        //Get exact location
        onLocationAvailable((location) -> {
            //randomize location a bit in a 200 meters radius
            double radius = 200; // meters
            Random random = new Random();
            double distance = Math.sqrt(random.nextDouble()) * radius / 111000;
            double angle = 2 * Math.PI * random.nextDouble();
            double longitude_delta = distance * Math.cos(angle);
            double latitude_delta = distance * Math.sin(angle);
            longitude_delta /= Math.cos(Math.toRadians(location.getLatitude()));

            //Create and set new location
            Location al = new Location("");
            al.setLongitude(location.getLongitude() + longitude_delta);
            al.setLatitude(location.getLatitude() + latitude_delta);

            //accept location with reduced precision
            approximateLocationAvailable.accept(al);
        });
    }

    /**
     * To be used when a picture is guessed, add 1 karma to it.
     */
    public void addKarmaForGuess(){
        updateKarma(1);
    }

    /**
     * To be used when a picture is reported, subtract 10 karma from it.
     */
    public void subtractKarmaForReport() {
        updateKarma(-10);
    }
}
