package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.github.wnder.Score;
import com.github.wnder.Storage;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ExistingPicture extends Picture{

    //Image content (as bitmap)
    private Bitmap bmp;

    public ExistingPicture(String uniqueId){
        super(uniqueId);
        User user = GlobalUser.getUser();
        if(user instanceof SignedInUser){

            addToUserGuessedPictures(user.getName());
        }
    }

    private void setBitmap(Bitmap bmp){
        this.bmp = bmp;
    }

    /**
     * Apply consumer function when image bitmap is available
     * @param bitmapAvailable consumer function to call when the location is available
     */
    public void onBitmapAvailable(Consumer<Bitmap> bitmapAvailable){
        if(bmp != null){

            bitmapAvailable.accept(bmp);
        } else {

            Task<byte[]> bitmapTask = Storage.downloadFromCloudStorage("pictures/"+getUniqueId()+".jpg");
            bitmapTask.addOnSuccessListener((bytes) -> {
                setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                bitmapAvailable.accept(bmp);
            });
        }
    }

    /**
     * Send a user's guess to the database
     * @param user the user guessing
     * @param guessedLocation the user's guessed location
     */
    public CompletableFuture<Void> sendUserGuess(String user, Location guessedLocation){
        CompletableFuture guessSent = new CompletableFuture();
        CompletableFuture scoreSent = new CompletableFuture();
        onLocationAvailable((location)-> {

            // Send guessed location
            onUpdatedGuessesAvailable((userGuesses)-> {
                userGuesses.put(user, guessedLocation);

                Map<String, Object> convertedGuesses = new TreeMap<>();
                for(Map.Entry<String, Location> e : userGuesses.entrySet()){
                    GeoPoint guess = new GeoPoint(e.getValue().getLatitude(),e.getValue().getLongitude());
                    convertedGuesses.put(e.getKey(), guess);
                }
                String[] path = {"pictures", getUniqueId(), "userData", "userGuesses"};
                Storage.uploadToFirestore(convertedGuesses, path).addOnSuccessListener((result)-> guessSent.complete(null));
            });

            // Send corresponding score
            onUpdatedScoreboardAvailable((scoreboard)-> {

                Double score = Score.computeScore(location, guessedLocation);
                scoreboard.put(user, score);
                Map<String, Object> convertedScoreboard = new TreeMap<>();
                for(Map.Entry<String, Double> e : scoreboard.entrySet()){
                    convertedScoreboard.put(e.getKey(), e.getValue());
                }
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
                List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
                List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");
                if (guessedPictures == null) {
                    guessedPictures = new ArrayList<>();
                }
                if (uploadedPictures == null) {
                    uploadedPictures = new ArrayList<>();
                }
                if (!guessedPictures.contains(getUniqueId())) {
                    guessedPictures.add(getUniqueId());
                }
                Map<String, Object> toUpload = new HashMap<>();
                toUpload.put("guessedPics", guessedPictures);
                toUpload.put("uploadedPics", uploadedPictures);
                Storage.uploadToFirestore(toUpload, "users", user);
            }
        });
    }

    /**
     * Apply consumer function when the karma is available
     * @param karmaAvailable consumer function to call when the karma is available
     */
    private void onKarmaUpdated(Consumer<Map<String, Object>> karmaAvailable){
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
        onKarmaUpdated((pictureAttributes) -> {
            long newKarma = (long)pictureAttributes.get("karma") + delta;
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
     * Returns the picture's location with purposely reduced precision
     * @return the location
     */
    public void onApproximateLocationAvailable(Consumer<Location> approximateLocationAvailable) {
        onLocationAvailable((location) -> {
            double radius = 200; // meters
            Random random = new Random();
            double distance = Math.sqrt(random.nextDouble()) * radius / 111000;
            double angle = 2 * Math.PI * random.nextDouble();
            double longitude_delta = distance * Math.cos(angle);
            double latitude_delta = distance * Math.sin(angle);
            longitude_delta /= Math.cos(Math.toRadians(location.getLatitude()));
            Location al = new Location("");
            al.setLongitude(location.getLongitude() + longitude_delta);
            al.setLatitude(location.getLatitude() + latitude_delta);
            approximateLocationAvailable.accept(al);
        });
    }
}
